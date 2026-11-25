# CareerPilot AI -- Authentication Service

## Complete Technical Documentation

**Project:** CareerPilot AI\
**Module:** Authentication Service\
**Stack:** Java, Spring Boot, Spring Security, Spring Data JPA,
PostgreSQL, JWT, Flyway, BCrypt, MapStruct, Maven

------------------------------------------------------------------------

## 1. Introduction

The Authentication Service manages user identity and controls access to
protected resources in CareerPilot AI.

Its responsibilities are user registration, login, password hashing, JWT
access-token authentication, refresh-token generation and rotation,
logout, logout from all sessions, account-status validation, role-based
authorization, and REST API protection.

The service uses two token types:

-   **Access token:** Authenticates API requests.
-   **Refresh token:** Obtains a new token pair without requiring the
    user to log in again.

## 2. Technology Stack

  Technology        Responsibility
  ----------------- ----------------------------------
  Java              Programming language
  Spring Boot       Application framework
  Spring Security   Authentication and authorization
  Spring Data JPA   Database interaction
  PostgreSQL        Persistent data storage
  JWT               Access-token authentication
  BCrypt            Password hashing
  Flyway            Database schema migrations
  MapStruct         Entity-to-DTO mapping
  Maven             Dependency and build management
  Postman           API testing

## 3. High-Level Architecture

The application follows a layered architecture:

``` text
Client (Postman / Frontend)
          |
       REST APIs
          |
          v
    AuthController
          |
          v
      AuthService
          |
          v
    AuthServiceImpl
          |
     +----+------------------+
     |                       |
     v                       v
UserRepository      RefreshTokenRepository
     |                       |
     +-----------+-----------+
                 |
                 v
             PostgreSQL
```

Spring Security protects incoming requests:

``` text
Incoming HTTP Request
          |
          v
    SecurityConfig
          |
          v
JwtAuthenticationFilter
          |
     Validate JWT
          |
    Extract User UUID
          |
          v
    UserRepository
          |
    Check User Status
          |
      Load Role
          |
          v
    SecurityContext
          |
          v
 Authorization Rules
          |
          v
    REST Controller
```

## 4. Database Design

The Auth Service uses two primary tables.

### 4.1 Users table

The `users` table stores user account information.

  Column            Description
  ----------------- ---------------------------------
  `id`              Unique user UUID
  `name`            User's name
  `email`           Unique email address
  `password_hash`   BCrypt password hash
  `role`            USER or ADMIN
  `status`          ACTIVE, INACTIVE or LOCKED
  `created_at`      Account creation timestamp
  `updated_at`      Last update timestamp
  `last_login_at`   Last successful login timestamp

Role enum:

``` java
public enum Role {
    USER,
    ADMIN
}
```

User status enum:

``` java
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    LOCKED
}
```

New registrations receive `USER` role and `ACTIVE` status. Public
registration must not allow users to assign themselves the ADMIN role.

### 4.2 Refresh tokens table

The `refresh_tokens` table manages active and revoked refresh tokens.

  Column                   Description
  ------------------------ ----------------------------
  `id`                     Unique token UUID
  `user_id`                Associated user UUID
  `token_hash`             Hashed refresh token
  `expires_at`             Token expiration timestamp
  `created_at`             Token creation timestamp
  `revoked_at`             Revocation timestamp
  `replaced_by_token_id`   Replacement token UUID

Token hashes are unique. Each refresh token belongs to a user. The
application stores the refresh-token hash rather than its original
value.

## 5. User Registration Flow

**API:** `POST /api/v1/auth/register`

Example request:

``` json
{
    "name": "Anshu Rai",
    "email": "avinash@gmail.com",
    "password": "example-password"
}
```

Flow:

1.  The client sends registration information.
2.  `AuthController` receives the request.
3.  The controller calls `AuthService`.
4.  `AuthServiceImpl` checks whether the email already exists.
5.  If it exists, `EmailAlreadyExistsException` is thrown.
6.  The password is hashed using BCrypt.
7.  A `User` entity is created and assigned a UUID.
8.  The backend assigns role `USER` and status `ACTIVE`.
9.  Creation and update timestamps are populated.
10. `UserRepository` saves the user.
11. `UserMapper` maps the entity to `UserResponse`.
12. The API returns the response.

