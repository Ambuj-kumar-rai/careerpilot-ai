# CareerPilot AI - Auth Service API

## 1. Overview

The Auth Service is responsible for user identity, authentication, authorization-related identity information, and token management.

The service provides APIs for:

- User registration
- User login
- Access token generation
- Refresh token generation
- Token refresh
- Logout
- Current user information
- Authentication-related validation

Base API path:

```text
/api/v1/auth
```

---

## 2. Authentication Model

CareerPilot AI will initially use:

- JWT access tokens
- Refresh tokens
- BCrypt password hashing
- Role-based authorization
- Stateless API authentication

Authentication flow:

```text
User
 |
 | email + password
 v
API Gateway
 |
 v
Auth Service
 |
 v
PostgreSQL
 |
 | credentials verified
 v
JWT + Refresh Token
 |
 v
Client
```

---

## 3. User Registration

### Endpoint

```http
POST /api/v1/auth/register
```

### Purpose

Creates a new CareerPilot AI user account.

### Authentication

Not required.

### Request Headers

```http
Content-Type: application/json
```

### Request Body

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "StrongPassword123"
}
```

### Request Fields

| Field | Type | Required | Rules |
|---|---|---|---|
| name | String | Yes | 2-100 characters |
| email | String | Yes | Valid email |
| password | String | Yes | Minimum 8 characters |

### Validation Rules

#### Name

- Must not be null
- Must not be blank
- Minimum 2 characters
- Maximum 100 characters

#### Email

- Must not be null
- Must not be blank
- Must be a valid email address
- Email comparison should be case-insensitive
- Email must be unique

#### Password

- Must not be null
- Must not be blank
- Minimum 8 characters
- Password must not be stored in plain text

The password will be hashed before storage.

### Successful Response

HTTP:

```text
201 Created
```

```json
{
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "USER"
  },
  "message": "User registered successfully"
}
```

### Error: Email Already Exists

HTTP:

```text
409 Conflict
```

```json
{
  "timestamp": "2026-08-22T10:30:00Z",
  "status": 409,
  "error": "DUPLICATE_RESOURCE",
  "message": "An account with this email already exists",
  "path": "/api/v1/auth/register",
  "correlationId": "8d5c8a1f-1234-4567-8901-abcdef123456"
}
```

### Error: Validation Failure

HTTP:

```text
400 Bad Request
```

```json
{
  "timestamp": "2026-08-22T10:30:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/auth/register",
  "fieldErrors": [
    {
      "field": "email",
      "message": "Invalid email address"
    }
  ]
}
```

---

## 4. User Login

### Endpoint

```http
POST /api/v1/auth/login
```

### Purpose

Authenticates an existing user and returns authentication tokens.

### Authentication

Not required.

### Request

```json
{
  "email": "john.doe@example.com",
  "password": "StrongPassword123"
}
```

### Successful Response

HTTP:

```text
200 OK
```

```json
{
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "refresh-token-value",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "message": "Login successful"
}
```

`expiresIn` represents the access-token lifetime in seconds.

The exact production token lifetime will be finalized during security implementation.

### Invalid Credentials

HTTP:

```text
401 Unauthorized
```

```json
{
  "timestamp": "2026-08-22T10:30:00Z",
  "status": 401,
  "error": "INVALID_CREDENTIALS",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login",
  "correlationId": "8d5c8a1f-1234-4567-8901-abcdef123456"
}
```

The API must not reveal whether the email exists.

Incorrect:

```text
Email does not exist.
```

Correct:

```text
Invalid email or password.
```

This prevents user-account enumeration.

---

## 5. Refresh Token

### Endpoint

```http
POST /api/v1/auth/refresh
```

### Purpose

Generates a new access token using a valid refresh token.

### Access Token

Not required.

### Refresh Token

Required.

### Request

```json
{
  "refreshToken": "refresh-token-value"
}
```

### Successful Response

HTTP:

```text
200 OK
```

```json
{
  "data": {
    "accessToken": "new-access-token",
    "refreshToken": "new-refresh-token",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "message": "Token refreshed successfully"
}
```

The refresh-token rotation strategy will be implemented as part of the security implementation.

### Invalid Refresh Token

HTTP:

```text
401 Unauthorized
```

```json
{
  "timestamp": "2026-08-22T10:30:00Z",
  "status": 401,
  "error": "INVALID_TOKEN",
  "message": "Invalid or expired refresh token",
  "path": "/api/v1/auth/refresh",
  "correlationId": "8d5c8a1f-1234-4567-8901-abcdef123456"
}
```

---

## 6. Logout

### Endpoint

```http
POST /api/v1/auth/logout
```

### Purpose

Logs the authenticated user out and invalidates the relevant refresh-token session.

### Authentication

Required.

```http
Authorization: Bearer <access-token>
```

### Request

The refresh token may be supplied so that the specific session can be invalidated.

```json
{
  "refreshToken": "refresh-token-value"
}
```

### Successful Response

HTTP:

```text
204 No Content
```

The response body should be empty.

---

## 7. Get Current User

### Endpoint

```http
GET /api/v1/auth/me
```

### Purpose

Returns information about the currently authenticated user.

### Authentication

Required.

```http
Authorization: Bearer <access-token>
```

### Successful Response

HTTP:

```text
200 OK
```

```json
{
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "USER",
    "status": "ACTIVE",
    "createdAt": "2026-08-22T10:30:00Z"
  },
  "message": "User information retrieved successfully"
}
```

---

## 8. Access Token

The access token will be a JWT.

Example:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

The access token should contain only information required for authentication and authorization.

Possible claims:

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "role": "USER",
  "iat": 1755858600,
  "exp": 1755859500
}
```

