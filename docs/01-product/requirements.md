# CareerPilot AI - Requirements

## 1. Overview

This document defines the functional and non-functional requirements for CareerPilot AI.

The requirements provide the foundation for:

- Product development
- API design
- Database design
- Testing
- Security
- Architecture decisions

Each requirement has a unique identifier so that it can be traced through implementation and testing.

---

# 2. Requirement ID Convention

Functional requirements use:

```text
FR-<DOMAIN>-<NUMBER>
```

Examples:

```text
FR-AUTH-001
FR-RESUME-001
FR-ATS-001
FR-AI-001
FR-JOB-001
```

Non-functional requirements use:

```text
NFR-<DOMAIN>-<NUMBER>
```

Examples:

```text
NFR-SEC-001
NFR-PERF-001
NFR-AVAIL-001
```

---

# 3. Authentication Requirements

## FR-AUTH-001

The system shall allow a user to register using:

- Name
- Email
- Password

---

## FR-AUTH-002

The system shall prevent duplicate email registration.

Email uniqueness shall be enforced at the database level.

---

## FR-AUTH-003

The system shall allow registered users to log in using their email and password.

---

## FR-AUTH-004

The system shall securely hash user passwords before storing them.

Plain-text passwords shall never be stored.

---

## FR-AUTH-005

The system shall issue a JWT access token after successful authentication.

---

## FR-AUTH-006

The system shall issue a refresh token after successful authentication.

---

## FR-AUTH-007

The system shall allow users to obtain a new access token using a valid refresh token.

---

## FR-AUTH-008

The system shall support refresh-token expiration.

---

## FR-AUTH-009

The system shall support refresh-token revocation.

---

## FR-AUTH-010

The system shall support refresh-token rotation.

---

## FR-AUTH-011

The system shall allow authenticated users to log out.

---

## FR-AUTH-012

The system shall prevent unauthenticated users from accessing protected resources.

---

## FR-AUTH-013

The system shall support role-based authorization.

Initial roles:

```text
USER
ADMIN
```

---

## FR-AUTH-014

The system shall assign the `USER` role to newly registered users by default.

Users shall not be allowed to assign themselves the `ADMIN` role.

---

## FR-AUTH-015

The system shall support account status management.

Initial statuses:

```text
ACTIVE
INACTIVE
LOCKED
```

---

## FR-AUTH-016

The system shall provide an endpoint to retrieve the currently authenticated user's information.

---

## FR-AUTH-017

The system shall return a generic authentication error for invalid login credentials and shall not reveal whether a specific email address exists.

---

# 4. Resume Management Requirements

## FR-RESUME-001

The system shall allow authenticated users to upload resumes.

---

## FR-RESUME-002

The system shall support PDF resume files.

---

## FR-RESUME-003

The system shall support DOCX resume files.

---

## FR-RESUME-004

The system shall validate uploaded file types.

---

## FR-RESUME-005

The system shall enforce a maximum resume file size.

The exact limit will be defined during Resume Service implementation.

---

## FR-RESUME-006

The system shall store resume metadata.

Resume metadata shall include, where applicable:

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

---

## FR-RESUME-007

The system shall associate every resume with the user who uploaded it.

---

## FR-RESUME-008

Users shall only be able to access their own resumes.

---

## FR-RESUME-009

The system shall allow users to retrieve their uploaded resumes.

---

## FR-RESUME-010

The system shall allow users to delete their resumes.

---

## FR-RESUME-011

The system shall support multiple versions of a resume.

---

## FR-RESUME-012

The system shall maintain resume version history.

Example:

```text
Resume
├── Version 1
├── Version 2
├── Version 3
└── Version 4
```

---

## FR-RESUME-013

The system shall parse uploaded resumes and extract structured information.

The parsing process may identify:

- Personal information
- Summary
- Skills
- Education
- Experience
- Projects
- Certifications
- Languages

---

## FR-RESUME-014

The system shall associate parsed resume information with the corresponding resume version.

---

# 5. ATS Analysis Requirements