Registration sequence:

``` text
Client -> AuthController -> AuthServiceImpl
                            |
                            v
                     Check email
                            |
                     UserRepository
                            |
                +-----------+-----------+
                |                       |
              Exists                 Not found
                |                       |
                v                       v
        EmailAlreadyExists        Hash password
                                        |
                                  Create User
                                        |
                                Assign USER/ACTIVE
                                        |
                                 Save to database
                                        |
                                    UserMapper
                                        |
                                  UserResponse
```

**Security decision:** Even if a client submits `role: ADMIN`, the
backend assigns `USER`. The registration DTO should not expose a role
field. This prevents privilege escalation through public registration.

## 6. Password Hashing Using BCrypt

Passwords must never be stored as plain text.

Password encoder configuration:

``` java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

During registration:

``` java
String encodedPassword =
        passwordEncoder.encode(request.getPassword());
```

During login:

``` java
passwordEncoder.matches(
    request.getPassword(),
    user.getPasswordHash()
);
```

BCrypt is a one-way password hashing algorithm. The original password is
not decrypted during login.

## 7. User Login Flow

**API:** `POST /api/v1/auth/login`

Example request:

``` json
{
    "email": "avinash@gmail.com",
    "password": "example-password"
}
```

Flow:

1.  Client sends email and password.
2.  `AuthController` receives the request.
3.  `AuthServiceImpl` normalizes the email.
4.  `UserRepository` searches for the user.
5.  The submitted password is checked using BCrypt.
6.  The user's account status is checked.
7.  Only ACTIVE users can log in.
8.  `JwtService` generates an access token.
9.  A refresh token is generated and its hash is saved in PostgreSQL.
10. The login timestamp is updated.
11. `LoginResponse` is returned.

Invalid credentials result in `InvalidCredentialsException` and HTTP
401.

## 8. JWT Access Token Generation

`JwtService` generates and validates access tokens.

A JWT contains three parts:

``` text
HEADER.PAYLOAD.SIGNATURE
```

The token contains:

-   Subject: User UUID
-   Issued-at timestamp
-   Expiration timestamp
-   Cryptographic signature

The token is signed using the configured secret key.

Example configuration:

``` properties
jwt.access-token-expiration=900000
```

This value represents 15 minutes. The actual expiration depends on the
runtime configuration.

**Design decision:** The JWT does not contain the user's role. The
application retrieves the current role from PostgreSQL during
authenticated requests. A database role change can therefore take effect
without waiting for the existing access token to expire.

## 9. JWT Authentication Filter

`JwtAuthenticationFilter` extends `OncePerRequestFilter` and runs within
the Spring Security filter chain.

For a request with a Bearer token, it:

1.  Reads the `Authorization` header.
2.  Checks that it starts with `Bearer`.
3.  Extracts the JWT.
4.  Validates it using `JwtService`.
5.  Extracts the subject.
6.  Converts the subject to a UUID.
7.  Fetches the user from PostgreSQL.
8.  Checks that the user exists and has ACTIVE status.
9.  Retrieves the user's role.
10. Creates a `UsernamePasswordAuthenticationToken`.
11. Adds the user's authority.
12. Stores the authentication in `SecurityContextHolder`.

Example header:

``` http
Authorization: Bearer <access-token>
```

Authority creation:

``` java
String authority = "ROLE_" + user.getRole().name();
```

For a USER, the authority is `ROLE_USER`. For an ADMIN, it is
`ROLE_ADMIN`.

Spring Security uses `SecurityContextHolder` to hold the current
request's authentication information:

``` java
SecurityContextHolder.getContext()
        .setAuthentication(authentication);