The `sub` claim represents the user identifier.

Sensitive information must not be stored inside the JWT.

Do not store:

- Password
- Password hash
- Refresh token
- Personal sensitive information
- Internal database information

---

## 9. Refresh Token

Refresh tokens are used to obtain new access tokens without requiring the user to log in again.

Refresh tokens must be:

- Random
- High entropy
- Stored securely
- Associated with a user
- Expirable
- Revocable

The preferred database design is to store a secure hash of the refresh token rather than the raw token value.

---

## 10. User Roles

The initial system will support:

```text
USER
ADMIN
```

Future roles may include:

```text
RECRUITER
MENTOR
```

The initial default role for registration is:

```text
USER
```

Users must not be allowed to select `ADMIN` during registration.

Incorrect:

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "StrongPassword123",
  "role": "ADMIN"
}
```

The server must determine the default role.

---

## 11. User Status

The initial user status values are:

```text
ACTIVE
INACTIVE
LOCKED
```

The default status after successful registration is:

```text
ACTIVE
```

Future statuses may include:

```text
PENDING_VERIFICATION
SUSPENDED
DELETED
```

---

## 12. Authentication Requirements

The following endpoints are public:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
```

The following endpoints require authentication:

```text
POST /api/v1/auth/logout
GET /api/v1/auth/me
```

---

## 13. Password Security

Passwords must never be stored in plain text.

```text
User Password
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
Stored Hash
```

The original password must never be recoverable from the database.

---

## 14. Email Uniqueness

Email addresses must be unique within the Auth Service.

The database must enforce this constraint.

Application-level checking alone is insufficient.

Correct approach:

```text
Application Validation
       +
Database UNIQUE Constraint
```

The database remains the final authority for uniqueness.

---

## 15. Registration Flow

The complete registration flow:

```text
User
 |
 | POST /api/v1/auth/register
 |
 v
API Gateway
 |
 v
Auth Service
 |
 ├── Validate request
 |
 ├── Normalize email
 |
 ├── Check account state
 |
 ├── Hash password
 |
 ├── Create User
 |
 v
PostgreSQL
 |
 v
User Created
 |
 v
Response
```

---

## 16. Login Flow

```text
User
 |
 | POST /api/v1/auth/login
 |
 v
API Gateway
 |
 v
Auth Service
 |
 ├── Validate request
 |
 ├── Find user by email
 |
 ├── Verify password
 |
 ├── Check user status
 |
 ├── Generate access token
 |
 ├── Generate refresh token
 |
 v
PostgreSQL
 |
 v
Authentication Response
 |
 v
User
```

---

## 17. Refresh Flow

```text
Client
 |
 | refresh token
 v
API Gateway
 |
 v
Auth Service
 |
 ├── Validate refresh token
 |
 ├── Check expiration
 |
 ├── Check revocation
 |
 ├── Identify user
 |
 ├── Generate new access token
 |
 └── Rotate refresh token
 |
 v
Client
```

---

## 18. Logout Flow

```text
Client
 |
 | logout
 v
API Gateway
 |
 v
Auth Service
 |
 ├── Identify user
 |
 ├── Identify refresh token
 |
 └── Revoke refresh token
 |
 v
204 No Content
```

