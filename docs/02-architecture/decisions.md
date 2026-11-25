# CareerPilot AI - Architecture Decision Records

## 1. Overview

This document records the major technical and architectural decisions made during the development of CareerPilot AI.

The purpose of Architecture Decision Records (ADRs) is to document:

* What decision was made
* Why the decision was made
* What alternatives were considered
* What consequences the decision creates
* When the decision should be reconsidered

Architectural decisions should not be changed casually. If a major decision needs to be changed, a new ADR should be created rather than silently modifying the original decision.

---

# 2. ADR Format

Each decision follows this structure:

```text
Status
Context
Decision
Alternatives Considered
Consequences
Revisit When
```

Possible statuses:

```text
Proposed
Accepted
Rejected
Deprecated
Superseded
```

---

# ADR-001: Use Microservices Architecture

## Status

Accepted

## Context

CareerPilot AI contains multiple independent business capabilities such as authentication, resume management, ATS analysis, and AI processing.

These capabilities have different responsibilities and may have different scaling requirements.

For example:

* Authentication may have predictable request patterns.
* Resume processing may involve large file operations.
* ATS analysis may require CPU-intensive processing.
* AI processing may depend on external LLM providers and have higher latency.

A single application could initially support these capabilities, but strong separation between business domains will make future scaling and independent development easier.

## Decision

CareerPilot AI will use a microservices-based architecture.

Initial services:

```text
API Gateway
Auth Service
Resume Service
ATS Service
AI Service
```

Each service will have a clearly defined business responsibility.

## Alternatives Considered

### Monolithic Architecture

A single Spring Boot application containing all functionality.

Advantages:

* Easier initial development
* Simpler deployment
* Easier local debugging

Disadvantages:

* Strong coupling
* Difficult independent scaling
* Larger codebase over time
* Business boundaries can become unclear

### Modular Monolith

A single deployable application with strict internal modules.

Advantages:

* Easier deployment
* Clear internal boundaries
* Lower infrastructure complexity

Disadvantages:

* Services cannot scale independently
* Eventually requires extraction if independent deployment becomes necessary

A modular monolith remains a valid future option if the operational complexity of microservices becomes greater than the product's needs.

## Consequences

Positive:

* Clear service boundaries
* Independent deployment
* Independent scaling
* Better fault isolation
* Easier future team ownership

Negative:

* More infrastructure
* More deployment complexity
* Network communication
* Distributed debugging
* Data consistency becomes more complex

## Revisit When

This decision should be reconsidered if:

* The system becomes unnecessarily difficult to operate
* Development velocity decreases significantly
* The number of services grows without clear business justification

---

# ADR-002: Use Java 21

## Status

Accepted

## Context

The backend requires a mature, strongly typed language with excellent support for enterprise applications, concurrency, security, testing, and cloud deployment.

The development team has strong Java and Spring Boot experience.

## Decision

Java 21 will be used as the primary backend programming language.

## Alternatives Considered

### Java 17

Stable and widely adopted, but Java 21 provides a newer long-term-support baseline and additional language/runtime improvements.

### Kotlin

Kotlin provides concise syntax and excellent JVM integration, but Java provides better alignment with the existing development skill set and enterprise ecosystem.

### Python

Python is useful for AI and data processing but is not selected as the primary backend language.

Python may be used for specialized AI/data-processing components in the future if required.

## Consequences

Positive:

* Strong Spring ecosystem
* Mature tooling
* Excellent IDE support
* Strong enterprise adoption
* Good performance
* Long-term support

Negative:

* More verbose than some alternatives
* JVM resource requirements

---

# ADR-003: Use Spring Boot

## Status

Accepted

## Context

CareerPilot AI requires REST APIs, security, database access, validation, testing, configuration management, and production monitoring.

## Decision

Spring Boot will be used as the primary backend framework.

The platform will use:

```text
Spring Boot
Spring Web
Spring Data JPA
Spring Security
Spring Boot Actuator
Spring Cloud Gateway
```