## FR-ATS-001

The system shall allow an authenticated user to request ATS analysis for a resume.

---

## FR-ATS-002

The system shall generate an overall ATS score.

Example:

```text
ATS Score: 82/100
```

---

## FR-ATS-003

The system shall analyze keywords present in the resume.

---

## FR-ATS-004

The system shall identify relevant keywords missing from the resume.

---

## FR-ATS-005

The system shall analyze technical and professional skills found in the resume.

---

## FR-ATS-006

The system shall compare resume skills against job-description requirements.

---

## FR-ATS-007

The system shall identify skills that are present in the resume but not relevant to the supplied job description when appropriate.

---

## FR-ATS-008

The system shall generate improvement suggestions based on ATS analysis.

---

## FR-ATS-009

The system shall provide an explanation or breakdown of the ATS score.

The score should not be presented as an unexplained number.

---

## FR-ATS-010

The system shall store ATS analysis results for supported resume versions.

---

## FR-ATS-011

The system shall associate ATS analysis results with the user and resume.

---

# 6. AI Resume Review Requirements

## FR-AI-001

The system shall allow authenticated users to request an AI-powered resume review.

---

## FR-AI-002

The AI review shall analyze resume content quality.

---

## FR-AI-003

The AI review shall identify strengths in the resume.

---

## FR-AI-004

The AI review shall identify weaknesses in the resume.

---

## FR-AI-005

The AI review shall provide actionable improvement suggestions.

---

## FR-AI-006

The AI review may evaluate:

- Professional language
- Clarity
- Achievement descriptions
- Technical skill presentation
- Resume summary
- Work experience descriptions
- Project descriptions

---

## FR-AI-007

The system shall allow the user to request AI-powered rewriting of selected resume content.

---

## FR-AI-008

AI-generated content shall not intentionally invent:

- Work experience
- Skills
- Certifications
- Employers
- Education
- Achievements
- Technologies

that were not provided by the user.

---

## FR-AI-009

The user shall remain responsible for reviewing AI-generated content before using it.

---

## FR-AI-010

The AI Service shall be designed so that the rest of the platform is not tightly coupled to a single AI provider.

---

# 7. Job Matching Requirements

## FR-JOB-001

The system shall allow authenticated users to provide a job description.

---

## FR-JOB-002

The system shall analyze the supplied job description.

---

## FR-JOB-003

The system shall compare the job description against the user's resume.

---

## FR-JOB-004

The system shall generate a job-match percentage.

Example:

```text
Match Percentage: 82%
```

---

## FR-JOB-005

The system shall identify skills matched between the resume and job description.

---

## FR-JOB-006

The system shall identify important skills missing from the resume.

---

## FR-JOB-007

The system shall identify relevant keywords from the job description.

---

## FR-JOB-008

The system shall provide actionable recommendations based on job matching.

---

## FR-JOB-009

The system shall distinguish, where possible, between:

```text
Skill possessed by user but missing from resume
```

and:

```text
Skill not demonstrated by the user
```

The system must not encourage users to falsely claim skills they do not possess.

---

# 8. API Requirements

## FR-API-001

All public APIs shall use the `/api/v1` version prefix.

---

## FR-API-002

All public client requests shall pass through the API Gateway in the production architecture.

---

## FR-API-003

Protected APIs shall require authentication.

---

## FR-API-004

APIs shall return appropriate HTTP status codes.

---

## FR-API-005

APIs shall return consistent error responses.

---

## FR-API-006

API input shall be validated before business processing.

---

## FR-API-007

APIs shall support correlation IDs for request tracing.

---

## FR-API-008

APIs shall be documented using OpenAPI.

---

# 9. Data Requirements

## FR-DATA-001

Each microservice shall own its business data.

---

## FR-DATA-002

The Auth Service shall own authentication data.

---

## FR-DATA-003

The Resume Service shall own resume-related data.

---

## FR-DATA-004

The ATS Service shall own ATS analysis data.

---

## FR-DATA-005

