# 📱 Verificación de Email para Apps Móviles

## 🎯 Resumen

Sistema de verificación de email adaptado para **aplicaciones móviles** usando **código de 6 dígitos** en lugar de enlaces clickeables.

## 🆚 Diferencias: Web vs Móvil

| Aspecto | Web | App Móvil |
|---------|-----|-----------|
| **Método** | Enlace clickeable | Código de 6 dígitos |
| **Token** | UUID (largo) | 6 dígitos numéricos |
| **Expiración** | 24 horas | 15 minutos |
| **Verificación** | Automática (un clic) | Manual (ingresar código) |
| **Intentos** | Ilimitados | 3 máximo |
| **Email** | Botón "Verificar" | Código destacado |
| **Endpoint** | GET `/api/registro/verify?token=xxx` | POST `/api/registro/verify-code` |

## 🔧 Configuración

### 1. Habilitar Modo Móvil

En `application.properties`:

```properties
# true = App Móvil (código 6 dígitos)
# false = Web (enlace clickeable)
app.verification.use-code=true
app.verification.max-attempts=3
app.verification.expiry-minutes=15
```

### 2. Configurar Email

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=xxxx-xxxx-xxxx-xxxx
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 📱 Flujo para App Móvil

### 1️⃣ Usuario se Registra

**POST** `/api/registro/cliente`

```json
{
  "name": "Juan",
  "lastName": "Pérez",
  "email": "juan@example.com",
  "password": "Pass123"
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Cliente registrado. Por favor, verifica tu email para activar tu cuenta.",
  "data": null
}
```

### 2️⃣ Usuario Recibe Email con Código

El usuario recibe un email con un **código de 6 dígitos**:

```
┌────────────────────────────────┐
│   ✂️ Clipper Barber Shop       │
├────────────────────────────────┤
│                                │
│  ¡Hola Juan!                   │
│                                │
│  Ingresa este código:          │
│                                │
│   ┌──────────┐                │
│   │ 123456   │ ← CÓDIGO       │
│   └──────────┘                │
│                                │
│  Expira en 15 minutos          │
│                                │
└────────────────────────────────┘
```

### 3️⃣ Usuario Ingresa Código en la App

La app muestra una pantalla para ingresar el código:

```
┌─────────────────────────┐
│   Verificar Email       │
├─────────────────────────┤
│                         │
│  Ingresa el código que  │
│  enviamos a tu email:   │
│                         │
│  ┌───┬───┬───┬───┬───┬───┐
│  │ 1 │ 2 │ 3 │ 4 │ 5 │ 6 │
│  └───┴───┴───┴───┴───┴───┘
│                         │
│  [Verificar]            │
│                         │
│  ¿No recibiste el email?│
│  [Reenviar código]      │
│                         │
└─────────────────────────┘
```

### 4️⃣ App Envía el Código al Backend

**POST** `/api/registro/verify-code`

```bash
POST http://localhost:8080/api/registro/verify-code?email=juan@example.com&code=123456
```

**Respuesta Exitosa:**
```json
{
  "status": "success",
  "mensaje": "Email verificado exitosamente",
  "data": null
}
```

**Respuesta con Error:**
```json
{
  "status": "error",
  "mensaje": "Código de verificación inválido o expirado",
  "data": null
}
```

### 5️⃣ Usuario Puede Hacer Login

**POST** `/api/auth/login`

```json
{
  "email": "juan@example.com",
  "password": "Pass123"
}
```

## 🔌 Integración con Apps Móviles

### Flutter / Dart

```dart
// 1. Registrar usuario
Future<void> register() async {
  final response = await http.post(
    Uri.parse('http://localhost:8080/api/registro/cliente'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({
      'name': 'Juan',
      'lastName': 'Pérez',
      'email': 'juan@example.com',
      'password': 'Pass123',
    }),
  );
  
  if (response.statusCode == 200) {
    // Navegar a pantalla de verificación
    Navigator.push(context, VerificationScreen());
  }
}

// 2. Verificar código
Future<void> verifyCode(String email, String code) async {
  final response = await http.post(
    Uri.parse('http://localhost:8080/api/registro/verify-code?email=$email&code=$code'),
  );
  
  if (response.statusCode == 200) {
    final data = jsonDecode(response.body);
    if (data['status'] == 'success') {
      // Email verificado, ir al login
      Navigator.push(context, LoginScreen());
    }
  }
}

// 3. Reenviar código
Future<void> resendCode(String email) async {
  await http.post(
    Uri.parse('http://localhost:8080/api/registro/resend-verification'),
    headers: {'Content-Type': 'application/json'},
    body: jsonEncode({'email': email}),
  );
}
```

