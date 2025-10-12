# Registro module (registro)

This module handles user and company registration flows. It follows the hexagonal architecture: controllers (adapters.in.web) call application services which use domain ports to persist data through persistence adapters.

Key responsibilities

- Register a company together with its owner (tenant + owner in one endpoint)
- Register employees associated to a company
- Register clients (no company association required)

Packages overview

- `adapters/in/web` — REST controllers (endpoints)
- `application/service` — use-cases and DTOs
- `domain/port/out` — repository ports (interfaces)
- `adapters/out/persistence` — Spring Data repositories and persistence adapters

Endpoints

1) Register company + owner (public)
- POST `/api/registro/empresa`
- Request JSON example:

```json
{
  "empresaNombre": "Acme Barber",
  "empresaEmail": "contact@acmebarber.com",
  "adminName": "Alice",
  "adminLastName": "Owner",
  "adminEmail": "alice@acmebarber.com",
  "adminPassword": "SuperSecret123"
}
```

Response (ApiResponse):

```json
{
  "status": "success",
  "mensaje": "Empresa y admin registrados",
  "data": null
}
```

2) Register employee (protected)
- POST `/api/registro/empleado`
- Requires authentication. Caller must be OWNER of a company. The endpoint extracts the admin user id from the token and uses the admin's company id automatically.
- Request JSON example:

```json
{
  "name": "Bob",
  "lastName": "Barber",
  "email": "bob@acmebarber.com",
  "password": "EmployeePass123"
}
```

Example curl (requires Authorization header with owner token):

```bash
curl -v -X POST http://localhost:8080/api/registro/empleado \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <OWNER_TOKEN>" \
  -d '{"name":"Bob","lastName":"Barber","email":"bob@acmebarber.com","password":"EmployeePass123"}'
```

Response (ApiResponse):

```json
{
  "status": "success",
  "mensaje": "Empleado registrado",
  "data": null
}
```

3) Register client (public)
- POST `/api/registro/cliente`
- Request JSON example:

```json
{
  "name": "Charlie",
  "lastName": "Client",
  "email": "charlie@example.com",
  "password": "ClientPass123"
}
```

Response (ApiResponse):

```json
{
  "status": "success",
  "mensaje": "Cliente registrado",
  "data": null
}
```

Notes & recommendations

- Passwords should be hashed using the application's `PasswordEncoder` before persisting. Consider updating the registration service to encode passwords.
- Add DTO validation (`@NotBlank`, `@Email`, `@Valid`) for stricter input validation.
- Currently endpoints return `200 OK` on success. Consider returning `201 Created` with created resource identifiers for better REST semantics.

*** End Patch