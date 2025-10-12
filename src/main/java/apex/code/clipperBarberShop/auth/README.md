# Módulo de Autenticación (Auth)

Este módulo sigue la arquitectura hexagonal (ports and adapters) para gestionar la autenticación de usuarios mediante JWT.

## Estructura

```
auth/
├── adapters/           # Adaptadores (driving/driven)
│   ├── in/            # Adaptadores de entrada (driving)
│   │   └── web/       # Controladores REST
│   └── out/           # Adaptadores de salida (driven)
├── application/        # Capa de aplicación
│   ├── dto/           # Data Transfer Objects
│   └── service/       # Servicios de aplicación (casos de uso)
├── domain/            # Capa de dominio
│   └── port/          # Puertos (interfaces)
│       ├── in/        # Puertos de entrada (use cases)
│       └── out/       # Puertos de salida (repositories, services externos)
├── JwtAuthenticationFilter.java  # Filtro de autenticación JWT
└── JwtTokenProvider.java         # Proveedor de tokens JWT
```

## Componentes

### Puertos de Entrada (Use Cases)
- **AuthUseCase**: Define las operaciones de autenticación disponibles

### Puertos de Salida
- **AuthUserRepositoryPort**: Acceso a datos de usuarios
- **TokenProviderPort**: Generación de tokens JWT
- **PasswordEncoderPort**: Codificación y verificación de contraseñas

### Adaptadores de Entrada
- **AuthController**: Controlador REST para endpoints de autenticación

### Adaptadores de Salida
- **AuthUserRepositoryAdapter**: Implementación del repositorio de usuarios
- **TokenProviderAdapter**: Implementación del proveedor de tokens
- **PasswordEncoderAdapter**: Implementación del encoder de contraseñas

### DTOs
- **AuthRequest**: Datos de entrada para autenticación
- **AuthResponse**: Respuesta con token JWT

## Flujo de Autenticación

1. **AuthController** recibe la petición HTTP
2. Delega al **AuthUseCase** (implementado por **AuthService**)
3. **AuthService** usa los puertos de salida para:
   - Buscar el usuario por email
   - Verificar la contraseña
   - Generar el token JWT
4. Retorna **AuthResponse** con el token

## Beneficios de esta Arquitectura

- **Separación de responsabilidades**: Cada capa tiene una responsabilidad específica
- **Inversión de dependencias**: El dominio no depende de la infraestructura
- **Testabilidad**: Fácil de testear mediante mocks de los puertos
- **Mantenibilidad**: Cambios en infraestructura no afectan la lógica de negocio
- **Flexibilidad**: Fácil intercambio de implementaciones de adaptadores

## Endpoints

### POST /api/auth/login
Autentica un usuario y retorna un token JWT.

**Request:**
```json
{
  "email": "alice@acmebarber.com",
  "password": "SuperSecret123"
}
```

**Response:**
```json
{
  "status": "success",
  "mensaje": "Autenticación exitosa",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Ejemplo con curl:**
```bash
curl -v -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@acmebarber.com","password":"SuperSecret123"}'
```

## Uso del Token

Agregar header: `Authorization: Bearer <token>` a endpoints protegidos.

## Configuración y Seguridad

- Secret JWT y expiración configurados vía propiedades `app.jwt.secret` y `app.jwt.expiration-ms`
- Establecer un secret seguro en producción
- Passwords must be stored hashed (BCrypt) in the database and compared using the `PasswordEncoder` bean.

*** End Patch