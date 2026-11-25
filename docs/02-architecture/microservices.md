# CareerPilot AI - Microservices Architecture

## 1. Overview

CareerPilot AI will use a microservices-based architecture in which each major business capability is implemented as an independently deployable service.

The initial backend will contain the following services:

1. API Gateway
2. Auth Service
3. Resume Service
4. ATS Service
5. AI Service

Each service will have:

* A clearly defined business responsibility
* Its own application code
* Its own data ownership
* Its own API boundary
* Independent configuration
* Independent testing
* Independent deployment capability

The goal is to maintain loose coupling between services while allowing each service to evolve independently.

---

# 2. Microservices Principles

The following principles will govern the design of all CareerPilot AI services.

## 2.1 Single Business Responsibility

Each service should focus on a clearly defined business capability.

For example:

```text
Auth Service   → Identity and authentication

Resume Service → Resume management

ATS Service    → Resume/JD analysis

AI Service     → AI/LLM capabilities
```

A service should not gradually become responsible for unrelated business functionality.

---

## 2.2 Database Ownership

Each business service owns its data.

Initial database ownership:

```text
Auth Service
    ↓
careerpilot_auth

Resume Service
    ↓
careerpilot_resume

ATS Service
    ↓
careerpilot_ats

AI Service
    ↓
careerpilot_ai
```

A service must not directly query another service's database.

Incorrect:

```text
ATS Service
     │
     └──────────────► careerpilot_resume
```

Correct:

```text
ATS Service
     │
     │ REST API
     ▼
Resume Service
     │
     ▼
careerpilot_resume
```

---

## 2.3 Independent Deployment

Each service should be capable of being built and deployed independently.

For example:

```text
auth-service
    ↓
auth-service Docker image
    ↓
Auth deployment
```

Updating the ATS Service should not require rebuilding the Auth Service.

---

## 2.4 API-Based Communication

Services should communicate through well-defined APIs rather than sharing implementation details.

Initial communication mechanism:

```text
REST / HTTP
```

Future asynchronous communication:

```text
Kafka
```

REST will be used when an immediate response is required.

Kafka will be introduced when asynchronous processing provides a clear benefit.

---

# 3. Service Inventory

The initial architecture contains:

| Service        | Responsibility              | Database           | Status |
| -------------- | --------------------------- | ------------------ | ------ |
| API Gateway    | Routing and edge security   | None               | MVP    |
| Auth Service   | Authentication and identity | careerpilot_auth   | MVP    |
| Resume Service | Resume management           | careerpilot_resume | MVP    |
| ATS Service    | ATS analysis and matching   | careerpilot_ats    | MVP    |
| AI Service     | AI/LLM capabilities         | careerpilot_ai     | MVP    |

Future services:

| Service              | Responsibility              | Status |
| -------------------- | --------------------------- | ------ |
| Interview Service    | AI mock interviews          | Future |
| Roadmap Service      | Personalized learning plans | Future |
| Notification Service | Email and notifications     | Future |
| Subscription Service | Plans and billing           | Future |
| Analytics Service    | Product analytics           | Future |
| Search Service       | Search and discovery        | Future |
| Admin Service        | Platform administration     | Future |

---

# 4. API Gateway

## 4.1 Purpose

The API Gateway is the single entry point for frontend requests.

The frontend should not directly access individual backend services.

Architecture:

```text
React Frontend
      │
      ▼
API Gateway
      │
      ├── Auth Service
      ├── Resume Service
      ├── ATS Service
      └── AI Service
```

---

## 4.2 Responsibilities

The API Gateway is responsible for:

* Request routing
* JWT validation
* CORS
* Rate limiting
* Request correlation
* Centralized request logging
* API version routing
* Basic security policies
* Timeout configuration

---

## 4.3 What the API Gateway Must Not Do

The API Gateway should not contain business logic.

Incorrect:

```text
API Gateway
    │
    ├── Calculate ATS score
    ├── Parse resume
    └── Generate AI response
```

Correct:

```text
API Gateway
    │
    ├── Route request
    │
    ▼
ATS Service
    │
    └── Calculate ATS score
```

---

# 5. Auth Service

## 5.1 Purpose

The Auth Service manages user identity and authentication.

---

## 5.2 Responsibilities

The Auth Service is responsible for:

* User registration
* User login
* Password hashing
* Password verification
* JWT access token generation
* Refresh token management
* User roles
* Account status
* Authentication-related security rules

---

