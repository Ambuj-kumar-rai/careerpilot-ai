# CareerPilot AI - API Standards

## 1. Overview

This document defines the common standards and conventions that all CareerPilot AI REST APIs must follow.

The purpose of these standards is to ensure that APIs across different microservices remain:

- Consistent
- Predictable
- Secure
- Easy to consume
- Easy to test
- Easy to document
- Easy to maintain

These standards apply to:

- API Gateway
- Auth Service
- Resume Service
- ATS Service
- AI Service
- Future backend services

Service-specific API contracts will be documented separately.

---

# CareerPilot AI - API Standards

## 1. Overview

This document defines the common standards and conventions that all CareerPilot AI REST APIs must follow.

The purpose of these standards is to ensure that APIs across different microservices remain:

- Consistent
- Predictable
- Secure
- Easy to consume
- Easy to test
- Easy to document
- Easy to maintain

These standards apply to:

- API Gateway
- Auth Service
- Resume Service
- ATS Service
- AI Service
- Future backend services

Service-specific API contracts will be documented separately.

---

# 2. API Architecture

All client-facing APIs will be exposed through the API Gateway.

```text
Frontend
   |
   | HTTPS
   v
API Gateway
   |
   +---- Auth Service
   |
   +---- Resume Service
   |
   +---- ATS Service
   |
   +---- AI Service
```

The frontend should not directly communicate with individual internal microservices in the production architecture.

---

# 3. API Base Path

All public APIs will use the following base path:

```text
/api/v1
```

Examples:

```http
POST /api/v1/auth/register

POST /api/v1/auth/login

GET /api/v1/resumes

POST /api/v1/ats/analyze

POST /api/v1/ai/review-resume
```

---

# 4. API Versioning

API versioning will be implemented using the URL path.

Example:

```text
/api/v1/auth/register
```

Future breaking changes may introduce:

```text
/api/v2/auth/register
```

API versions should only be changed for breaking contract changes.

Non-breaking changes should normally remain within the existing API version.

---

# 5. URL Naming Convention

URLs should represent resources rather than implementation details.

Use lowercase and plural resource names where appropriate.

Correct:

```http
GET /api/v1/resumes
GET /api/v1/resumes/{resumeId}
POST /api/v1/resumes
DELETE /api/v1/resumes/{resumeId}
```

Avoid:

```http
GET /api/v1/getAllResumes
POST /api/v1/createResume
GET /api/v1/getResumeById
```

The HTTP method should communicate the operation.

---

# 6. HTTP Methods

The following HTTP methods will be used according to their standard semantics.

| Method | Purpose |
|---|---|
| GET | Retrieve resource |
| POST | Create resource or execute an operation |
| PUT | Replace a resource |
| PATCH | Partially update a resource |
| DELETE | Delete a resource |

Examples:

```http
GET /api/v1/resumes
```

Retrieve resumes.

```http
POST /api/v1/resumes
```

Create or upload a resume.

```http
PATCH /api/v1/users/{userId}
```

Partially update user information.

```http
DELETE /api/v1/resumes/{resumeId}
```

Delete a resume.

---

# 7. HTTP Status Codes

APIs must use appropriate HTTP status codes.

## 200 OK

Successful request.

Example:

```http
GET /api/v1/resumes/123
```

---

## 201 Created

A new resource was successfully created.

Example:

```http
POST /api/v1/auth/register
```

---

## 202 Accepted

The request was accepted for asynchronous processing.

Example:

```http
POST /api/v1/resumes/123/analyze
```

if analysis is processed asynchronously.

---

## 204 No Content

Successful operation with no response body.

Example:

```http
DELETE /api/v1/resumes/123
```

---

## 400 Bad Request

The request is malformed or contains invalid data.

---

## 401 Unauthorized

The request requires authentication or the supplied authentication credentials are invalid.

---

## 403 Forbidden

The user is authenticated but does not have permission to perform the operation.

---

## 404 Not Found

The requested resource does not exist.

---

## 409 Conflict

The request conflicts with existing application state.

Example:

```text
Email already registered.
```

---

## 422 Unprocessable Entity

The request structure is valid but business validation fails.

This status may be used when the distinction from `400 Bad Request` provides meaningful value.

---

## 429 Too Many Requests

The client has exceeded the allowed rate limit.

---

## 500 Internal Server Error

An unexpected server-side error occurred.

Internal implementation details must not be exposed to the client.