```

## 10. Authentication vs Authorization

**Authentication** answers: *Who are you?*

A valid JWT is checked and used to identify the user.

**Authorization** answers: *What are you allowed to access?*

For example, a user with `ROLE_USER` cannot access an ADMIN-only
endpoint.

In this application:

-   JWT validation handles token authentication.
-   Database lookup verifies current account status.
-   Spring Security authorization rules control endpoint access.

## 11. Spring Security Configuration

`SecurityConfig` defines public endpoints and endpoints requiring
authentication or specific roles.

Public endpoints:

``` text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

Admin-only endpoints:

``` text
/api/v1/admin/**
```

They require:

``` java
.hasRole("ADMIN")
```

Spring Security checks for the authority `ROLE_ADMIN`.

Authenticated endpoint example:

``` text
POST /api/v1/auth/logout-all
```

General rule:

``` java
.anyRequest().authenticated()
```

All other endpoints require authentication.

The `/error` endpoint is permitted so Spring Boot can render error
responses without turning an original authorization response into an
authentication error.

## 12. Refresh Token Flow

When an access token expires, the client can use its refresh token to
obtain a new token pair.

**API:** `POST /api/v1/auth/refresh`

Example request:

``` json
{
    "refreshToken": "<refresh-token>"
}
```

Flow:

1.  Client sends the refresh token.
2.  The application hashes the submitted token.
3.  It searches for the corresponding record in PostgreSQL.
4.  It checks whether the token exists, has expired, or has been
    revoked.
5.  If valid, the existing refresh token is revoked.
6.  A new access token and refresh token are generated.
7.  The new refresh-token hash is stored.
8.  The old token record is linked to its replacement using
    `replaced_by_token_id`.
9.  The new tokens are returned.

Rotation sequence:

``` text
Old Refresh Token
        |
        v
   Validate token
        |
        v
   Revoke old token
        |
        v
 Generate new token pair
        |
        +-------------------+
        |                   |
        v                   v
  New Access Token    New Refresh Token
                            |
                            v
                    Store token hash
                            |
                            v
                     Return response
```

Rotation prevents a revoked refresh token from being reused. The
application rejects attempts to reuse revoked refresh tokens.

## 13. Logout Flow

**API:** `POST /api/v1/auth/logout`

Flow:

1.  Client sends its refresh token.
2.  Application hashes the token.
3.  The corresponding database record is found.
4.  The token is revoked.
5.  The endpoint returns HTTP 204 No Content.

A revoked refresh token cannot be used to generate new tokens.

**Important:** Logout revokes the refresh token. It does not
automatically invalidate an already-issued JWT access token. That access
token remains usable until expiration unless an additional access-token
revocation mechanism is implemented.

## 14. Logout From All Devices

**API:** `POST /api/v1/auth/logout-all`

This endpoint requires authentication.

Flow:

1.  Client sends a valid access token.
2.  `JwtAuthenticationFilter` authenticates the user.
3.  The controller receives the authenticated principal.
4.  The user's UUID is extracted from `authentication.getName()`.
5.  All active refresh tokens belonging to that user are revoked.
6.  The endpoint returns HTTP 204 No Content.

This prevents existing refresh tokens from being used to establish new
sessions. Already-issued access tokens are not automatically invalidated
by refresh-token revocation.

## 15. User Account Status Validation

The application supports:

``` text
ACTIVE
INACTIVE
LOCKED
```

The JWT authentication filter checks the user's status on authenticated
requests.

Example:

1.  A user logs in while their account is ACTIVE and receives a JWT.
2.  An administrator changes the user's status to LOCKED in PostgreSQL.
3.  On the next request, the JWT is validated and the user UUID is
    extracted.
4.  The user is fetched from PostgreSQL.
5.  The status check fails.
6.  The user is not authenticated and a protected endpoint returns HTTP
    401.

Changing account status therefore takes effect without waiting for the
access token to expire.

## 16. Role-Based Access Control

The application currently supports `USER` and `ADMIN`.

-   A USER can access authenticated user-level endpoints.
-   A USER cannot access `/api/v1/admin/**`.
-   An ADMIN can access admin-only endpoints.

  User role           Endpoint                  Expected result
  ------------------- ------------------------- -----------------
  USER                Protected user endpoint   200
  USER                Admin endpoint            403
  ADMIN               Admin endpoint            200
  No authentication   Protected endpoint        401