## 5.3 Database

```text
careerpilot_auth
```

Initial entities:

```text
User
RefreshToken
```

Future entities may include:

```text
Role
Permission
LoginAttempt
PasswordResetToken
EmailVerificationToken
```

---

## 5.4 Initial APIs

```http
POST /api/v1/auth/register

POST /api/v1/auth/login

POST /api/v1/auth/refresh

POST /api/v1/auth/logout

GET /api/v1/auth/me
```

The exact request and response contracts will be defined separately in:

```text
docs/03-api/api-standards.md
```

and service-specific API documentation.

---

## 5.5 Security

Passwords must never be stored as plain text.

The service will use a secure password hashing mechanism such as:

```text
BCrypt
```

Authentication tokens will be based on JWT.

---

## 5.6 What Auth Service Must Not Do

Auth Service should not:

* Parse resumes
* Calculate ATS scores
* Call an LLM
* Store resume files
* Generate job matches
* Own subscription business logic

---

# 6. Resume Service

## 6.1 Purpose

The Resume Service manages user resumes and resume-related data.

---

## 6.2 Responsibilities

The Resume Service is responsible for:

* Resume upload
* Resume metadata
* Resume version management
* Resume retrieval
* Resume deletion
* File validation
* Resume processing status
* Resume parsing coordination

---

## 6.3 Database

```text
careerpilot_resume
```

Possible entities:

```text
Resume
ResumeVersion
ParsedResume
ResumeProcessingStatus
```

The exact schema will be defined in:

```text
docs/04-database/
```

---

## 6.4 File Storage

Actual PDF/DOCX files should not be stored directly in PostgreSQL in production.

Recommended architecture:

```text
Resume Service
      │
      ├──────────────► PostgreSQL
      │                 Metadata
      │
      └──────────────► AWS S3
                        Resume File
```

During local development, local file storage or a local S3-compatible solution may be used.

---

## 6.5 Initial APIs

```http
POST /api/v1/resumes

GET /api/v1/resumes

GET /api/v1/resumes/{resumeId}

DELETE /api/v1/resumes/{resumeId}

GET /api/v1/resumes/{resumeId}/versions
```

---

## 6.6 What Resume Service Must Not Do

Resume Service should not:

* Authenticate users itself
* Calculate ATS scores
* Own AI prompt logic
* Calculate subscription limits
* Directly access the ATS database

It may validate the authenticated user context and communicate with other services through APIs or events.

---

# 7. ATS Service

## 7.1 Purpose

The ATS Service is the core resume-analysis component.

It evaluates how well a resume matches ATS and job-description requirements.

---

## 7.2 Responsibilities

The ATS Service is responsible for:

* ATS score calculation
* Keyword analysis
* Skill analysis
* Job description analysis
* Resume/JD matching
* Missing keyword detection
* Missing skill detection
* Resume quality analysis
* Improvement recommendations

---

## 7.3 Database

```text
careerpilot_ats
```

Possible entities:

```text
ATSReport
ATSScore
KeywordMatch
SkillMatch
JobDescription
JobMatch
```

---

## 7.4 ATS Score

An initial score may contain:

```text
Overall Score
Skill Score
Keyword Score
Experience Score
Education Score
Formatting Score
Grammar Score
```

Example:

```json
{
  "overallScore": 84,
  "skillScore": 90,
  "keywordScore": 78,
  "experienceScore": 85,
  "formattingScore": 88,
  "missingKeywords": [
    "Kafka",
    "Docker"
  ]
}
```

The exact scoring algorithm will be documented separately.

---

## 7.5 Initial APIs

```http
POST /api/v1/ats/analyze

POST /api/v1/ats/job-match

GET /api/v1/ats/reports/{reportId}

GET /api/v1/ats/reports
```

---

## 7.6 What ATS Service Must Not Do

ATS Service should not:

* Store user passwords
* Directly access Auth DB
* Directly access Resume DB
* Manage subscriptions
* Own LLM provider credentials
* Implement frontend logic

---

# 8. AI Service

## 8.1 Purpose

The AI Service provides a centralized abstraction for AI and Large Language Model capabilities.

---

## 8.2 Responsibilities

The AI Service is responsible for:

* Resume review
* Resume rewriting
* Cover letter generation
* AI recommendations
* Prompt management
* LLM provider communication
* Structured AI responses
* AI response validation
* AI usage tracking

---

## 8.3 Database

```text
careerpilot_ai
```

Possible entities:

