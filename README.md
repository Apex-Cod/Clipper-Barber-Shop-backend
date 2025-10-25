# Clipper Barber Shop - Backend

This repository contains the backend for Clipper Barber Shop — a Spring Boot application providing multi-tenant SaaS features for barber shops (companies), user management, bookings, availability, promotions and more.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Supabase account (for image storage)

### 1. Clone the repository
```bash
git clone https://github.com/Apex-Cod/Clipper-Barber-Shop-backend.git
cd Clipper-Barber-Shop-backend
```

### 2. Configure Environment Variables
```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your actual values
nano .env
```

**Required variables:**
- Database: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- JWT: `JWT_SECRET` (use a strong random key in production)
- Email: `MAIL_USERNAME`, `MAIL_PASSWORD` (Gmail App Password)
- Supabase: `SUPABASE_URL`, `SUPABASE_API_KEY` (service_role key)

See [ENV-SETUP.md](ENV-SETUP.md) for detailed configuration guide.

### 3. Setup Database
```bash
# Create database
createdb clipperdb

# Run initialization scripts (optional)
psql -d clipperdb -f init/clipper-barberShop.sql
```

### 4. Run the application
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

## 📋 Quick Summary
- Java 17, Spring Boot 3.5.x (Jakarta namespace)
- Spring Data JPA + PostgreSQL
- Hexagonal architecture (ports & adapters)
- JWT-based authentication; users stored in DB and used by Spring Security
- Standardized JSON responses using a shared ApiResponse: { status, mensaje, data }
- Multi-bucket Supabase Storage integration for images

## 📁 Repository Structure (High Level)

```
clipperBarberShop/
├─ pom.xml
├─ .env.example          # Environment variables template
├─ ENV-SETUP.md          # Environment configuration guide
├─ SUPABASE-SETUP.md     # Supabase configuration guide
├─ src/main/java/apex/code/clipperBarberShop/
│  ├─ ClipperBarberShopApplication.java
│  ├─ SecurityConfig.java
│  ├─ auth/             # Authentication module (login, JWT, security)
│  ├─ register/         # Registration module (company/user registration)
│  ├─ empresa/          # Company configuration & image management
│  ├─ servicio/         # Services module (CRUD by role)
│  ├─ reserva/          # Reservations module (booking system)
│  ├─ shared/           # Shared helpers (ApiResponse, exception handler)
│  ├─ Entities/         # JPA entities for database tables
│  └─ adapters/in/web   # REST controllers
└─ src/main/resources/
   ├─ application.properties  # Uses environment variables
   └─ init/                   # SQL init scripts
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

