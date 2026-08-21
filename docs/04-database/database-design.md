# CareerPilot AI - Database Design

## 1. Overview

This document defines the database architecture and design principles for CareerPilot AI.

The database architecture follows the microservices principle of:

> Database per service.

Each business service owns its data and is responsible for its database schema.

The database must not become a shared integration layer between services.

---

# 2. Database Technology

The primary relational database technology for CareerPilot AI is:

```text
PostgreSQL
```

PostgreSQL is selected because it provides:

- Strong relational data integrity
- ACID transactions
- Excellent indexing capabilities
- Advanced SQL support
- JSONB support
- UUID support
- Strong Spring Boot integration
- Mature production ecosystem

---

# 3. Database Per Service

Each major microservice owns its database.

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

A service must never directly access another service's database.

For example:

```text
ATS Service
     |
     X
     |
     X  Direct database access
     |
     v
Resume Database
```

Instead:

```text
ATS Service
     |
     | REST / Event
     v
Resume Service
     |
     v
Resume Database
```

---

# 4. Auth Database

The initial Auth Service database will be:

```text
careerpilot_auth
```

The primary responsibility of this database is storing authentication and identity information.

Initial tables:

```text
careerpilot_auth
│
├── users
│
└── refresh_tokens
```

Future authentication-related tables may include:

```text
email_verification_tokens
password_reset_tokens
login_attempts
user_sessions
audit_logs
```

These are intentionally outside the initial MVP.

---

# 5. Users Table

The `users` table stores the core identity information of CareerPilot AI users.

Logical structure:

```text
users
│
├── id
├── name
├── email
├── password_hash
├── role
├── status
├── created_at
├── updated_at
└── last_login_at
```

---

# 6. Users Table Design

| Column | Type | Nullable | Constraint | Description |
|---|---|---:|---|---|
| id | UUID | No | Primary Key | Unique user identifier |
| name | VARCHAR(100) | No | NOT NULL | User's display name |
| email | VARCHAR(255) | No | UNIQUE, NOT NULL | User's email address |
| password_hash | VARCHAR(255) | No | NOT NULL | BCrypt password hash |
| role | VARCHAR(30) | No | NOT NULL | User role |
| status | VARCHAR(30) | No | NOT NULL | Account status |
| created_at | TIMESTAMP WITH TIME ZONE | No | NOT NULL | Account creation time |
| updated_at | TIMESTAMP WITH TIME ZONE | No | NOT NULL | Last update time |
| last_login_at | TIMESTAMP WITH TIME ZONE | Yes | | Last successful login |

---

# 7. User ID

The user ID will use UUID.

Example:

```text
550e8400-e29b-41d4-a716-446655440000
```

Reasons for using UUID:

- Globally unique
- Suitable for distributed systems
- Does not expose sequential database IDs
- Can be generated independently
- Suitable for microservices

The user ID will be used by other services as an external reference.

Example:

```text
Resume Service
    |
    +-- user_id
```

The Resume Service does not own the user record.

---

# 8. User Name

The user's name will be stored as:

```text
VARCHAR(100)
```

Rules:

- Required
- Minimum 2 characters
- Maximum 100 characters
- Leading/trailing whitespace should be normalized

Example:

```text
John Doe
```

---

# 9. Email

The email field will use:

```text
VARCHAR(255)
```

The email must be unique.

Database constraint:

```text
UNIQUE(email)
```

Application validation should also verify the email format.

Email normalization should be performed before storage.

For example:

```text
John.Doe@Example.com
```

may be normalized to:

```text
john.doe@example.com
```

The exact normalization rules will be finalized during implementation.

---

# 10. Password Hash

The database will never store the user's plain-text password.

The column will be:

```text
password_hash VARCHAR(255)
```

Password processing:

```text
Plain Password
      |
      v
BCrypt
      |
      v
Password Hash
      |
      v
PostgreSQL
```