### React Native / JavaScript

```javascript
// 1. Registrar usuario
const register = async () => {
  const response = await fetch('http://localhost:8080/api/registro/cliente', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      name: 'Juan',
      lastName: 'Pérez',
      email: 'juan@example.com',
      password: 'Pass123',
    }),
  });
  
  if (response.ok) {
    navigation.navigate('Verification');
  }
};

// 2. Verificar código
const verifyCode = async (email, code) => {
  const response = await fetch(
    `http://localhost:8080/api/registro/verify-code?email=${email}&code=${code}`,
    { method: 'POST' }
  );
  
  const data = await response.json();
  if (data.status === 'success') {
    navigation.navigate('Login');
  } else {
    Alert.alert('Error', data.mensaje);
  }
};

// 3. Reenviar código
const resendCode = async (email) => {
  await fetch('http://localhost:8080/api/registro/resend-verification', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  });
};
```

### Kotlin (Android)

```kotlin
// 1. Registrar usuario
fun register() {
    val client = OkHttpClient()
    val json = JSONObject()
    json.put("name", "Juan")
    json.put("lastName", "Pérez")
    json.put("email", "juan@example.com")
    json.put("password", "Pass123")
    
    val body = json.toString()
        .toRequestBody("application/json".toMediaType())
    
    val request = Request.Builder()
        .url("http://localhost:8080/api/registro/cliente")
        .post(body)
        .build()
    
    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                // Navegar a pantalla de verificación
            }
        }
        override fun onFailure(call: Call, e: IOException) {
            // Manejar error
        }
    })
}

// 2. Verificar código
fun verifyCode(email: String, code: String) {
    val request = Request.Builder()
        .url("http://localhost:8080/api/registro/verify-code?email=$email&code=$code")
        .post(RequestBody.create(null, ""))
        .build()
    
    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val json = JSONObject(response.body?.string())
            if (json.getString("status") == "success") {
                // Email verificado
            }
        }
    })
}
```

### Swift (iOS)

```swift
// 1. Registrar usuario
func register() {
    guard let url = URL(string: "http://localhost:8080/api/registro/cliente") else { return }
    
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    request.setValue("application/json", forHTTPHeaderField: "Content-Type")
    
    let body: [String: Any] = [
        "name": "Juan",
        "lastName": "Pérez",
        "email": "juan@example.com",
        "password": "Pass123"
    ]
    request.httpBody = try? JSONSerialization.data(withJSONObject: body)
    
    URLSession.shared.dataTask(with: request) { data, response, error in
        if let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 {
            // Navegar a pantalla de verificación
        }
    }.resume()
}