### HTTP 401 vs HTTP 403

**401 Unauthorized** means the request does not have valid
authentication. Examples include a missing, invalid, or expired JWT, or
an inactive/locked account.

**403 Forbidden** means the user is authenticated but does not have
permission. For example, a USER attempts to access an ADMIN-only
endpoint.

## 17. Exception Handling

The application uses `GlobalExceptionHandler` to return consistent
application error responses.

Important exceptions:

  Exception                       Meaning
  ------------------------------- ---------------------------
  `EmailAlreadyExistsException`   Email already registered
  `InvalidCredentialsException`   Invalid login credentials

Spring Security also uses an authentication entry point for 401
responses and access-denied handling for 403 responses. The
`ErrorResponse` DTO provides a consistent application error structure.

## 18. Important Classes and Responsibilities

  -----------------------------------------------------------------------
  Class                               Responsibility
  ----------------------------------- -----------------------------------
  `AuthController`                    Exposes authentication REST
                                      endpoints

  `AuthService`                       Defines authentication operations

  `AuthServiceImpl`                   Implements authentication business
                                      logic

  `User`                              Represents the users database
                                      entity

  `Role`                              Defines user roles

  `UserStatus`                        Defines account statuses

  `UserRepository`                    Performs user database operations

  `RefreshToken`                      Represents refresh-token records

  `RefreshTokenRepository`            Performs refresh-token database
                                      operations

  `JwtService`                        Generates and validates access JWTs

  `JwtAuthenticationFilter`           Authenticates incoming requests

  `SecurityConfig`                    Defines security rules

  `UserMapper`                        Maps User entities to DTOs

  `RegisterRequest`                   Registration request DTO

  `LoginRequest`                      Login request DTO

  `LoginResponse`                     Login response DTO

  `UserResponse`                      User response DTO

  `ErrorResponse`                     Standard application error DTO

  `GlobalExceptionHandler`            Centralized exception handling
  -----------------------------------------------------------------------

## 19. API Reference

  ---------------------------------------------------------------------------
  Method                  Endpoint                    Purpose
  ----------------------- --------------------------- -----------------------
  POST                    `/api/v1/auth/register`     Register a user

  POST                    `/api/v1/auth/login`        Authenticate a user

  POST                    `/api/v1/auth/refresh`      Rotate refresh token
                                                      and obtain new tokens

  POST                    `/api/v1/auth/logout`       Revoke a refresh token

  POST                    `/api/v1/auth/logout-all`   Revoke all user refresh
                                                      tokens

  GET                     `/api/v1/admin/test`        Test admin
                                                      authorization
  ---------------------------------------------------------------------------

The temporary `/api/v1/auth/test` endpoint was used during development
and should remain removed after testing.

## 20. Configuration

The application uses `application.properties` for local configuration.

Important properties:

``` properties
spring.application.name=careerpilot-auth-service
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/careerpilot
spring.datasource.username=appuser

spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true

jwt.secret=${JWT_SECRET}
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000
```

The JWT secret is provided through an environment variable. Never commit
real database passwords, JWT secrets, access tokens, refresh tokens, or
other credentials to GitHub.

## 21. Database Migrations

Flyway manages database schema changes. Current migration files:

``` text
V1__create_users_table.sql
V2__create_refresh_tokens_table.sql
```

Flyway applies migrations in version order and records completed
migrations in its schema history table.

Hibernate uses:

``` properties
spring.jpa.hibernate.ddl-auto=validate
```

This means Hibernate validates entity mappings against the existing
database schema instead of creating or modifying tables automatically.

## 22. End-to-End Authentication Flow

### Stage 1: Registration

``` text
Client
  |
  v
Register API
  |
  v
Validate Email
  |
  v
Hash Password
  |
  v
Create USER Account
  |
  v
Save in PostgreSQL
  |
  v
Return UserResponse
```

### Stage 2: Login