During login:

```text
Submitted Password
      |
      v
BCrypt Verification
      |
      v
Stored Password Hash
```

The original password must never be recoverable.

---

# 11. User Role

The initial user roles are:

```text
USER
ADMIN
```

Default registration role:

```text
USER
```

A user must not be allowed to select `ADMIN` during public registration.

The server must assign the default role.

Example:

```text
Registration
     |
     v
role = USER
```

Administrative role assignment must require privileged authorization.

---

# 12. User Status

Initial account statuses:

```text
ACTIVE
INACTIVE
LOCKED
```

Default status:

```text
ACTIVE
```

Possible future statuses:

```text
PENDING_VERIFICATION
SUSPENDED
DELETED
```

The application must verify account status during authentication.

For example:

```text
LOCKED
   |
   v
Login denied
```

---

# 13. Created At

The `created_at` field records when the account was created.

Type:

```text
TIMESTAMP WITH TIME ZONE
```

Example:

```text
2026-08-22T10:30:00Z
```

Timestamps should be stored in UTC.

---

# 14. Updated At

The `updated_at` field records the last modification time.

It should be updated whenever relevant user information changes.

Example:

```text
User created
    |
    v
created_at = current timestamp
updated_at = current timestamp
```

Later:

```text
User updated
    |
    v
updated_at = current timestamp
```

---

# 15. Last Login At

The `last_login_at` field records the timestamp of the user's most recent successful login.

It is nullable because a newly registered user may not have logged in yet.

Example:

```text
New User
    |
    v
last_login_at = NULL
```

After login:

```text
Successful Login
    |
    v
last_login_at = current timestamp
```

---

# 16. Users Table Constraints

The database should enforce important business constraints.

Required constraints:

```text
PRIMARY KEY(id)

UNIQUE(email)

NOT NULL(name)

NOT NULL(email)

NOT NULL(password_hash)

NOT NULL(role)

NOT NULL(status)

NOT NULL(created_at)

NOT NULL(updated_at)
```

The database should remain the final authority for data integrity.

---

# 17. Users Indexes

The most important lookup operation during authentication is:

```text
Find user by email
```

Because email is unique, the database unique constraint will provide an index suitable for this lookup in PostgreSQL.

Additional indexes should only be introduced when justified by actual query patterns.

Possible future indexes:

```text
status
created_at
```

These should not be added unnecessarily during the MVP.

---

# 18. Refresh Tokens

Refresh tokens require persistent storage because they must be:

- Validated
- Expired
- Revoked
- Associated with users
- Rotated

Initial structure:

```text
refresh_tokens
│
├── id
├── user_id
├── token_hash
├── expires_at
├── created_at
├── revoked_at
└── replaced_by_token_id
```

---

# 19. Refresh Tokens Table

| Column | Type | Nullable | Constraint | Description |
|---|---|---:|---|---|
| id | UUID | No | Primary Key | Refresh-token record ID |
| user_id | UUID | No | Foreign Key | Owner of the token |
| token_hash | VARCHAR(255) | No | UNIQUE | Secure token hash |
| expires_at | TIMESTAMP WITH TIME ZONE | No | NOT NULL | Token expiration |
| created_at | TIMESTAMP WITH TIME ZONE | No | NOT NULL | Token creation time |
| revoked_at | TIMESTAMP WITH TIME ZONE | Yes | | Token revocation time |
| replaced_by_token_id | UUID | Yes | | Token rotation reference |

---

# 20. Refresh Token Storage

The raw refresh token should not be stored in PostgreSQL.

Instead:

```text
Raw Refresh Token
        |
        v
Secure Hash
        |
        v
PostgreSQL
```

Example:

```text
Client
   |
   | refresh-token-value
   v
Auth Service
   |
   | hash
   v
Database
```

If the database is compromised, storing only a hash reduces the risk of immediately usable refresh credentials.

---