```text
AIRequest
AIResponse
PromptTemplate
AIUsage
```

---

## 8.4 LLM Abstraction

The rest of the application should not directly depend on a specific AI provider.

Preferred architecture:

```text
ATS Service
     │
     ▼
AI Service
     │
     ▼
LLM Abstraction
     │
     ├── Provider A
     │
     ├── Provider B
     │
     └── Future Provider
```

This allows the underlying provider to be changed without redesigning the entire application.

---

## 8.5 Initial APIs

```http
POST /api/v1/ai/review-resume

POST /api/v1/ai/rewrite-resume

POST /api/v1/ai/generate-cover-letter

POST /api/v1/ai/recommendations
```

---

## 8.6 What AI Service Must Not Do

AI Service should not:

* Manage user authentication
* Store passwords
* Directly manage resume ownership
* Calculate subscription payments
* Contain frontend-specific logic

---

# 9. Service Communication Matrix

The initial communication model is:

| Caller         | Target         | Method     | Purpose              |
| -------------- | -------------- | ---------- | -------------------- |
| Frontend       | API Gateway    | REST       | All backend requests |
| Gateway        | Auth Service   | REST       | Authentication       |
| Gateway        | Resume Service | REST       | Resume operations    |
| Gateway        | ATS Service    | REST       | ATS analysis         |
| Gateway        | AI Service     | REST       | AI operations        |
| Resume Service | ATS Service    | REST/Event | Resume analysis      |
| ATS Service    | AI Service     | REST       | AI-assisted analysis |

Direct database communication between services is prohibited.

---

# 10. Authentication Between Services

The initial system will primarily authenticate the end user through JWT.

Example:

```text
Client
  │
  │ JWT
  ▼
API Gateway
  │
  │ authenticated request
  ▼
Backend Service
```

For internal service-to-service communication, the authentication mechanism will be defined as the system evolves.

Possible future approaches include:

* Service-to-service JWT
* OAuth2 client credentials
* mTLS
* Internal network security

The MVP should avoid unnecessary complexity.

---

# 11. Synchronous Communication

REST will be used when the caller requires an immediate response.

Example:

```text
Frontend
    │
    ▼
API Gateway
    │
    ▼
ATS Service
    │
    ▼
Response
```

Suitable use cases:

* Login
* Get user profile
* Get resume
* Get ATS report
* Request AI rewrite

---

# 12. Asynchronous Communication

Kafka may be introduced for long-running or independent processing.

Example:

```text
Resume Service
      │
      │ ResumeUploaded
      ▼
Kafka
      │
      ├──────────► ATS Service
      │
      ├──────────► AI Service
      │
      └──────────► Analytics Service
```

Potential events:

```text
UserRegistered
ResumeUploaded
ResumeParsed
ATSAnalysisCompleted
AIReviewCompleted
InterviewCompleted
SubscriptionChanged
```

Kafka is not required for the first authentication implementation.

---

# 13. Data Ownership Rules

The following rules are mandatory.

### Rule 1

A service owns its database.

### Rule 2

Other services must not directly query that database.

### Rule 3

Foreign keys should not be created across service databases.

For example:

```text
❌ ATS DB → FK → Resume DB
```

Instead, store the required external identifier:

```text
ATSReport
----------------
id
userId
resumeId
score
createdAt
```

The ATS Service can use the `resumeId` as an external reference without creating a database-level foreign key to the Resume database.

---

# 14. Shared Libraries

A small shared library may eventually contain technical utilities such as:

```text
API response models
Common error models
Correlation ID utilities
Logging utilities
```

However, business logic should not be placed into a shared library.

Avoid creating a giant:

```text
common-library
```

that becomes a hidden dependency between all services.

Business logic belongs to the service that owns it.

---

# 15. Configuration Management

Each service should maintain its own configuration.

Example:

```text
auth-service
    application.yml

resume-service
    application.yml

ats-service
    application.yml

ai-service
    application.yml
```

Environment-specific configuration should be externalized.

Example:

```text
Local
Development
Test
Production
```

Sensitive configuration such as:

```text
Database passwords
JWT secrets
LLM API keys
AWS credentials
```

must not be committed to Git.

---

# 16. Error Handling

All services should follow a common error response structure.

Example:

```json
{
  "timestamp": "2026-08-22T10:30:00Z",
  "status": 404,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Resume not found",
  "path": "/api/v1/resumes/123"
}
```

Detailed API error standards will be defined in:

```text
docs/03-api/api-standards.md
```

---

# 17. Logging

Each service should use structured logging.

Important fields include:

```text
timestamp
service
level
correlationId
requestId
userId
operation
duration
status
```

Sensitive information must not be logged.

Never log:

```text
Passwords
JWT tokens
Refresh tokens
API keys
Database passwords
Full resume contents
Sensitive AI data
```

---

# 18. Health Checks

Each service should expose a health endpoint using Spring Boot Actuator.

Example:

```http
GET /actuator/health
```

Response:

```json
{
  "status": "UP"
}
```

Health checks will later be used by Docker, load balancers and cloud orchestration platforms.

---

# 19. Service Failure Isolation

A failure in one service should not unnecessarily bring down the entire platform.

Example:

```text
AI Service DOWN
     │
     ▼
Resume Upload
     │
     ▼
Should still work
```

The system may temporarily disable AI-related functionality while allowing basic resume management to continue.

Similarly:

```text
Notification Service DOWN
     │
     ▼
Resume Upload
     │
     ▼
Should not fail only because notification delivery failed
```

Asynchronous processing and retry mechanisms will be introduced where appropriate.

---

# 20. Future Services

The following services are intentionally not part of the first implementation.

## Interview Service

Responsibilities:

* Mock interviews
* Interview sessions
* Question management
* Interview scoring
* Feedback

---

## Roadmap Service

Responsibilities:

* Career goals
* Skill gaps
* Personalized learning plans
* Daily tasks
* Progress tracking

---

## Notification Service

Responsibilities:

* Email
* In-app notifications
* Notification preferences
* Delivery tracking

---

## Subscription Service

Responsibilities:

* Plans
* Entitlements
* Usage limits
* Payments
* Invoices

---

## Analytics Service

Responsibilities:

* Product events
* User activity
* Conversion metrics
* Usage analytics

---

## Search Service

Responsibilities:

* Job search
* Skill search
* Question search
* Content search

---

# 21. Initial Service Dependency Diagram

```text
                         React Frontend
                                │
                                ▼
                         API Gateway
                                │
             ┌──────────────────┼──────────────────┐
             │                  │                  │
             ▼                  ▼                  ▼
        Auth Service       Resume Service      ATS Service
             │                  │                  │
             ▼                  ▼                  │
        Auth Database      Resume Database         │
                                                   │
                                                   ▼
                                             AI Service
                                                   │
                                                   ▼
                                             LLM Provider
```

The dependency direction should remain controlled.

---

# 22. MVP Implementation Order

The services will be implemented in the following order:

```text
1. Auth Service
       ↓
2. Resume Service
       ↓
3. Resume Parsing
       ↓
4. ATS Service
       ↓
5. AI Service
       ↓
6. API Gateway
       ↓
7. End-to-End Integration
```

Although the API Gateway exists in the architecture from the beginning, the initial service development can be performed directly against individual services for easier local development.

The Gateway will become the primary entry point during integration.

---

# 23. Service-Level Definition of Done

A service feature is considered complete when:

* API contract is documented
* Request validation is implemented
* Business logic is implemented
* Database changes are version-controlled
* Unit tests are written
* Integration tests are added where appropriate
* Security rules are implemented
* Error handling is implemented
* Logging is implemented
* Health checks are available
* README/documentation is updated
* Code is reviewed
* Application runs successfully in the local environment

---

# 24. Current Development Scope

At the current stage, only the Auth Service will be implemented.

The immediate target is:

```text
Auth Service
     │
     ├── User Registration
     ├── User Login
     ├── Password Hashing
     ├── JWT Access Token
     ├── Refresh Token
     └── Role Management
```

The first database will be:

```text
careerpilot_auth
```

The first major entity will be:

```text
User
```

---

# 25. Conclusion

The microservices architecture establishes clear boundaries between CareerPilot AI's business capabilities.

The initial architecture deliberately contains only five backend components:

```text
API Gateway
Auth Service
Resume Service
ATS Service
AI Service
```

Each service owns its business logic and data.

The system will initially favor simple REST communication and PostgreSQL. Redis, Kafka, additional services, advanced cloud infrastructure and other components will be introduced only when they solve an actual product or scalability problem.

The immediate implementation priority is the Auth Service.

The next architectural step is to define the technical decisions behind these choices in:

```text
docs/02-architecture/decisions.md
```

After that, the project will move into detailed API and database design before implementing the first authentication feature.