``` text
Client
  |
  v
Login API
  |
  v
Find User
  |
  v
Verify Password
  |
  v
Check Account Status
  |
  v
Generate Access JWT
  |
  v
Generate Refresh Token
  |
  v
Save Refresh Token Hash
  |
  v
Return LoginResponse
```

### Stage 3: Access a protected API

``` text
Client
  |
  | Authorization: Bearer JWT
  v
Spring Security Filter Chain
  |
  v
JwtAuthenticationFilter
  |
  v
Validate JWT
  |
  v
Extract User UUID
  |
  v
Fetch User from Database
  |
  v
Check Account Status
  |
  v
Load User Role
  |
  v
Set SecurityContext
  |
  v
Check Authorization Rules
  |
  +------ USER endpoint ---> Allow USER
  |
  +------ ADMIN endpoint --> Require ADMIN
  |
  v
Controller
  |
  v
Response
```

### Stage 4: Refresh

``` text
Access Token Expires
         |
         v
Client Sends Refresh Token
         |
         v
Validate Stored Token Hash
         |
         v
Revoke Existing Refresh Token
         |
         v
Generate New Token Pair
         |
         v
Store New Refresh Token Hash
         |
         v
Return New Tokens
```

## 23. Testing Performed

The following scenarios were tested during development:

  Test case                        Expected behavior           Result
  -------------------------------- --------------------------- --------
  Register new user                Successful registration     Passed
  Register duplicate email         HTTP 409                    Passed
  Login with valid credentials     Return tokens               Passed
  Login with invalid credentials   HTTP 401                    Passed
  Refresh with valid token         Return rotated tokens       Passed
  Reuse old refresh token          Reject token                Passed
  Refresh with invalid token       Reject token                Passed
  Logout                           HTTP 204                    Passed
  Logout-all                       HTTP 204                    Passed
  Missing JWT                      HTTP 401                    Passed
  Invalid JWT                      HTTP 401                    Passed
  Expired access token             HTTP 401                    Passed
  Expired refresh token            Reject token                Passed
  ACTIVE user                      Authenticate successfully   Passed
  LOCKED user                      Reject protected request    Passed
  USER accessing admin API         HTTP 403                    Passed
  ADMIN accessing admin API        HTTP 200                    Passed

## 24. Important Security Decisions

1.  Passwords are hashed using BCrypt.
2.  Public registration always assigns USER.
3.  JWTs contain the user's UUID as their subject.
4.  User roles are retrieved from the database on authenticated
    requests.
5.  User status is checked on authenticated requests.
6.  Refresh tokens are stored as hashes.
7.  Refresh tokens are rotated when refreshed.
8.  Old refresh tokens are revoked.
9.  Logout revokes the relevant refresh token.
10. Logout-all revokes all active refresh tokens belonging to the user.
11. Admin APIs require `ROLE_ADMIN`.
12. Application secrets are kept outside Git.

## 25. Knowledge Check Before Moving to the Next Service

Make sure you can explain these questions without referring to the code:

1.  What happens internally when a user registers?
2.  Why do we use BCrypt instead of storing passwords directly?
3.  What is the difference between an access token and a refresh token?
4.  How does Spring Security validate a JWT?
5.  What is the responsibility of `JwtAuthenticationFilter`?
6.  What is `SecurityContextHolder`?
7.  What is the difference between authentication and authorization?
8.  Why do we retrieve the user's role from PostgreSQL instead of
    putting it in the JWT?
9.  Why do we rotate refresh tokens?
10. What is the difference between HTTP 401 and HTTP 403?
11. Why can a LOCKED user no longer access protected endpoints?
12. Why should public registration never allow ADMIN role assignment?

If you can explain these concepts and trace a request from the
controller through the service, repository, database, and security
filter, you have a good understanding of the Auth Service.

------------------------------------------------------------------------

## Final Note

This document describes the implementation and behavior discussed and
tested during development. Before treating it as definitive repository
documentation, compare it with the current source code, particularly the
exact refresh-token validation, transaction handling, and
exception-handling implementations.

**CareerPilot AI -- Backend Development**
