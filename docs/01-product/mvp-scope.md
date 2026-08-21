# CareerPilot AI - MVP Scope

## 1. Overview

The Minimum Viable Product (MVP) of CareerPilot AI will provide users with a platform to:

- Create an account
- Upload resumes
- Analyze resumes against ATS requirements
- Receive AI-powered resume feedback
- Rewrite resume content
- Match resumes against job descriptions
- Identify missing skills
- Track resume versions

The MVP is designed to validate the core product idea before introducing advanced features.

---

# 2. MVP Goal

The primary goal of the MVP is:

> Help a job seeker understand how well their resume performs against a job description and provide actionable AI-powered improvements.

The primary user journey is:

```text
User
  |
  v
Register / Login
  |
  v
Upload Resume
  |
  v
Resume Parsing
  |
  v
ATS Analysis
  |
  v
ATS Score
  |
  v
Keyword + Skill Analysis
  |
  v
AI Review
  |
  v
Improvement Suggestions
  |
  v
Resume Rewrite
  |
  v
Job Matching
```

---

# 3. Target User

The primary MVP user is:

```text
Job Seeker
```

The platform is initially focused on individual users who want to improve their resumes and increase their chances of passing ATS screening.

---

# 4. MVP Features

The MVP consists of the following major capabilities:

```text
Authentication
Resume Management
Resume Parsing
ATS Analysis
AI Resume Review
AI Resume Rewrite
Job Matching
```

---

# 5. Authentication

## 5.1 Registration

Users can create an account using:

- Name
- Email
- Password

Requirements:

- Email must be unique
- Password must be securely hashed
- Default role is `USER`
- Account status is `ACTIVE`

API:

```text
POST /api/v1/auth/register
```

---

## 5.2 Login

Users can authenticate using:

- Email
- Password

The system returns:

- Access token
- Refresh token

API:

```text
POST /api/v1/auth/login
```

---

## 5.3 JWT Authentication

Protected APIs will use JWT access tokens.

Example:

```http
Authorization: Bearer <access-token>
```

---

## 5.4 Refresh Token

Users can obtain a new access token using a valid refresh token.

API:

```text
POST /api/v1/auth/refresh
```

Refresh tokens must support:

- Expiration
- Revocation
- Rotation

---

## 5.5 Logout

Users can invalidate their authentication session.

API:

```text
POST /api/v1/auth/logout
```

---

## 5.6 Role Management

The initial roles are:

```text
USER
ADMIN
```

New users automatically receive:

```text
USER
```

Users cannot assign themselves the `ADMIN` role.

---

# 6. Resume Management

## 6.1 Resume Upload

Users can upload resumes in:

```text
PDF
DOCX
```

The system will validate:

- File type
- File size
- MIME type
- File content

Example API:

```text
POST /api/v1/resumes
```

---

## 6.2 Resume Metadata

The system stores metadata such as:

```text
Resume ID
User ID
File Name
File Type
File Size
Storage Key
Version
Created At
Updated At
```

The actual resume file will be stored separately from the relational database.

---

## 6.3 Resume History

Users can maintain multiple versions of their resume.

Example:

```text
Resume
│
├── Version 1
├── Version 2
├── Version 3
└── Version 4
```

This allows users to compare improvements over time.

---

## 6.4 Resume Parsing

The system will extract structured information from uploaded resumes.

Potential information includes:

```text
Personal Information
Summary
Skills
Education
Experience
Projects
Certifications
Languages
```

The parsed information will be used by ATS and AI services.

---

# 7. ATS Analysis

The ATS module evaluates the resume against ATS-oriented criteria.

## 7.1 ATS Score

The system generates an overall ATS score.

Example:

```text
ATS Score: 78/100
```

The exact scoring algorithm will be defined during ATS implementation.

---

## 7.2 Keyword Analysis

The system identifies important keywords from the resume.

Example:

```text
Java        ✓
Spring Boot ✓
Docker      ✓
Kafka       ✗
AWS         ✓
```

The system should identify:

- Present keywords
- Missing keywords
- Keyword frequency
- Keyword relevance

---

## 7.3 Skill Analysis

The system identifies skills from:

- Resume
- Job description

Example:

```text
Resume Skills:
Java
Spring Boot
MySQL
Docker

Job Requirements:
Java
Spring Boot
Docker
Kafka
AWS
```

Result:

```text
Matched:
Java
Spring Boot
Docker

Missing:
Kafka
AWS
```

---

## 7.4 Improvement Suggestions

The ATS module provides actionable suggestions.

Examples:

```text
Add measurable achievements to work experience.

Include missing technical skills where they are genuinely applicable.

Improve keyword coverage.

Reduce unnecessary content.

Improve section structure.
```

---

# 8. AI Resume Review

