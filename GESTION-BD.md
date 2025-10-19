# Gestión del Esquema de Base de Datos

## Estrategias de Sincronización

### 1. Desarrollo Local - `ddl-auto=update` (ACTUAL)

**Configuración actual en `application.properties`:**
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Ventajas:**
- ✅ Los cambios en las entidades se reflejan automáticamente en la BD
- ✅ No se pierden datos existentes
- ✅ Rápido para desarrollo

**Desventajas:**
- ❌ No elimina columnas obsoletas
- ❌ Algunos cambios complejos pueden fallar
- ❌ No recomendado para producción

**Cómo funciona:**
Cuando inicias la aplicación, Hibernate:
1. Compara las entidades Java con el esquema de la BD
2. Genera y ejecuta los `ALTER TABLE` necesarios
3. Agrega nuevas tablas y columnas
4. Modifica tipos de datos si es necesario

### 2. Migración Manual con Scripts SQL

Si `ddl-auto=update` no funciona correctamente:

**Paso 1:** Cambiar a modo validación
```properties
spring.jpa.hibernate.ddl-auto=validate
```

**Paso 2:** Ejecutar el script SQL manualmente
```bash
psql -U clipper -d clipperdb -f init/update-schema-validations.sql
```

### 3. Producción - Flyway o Liquibase (RECOMENDADO)

Para entornos de producción, usa herramientas de migración:

#### Opción A: Flyway

**1. Agregar dependencia en `pom.xml`:**
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

**2. Configurar en `application.properties`:**
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

**3. Crear archivos de migración:**
- `src/main/resources/db/migration/V1__initial_schema.sql`
- `src/main/resources/db/migration/V2__add_validations.sql`

## Cambios Actuales en las Entidades

### Usuario
```java
@Column(nullable = false, unique = true, length = 100)
private String email; // Max 100 caracteres

@Column(name = "last_name", nullable = false, length = 50)
private String lastName; // Max 50 caracteres

@Column(nullable = false, length = 50)
private String name; // Max 50 caracteres

@Column(nullable = false, length = 60)
private String password; // BCrypt siempre genera 60 caracteres
```

### Empresa
```java
@Column(nullable = false, length = 100)
private String nombre; // Max 100 caracteres

@Column(length = 100)
private String email; // Max 100 caracteres
```

## Verificar los Cambios en la Base de Datos

### Opción 1: Desde la aplicación
Al iniciar la aplicación con:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Verás en la consola los SQL que ejecuta Hibernate:
```sql
alter table usuarios alter column email type varchar(100)
alter table usuarios alter column password type varchar(60)
...
```

### Opción 2: Consulta directa a PostgreSQL
```sql
SELECT 
    table_name,
    column_name,
    data_type,
    character_maximum_length,
    is_nullable
FROM 
    information_schema.columns
WHERE 
    table_name IN ('usuarios', 'empresas')
ORDER BY 
    table_name, ordinal_position;
```

## Problemas Comunes y Soluciones

### Problema 1: La columna no se actualiza
**Causa:** Hibernate no puede cambiar algunos tipos de datos automáticamente
**Solución:** Ejecutar manualmente el script SQL en `init/update-schema-validations.sql`

### Problema 2: Error "column too short"
**Causa:** Datos existentes exceden la nueva longitud
**Solución:** 
1. Identificar registros problemáticos
2. Truncar o migrar esos datos
3. Aplicar el cambio

### Problema 3: Contraseñas truncadas
**Causa:** La columna `password` era VARCHAR(255) y se cambió a VARCHAR(60)
**Solución:** 
- BCrypt siempre genera exactamente 60 caracteres
- Los hashes existentes se mantendrán correctos
- Las nuevas contraseñas funcionarán correctamente

## Estrategia Recomendada por Entorno

| Entorno | Configuración | Razón |
|---------|---------------|-------|
| **Desarrollo** | `ddl-auto=update` | Iteración rápida |
| **Testing** | `ddl-auto=create-drop` | BD limpia en cada test |
| **Staging** | `ddl-auto=validate` + Flyway | Simula producción |
| **Producción** | `ddl-auto=validate` + Flyway | Control total de cambios |

## Comandos Útiles

### Reiniciar BD desde cero (DESARROLLO)
```bash
# Conectar a PostgreSQL
psql -U clipper

# Eliminar y recrear la base de datos
DROP DATABASE IF EXISTS clipperdb;
CREATE DATABASE clipperdb;

# Ejecutar script inicial
\c clipperdb
\i init/clipper-barberShop.sql
```

### Ver estado actual de la BD
```bash
psql -U clipper -d clipperdb -c "\d+ usuarios"
psql -U clipper -d clipperdb -c "\d+ empresas"
```

### Aplicar cambios manualmente
```bash
psql -U clipper -d clipperdb -f init/update-schema-validations.sql
```

## Próximos Pasos Recomendados

1. ✅ **Ahora:** Usar `ddl-auto=update` en desarrollo
2. 📝 **Corto plazo:** Documentar cada cambio de esquema
3. 🚀 **Mediano plazo:** Implementar Flyway antes de producción
4. 🔒 **Largo plazo:** Cambiar a `validate` en producción

## Notas Importantes sobre BCrypt

- BCrypt **siempre** genera hashes de **exactamente 60 caracteres**
- El formato es: `$2a$10$[22 chars salt][31 chars hash]`
- VARCHAR(60) es **suficiente y óptimo**
- NO uses VARCHAR(255) para passwords hasheados (desperdicio de espacio)
- Ejemplo de hash BCrypt:
  ```
  $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
  ^   ^  ^                                                    ^
  |   |  |                                                    |
  |   |  Salt (22 chars)                                      |
  |   Cost factor                                             Hash (31 chars)
  Algorithm
  ```