---

## 502 Bad Gateway

The gateway received an invalid response from an upstream service.

---

## 503 Service Unavailable

A service is temporarily unavailable.

---

# 8. Request Format

JSON will be the default request format.

Example:

```json
{
  "email": "user@example.com",
  "password": "examplePassword"
}
```

The request content type should normally be:

```http
Content-Type: application/json
```

File-upload APIs may use:

```http
Content-Type: multipart/form-data
```

---

# 9. Response Format

Successful responses should use a consistent structure where appropriate.

Recommended format:

```json
{
  "data": {
    "id": "123",
    "name": "John Doe"
  },
  "message": "Success"
}
```

For collection responses:

```json
{
  "data": [
    {
      "id": "123",
      "name": "Resume 1"
    },
    {
      "id": "456",
      "name": "Resume 2"
    }
  ],
  "message": "Resumes retrieved successfully"
}
```

The response structure should not be unnecessarily wrapped for very simple endpoints.

Consistency is preferred, but the API should remain practical.

---

# 10. Resource Identifiers

Public resource identifiers should not expose database implementation details.

UUID will be the preferred identifier type.

Example:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

The database may use an appropriate internal representation, but APIs should expose stable identifiers.

---

# 11. JSON Naming Convention

JSON properties will use camelCase.

Correct:

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2026-08-22T10:30:00Z"
}
```

Avoid:

```json
{
  "first_name": "John",
  "created_at": "..."
}
```

Java fields should also normally use camelCase.

---

# 12. Date and Time Format

All API timestamps should use ISO-8601 format.

Example:

```text
2026-08-22T10:30:00Z
```

The backend should store timestamps consistently, preferably in UTC.

Client applications may convert timestamps to the user's local timezone for display.

---

# 13. Authentication

Protected APIs will use Bearer Token authentication.

Example:

```http
Authorization: Bearer <access-token>
```

Authentication will initially use JWT.

Public endpoints may include:

```text
/api/v1/auth/register
/api/v1/auth/login
```

Protected endpoints require authentication.

---

# 14. Authorization

Authentication and authorization are separate concepts.

Authentication answers:

> Who is the user?

Authorization answers:

> Is this user allowed to perform this operation?

Example:

```text
User A
   |
   | GET /api/v1/resumes/{resumeId}
   v
Resume Service
   |
   +-- Verify JWT
   |
   +-- Verify ownership
   |
   v
Allow / Deny
```

A valid JWT alone must not grant access to another user's resources.

---

# 15. Error Response

All APIs should use a common error structure.

Recommended format:

```json
{
  "timestamp": "2026-08-22T10:30:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Email address is invalid",
  "path": "/api/v1/auth/register",
  "correlationId": "8d5c8a1f-1234-4567-8901-abcdef123456"
}
```

---

# 16. Error Codes

Application-level error codes should be stable and machine-readable.

Examples:

```text
VALIDATION_ERROR
RESOURCE_NOT_FOUND
UNAUTHORIZED
FORBIDDEN
DUPLICATE_RESOURCE
INVALID_CREDENTIALS
TOKEN_EXPIRED
INVALID_TOKEN
RATE_LIMIT_EXCEEDED
INTERNAL_ERROR
DEPENDENCY_FAILURE
```

The frontend should use the error code rather than relying on the human-readable message for application logic.

---

# 17. Validation

Input validation must happen at the API boundary.

Spring Bean Validation will be used.

Example:

```java
@NotBlank
private String name;

@NotBlank
@Email
private String email;

@NotBlank
@Size(min = 8)
private String password;
```

Validation should include:

- Required fields
- String length
- Email format
- Numeric ranges
- Enum values
- File type
- File size
- Business constraints

---

# 18. Validation Error Response

For validation failures, the API should provide useful field-level information.

Example:

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
    },
    {
      "field": "password",
      "message": "Password must contain at least 8 characters"
    }
  ]
}
```

---

# 19. Pagination

Collection APIs that may return large datasets should support pagination.

Recommended query parameters:

```text
page
size
```

Example:

```http
GET /api/v1/resumes?page=0&size=20
```

Pagination should be zero-based.

Example response:

```json
{
  "data": [
    {
      "id": "123",
      "fileName": "resume.pdf"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 57,
    "totalPages": 3
  }
}
```

---

# 20. Pagination Limits

The server must enforce a maximum page size.

Example:

```text
Default size: 20
Maximum size: 100
```

A request such as:

```http
GET /api/v1/resumes?page=0&size=10000
```

must not cause the server to retrieve 10,000 records without applying a limit.

---

# 21. Sorting

Collection endpoints may support sorting.

Example:

```http
GET /api/v1/resumes?sort=createdAt,desc
```

Multiple sorting fields may be supported when required.

Example:

```http
GET /api/v1/resumes?sort=createdAt,desc&sort=fileName,asc
```

Only approved sortable fields should be accepted.

Clients should not be allowed to provide arbitrary database column names.

---

# 22. Filtering

Filtering should use query parameters.

Example:

```http
GET /api/v1/resumes?status=COMPLETED
```

Multiple filters may be supported when appropriate.

Filtering rules must be documented by each service.

---

# 23. Idempotency

Operations that may be retried and could create duplicate resources should support idempotency where appropriate.

Example:

```http
Idempotency-Key: 2b8f8f36-7b3d-4c7c-9c2c-123456789abc
```

This is particularly important for future operations involving:

- Payments
- Subscription changes
- Resume processing requests
- Long-running AI jobs

Idempotency should not be added to every endpoint unnecessarily.

---

# 24. Correlation ID

Every incoming request should have a correlation ID.

Header:

```http
X-Correlation-ID: 8d5c8a1f-1234-4567-8901-abcdef123456
```

If the client does not provide one, the API Gateway should generate it.

The correlation ID should be propagated between services.

Example:

```text
Frontend
   |
   | X-Correlation-ID
   v
API Gateway
   |
   +------> Auth Service
   |
   +------> Resume Service
   |
   +------> ATS Service
```

This allows a request to be traced across multiple services.

---

# 25. Request ID

A request ID may also be generated for individual service processing.

Example:

```text
correlationId
requestId
```

The correlation ID represents the overall request flow.

The request ID identifies processing within a specific service.

---

# 26. API Timeout

Every external service call must have a timeout.

A service must not wait indefinitely for another service.

Example:

```text
Resume Service
      |
      | timeout: configured value
      v
ATS Service
```

Timeout values should be configured rather than hard-coded.

---

# 27. Retry Policy

Retries should only be used when the failure is likely to be temporary.

Suitable examples:

```text
Network timeout
Temporary service unavailable
Transient infrastructure failure
```

Retries should not be used for:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
Validation failures
```

Retries must use bounded attempts and appropriate backoff.

---

# 28. Circuit Breaking

Circuit breaking may be introduced for unstable external dependencies.

Example:

```text
AI Service
    |
    v
LLM Provider
    |
    X unavailable
```

A circuit breaker can prevent repeated requests from overwhelming an unavailable dependency.

This will be introduced when service-to-service and external-provider usage justifies it.

---

# 29. File Upload APIs

Resume uploads will use:

```http
Content-Type: multipart/form-data
```

Example:

```http
POST /api/v1/resumes
```

Validation must include:

- Allowed file types
- Maximum file size
- File extension
- MIME type
- File content validation
- Malicious file detection where appropriate

Supported formats for the MVP:

```text
PDF
DOCX
```

The exact limits will be defined in the Resume Service API contract.

---

# 30. API Security

API endpoints must follow secure development practices.

Requirements:

- HTTPS in production
- JWT authentication
- Authorization checks
- Input validation
- Rate limiting
- Secure headers
- CORS configuration
- File validation
- No sensitive data in URLs
- No passwords in logs
- No tokens in logs
- No secrets in source code

---

# 31. Sensitive Data

Sensitive information must not be returned unnecessarily.

Examples:

```text
Password hash
Refresh token
Internal database identifiers
LLM API key
AWS credentials
Internal service URLs
```

For example, the user registration response must not return:

```json
{
  "passwordHash": "$2a$..."
}
```

---

# 32. HTTP Headers

Common headers may include:

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <token>
X-Correlation-ID: <id>
```

For file uploads:

```http
Content-Type: multipart/form-data
```

---

# 33. API Naming

Use nouns for resources.

Preferred:

```http
GET /api/v1/resumes
GET /api/v1/users
GET /api/v1/ats/reports
```

Avoid unnecessarily verbose action-based URLs:

```http
GET /api/v1/getAllUserResumes
GET /api/v1/getUserDetails
```

Actions are appropriate when the operation is not naturally represented as CRUD.

Example:

```http
POST /api/v1/ats/analyze
POST /api/v1/ai/review-resume
```

---

# 34. Internal vs Public APIs

Not every service endpoint should automatically be exposed through the API Gateway.

Public APIs:

```text
/api/v1/auth/**
/api/v1/resumes/**
/api/v1/ats/**
/api/v1/ai/**
```

Internal service APIs should remain inaccessible from the public internet.

Example:

```text
ATS Service
     |
     | Internal API
     v
Resume Service
```

Internal endpoints should be protected using appropriate service-to-service authentication.

---

# 35. API Documentation

All APIs must be documented using OpenAPI.

Documentation should include:

- Endpoint
- HTTP method
- Description
- Authentication requirement
- Request parameters
- Request body
- Response body
- Status codes
- Validation rules
- Error responses
- Example requests
- Example responses

Swagger UI will be used during development.

---

# 36. API Contract Ownership

Each service owns its API contract.

Example:

```text
Auth Service
    |
    v
Auth API Contract

Resume Service
    |
    v
Resume API Contract

ATS Service
    |
    v
ATS API Contract

AI Service
    |
    v
AI API Contract
```

The API Gateway should route requests but should not redefine the business contract of the underlying service.

---

# 37. Backward Compatibility

Existing APIs should not be broken without a planned migration.

Breaking changes include:

- Removing fields
- Changing field types
- Changing endpoint paths
- Changing required fields
- Changing authentication requirements
- Changing response structure

For breaking changes, introduce a new API version when appropriate.

---

# 38. API Deprecation

Deprecated APIs must be documented.

Example:

```text
/api/v1/auth/login
```

may eventually be replaced by:

```text
/api/v2/auth/login
```

The deprecated version should have a documented sunset and migration plan.

---

# 39. API Testing

Every API should have tests covering:

### Happy Path

```text
Valid request
→ Expected successful response
```

### Validation

```text
Invalid request
→ 400
```

### Authentication

```text
Missing token
→ 401
```

### Authorization

```text
Insufficient permission
→ 403
```

### Resource Not Found

```text
Unknown ID
→ 404
```

### Conflict

```text
Duplicate resource
→ 409
```

### Unexpected Failure

```text
Internal error
→ 500
```

---

# 40. API Design Example

A properly designed resource API should look like:

```text
POST   /api/v1/resumes
GET    /api/v1/resumes
GET    /api/v1/resumes/{resumeId}
PATCH  /api/v1/resumes/{resumeId}
DELETE /api/v1/resumes/{resumeId}
```

For an operation:

```text
POST /api/v1/ats/analyze
```

For AI processing:

```text
POST /api/v1/ai/review-resume
```

---

# 41. API Standards Summary

| Standard | Decision |
|---|---|
| Protocol | HTTPS in production |
| Architecture | REST |
| Versioning | URL path |
| Version | `/api/v1` |
| JSON naming | camelCase |
| Resource naming | lowercase/plural where appropriate |
| IDs | UUID |
| Authentication | JWT |
| Authorization | Resource + role based |
| Documentation | OpenAPI |
| Validation | Jakarta Bean Validation |
| Error format | Standard error object |
| Pagination | `page` + `size` |
| Sorting | `sort` |
| Correlation | `X-Correlation-ID` |
| Date format | ISO-8601 UTC |
| File upload | multipart/form-data |
| API Gateway | Single public entry point |
| Database access | Service-owned only |

---

# 42. Current API Development Priority

The first API contract to be created is the Auth Service API.

The initial endpoints will be:

```text
POST /api/v1/auth/register

POST /api/v1/auth/login

POST /api/v1/auth/refresh

POST /api/v1/auth/logout

GET /api/v1/auth/me
```

These endpoints will be documented separately in:

```text
docs/03-api/auth-api.md
```

The Auth API contract will define:

- Request DTOs
- Response DTOs
- Validation
- Status codes
- Error scenarios
- Authentication requirements
- Example requests
- Example responses

---

# 43. Final Principle

The API should be designed for consumers, not for the database.

The API contract should remain stable even if the internal implementation changes.

For example:

```text
Frontend
   |
   | POST /api/v1/auth/register
   v
API Gateway
   |
   v
Auth Service
   |
   v
PostgreSQL
```

The frontend should not need to know:

- Database structure
- JPA entities
- Internal service classes
- Repository implementation
- Internal infrastructure

Only the API contract should be exposed to API consumers.