The AI Service provides a deeper qualitative review of the resume.

The review may include:

```text
Resume Strengths
Resume Weaknesses
Content Quality
Clarity
Professional Language
Achievement Quality
Technical Skill Presentation
Formatting Recommendations
```

Example:

```text
Strength:
Strong Java and Spring Boot experience.

Weakness:
Several experience bullets describe responsibilities but do not
include measurable outcomes.
```

---

# 9. AI Resume Rewrite

The system can generate improved versions of resume content.

Examples:

```text
Original:
Worked on Java application.

Improved:
Developed and maintained Java-based backend services,
improving application reliability and reducing processing time.
```

The AI must not invent experience, skills, certifications, employers, or achievements that the user did not provide.

The user remains responsible for reviewing AI-generated content before using it.

---

# 10. Job Matching

Users can provide a job description.

Example:

```text
Job Description
----------------
Java Developer
Spring Boot
Microservices
Docker
AWS
Kafka
```

The system compares the job description against the user's resume.

---

## 10.1 Match Percentage

Example:

```text
Job Match: 82%
```

The exact matching algorithm will be defined during implementation.

---

## 10.2 Matching Skills

The system identifies skills present in both documents.

Example:

```text
Matched Skills:
Java
Spring Boot
Docker
AWS
```

---

## 10.3 Missing Skills

The system identifies important skills present in the job description but missing from the resume.

Example:

```text
Missing Skills:
Kafka
Kubernetes
Terraform
```

The system should distinguish between:

```text
Skill genuinely possessed but missing from resume
```

and:

```text
Skill not known by the user
```

The platform should not encourage users to falsely add skills they do not possess.

---

# 11. MVP User Journey

The complete MVP user journey:

```text
                    ┌───────────────┐
                    │     User      │
                    └───────┬───────┘
                            |
                            v
                    ┌───────────────┐
                    │ Register/Login│
                    └───────┬───────┘
                            |
                            v
                    ┌───────────────┐
                    │ Upload Resume │
                    └───────┬───────┘
                            |
                            v
                    ┌───────────────┐
                    │ Parse Resume  │
                    └───────┬───────┘
                            |
                            v
                    ┌───────────────┐
                    │  ATS Analysis │
                    └───────┬───────┘
                            |
              ┌─────────────┼─────────────┐
              |             |             |
              v             v             v
        ATS Score       Keywords       Skills
              |             |             |
              └─────────────┼─────────────┘
                            |
                            v
                    ┌───────────────┐
                    │   AI Review   │
                    └───────┬───────┘
                            |
                            v
                    ┌───────────────┐
                    │ AI Suggestions│
                    └───────┬───────┘
                            |
                            v
                    ┌───────────────┐
                    │ AI Rewrite    │
                    └───────┬───────┘
                            |
                            v
                    ┌───────────────┐
                    │ Job Matching  │
                    └───────────────┘
```

---

# 12. MVP Microservices

The MVP will use the following services:

```text
API Gateway
Auth Service
Resume Service
ATS Service
AI Service
```

The initial responsibility of each service:

| Service | Responsibility |
|---|---|
| API Gateway | Routing, authentication at edge, rate limiting |
| Auth Service | Users, authentication, tokens |
| Resume Service | Resume files, metadata, versions |
| ATS Service | ATS scoring, keywords, skills |
| AI Service | AI review and rewriting |

---

# 13. MVP Database Scope

The MVP will use PostgreSQL.

Each service will own its database.

```text
Auth Service
    |
    v
careerpilot_auth

Resume Service
    |
    v
careerpilot_resume

ATS Service
    |
    v
careerpilot_ats

AI Service
    |
    v
careerpilot_ai
```

---

# 14. MVP Storage

Resume files will not be stored directly in PostgreSQL.

The architecture will use object storage.

Production target:

```text
AWS S3
```

The database stores metadata such as:

```text
resume_id
user_id
file_name
file_type
file_size
storage_key
```

Local development may use a local storage abstraction or an S3-compatible approach.

---

# 15. MVP AI Capability

The AI Service will initially support:

```text
Resume Review
Resume Improvement Suggestions
Resume Content Rewrite
```

The AI provider will be abstracted behind the AI Service.

Example:

```text
ATS Service / Resume Service
          |
          v
      AI Service
          |
          v
   AI Provider Adapter
          |
          v
      LLM Provider
```

This prevents the rest of the system from becoming tightly coupled to a single AI provider.

---

# 16. MVP Security

Security requirements include:

- JWT authentication
- BCrypt password hashing
- Role-based authorization
- Input validation
- API rate limiting
- Secure file validation
- HTTPS in production
- Secrets stored outside source code
- No passwords in logs
- No tokens in logs
- Resource ownership validation

---

# 17. MVP Observability

The initial system should provide basic observability.

