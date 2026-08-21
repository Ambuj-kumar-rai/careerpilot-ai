# CareerPilot AI - High-Level Architecture

## 1. Overview

CareerPilot AI is an AI-powered career platform designed to help software professionals improve their resumes, evaluate ATS compatibility, match resumes against job descriptions, and receive AI-powered career recommendations.

The initial backend will use a microservices-based architecture built using Java and Spring Boot.

The architecture is designed to provide:

* Clear separation of business responsibilities
* Independent service ownership
* Independent data ownership
* Scalability
* Fault isolation
* Secure communication
* Maintainability
* Easy integration with AI providers
* Independent deployment of services

The initial architecture will intentionally remain small. Additional services such as Interview, Roadmap, Notification, Subscription, Analytics, and Search will be introduced only when the product requires them.

---

# 2. Architecture Goals

The primary goals of the architecture are:

1. Keep business capabilities isolated.
2. Prevent services from directly accessing another service's database.
3. Provide stable REST APIs.
4. Support secure authentication and authorization.
5. Allow individual services to scale independently.
6. Support asynchronous processing where required.
7. Make AI integration replaceable.
8. Provide centralized observability.
9. Make local development simple.
10. Allow future migration to cloud infrastructure.

---

# 3. Architecture Style

CareerPilot AI will use a microservices-based architecture.

Each major business capability will be implemented as an independently deployable Spring Boot service.

Initial services:

1. API Gateway
2. Auth Service
3. Resume Service
4. ATS Service
5. AI Service

High-level architecture:

```text
                         ┌─────────────────────┐
                         │    React Frontend   │
                         └──────────┬──────────┘
                                    │
                                  HTTPS
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │     API Gateway     │
                         │ Spring Cloud        │
                         │ Gateway              │
                         └──────────┬──────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
       ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
       │    Auth     │       │   Resume    │       │     ATS     │
       │   Service   │       │   Service   │       │   Service   │
       └──────┬──────┘       └──────┬──────┘       └──────┬──────┘
              │                     │                     │
              ▼                     ▼                     ▼
       ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
       │ PostgreSQL  │       │ PostgreSQL  │       │ PostgreSQL  │
       │careerpilot_ │       │careerpilot_ │       │careerpilot_ │
       │    auth     │       │   resume    │       │     ats     │
       └─────────────┘       └─────────────┘       └─────────────┘
                                    │
                                    ▼
                            ┌─────────────┐
                            │ AI Service  │
                            └──────┬──────┘
                                   │
                                   ▼
                            ┌─────────────┐
                            │ LLM Provider│
                            └─────────────┘
```

---

# 4. System Components

## 4.1 Frontend

The frontend will be implemented using React.

Responsibilities:

* User registration
* User login
* Dashboard
* Resume upload
* Resume analysis
* ATS report
* AI recommendations
* Resume rewriting
* Job description matching
* User profile management

The frontend will communicate with the backend through the API Gateway.

The frontend should not directly communicate with individual microservices.

