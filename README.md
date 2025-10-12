# Clipper Barber Shop - Backend

This repository contains the backend for Clipper Barber Shop — a Spring Boot application providing multi-tenant SaaS features for barber shops (companies), user management, bookings, availability, promotions and more.

Quick summary
- Java 17, Spring Boot 3.5.x (Jakarta namespace)
- Spring Data JPA + PostgreSQL
- Hexagonal architecture (ports & adapters)
- JWT-based authentication; users stored in DB and used by Spring Security
- Standardized JSON responses using a shared ApiResponse: { status, mensaje, data }

Repository structure (high level)

```
clipperBarberShop/
├─ pom.xml
├─ src/main/java/apex/code/clipperBarberShop/
│  ├─ ClipperBarberShopApplication.java
│  ├─ SecurityConfig.java
│  ├─ auth/             # Authentication module (login, JWT, security)
│  ├─ registro/         # Registration module (company/user registration)
│  ├─ shared/           # shared helpers (ApiResponse, exception handler)
│  ├─ Entities/         # JPA entities for database tables
│  └─ adapters/in/web   # sample controllers (TestController)
└─ src/main/resources/
   ├─ application.properties
   └─ init/             # SQL init scripts
```

Architecture and important behavior
- Hexagonal (Ports & Adapters): business logic (application/domain) is decoupled from infrastructure.
- All REST controllers return a standard JSON envelope: ApiResponse { status, mensaje, data }.
- Passwords are hashed with BCrypt (via Spring's PasswordEncoder) before storing.
- Authentication:
  - `POST /api/auth/login` returns a JWT inside ApiResponse.data.token.
  - A `JwtAuthenticationFilter` validates tokens and populates the SecurityContext.
  - `DatabaseUserDetailsService` loads user details from the `usuarios` table when needed by Spring Security.

Public vs protected endpoints
- Public (no token required):
  - `POST /api/registro/empresa` — register company + owner
  - `POST /api/registro/cliente` — register client (no company)
  - `POST /api/auth/login` — obtain JWT
  - `GET /api/test/public` — test public endpoint
- Protected (token required):
  - `POST /api/registro/empleado` — register employee *requires authentication*; the caller must be OWNER of a company (role check enforced in service/method level)
  - `GET /api/test/protected` — example protected endpoint (requires role)

How responses look
All responses use the shared ApiResponse envelope. Examples:

Success (login):
```json
{
  "status": "success",
  "mensaje": "Autenticación exitosa",
  "data": { "token": "eyJ..." }
}
```

Error (not found / bad request):
```json
{
  "status": "error",
  "mensaje": "Usuario no encontrado",
  "data": null
}
```

How to run

1. Start PostgreSQL (docker-compose included):

```bash
docker-compose up -d
```

2. Build & run the app:

```bash
./mvnw spring-boot:run
```

3. App runs at `http://localhost:8080`.

Configuration notes
- Set a secure JWT secret and expiration in `application.properties` or environment variables:

```properties
app.jwt.secret=your-very-secret-key
app.jwt.expiration-ms=3600000
```

Development tips
- Use `POST /api/registro/empresa` to create an owner; then `POST /api/auth/login` with the owner's email/password to get a token. Use that token to call protected endpoints (e.g., create employee).
- The `ApiResponse.mensaje` field uses Spanish messages in the current implementation; you can easily switch to English or i18n.

Module READMEs
- See `src/main/java/apex/code/clipperBarberShop/registro/README.md` and `src/main/java/apex/code/clipperBarberShop/auth/README.md` for detailed endpoint docs and examples.