JWT access tokens are short-lived. Logout primarily invalidates the refresh-token session so that new access tokens cannot be obtained from that session.

---

## 19. Account Ownership

Every user owns their authentication account.

The Auth Service is the source of truth for:

```text
User ID
Email
Password Hash
Role
Account Status
Authentication Sessions
```

Other services may store the user's ID as an external reference.

Example:

```text
Resume Service

resume_id
user_id
file_name
storage_key
```

The Resume Service must not copy the user's password, authentication credentials, or other authentication-sensitive data.

---

## 20. Rate Limiting

Authentication endpoints are sensitive to brute-force attacks.

The following endpoints should have stricter rate limits:

```text
POST /api/v1/auth/login
POST /api/v1/auth/register
POST /api/v1/auth/refresh
```

The exact rate-limit values will be configured later.

Redis may eventually be used to implement distributed rate limiting.

---

## 21. Security Logging

Authentication events should be logged appropriately.

Examples:

```text
LOGIN_SUCCESS
LOGIN_FAILURE
USER_REGISTERED
TOKEN_REFRESHED
LOGOUT
ACCOUNT_LOCKED
```

Logs must not contain:

- Passwords
- Password hashes
- Access tokens
- Refresh tokens
- API keys

---

## 22. API Status Summary

| Endpoint | Method | Authentication | Success |
|---|---|---|---|
| `/api/v1/auth/register` | POST | No | 201 |
| `/api/v1/auth/login` | POST | No | 200 |
| `/api/v1/auth/refresh` | POST | Refresh Token | 200 |
| `/api/v1/auth/logout` | POST | Yes | 204 |
| `/api/v1/auth/me` | GET | Yes | 200 |

---

## 23. Common Error Codes

The Auth Service may return:

```text
VALIDATION_ERROR
DUPLICATE_RESOURCE
INVALID_CREDENTIALS
INVALID_TOKEN
TOKEN_EXPIRED
ACCOUNT_LOCKED
ACCOUNT_INACTIVE
UNAUTHORIZED
FORBIDDEN
RATE_LIMIT_EXCEEDED
INTERNAL_ERROR
```

---

## 24. API Security Checklist

Before considering the Auth API production-ready:

- [ ] Password hashing implemented
- [ ] Password never logged
- [ ] Email uniqueness enforced
- [ ] JWT signing key secured
- [ ] Access-token expiration configured
- [ ] Refresh-token expiration configured
- [ ] Refresh-token rotation implemented
- [ ] Refresh-token revocation implemented
- [ ] Login rate limiting implemented
- [ ] Account status checked
- [ ] Authorization implemented
- [ ] Input validation implemented
- [ ] Global exception handling implemented
- [ ] Security headers configured
- [ ] HTTPS enabled in production
- [ ] API documentation available
- [ ] Unit tests implemented
- [ ] Integration tests implemented

---

## 25. Implementation Sequence

The Auth Service implementation should follow this order:

```text
1. Database design
        ↓
2. Flyway migration
        ↓
3. User entity
        ↓
4. User repository
        ↓
5. Registration request DTO
        ↓
6. Registration response DTO
        ↓
7. User service
        ↓
8. Password hashing
        ↓
9. Registration controller
        ↓
10. Login request DTO
        ↓
11. Authentication service
        ↓
12. JWT generation
        ↓
13. Refresh token
        ↓
14. Security configuration
        ↓
15. /me endpoint
        ↓
16. Logout
        ↓
17. Unit tests
        ↓
18. Integration tests
```

---

## 26. Related Documentation

This API contract should be read together with:

```text
docs/01-product/requirements.md
docs/01-product/mvp-scope.md

docs/02-architecture/high-level-architecture.md
docs/02-architecture/microservices.md
docs/02-architecture/decisions.md

docs/03-api/api-standards.md
```

Database-specific design will be documented under:

```text
docs/04-database/
```

---

## 27. Future Authentication Features

The following features are intentionally outside the first implementation:

- Email verification
- Forgot password
- Password reset
- Google login
- GitHub login
- Multi-factor authentication
- Device/session management
- Login history
- Advanced account security
- Passkeys

These features may be added after the basic authentication flow is stable.

---

## 28. Final Auth API Contract

The initial Auth Service exposes:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
GET  /api/v1/auth/me
```

The contract defined in this document will be the reference point for implementing the Auth Service.

Changes to the contract should be reviewed before implementation and documented when they affect existing consumers.