Additional Spring projects will be introduced only when required.

## Alternatives Considered

### Quarkus

Excellent for cloud-native Java applications but not necessary for the initial project.

### Micronaut

Provides fast startup and low memory usage but has a smaller ecosystem compared with Spring.

## Consequences

Positive:

* Mature ecosystem
* Strong documentation
* Excellent database support
* Strong security framework
* Excellent testing support

Negative:

* Can introduce significant framework complexity
* Requires understanding of Spring's dependency injection and configuration model

---

# ADR-004: Use PostgreSQL

## Status

Accepted

## Context

CareerPilot AI contains strongly relational data:

```text
Users
Resumes
Resume Versions
ATS Reports
Job Matches
Interviews
Subscriptions
```

At the same time, AI-generated results may contain flexible structures.

## Decision

PostgreSQL will be the primary application database.

PostgreSQL's relational capabilities will be used for core business data, while JSON/JSONB may be used where flexible document structures provide a clear benefit.

Initial databases:

```text
careerpilot_auth
careerpilot_resume
careerpilot_ats
careerpilot_ai
```

## Alternatives Considered

### MySQL

MySQL is a capable relational database and could support the application.

PostgreSQL was selected because of its strong feature set, advanced SQL capabilities, JSONB support, and suitability for complex analytical and relational workloads.

### MongoDB

MongoDB is well suited to flexible document structures, but PostgreSQL provides a stronger default foundation for the platform's relational business data.

MongoDB may still be introduced for a specialized use case if future requirements justify it.

## Consequences

Positive:

* Strong relational integrity
* Transactions
* JSONB
* Advanced SQL
* Mature Spring integration
* Excellent AWS RDS support

Negative:

* Requires schema management
* Relational modeling requires planning

---

# ADR-005: Database Per Service

## Status

Accepted

## Context

Microservices should remain loosely coupled.

A shared database would allow services to directly depend on another service's internal tables, creating tight coupling.

## Decision

Each business service will own its database.

```text
Auth Service   → careerpilot_auth

Resume Service → careerpilot_resume

ATS Service    → careerpilot_ats

AI Service     → careerpilot_ai
```

A service must never directly query another service's database.

## Alternatives Considered

### Shared Database

All services use the same database.

Advantages:

* Simple initially
* Easy joins
* Simple transactions

Disadvantages:

* Tight coupling
* Difficult independent deployment
* Database changes affect multiple services

### Database Per Service

Selected because it preserves service ownership and independent evolution.

## Consequences

Positive:

* Strong ownership
* Loose coupling
* Independent schema evolution
* Independent scaling

Negative:

* Cross-service queries require APIs/events
* Distributed transactions are more difficult
* Data duplication may sometimes be necessary

---

# ADR-006: Use REST for Initial Service Communication

## Status

Accepted

## Context

The MVP requires communication between services but should remain simple enough for a solo developer to develop and debug.

## Decision

REST/HTTP will be the initial synchronous communication mechanism.

Example:

```text
ATS Service
     │
     │ REST
     ▼
Resume Service
```

## Alternatives Considered

### gRPC

gRPC provides strong contracts and efficient communication but introduces additional complexity that is not necessary for the initial MVP.

### Kafka

Kafka is excellent for asynchronous event-driven communication but should not be introduced everywhere simply because the architecture is microservices-based.

## Consequences

Positive:

* Simple
* Easy to debug
* Easy Postman testing
* Familiar technology
* Good Spring Boot support

Negative:

* Synchronous dependency between services
* Network latency
* Requires timeout/retry design

---

# ADR-007: Introduce Kafka Later

## Status

Accepted

## Context

Some CareerPilot AI operations may become long-running or require multiple independent consumers.

Example:

```text
Resume Uploaded
       │
       ▼
     Kafka
       │
       ├── ATS Service
       ├── AI Service
       └── Analytics Service
```

However, adding Kafka to every operation from the beginning would increase development and operational complexity.

## Decision