Requirements:

- Application logs
- Correlation IDs
- Health checks
- Error logging
- Basic metrics

Spring Boot Actuator will be used for service health checks.

Example:

```http
GET /actuator/health
```

Advanced observability such as distributed tracing can be introduced later.

---

# 18. MVP Testing

The MVP should include:

### Unit Tests

Testing individual business components.

Examples:

```text
UserService
ResumeService
ATS scoring logic
AI response processing
```

### Integration Tests

Testing:

```text
Controller
Service
Repository
PostgreSQL
```

### API Tests

Testing:

```text
Registration
Login
Resume upload
ATS analysis
AI review
Job matching
```

### Security Tests

Testing:

```text
Missing JWT
Invalid JWT
Expired JWT
Unauthorized resource access
Role restrictions
```

---

# 19. MVP Out of Scope

The following features are intentionally excluded from the initial MVP.

## Authentication

- Google login
- GitHub login
- Multi-factor authentication
- Passkeys
- Advanced device management
- Enterprise SSO

## Resume

- Automatic resume template generation
- Advanced visual resume editor
- Multiple export formats
- Resume marketplace

## AI

- Voice-based AI interview
- AI interview simulation
- Fully autonomous job application
- Autonomous resume customization for every job

## Job Matching

- Automatic job scraping
- Automatic job applications
- LinkedIn automation
- Job board integrations

## Platform

- Enterprise administration
- Team accounts
- Recruiter marketplace
- Mentor marketplace
- Advanced subscription management

These features may be considered after MVP validation.

---

# 20. MVP Success Criteria

The MVP should be considered technically successful when a user can complete the following journey:

```text
Register
   ↓
Login
   ↓
Upload Resume
   ↓
Resume Parsed
   ↓
ATS Score Generated
   ↓
Keywords Analyzed
   ↓
Skills Analyzed
   ↓
AI Review Generated
   ↓
Improvement Suggestions Generated
   ↓
Resume Content Rewritten
   ↓
Job Description Added
   ↓
Resume/JD Match Generated
```

---

# 21. MVP Quality Requirements

The MVP should prioritize:

```text
Correctness
   ↓
Security
   ↓
Reliability
   ↓
Testability
   ↓
Maintainability
   ↓
Performance
   ↓
Scalability
```

The goal is not to build every possible feature.

The goal is to build a reliable core product that can be expanded later.

---

# 22. MVP Development Phases

The implementation will be divided into phases.

## Phase 1 - Foundation

```text
Project structure
Documentation
Database architecture
API standards
Docker
Configuration
```

Status:

```text
Documentation → Completed
```

---

## Phase 2 - Authentication

```text
User registration
Login
Password hashing
JWT
Refresh tokens
Logout
Authorization
Testing
```

---

## Phase 3 - Resume Management

```text
Resume upload
File validation
Resume metadata
Resume versions
Resume retrieval
Resume deletion
Resume parsing
```

---

## Phase 4 - ATS Analysis

```text
Resume analysis
Keyword extraction
Skill extraction
ATS score
Job description comparison
Improvement suggestions
```

---

## Phase 5 - AI Integration

```text
AI provider abstraction
Resume review
AI suggestions
Resume rewriting
Prompt management
AI response validation
```

---

## Phase 6 - Job Matching

```text
Job description input
Skill matching
Keyword matching
Missing skills
Match percentage
```

---

## Phase 7 - Testing and Hardening

```text
Unit testing
Integration testing
API testing
Security testing
Performance testing
Error handling
Observability
```

---

# 23. MVP Technical Stack

## Backend

```text
Java 21
Spring Boot
Spring Security
Spring Data JPA
Spring Cloud Gateway
Hibernate
Flyway
```

## Database

```text
PostgreSQL
```

## Storage

```text
AWS S3
```

## Authentication

```text
JWT
BCrypt
```

## Infrastructure

```text
Docker
Docker Compose
```

## API Documentation

```text
OpenAPI
Swagger UI
```

## Testing

```text
JUnit
Mockito
Spring Boot Test
Testcontainers
```

---

# 24. Future Scalability

The MVP architecture should leave room for future additions.

Potential future infrastructure:

```text
Redis
Kafka
AWS SQS/SNS
Elasticsearch/OpenSearch
Kubernetes
Distributed tracing
Advanced monitoring
```

These technologies should only be introduced when actual requirements justify them.

---

# 25. Final MVP Boundary

The MVP is successful when CareerPilot AI can reliably answer:

> "How well does my resume match this job, what is missing, and how can I improve it?"

The MVP therefore focuses on:

```text
Resume
   +
Job Description
   |
   v
ATS Analysis
   +
AI Analysis
   |
   v
Actionable Improvements
```

Everything outside this core value proposition should be considered secondary until the MVP is validated.