The AI Service shall own AI-related data.

---

## FR-DATA-006

Services shall not directly access another service's database.

---

## FR-DATA-007

User identifiers may be stored by other services as external references.

---

## FR-DATA-008

Sensitive authentication information shall not be duplicated across services.

---

# 10. File Storage Requirements

## FR-FILE-001

Resume files shall be stored separately from relational business data.

---

## FR-FILE-002

The production architecture shall use object storage for resume files.

---

## FR-FILE-003

The database shall store resume file metadata and storage references.

---

## FR-FILE-004

Resume file access shall be authorized based on the owning user.

---

## FR-FILE-005

The system shall validate uploaded files before processing them.

---

# 11. Security Requirements

## NFR-SEC-001

All production API communication shall use HTTPS.

---

## NFR-SEC-002

Passwords shall never be stored in plain text.

---

## NFR-SEC-003

Passwords shall be securely hashed using an approved password-hashing mechanism such as BCrypt.

---

## NFR-SEC-004

Authentication tokens shall not be written to application logs.

---

## NFR-SEC-005

Passwords shall not be written to application logs.

---

## NFR-SEC-006

Secrets and credentials shall not be committed to source control.

---

## NFR-SEC-007

Protected resources shall verify both authentication and authorization.

---

## NFR-SEC-008

Users shall not be able to access another user's resumes or analysis results without authorization.

---

## NFR-SEC-009

Authentication endpoints shall have protection against excessive requests and brute-force attempts.

---

## NFR-SEC-010

Uploaded files shall be validated before being processed.

---

# 12. Performance Requirements

## NFR-PERF-001

Normal API requests should respond within an acceptable latency target under expected MVP load.

The exact target will be established through performance testing.

---

## NFR-PERF-002

Long-running operations such as resume parsing and AI analysis should not unnecessarily block HTTP requests.

Where appropriate, these operations may be processed asynchronously.

---

## NFR-PERF-003

Database queries should use appropriate indexes for frequently accessed data.

---

## NFR-PERF-004

The system shall enforce pagination for potentially large collections.

---

## NFR-PERF-005

External service calls shall use configured timeouts.

---

# 13. Reliability Requirements

## NFR-REL-001

A failure in one microservice should not unnecessarily bring down unrelated services.

---

## NFR-REL-002

External service failures shall be handled gracefully.

---

## NFR-REL-003

The system shall use appropriate retry policies for transient failures.

---

## NFR-REL-004

The system may use circuit breakers for unstable external dependencies where justified.

---

## NFR-REL-005

Database transactions shall maintain data consistency within each service.

---

# 14. Availability Requirements

## NFR-AVAIL-001

The MVP should be designed for reliable operation during normal expected usage.

---

## NFR-AVAIL-002

Services shall provide health-check endpoints.

Example:

```http
GET /actuator/health
```

---

## NFR-AVAIL-003

The system should provide sufficient logging to diagnose service failures.

---

# 15. Scalability Requirements

## NFR-SCALE-001

Services should be independently deployable.

---

## NFR-SCALE-002

Services should be independently scalable where required.

---

## NFR-SCALE-003

The architecture should support future horizontal scaling.

---

## NFR-SCALE-004

The system should allow asynchronous processing to be introduced for workloads that require it.

---

## NFR-SCALE-005

The architecture should allow Redis, Kafka, or other infrastructure components to be introduced when justified by actual requirements.

---

# 16. Maintainability Requirements

## NFR-MAINT-001

Services shall follow clear separation of concerns.

---

## NFR-MAINT-002

Business logic shall not be placed directly inside controllers.

---

## NFR-MAINT-003

API models shall be separated from persistence entities.

---

## NFR-MAINT-004

Database migrations shall be version controlled.

---

## NFR-MAINT-005

Architecture decisions shall be documented.

---

## NFR-MAINT-006

Public API contracts shall be documented.

---

# 17. Testability Requirements

## NFR-TEST-001

Core business logic shall have unit tests.

---

## NFR-TEST-002