Kafka will be introduced when asynchronous processing provides a measurable benefit.

Potential future events:

```text
UserRegistered
ResumeUploaded
ResumeParsed
ATSAnalysisCompleted
AIReviewCompleted
InterviewCompleted
SubscriptionChanged
```

## Alternatives Considered

### REST Only

Simple but creates synchronous dependencies for long-running operations.

### Kafka Everywhere

Flexible but unnecessarily complex for the MVP.

## Consequences

The initial system remains simpler while preserving a clear path toward event-driven architecture.

---

# ADR-008: Use JWT for Authentication

## Status

Accepted

## Context

The frontend and backend services require stateless authentication.

## Decision

JWT-based authentication will be used for the initial authentication architecture.

Example:

```http
Authorization: Bearer <access-token>
```

The Auth Service will issue tokens and protected services/gateway will validate them according to the security architecture.

## Alternatives Considered

### Server-Side Sessions

Simple but introduces centralized session state.

### OAuth2/OIDC

Excellent for larger identity platforms but may be more complexity than required for the initial MVP.

OAuth2/OIDC may be introduced later, particularly if social login or external identity providers are added.

## Consequences

Positive:

* Stateless authentication
* Easy API integration
* Good Spring Security support

Negative:

* Token revocation requires additional design
* Token expiry and refresh must be handled carefully
* Token contents must remain minimal

---

# ADR-009: Use API Gateway

## Status

Accepted

## Context

The frontend should not need to know the internal network locations of backend services.

Centralizing routing and edge-level security also provides a consistent entry point.

## Decision

Spring Cloud Gateway will be used as the initial API Gateway.

Example:

```text
/api/v1/auth/**

/api/v1/resumes/**

/api/v1/ats/**

/api/v1/ai/**
```

## Responsibilities

* Routing
* JWT validation
* CORS
* Rate limiting
* Correlation IDs
* Request logging

The Gateway must not contain business logic.

## Consequences

Positive:

* Single API entry point
* Centralized edge policies
* Cleaner frontend integration

Negative:

* Additional component
* Gateway becomes an important infrastructure dependency

---

# ADR-010: Use Docker

## Status

Accepted

## Context

CareerPilot AI requires multiple infrastructure components and services.

Developers need a reproducible local environment.

## Decision

Docker will be used for local infrastructure and application containerization.

Initial containerized components may include:

```text
PostgreSQL
Redis
Kafka
Backend services
API Gateway
```

Only the components required for the current development stage will be started.

## Alternatives Considered

### Native Installation

Installing PostgreSQL, Redis, and other services directly on the developer machine.

Disadvantage:

* Environment differences
* Difficult cleanup
* Version conflicts

## Consequences

Positive:

* Reproducible environments
* Easier onboarding
* Consistent versions

Negative:

* Requires Docker knowledge
* Additional resource usage

---

# ADR-011: Store Resume Files in Object Storage

## Status

Accepted

## Context

Resume files can be relatively large binary documents.

Storing them directly inside PostgreSQL would increase database size and backup complexity.

## Decision

Resume files will be stored in object storage.

Production:

```text
AWS S3
```

PostgreSQL will store metadata:

```text
resumeId
userId
fileName
fileType
storageKey
fileSize
version
createdAt
```

## Alternatives Considered

### PostgreSQL Binary Storage

Rejected for the production architecture because large files can unnecessarily increase database size and backup/restore overhead.

### Local File System

Useful for local development but not suitable as the production storage mechanism.

## Consequences

Positive:

* Scalable file storage
* Independent file lifecycle
* Better database performance

Negative:

* Additional infrastructure
* Requires secure object-storage access

---

# ADR-012: Use Redis Later for Caching

## Status

Accepted

## Context

Some operations may benefit from low-latency temporary storage.

Potential use cases:

```text
Rate limiting
Caching
Temporary state
Short-lived tokens
Distributed locks
```

## Decision

Redis will be introduced when there is a demonstrated caching or distributed-state requirement.

Redis will not be introduced into every service by default.