# 21. Refresh Token Ownership

Each refresh token belongs to exactly one user.

Logical relationship:

```text
User
 |
 | 1
 |
 | N
 v
Refresh Tokens
```

One user can have multiple active refresh-token sessions.

For example:

```text
User
 ├── Laptop session
 ├── Mobile session
 └── Browser session
```

This allows future device/session management.

---

# 22. Foreign Key

The `refresh_tokens.user_id` column references:

```text
users.id
```

Relationship:

```text
users.id
     |
     | 1
     |
     | N
     v
refresh_tokens.user_id
```

This ensures that every refresh token belongs to an existing user.

---

# 23. Refresh Token Expiration

Every refresh token must have an expiration timestamp.

Example:

```text
created_at
    |
    +--------------------+
                         |
                         v
                    expires_at
```

The application must reject an expired refresh token.

Example:

```text
Current Time > expires_at
        |
        v
Token Invalid
```

---

# 24. Refresh Token Revocation

A refresh token can be revoked before expiration.

Example:

```text
revoked_at = NULL
```

means the token has not been revoked.

Example:

```text
revoked_at = 2026-08-22T12:00:00Z
```

means the token has been revoked.

A token is considered usable only if:

```text
Not expired
AND
Not revoked
AND
User is active
```

---

# 25. Refresh Token Rotation

Refresh-token rotation will be supported.

Flow:

```text
Refresh Token A
      |
      v
Refresh Request
      |
      v
Token A Revoked
      |
      v
Token B Created
```

The relationship may be recorded using:

```text
replaced_by_token_id
```

Example:

```text
Token A
   |
   v
Token B
```

This helps detect suspicious token reuse and provides a trace of token rotation.

---

# 26. Authentication Database ER Model

Initial logical ER relationship:

```text
+----------------------+
|       users          |
+----------------------+
| id PK                |
| name                 |
| email UNIQUE         |
| password_hash        |
| role                 |
| status               |
| created_at           |
| updated_at           |
| last_login_at        |
+----------+-----------+
           |
           | 1
           |
           | N
           v
+----------------------+
|   refresh_tokens     |
+----------------------+
| id PK                |
| user_id FK           |
| token_hash UNIQUE    |
| expires_at           |
| created_at           |
| revoked_at           |
| replaced_by_token_id |
+----------------------+
```

---

# 27. Database Ownership

The Auth Service owns:

```text
users
refresh_tokens
```

The Resume Service owns resume-related data.

The ATS Service owns ATS analysis data.

The AI Service owns AI-related data.

Example:

```text
Auth DB
  └── users
  └── refresh_tokens

Resume DB
  └── resumes
  └── resume_versions

ATS DB
  └── ats_reports
  └── keyword_analysis

AI DB
  └── ai_reviews
  └── ai_suggestions
```

---

# 28. Cross-Service User References

Other services may store:

```text
user_id
```

as an external reference.

For example:

```text
Resume Service

resume_id
user_id
file_name
storage_key
```

The Resume Service must not create a database foreign key to:

```text
careerpilot_auth.users
```

because that would violate database ownership.

Instead:

```text
Resume Service
      |
      | user_id
      v
Auth Service API
```

or another approved service communication mechanism may be used when user information is required.

---

# 29. Transactions

Transactions should be used within a service's own database.

Example registration transaction:

```text
Begin Transaction
      |
      v
Create User
      |
      v
Commit
```

If the operation fails:

```text
Rollback
```

A service should not attempt to create a distributed database transaction across multiple microservices during the MVP.

---

# 30. Soft Delete

The initial `users` table will not implement physical soft deletion automatically.

A future implementation may introduce:

```text
deleted_at
```

if account recovery, compliance, or auditing requirements justify it.

For the MVP:

```text
status
```

will be used to control account availability.

---

# 31. Database Naming Convention

Database objects will follow snake_case naming.

Examples:

```text
users
refresh_tokens

password_hash
created_at
updated_at
last_login_at
```

Java objects will use camelCase:

```java
passwordHash
createdAt
updatedAt
lastLoginAt
```

---

# 32. Primary Key Naming

Primary keys should normally use:

```text
id
```

Example:

```text
users.id
refresh_tokens.id
```

Foreign keys should use:

```text
<entity>_id
```

Example:

```text
refresh_tokens.user_id
```

---

# 33. Enum Storage

Application enums such as:

```text
USER
ADMIN

ACTIVE
INACTIVE
LOCKED
```

will initially be stored as strings rather than database-specific enum types.

Example:

```text
role = 'USER'
status = 'ACTIVE'
```

This keeps schema migrations and application evolution simpler.

The application layer will validate allowed values.

---

# 34. Nullability

Nullable columns should be used only when the absence of a value has a valid business meaning.

Examples:

```text
last_login_at
revoked_at
replaced_by_token_id
```

may be nullable.

Core identity fields should not be nullable:

```text
id
name
email
password_hash
role
status
created_at
updated_at
```

---

# 35. Data Integrity Principles

The database must enforce important integrity rules.

Examples:

```text
Primary Key
Unique Constraint
Foreign Key
Not Null
```

Application validation and database constraints should complement each other.

Example:

```text
Application
    |
    | Validate email
    v
Database
    |
    | UNIQUE(email)
    v
Stored User
```

---

# 36. Database Migration Strategy

Database schema changes will be managed using:

```text
Flyway
```

Schema changes must be version controlled.

Example:

```text
V1__create_users_table.sql
V2__create_refresh_tokens_table.sql
V3__add_last_login_at.sql
```

Migration files will be committed to Git.

---

# 37. Migration Rules

Migration files should:

- Be immutable after deployment
- Have sequential versions
- Have descriptive names
- Be tested before deployment
- Never contain environment-specific credentials
- Avoid destructive operations unless explicitly reviewed

Example:

```text
V1__create_users_table.sql
```

Once deployed, it should not be edited.

A new migration should be created for changes.

---

# 38. Initial Migration Plan

The Auth Service initial migrations will be:

```text
V1__create_users_table.sql
V2__create_refresh_tokens_table.sql
```

Future examples:

```text
V3__add_user_verification_status.sql
V4__create_password_reset_tokens.sql
V5__create_login_attempts.sql
```

Only required migrations should be created.

---

# 39. Database Environment Strategy

The project will have separate database environments.

Example:

```text
Development
    |
    v
careerpilot_auth_dev

Testing
    |
    v
careerpilot_auth_test

Production
    |
    v
careerpilot_auth
```

Production credentials must never be committed to Git.

---

# 40. Local Development

For local development, PostgreSQL may be run using Docker.

Example architecture:

```text
Docker
 |
 +---- PostgreSQL
 |
 +---- Auth Service
```

The exact Docker Compose configuration will be documented separately.

---

# 41. Database Configuration

Database configuration should be provided through environment-specific configuration.

Example:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

Secrets must not be hard-coded in:

```text
application.properties
application.yml
source code
Git repository
```

Local development may use environment variables or a secure local configuration mechanism.

---

# 42. Connection Pooling

The Auth Service will use the Spring Boot default connection pool configuration based on HikariCP.

Connection pool settings should be configured based on actual workload.

The initial implementation should avoid premature tuning.

Important settings may include:

```text
maximumPoolSize
minimumIdle
connectionTimeout
idleTimeout
maxLifetime
```

---

# 43. Database Transactions and Isolation

PostgreSQL transactions will use the database's standard transaction isolation behavior unless a specific business requirement requires otherwise.

Transactions should be kept:

- Short
- Focused
- Service-local
- Free of unnecessary external API calls

An external API call should generally not remain inside a database transaction.

---

# 44. Query Design

Queries should retrieve only the data required.

Avoid:

```text
SELECT *
```

when a smaller projection is sufficient.

Avoid unnecessary database calls.

Authentication should efficiently retrieve a user by email.

Example logical operation:

```text
Find User By Email
```

This should be supported by the unique email constraint/index.

---

# 45. N+1 Query Prevention

The application must avoid unnecessary N+1 database queries.

Example problem:

```text
Query Users
     |
     +---- Query Token 1
     +---- Query Token 2
     +---- Query Token 3
     +---- ...
```

Queries and entity relationships should be designed intentionally.

The Auth Service is initially simple enough that excessive entity relationships should be avoided.

---

# 46. Database Security

Database security requirements include:

- Strong database credentials
- Least-privilege database users
- TLS for production database connections where required
- Encrypted storage in production
- Restricted network access
- Regular backups
- Secret management
- No credentials in Git

The application database user should have only the permissions required by the service.

---

# 47. Backup and Recovery

Production databases must have a backup strategy.

Requirements include:

- Automated backups
- Point-in-time recovery where supported
- Backup retention policy
- Recovery testing

Backup strategy will be finalized during cloud infrastructure implementation.

---

# 48. Monitoring

Database monitoring should include:

- Connection count
- Query latency
- CPU utilization
- Memory usage
- Storage usage
- Slow queries
- Failed connections
- Transaction failures

Production monitoring will be implemented as part of the infrastructure and observability stage.

---

# 49. Current Auth Database Scope

The MVP Auth database contains:

```text
careerpilot_auth
│
├── users
│
└── refresh_tokens
```

The `users` table manages:

```text
Identity
Credentials
Role
Status
Account timestamps
```

The `refresh_tokens` table manages:

```text
Authentication sessions
Token expiration
Token revocation
Token rotation
```

---

# 50. Database Design Summary

| Area | Decision |
|---|---|
| Database | PostgreSQL |
| Architecture | Database per service |
| Auth Database | `careerpilot_auth` |
| Primary Key | UUID |
| User Table | `users` |
| Token Table | `refresh_tokens` |
| Password Storage | BCrypt hash |
| Email | Unique |
| Role | String |
| Status | String |
| Timestamp | UTC |
| Token Storage | Secure hash |
| Migration Tool | Flyway |
| Local Database | Docker PostgreSQL |
| API/DB Ownership | Service-specific |
| Cross-Service FK | Not allowed |

---

# 51. Implementation Sequence

The database implementation should follow this order:

```text
1. Create PostgreSQL database
        |
        v
2. Configure Spring Boot connection
        |
        v
3. Add Flyway dependency
        |
        v
4. Create V1 users migration
        |
        v
5. Create V2 refresh_tokens migration
        |
        v
6. Run migrations
        |
        v
7. Verify schema
        |
        v
8. Create User entity
        |
        v
9. Create RefreshToken entity
        |
        v
10. Create repositories
        |
        v
11. Write repository tests
```

---

# 52. Future Database Evolution

The database design will evolve as product requirements grow.

Possible future Auth tables:

```text
email_verification_tokens
password_reset_tokens
user_sessions
login_attempts
audit_logs
```

These should only be introduced when the corresponding business requirements are implemented.

The database should not contain unused tables simply because they may be useful someday.

---

# 53. Final Principle

The database should support the domain model without becoming the domain model.

The service owns its data.

The API owns the external contract.

The database owns persistence and data integrity.

The application owns business logic.

The architecture should maintain this separation:

```text
Client
   |
   v
API
   |
   v
Business Logic
   |
   v
Repository
   |
   v
Database
```

For CareerPilot AI:

```text
Frontend
    |
    v
API Gateway
    |
    v
Auth Service
    |
    +---- Business Logic
    |
    +---- Repository
    |
    v
careerpilot_auth
    |
    +---- users
    |
    +---- refresh_tokens
```

This design provides a clean foundation for implementing the Auth Service.