Important database operations shall have integration tests.

---

## NFR-TEST-003

Critical APIs shall have API/integration tests.

---

## NFR-TEST-004

Authentication and authorization flows shall have security tests.

---

## NFR-TEST-005

Integration tests should use realistic infrastructure where appropriate.

Testcontainers may be used for PostgreSQL and other infrastructure dependencies.

---

# 18. Observability Requirements

## NFR-OBS-001

Services shall produce structured and meaningful application logs.

---

## NFR-OBS-002

Requests should contain a correlation ID.

---

## NFR-OBS-003

The correlation ID should be propagated between services.

---

## NFR-OBS-004

Services shall expose health information.

---

## NFR-OBS-005

Sensitive information shall not be included in logs.

---

# 19. API Compatibility Requirements

## NFR-API-001

Existing APIs should not be broken without a planned migration.

---

## NFR-API-002

Breaking API changes should result in a new API version when appropriate.

---

## NFR-API-003

API responses should remain backward compatible when introducing non-breaking changes.

---

# 20. Data Privacy Requirements

## NFR-PRIV-001

The system shall collect only information required to provide the product's functionality.

---

## NFR-PRIV-002

Sensitive authentication information shall be protected.

---

## NFR-PRIV-003

Users shall only be able to access their own private resume and analysis data.

---

## NFR-PRIV-004

AI processing should not expose user information beyond what is required for the requested operation.

---

# 21. MVP Constraints

The following constraints apply to the initial MVP:

```text
Primary Backend Language: Java 21
Backend Framework: Spring Boot
Database: PostgreSQL
Authentication: JWT
Password Hashing: BCrypt
Migration Tool: Flyway
Containerization: Docker
API Style: REST
API Documentation: OpenAPI
```

---

# 22. MVP Scope Boundary

The following capabilities are part of the MVP:

```text
Authentication
Resume Management
Resume Parsing
ATS Analysis
AI Resume Review
AI Resume Rewrite
Job Matching
```

The following are outside the initial MVP:

```text
Google Login
GitHub Login
MFA
Passkeys
Automatic Job Applications
LinkedIn Automation
Enterprise SSO
Recruiter Marketplace
Mentor Marketplace
Advanced Interview Simulation
```

These features may be considered in future releases.

---

# 23. Requirement Traceability

Requirements should be traceable through the development lifecycle.

Example:

```text
Requirement
    |
    v
API Contract
    |
    v
Database Design
    |
    v
Implementation
    |
    v
Unit Test
    |
    v
Integration Test
```

Example:

```text
FR-AUTH-001
    |
    v
POST /api/v1/auth/register
    |
    v
users table
    |
    v
RegistrationService
    |
    v
RegistrationControllerTest
```

This approach allows the team to verify that important product requirements are actually implemented.

---

# 24. MVP Acceptance Criteria

The MVP should satisfy the following end-to-end flow:

```text
User Registration
       |
       v
User Login
       |
       v
JWT Authentication
       |
       v
Resume Upload
       |
       v
Resume Parsing
       |
       v
ATS Analysis
       |
       +---- ATS Score
       |
       +---- Keyword Analysis
       |
       +---- Skill Analysis
       |
       v
AI Resume Review
       |
       +---- Strengths
       |
       +---- Weaknesses
       |
       +---- Suggestions
       |
       v
AI Resume Rewrite
       |
       v
Job Description Input
       |
       v
Resume/JD Matching
       |
       +---- Match Percentage
       |
       +---- Matched Skills
       |
       +---- Missing Skills
```

---

# 25. Final Requirement Principle

Requirements define what CareerPilot AI must do.

Architecture defines how the system is structured.

API documentation defines how systems communicate.

Database design defines how data is persisted.

Implementation translates these decisions into working software.

The development process should therefore follow:

```text
Requirements
      |
      v
Architecture
      |
      v
API Design
      |
      v
Database Design
      |
      v
Implementation
      |
      v
Testing
      |
      v
Deployment
```

Requirements should be updated when major product behavior changes.