```text
React
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

# 5. API Gateway

Technology:

* Spring Cloud Gateway
* Spring Boot

The API Gateway acts as the single entry point for frontend requests.

Responsibilities:

* Request routing
* JWT validation
* CORS handling
* Rate limiting
* Request correlation
* Centralized security policies
* Request logging
* Timeout handling
* API version routing

Example routes:

```text
/api/v1/auth/**       → Auth Service

/api/v1/resumes/**    → Resume Service

/api/v1/ats/**        → ATS Service

/api/v1/ai/**         → AI Service
```

The frontend should not know the internal hostnames or ports of individual services.

---

# 6. Auth Service

The Auth Service is responsible for identity and access management.

Responsibilities:

* User registration
* User login
* Password management
* Password hashing
* JWT generation
* Refresh token management
* Role management
* Account status management
* Authentication-related security policies

Database:

```text
careerpilot_auth
```

Initial major entity:

```text
User
```

Future entities may include:

```text
RefreshToken
Role
Permission
LoginAttempt
```

The Auth Service owns all authentication-related data.

No other service should directly access the Auth Service database.

---

# 7. Resume Service

The Resume Service manages user resumes and resume-related operations.

Responsibilities:

* Resume upload
* Resume metadata
* Resume version management
* Resume retrieval
* Resume deletion
* Resume parsing coordination
* Resume processing status

Database:

```text
careerpilot_resume
```

Actual PDF/DOCX files should not be stored directly in PostgreSQL.

In production, files will be stored in:

```text
AWS S3
```

PostgreSQL will store metadata such as:

```text
resume_id
user_id
file_name
file_type
storage_key
file_size
version
status
created_at
updated_at
```

---

# 8. ATS Service

The ATS Service is responsible for analyzing resumes and calculating ATS compatibility.

Responsibilities:

* ATS score calculation
* Keyword analysis
* Skill analysis
* Resume quality analysis
* Job description matching
* Missing keyword identification
* Missing skill identification
* Improvement recommendations

Database:

```text
careerpilot_ats
```

Example ATS result:

```json
{
  "overallScore": 84,
  "skillsScore": 90,
  "experienceScore": 82,
  "keywordScore": 78,
  "formattingScore": 88,
  "missingKeywords": [
    "Kafka",
    "Docker"
  ],
  "missingSkills": [
    "Kubernetes"
  ]
}
```

The scoring algorithm will be versioned so that future changes do not invalidate historical reports.

---

# 9. AI Service

The AI Service is responsible for communication with external Large Language Models.

Responsibilities:

* AI resume review
* Resume rewriting
* Cover letter generation
* AI recommendations
* AI-powered career suggestions
* Prompt management
* LLM provider abstraction
* AI response validation

Database:

```text
careerpilot_ai
```

The AI Service should hide the external LLM implementation from the rest of the system.

For example:

```text
ATS Service
     │
     ▼
AI Service
     │
     ├── OpenAI
     │
     ├── Gemini
     │
     └── Future Provider
```

Other services should not directly call an LLM provider.

This allows the provider to be changed later without changing the entire application.

---

# 10. Database Architecture

CareerPilot AI will follow a database-per-service ownership model.

Initial databases:

```text
careerpilot_auth
careerpilot_resume
careerpilot_ats
careerpilot_ai
```

Architecture:

```text
Auth Service
     │
     ▼
careerpilot_auth

Resume Service
     │
     ▼
careerpilot_resume

ATS Service
     │
     ▼
careerpilot_ats

AI Service
     │
     ▼
careerpilot_ai
```

## Database Ownership Rule

A microservice must not directly query another microservice's database.

Incorrect:

```text
ATS Service
     │
     └──────────────► Resume Database
```

Correct:

```text
ATS Service
     │
     │ API request
     ▼
Resume Service
     │
     ▼
Resume Database
```

This keeps service boundaries clear.

---

# 11. Authentication Flow

The authentication flow will initially use JWT-based authentication.

## Login

```text
User
 │
 │ email + password
 ▼
API Gateway
 │
 ▼
Auth Service
 │
 ▼
PostgreSQL
 │
 │ Verify credentials
 ▼
JWT generated
 │
 ▼
Auth Service
 │
 ▼
API Gateway
 │
 ▼
User
```

The client will then store/use the access token according to the frontend security design.

Subsequent protected requests will contain:

```http
Authorization: Bearer <access-token>
```

---

# 12. Protected Request Flow

Example: user requests their resume.

```text
User
 │
 │ GET /api/v1/resumes
 │ Authorization: Bearer JWT
 ▼
API Gateway
 │
 │ Validate JWT
 ▼
Resume Service
 │
 ▼
Resume Database
 │
 ▼
Response
 │
 ▼
API Gateway
 │
 ▼
User
```

The Resume Service should also verify authorization at the resource level.

For example, a user must not be able to retrieve another user's resume simply by changing the resume ID.

---

# 13. Resume Processing Flow

The initial business flow will be:

```text
User
 │
 │ Upload Resume
 ▼
API Gateway
 │
 ▼
Resume Service
 │
 ├── Validate file
 │
 ├── Store metadata
 │
 └── Store file
          │
          ▼
      Resume Parser
          │
          ▼
      Parsed Resume
          │
          ▼
      ATS Service
          │
          ▼
      ATS Analysis
          │
          ▼
      AI Service
          │
          ▼
      AI Suggestions
          │
          ▼
      User
```

For the first implementation, some of these operations may be synchronous.

As the system grows, long-running operations can be moved to asynchronous processing using Kafka.

---

# 14. Service Communication

The system will initially use REST for synchronous communication.

Example:

```text
Resume Service
      │
      │ REST
      ▼
ATS Service
```

Asynchronous communication can later be introduced using Kafka.

Example future flow:

```text
Resume Service
      │
      │ ResumeUploaded event
      ▼
Kafka
      │
      ├──────────────► ATS Service
      │
      ├──────────────► AI Service
      │
      └──────────────► Analytics Service
```

Kafka will not be introduced into the MVP until asynchronous processing provides a clear benefit.

---

# 15. External Systems

The initial system may integrate with:

```text
PostgreSQL
LLM Provider
AWS S3
```

Future integrations:

```text
Redis
Kafka
Email Provider
Payment Provider
OpenSearch
CloudWatch
```

External systems should be accessed through clearly defined adapters/interfaces where practical.

---

# 16. Security Architecture

Security requirements include:

* Spring Security
* JWT authentication
* Password hashing
* Role-based authorization
* Input validation
* File validation
* API rate limiting
* Secure HTTP communication
* Secret management
* Audit logging
* Resource-level authorization

Passwords must never be stored as plain text.

Secrets must never be committed to Git.

Example:

```text
❌ application.properties

OPENAI_API_KEY=actual-secret
```

Instead, use environment variables or a proper secret-management system.

---

# 17. Error Handling

All services should use a consistent error response format.

Example:

```json
{
  "timestamp": "2026-08-22T10:30:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Email address is invalid",
  "path": "/api/v1/auth/register"
}
```

The exact error contract will be finalized in:

```text
docs/03-api/api-standards.md
```

---

# 18. Observability

Every service should eventually provide:

* Health checks
* Structured logging
* Correlation IDs
* Request latency metrics
* Error metrics
* Dependency health
* Application metrics

Spring Boot Actuator will be used for initial health and metrics endpoints.

Example:

```text
GET /actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

---

# 19. Deployment Architecture

The initial development environment will use Docker.

Future production architecture:

```text
                         Internet
                            │
                            ▼
                     CloudFront / DNS
                            │
                            ▼
                     Load Balancer
                            │
                            ▼
                     API Gateway
                            │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
          ▼                 ▼                 ▼
        Auth             Resume              ATS
       Service           Service            Service
          │                 │                 │
          ▼                 ▼                 ▼
        RDS               RDS               RDS
```

AI Service will communicate with the selected external LLM provider.

Actual AWS deployment decisions will be documented separately.

---

# 20. Future Architecture

The architecture may later include:

```text
Interview Service
Roadmap Service
Notification Service
Subscription Service
Analytics Service
Search Service
Admin Service
```

Potential infrastructure:

```text
Redis
Kafka
OpenSearch
AWS S3
AWS RDS
CloudWatch
ECS/EKS
```

These components are intentionally excluded from the initial implementation to avoid unnecessary complexity.

---

# 21. Architectural Principles

The following principles must be followed throughout development:

### Principle 1 — Service Ownership

Each service owns its business logic.

### Principle 2 — Data Ownership

Each service owns its database.

### Principle 3 — No Direct Database Sharing

Services communicate through APIs or events.

### Principle 4 — API First

API contracts should be defined before implementation.

### Principle 5 — Security by Default

Protected endpoints must require authentication and authorization.

### Principle 6 — Observable by Default

Important operations must generate useful logs and metrics.

### Principle 7 — Fail Gracefully

External dependency failures should not unnecessarily bring down unrelated services.

### Principle 8 — Keep the MVP Simple

Do not introduce infrastructure that does not provide immediate value.

---

# 22. Initial Technology Stack

| Layer               | Technology           |
| ------------------- | -------------------- |
| Language            | Java 21              |
| Framework           | Spring Boot          |
| Gateway             | Spring Cloud Gateway |
| Security            | Spring Security      |
| API                 | REST                 |
| Database            | PostgreSQL           |
| ORM                 | Spring Data JPA      |
| Build               | Maven                |
| Containerization    | Docker               |
| API Documentation   | OpenAPI / Swagger    |
| Testing             | JUnit 5              |
| Integration Testing | Testcontainers       |
| Cache               | Redis - later        |
| Messaging           | Kafka - later        |
| File Storage        | AWS S3 - production  |
| AI                  | LLM Provider         |
| Cloud               | AWS                  |

---

# 23. Initial Repository Structure

```text
careerpilot-ai/
│
├── backend/
│   │
│   ├── api-gateway/
│   │
│   ├── auth-service/
│   │
│   ├── resume-service/
│   │
│   ├── ats-service/
│   │
│   └── ai-service/
│
├── frontend/
│
├── infrastructure/
│
├── docs/
│   ├── 01-product/
│   ├── 02-architecture/
│   ├── 03-api/
│   ├── 04-database/
│   └── 05-development/
│
└── README.md
```

---

# 24. Architecture Evolution

The architecture will evolve in stages.

## Stage 1 — MVP

```text
Gateway
   │
   ├── Auth
   ├── Resume
   ├── ATS
   └── AI
```

## Stage 2 — Growth

```text
Gateway
   │
   ├── Auth
   ├── Resume
   ├── ATS
   ├── AI
   ├── Interview
   ├── Roadmap
   └── Notification
```

## Stage 3 — Scale

```text
Gateway
   │
   ├── Business Services
   │
   ├── Kafka
   ├── Redis
   ├── Search
   ├── Analytics
   └── Subscription
```

The architecture should evolve based on actual product requirements and traffic rather than assumptions.

---

# 25. Current Development Focus

At the current stage, development will focus only on:

```text
1. Auth Service
2. PostgreSQL
3. Authentication APIs
4. JWT security
5. API documentation
6. Testing
```

The next service to implement after Auth will be Resume Service.

The ATS and AI services will be implemented after the resume data flow is established.

---

# 26. Architecture Decision Status

| Decision             | Status                             |
| -------------------- | ---------------------------------- |
| Java 21              | Approved                           |
| Spring Boot          | Approved                           |
| Microservices        | Approved                           |
| PostgreSQL           | Approved                           |
| API Gateway          | Approved                           |
| JWT                  | Approved                           |
| Database per service | Approved                           |
| REST                 | Approved for initial communication |
| Redis                | Future                             |
| Kafka                | Future                             |
| AWS S3               | Production                         |
| Kubernetes           | Not required for MVP               |

---

# 27. Conclusion

The initial CareerPilot AI architecture is intentionally designed to balance startup development speed with long-term scalability.

The MVP will begin with a small number of services:

```text
API Gateway
Auth Service
Resume Service
ATS Service
AI Service
```

Each service will have a clear responsibility and own its data.

The architecture will be expanded only when additional product capabilities or scalability requirements justify the complexity.

The immediate implementation priority is:

```text
Auth Service
      ↓
PostgreSQL
      ↓
Registration
      ↓
Login
      ↓
JWT
      ↓
API Gateway
```

This will establish the foundation on which the remaining CareerPilot AI platform will be built.