// 2. Verificar código
func verifyCode(email: String, code: String) {
    guard let url = URL(string: "http://localhost:8080/api/registro/verify-code?email=\(email)&code=\(code)") else { return }
    
    var request = URLRequest(url: url)
    request.httpMethod = "POST"
    
    URLSession.shared.dataTask(with: request) { data, response, error in
        if let data = data,
           let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
           json["status"] as? String == "success" {
            // Email verificado
        }
    }.resume()
}
```

## 🎨 UI/UX Recomendaciones

### Pantalla de Verificación

```
┌─────────────────────────────────┐
│ ←  Verificar Email              │
├─────────────────────────────────┤
│                                 │
│  Enviamos un código de 6        │
│  dígitos a:                     │
│                                 │
│  juan@example.com               │
│                                 │
│  ┌───┬───┬───┬───┬───┬───┐     │
│  │ _ │ _ │ _ │ _ │ _ │ _ │     │
│  └───┴───┴───┴───┴───┴───┘     │
│                                 │
│  ⏱️ Expira en 14:32             │
│                                 │
│  [Verificar]                    │
│                                 │
│  ¿No recibiste el email?        │
│  [Reenviar código] (espera 60s) │
│                                 │
└─────────────────────────────────┘
```

### Features a Implementar

1. **Input de 6 dígitos separados** - Mejor UX
2. **Auto-focus** - Pasar automáticamente al siguiente campo
3. **Contador de tiempo** - Mostrar cuánto tiempo queda
4. **Límite de reenvío** - Evitar spam (1 minuto entre reenvíos)
5. **Validación en tiempo real** - Verificar mientras escribe
6. **Feedback visual** - Mostrar errores claramente
7. **Teclado numérico** - Forzar teclado de números
8. **Auto-verificar** - Al completar 6 dígitos, verificar automáticamente

## 🔒 Seguridad

### Protecciones Implementadas

✅ **Máximo 3 intentos** - Previene fuerza bruta  
✅ **Expiración 15 minutos** - Ventana corta de tiempo  
✅ **Código aleatorio** - Generado con SecureRandom  
✅ **Un solo uso** - El código se elimina después de usarse  
✅ **Rate limiting** - Reenvío limitado (implementar en app)  

### Recomendaciones Adicionales

```kotlin
// Rate limiting en la app (ejemplo Android)
private var lastResendTime = 0L
private val RESEND_COOLDOWN = 60_000L // 60 segundos

fun resendCode() {
    val now = System.currentTimeMillis()
    if (now - lastResendTime < RESEND_COOLDOWN) {
        val remaining = (RESEND_COOLDOWN - (now - lastResendTime)) / 1000
        showError("Espera $remaining segundos para reenviar")
        return
    }
    
    lastResendTime = now
    // Llamar al API
}
```

## 📊 Estados y Errores

| Estado | Mensaje | Acción |
|--------|---------|--------|
| ✅ Código correcto | "Email verificado exitosamente" | Ir a login |
| ❌ Código incorrecto | "Código inválido. Te quedan 2 intentos" | Permitir reintento |
| ⏱️ Código expirado | "El código expiró. Solicita uno nuevo" | Botón reenviar |
| 🚫 Máx. intentos | "Excediste los intentos. Solicita un nuevo código" | Botón reenviar |
| 📧 Email no existe | "Usuario no encontrado" | Volver a registro |
| ✅ Ya verificado | "Tu email ya está verificado" | Ir a login |

## 🧪 Testing

### Test con cURL

```bash
# 1. Registrar
curl -X POST http://localhost:8080/api/registro/cliente \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "Test123"
  }'

# 2. Revisar email y obtener código (ej: 123456)

# 3. Verificar código
curl -X POST "http://localhost:8080/api/registro/verify-code?email=test@example.com&code=123456"

# 4. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123"
  }'
```

## 🔄 Cambiar entre Web y Móvil

Para cambiar el modo:

```properties
# App Móvil (código 6 dígitos)
app.verification.use-code=true

# Web (enlace clickeable)
app.verification.use-code=false
```

No requiere cambios en el código. El sistema se adapta automáticamente.

## 📝 Endpoints API

| Método | Endpoint | Uso | Cliente |
|--------|----------|-----|---------|
| POST | `/api/registro/cliente` | Registrar usuario | Móvil + Web |
| POST | `/api/registro/verify-code` | Verificar con código | **Móvil** |
| GET | `/api/registro/verify` | Verificar con enlace | **Web** |
| POST | `/api/registro/resend-verification` | Reenviar código/enlace | Móvil + Web |
| POST | `/api/auth/login` | Iniciar sesión | Móvil + Web |

## 💡 Próximos Pasos

1. ✅ **Backend** - Completado
2. 📱 **App Móvil** - Implementar UI de verificación
3. 🎨 **UI/UX** - Diseñar pantallas
4. 🧪 **Testing** - Probar flujo completo
5. 🚀 **Deploy** - Configurar producción

---

**¡Sistema listo para apps móviles! 📱**
