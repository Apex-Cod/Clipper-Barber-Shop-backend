# Validaciones y Controles de Seguridad

Este documento describe las validaciones y controles implementados en los módulos de autenticación y registro.

## Validaciones de Contraseña

Las contraseñas deben cumplir con los siguientes requisitos de seguridad:

### Requisitos Obligatorios
- **Longitud mínima:** 8 caracteres
- **Longitud máxima:** 100 caracteres
- **Al menos una letra mayúscula** (A-Z)
- **Al menos una letra minúscula** (a-z)
- **Al menos un número** (0-9)
- **Al menos un carácter especial:** `!@#$%^&*()_+-=[]{}|;:,.<>?`

### Ejemplos
✅ **Válidas:**
- `Password123!`
- `MySecure@Pass1`
- `Admin#2024Pass`

❌ **Inválidas:**
- `password` (falta mayúscula, número y carácter especial)
- `PASSWORD123` (falta minúscula y carácter especial)
- `Pass1!` (muy corta, menos de 8 caracteres)

## Validaciones de Email

### Requisitos
- Formato de email válido (RFC 5322)
- Longitud máxima: 100 caracteres
- No puede estar vacío
- Se convierte automáticamente a minúsculas
- Se eliminan espacios en blanco al inicio y final

### Ejemplos
✅ **Válidos:**
- `usuario@example.com`
- `admin.user@empresa.com.mx`
- `empleado_1@domain.co`

❌ **Inválidos:**
- `usuario@` (formato incompleto)
- `@example.com` (falta parte local)
- `usuario example.com` (falta @)

## Validaciones de Nombres y Apellidos

### Requisitos
- Longitud mínima: 2 caracteres
- Longitud máxima: 50 caracteres
- Solo pueden contener letras (incluye acentos y ñ) y espacios
- No puede estar vacío
- Se eliminan espacios en blanco al inicio y final
- Se normalizan espacios múltiples a un solo espacio

### Ejemplos
✅ **Válidos:**
- `Juan`
- `María José`
- `Pérez Rodríguez`
- `José Ángel`

❌ **Inválidos:**
- `J` (muy corto)
- `Juan123` (contiene números)
- `María@` (contiene caracteres especiales)

## Validaciones de Nombre de Empresa

### Requisitos
- Longitud mínima: 2 caracteres
- Longitud máxima: 100 caracteres
- No puede estar vacío
- Se eliminan espacios en blanco al inicio y final

## Validaciones en Endpoints

### POST `/api/registro/empresa`
Registra una nueva empresa con su administrador.

**Validaciones:**
- Todos los campos obligatorios
- Email de empresa único
- Email de administrador único
- Contraseña segura según reglas definidas
- Nombres y apellidos con formato válido

### POST `/api/registro/cliente`
Registra un nuevo cliente.

**Validaciones:**
- Todos los campos obligatorios
- Email único
- Contraseña segura según reglas definidas
- Nombres y apellidos con formato válido

### POST `/api/registro/empleado`
Registra un nuevo empleado (requiere rol OWNER).

**Validaciones:**
- Todos los campos obligatorios
- ID de empresa válido (numérico)
- Email único
- Contraseña segura según reglas definidas
- Nombres y apellidos con formato válido
- Usuario autenticado debe tener rol OWNER

### POST `/api/auth/login`
Autentica un usuario.

**Validaciones:**
- Email con formato válido
- Contraseña no vacía (entre 1 y 100 caracteres)

## Sanitización de Datos

Todos los datos de entrada son sanitizados automáticamente:

1. **Trim:** Se eliminan espacios en blanco al inicio y final
2. **Normalización de emails:** Se convierten a minúsculas
3. **Prevención de duplicados:** Se verifica que el email no esté registrado
4. **Normalización de espacios:** Espacios múltiples se convierten a uno solo

## Respuestas de Error

### Error de Validación (400 Bad Request)
```json
{
  "success": false,
  "message": "Error de validación: [detalles del error]",
  "data": {
    "campo1": "mensaje de error",
    "campo2": "mensaje de error"
  }
}
```

### Error de Email Duplicado (400 Bad Request)
```json
{
  "success": false,
  "message": "El email ya está registrado",
  "data": null
}
```

### Error de Permisos (403 Forbidden)
Cuando un usuario sin permisos intenta acceder a un endpoint protegido.

## Seguridad Adicional

- Las contraseñas se almacenan encriptadas con BCrypt
- Los emails se normalizan para evitar duplicados con diferentes formatos
- Se valida la existencia de empresas antes de crear empleados
- Se verifica el rol del usuario antes de permitir operaciones sensibles

## Clases Relacionadas

- **Validadores:**
  - `ValidPassword` - Anotación de validación de contraseña
  - `PasswordValidator` - Implementación del validador

- **DTOs:**
  - `RegistroRequest` - Registro de empresa con admin
  - `ClienteRequest` - Registro de cliente
  - `EmpleadoRequest` - Registro de empleado
  - `AuthRequest` - Autenticación

- **Utilidades:**
  - `StringUtils` - Utilidades para sanitización de strings

- **Manejo de Errores:**
  - `GlobalExceptionHandler` - Manejo centralizado de excepciones
