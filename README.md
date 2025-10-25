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
├─ SERVICIOS-DEFAULT.md  # Default services on company registration
├─ SERVICIOS-RESERVAS.md # Services and reservations API documentation
├─ PROMOCIONES.md        # Promotions system documentation
├─ src/main/java/apex/code/clipperBarberShop/
│  ├─ ClipperBarberShopApplication.java
│  ├─ SecurityConfig.java
│  ├─ auth/             # Authentication module (login, JWT, security)
│  ├─ register/         # Registration module (company/user registration)
│  ├─ empresa/          # Company configuration & image management
│  ├─ servicio/         # Services module (CRUD by role, default services)
│  ├─ reserva/          # Reservations module (booking system)
│  ├─ promocion/        # Promotions module (discounts and offers)
│  ├─ shared/           # Shared helpers (ApiResponse, exception handler)
│  ├─ Entities/         # JPA entities for database tables
│  └─ adapters/in/web   # REST controllers
└─ src/main/resources/
   ├─ application.properties  # Uses environment variables
   └─ init/                   # SQL init scripts
```

## 🎯 Features

### 🔐 Authentication & Authorization
- JWT-based authentication with role-based access control (OWNER, EMPLOYEE, CLIENT)
- Email verification system with 6-digit codes
- Password reset functionality
- Secure user registration with input validation

### 🏢 Multi-Tenant Company Management
- Companies with geolocation and business hours
- Image management with Supabase Storage (logos and banners)
- Social media integration
- **Public objective targeting** (Men, Women, Unisex, Kids)

### 💈 Services Module
- **Role-based CRUD**: OWNER full control, CLIENT read-only
- **Default services**: Auto-created on company registration based on target audience
  - 4 services for Men (barber services)
  - 5 services for Women (salon services)
  - 5 services for Unisex (general services)
  - 3 services for Kids (children services)
- Image support via Supabase Storage
- Service pricing and duration management
- See [SERVICIOS-DEFAULT.md](SERVICIOS-DEFAULT.md) for details

### 📅 Reservations System
- Client booking with conflict detection
- Business hours validation
- Reservation status management (PENDING, CONFIRMED, COMPLETED, CANCELLED, RESCHEDULED)
- Price calculation with promotion support
- See [SERVICIOS-RESERVAS.md](SERVICIOS-RESERVAS.md) for API documentation

### 🎁 Promotions Module
- **OWNER-only management**: Full CRUD operations
- Two discount types: Percentage or Fixed Amount
- Validity period with date ranges
- Usage limits and tracking
- Service-specific or general promotions
- Minimum amount requirements
- Automatic price calculation in reservations
- See [PROMOCIONES.md](PROMOCIONES.md) for complete documentation

### 📧 Email System
- Verification emails with codes
- Welcome emails
- Password reset emails
- Gmail SMTP integration

### 🗄️ Data Management
- Soft delete pattern across all entities
- Input sanitization and validation
- Standardized API responses
- Comprehensive error handling

## 📚 Documentation

- **[ENV-SETUP.md](ENV-SETUP.md)** - Complete environment configuration guide
- **[SUPABASE-SETUP.md](SUPABASE-SETUP.md)** - Supabase Storage setup for images
- **[SERVICIOS-DEFAULT.md](SERVICIOS-DEFAULT.md)** - Default services on registration
- **[SERVICIOS-RESERVAS.md](SERVICIOS-RESERVAS.md)** - Services & reservations API
- **[PROMOCIONES.md](PROMOCIONES.md)** - Promotions system documentation

## Architecture and important behavior
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