## Consequences

The MVP remains simpler while preserving a clear path to performance improvements.

---

# ADR-013: Use OpenAPI for API Documentation

## Status

Accepted

## Context

Multiple services require clear API contracts.

Developers and frontend applications need to understand:

* Endpoints
* Request models
* Response models
* Error responses
* Authentication requirements

## Decision

OpenAPI/Swagger will be used to document REST APIs.

Each service should expose an API specification appropriate to its deployment architecture.

## Consequences

Positive:

* Interactive API documentation
* Better frontend/backend collaboration
* Easier API testing
* Clear contracts

---

# ADR-014: Use Flyway for Database Migrations

## Status

Accepted

## Context

Database schema changes must be reproducible across environments.

Manually changing database tables through pgAdmin or SQL clients creates a risk that local and production schemas will diverge.

## Decision

Flyway will be used to manage database migrations.

Example:

```text
V1__create_users_table.sql
V2__add_user_status.sql
V3__add_refresh_token_table.sql
```

Migrations will be committed to Git.

## Alternatives Considered

### Hibernate Auto DDL

Useful during experimentation but not appropriate as the primary production schema-management strategy.

### Liquibase

A valid alternative, but Flyway is simpler for the initial project.

## Consequences

Positive:

* Reproducible database changes
* Version-controlled schema
* Easier deployment

Negative:

* Requires migration discipline
* Existing data must be considered before destructive changes

---

# ADR-015: Use DTOs Instead of Exposing Entities

## Status

Accepted

## Context

Database entities represent persistence concerns, while API models represent external contracts.

Directly exposing JPA entities can tightly couple APIs to database structure.

## Decision

The API layer will use DTOs.

Example:

```text
Request DTO
     ↓
Controller
     ↓
Service
     ↓
Entity
     ↓
Repository
```

Responses will also use response DTOs.

## Consequences

Positive:

* API/database separation
* Better security
* Easier API evolution
* Controlled response data

Negative:

* Additional mapping code

---

# ADR-016: Use Constructor Injection

## Status

Accepted

## Context

Spring services require dependencies such as repositories and other services.

## Decision

Constructor injection will be the standard dependency injection mechanism.

Example:

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

Field injection will not be used.

## Consequences

Positive:

* Immutable dependencies
* Easier unit testing
* Explicit dependencies
* Better design

---

# ADR-017: Use Global Exception Handling

## Status

Accepted

## Context

Each API needs consistent error responses.

## Decision

Spring's global exception-handling mechanism will be used.

Typical implementation:

```text
@RestControllerAdvice
```

The application will map domain and validation exceptions into a standard error response.

Example:

```json
{
  "timestamp": "2026-08-22T10:30:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Email is invalid",
  "path": "/api/v1/auth/register"
}
```

---

# ADR-018: Use Testcontainers for Integration Testing

## Status

Accepted

## Context

Unit tests can mock database behavior, but realistic integration testing requires an actual database.

## Decision

Testcontainers will be used for integration tests that require PostgreSQL or other infrastructure dependencies.

Example:

```text
Integration Test
      │
      ▼
PostgreSQL Container
      │
      ▼
Spring Boot Application
```

## Consequences

Positive:

* Tests against real infrastructure
* Reduced environment-specific failures
* Reproducible integration tests

Negative:

* Tests require Docker
* Integration tests are slower than unit tests

---

# ADR-019: Use Spring Boot Actuator

## Status

Accepted

## Context

Services need health and operational information.

## Decision

Spring Boot Actuator will be used for:

* Health checks
* Application metrics
* Operational endpoints

Example:

```http
GET /actuator/health
```

Security rules will ensure sensitive actuator endpoints are not unnecessarily exposed publicly.

---

# ADR-020: Keep MVP Infrastructure Minimal

## Status

Accepted

## Context

CareerPilot AI is initially being developed by a small team/individual developer.

Introducing every production technology immediately would slow development and make debugging harder.

## Decision

The MVP will initially use:

```text
Java 21
Spring Boot
PostgreSQL
Docker
REST
JWT
OpenAPI
JUnit
```

The following will be introduced only when justified:

```text
Redis
Kafka
AWS S3
Advanced observability
Search infrastructure
Kubernetes
```

## Consequences

Positive:

* Faster development
* Easier debugging
* Lower infrastructure complexity
* Lower initial cost

Negative:

* Some advanced scalability capabilities will be added later

---

# 3. Decision Summary

| ADR     | Decision                   | Status   |
| ------- | -------------------------- | -------- |
| ADR-001 | Microservices Architecture | Accepted |
| ADR-002 | Java 21                    | Accepted |
| ADR-003 | Spring Boot                | Accepted |
| ADR-004 | PostgreSQL                 | Accepted |
| ADR-005 | Database Per Service       | Accepted |
| ADR-006 | REST Communication         | Accepted |
| ADR-007 | Kafka Later                | Accepted |
| ADR-008 | JWT Authentication         | Accepted |
| ADR-009 | API Gateway                | Accepted |
| ADR-010 | Docker                     | Accepted |
| ADR-011 | Object Storage for Resumes | Accepted |
| ADR-012 | Redis Later                | Accepted |
| ADR-013 | OpenAPI                    | Accepted |
| ADR-014 | Flyway                     | Accepted |
| ADR-015 | DTOs                       | Accepted |
| ADR-016 | Constructor Injection      | Accepted |
| ADR-017 | Global Exception Handling  | Accepted |
| ADR-018 | Testcontainers             | Accepted |
| ADR-019 | Spring Boot Actuator       | Accepted |
| ADR-020 | Minimal MVP Infrastructure | Accepted |

---

# 4. Decision Review Process

Architecture decisions should be revisited when:

* Product requirements change
* Significant performance problems appear
* Infrastructure costs become excessive
* A technology becomes unsupported
* A service boundary becomes incorrect
* Operational complexity becomes too high
* A new requirement cannot be handled cleanly with the current architecture

When a decision changes, create a new ADR rather than deleting the historical decision.

Example:

```text
ADR-004: Use PostgreSQL
Status: Superseded

ADR-021: Introduce MongoDB for AI Document Storage
Status: Accepted
```

This preserves the architectural history of the project.

---

# 5. Current Architectural Direction

The current architecture is intentionally designed around simplicity first and scalability second.

The initial platform is:

```text
                    React Frontend
                          │
                          ▼
                    API Gateway
                          │
          ┌───────────────┼───────────────┐
          │               │               │
          ▼               ▼               ▼
        Auth           Resume            ATS
       Service         Service          Service
          │               │               │
          ▼               ▼               ▼
       Auth DB         Resume DB         ATS DB
                          │
                          ▼
                      AI Service
                          │
                          ▼
                     LLM Provider
```

The architecture will evolve based on actual product requirements, user traffic, operational experience, and measurable performance needs.

---

# 6. Next Development Stage

After the product and architecture documentation is completed, implementation will begin with the Auth Service.

The immediate implementation sequence will be:

```text
1. Auth database design
        ↓
2. User entity design
        ↓
3. Flyway migration
        ↓
4. User repository
        ↓
5. Registration DTO
        ↓
6. Registration API
        ↓
7. Password hashing
        ↓
8. Login API
        ↓
9. JWT authentication
        ↓
10. Refresh token
        ↓
11. Unit tests
        ↓
12. Integration tests
```

No additional microservice should be implemented until the Auth Service foundation is stable.

---

# 7. Final Principle

The architecture should support the product rather than become the product.

CareerPilot AI should prioritize:

```text
User Value
    ↓
Simple Design
    ↓
Correct Business Logic
    ↓
Reliable APIs
    ↓
Security
    ↓
Testing
    ↓
Observability
    ↓
Scalability
```

Technology should be introduced when it solves a real problem.

The goal is not to use every modern technology.

The goal is to build a reliable, maintainable and scalable career platform.
