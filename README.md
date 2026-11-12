# Bitácora API

API REST en C# con ASP.NET Core para administrar los registros de una bitácora de campo.
Organizada por capas según los principios de arquitectura limpia.
**Protegida con autenticación JWT y autorización por roles (Admin / Usuario).**

## Estructura de la solución

```
Bitacora.slnx
├── Bitacora.Domain            # Entidades Registro y Usuario, roles. Sin dependencias.
├── Bitacora.Application       # DTOs, interfaces, servicios (RegistroService, AuthService).
├── Bitacora.Infrastructure    # EF Core (SQLite), repositorios, hash BCrypt, seed del Admin.
└── Bitacora.Api               # Controladores, JWT (emisión y validación), DI y middleware.
```

Dependencias (apuntan hacia el dominio):

```
Bitacora.Api --> Bitacora.Application
Bitacora.Api --> Bitacora.Infrastructure
Bitacora.Infrastructure --> Bitacora.Application
Bitacora.Infrastructure --> Bitacora.Domain
Bitacora.Application --> Bitacora.Domain
Bitacora.Domain --> (ninguno)
```

## Requisitos

- .NET SDK 10.0 o superior.
- (Opcional) dotnet-ef tool para migraciones: `dotnet tool install --global dotnet-ef`.

## Configuración de secretos (obligatoria antes de ejecutar)

La clave de firma del JWT y las credenciales del Admin inicial **no están en el repositorio**.
Cada integrante del equipo las configura en su propia máquina con `user-secrets`
(se guardan fuera del proyecto, en el perfil del usuario del sistema operativo):

```bash
cd Bitacora.Api
dotnet user-secrets set "Jwt:Key" "<cadena aleatoria de 32 o más caracteres>"
dotnet user-secrets set "Seed:AdminCorreo" "admin@bitacora.test"
dotnet user-secrets set "Seed:AdminPassword" "<contraseña del admin, mín. 8 caracteres>"
```

Para generar una clave aleatoria: `openssl rand -base64 48` (Linux/macOS/Git Bash) o, en PowerShell,
`[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Maximum 256 }))`.

En un servidor se usan variables de entorno equivalentes: `Jwt__Key`, `Seed__AdminCorreo`, `Seed__AdminPassword`.
Si falta `Jwt:Key` la API no arranca y muestra un mensaje explicando qué configurar.
El usuario Admin se crea automáticamente la primera vez que arranca la API con esos valores.

Emisor, audiencia y duración del token están en `appsettings.json` (sección `Jwt`, `ExpirationMinutes` = 10).
Para probar el vencimiento rápido, ponga `ExpirationMinutes` en 1 con
`dotnet user-secrets set "Jwt:ExpirationMinutes" "1"`.

## Ejecución

```bash
cd Bitacora.Api
dotnet dev-certs https --trust     # una sola vez: certificado HTTPS de desarrollo
dotnet run --launch-profile https
```

- **HTTPS (Swagger y Postman):** `https://localhost:7286` — Swagger UI en `https://localhost:7286/swagger`.
- **Aplicación Android en red local:** use el perfil `http` (`dotnet run --launch-profile http`), que escucha en
  `http://0.0.0.0:5080`. La API tiene `UseHttpsRedirection` activo, pero con ese perfil no hay puerto HTTPS
  configurado, así que no redirige (el teléfono no confiaría en el certificado de desarrollo).
  En producción siempre se publica solo por HTTPS.

### Probar desde Swagger

1. `POST /api/auth/login` con el correo y la contraseña del Admin (o de un usuario registrado).
2. Copie el valor de `token` de la respuesta.
3. Botón **Authorize** (arriba a la derecha) → pegue solo el token → **Authorize**.

## Migraciones

La base de datos SQLite (`bitacora.db`) se crea automáticamente al iniciar la API (se aplican las migraciones pendientes).

Para aplicar las migraciones manualmente:

```bash
dotnet ef database update \
  --project Bitacora.Infrastructure/Bitacora.Infrastructure.csproj \
  --startup-project Bitacora.Api/Bitacora.Api.csproj
```

Para crear una nueva migración:

```bash
dotnet ef migrations add NombreMigracion \
  --project Bitacora.Infrastructure/Bitacora.Infrastructure.csproj \
  --startup-project Bitacora.Api/Bitacora.Api.csproj \
  --output-dir Data/Migrations
```

## Endpoints

| Método | Ruta | Acceso | Acción | Respuestas |
|--------|------|--------|--------|------------|
| POST | `/api/auth/registro` | Anónimo | Registrar usuario (siempre rol `Usuario`) | 201, 400, 409 |
| POST | `/api/auth/login` | Anónimo | Iniciar sesión → token, `expiraEn`, rol | 200, 400, 401 |
| GET | `/api/registros` | Autenticado | Listar registros (sin foto) | 200, 401 |
| GET | `/api/registros/{id}` | Autenticado | Consultar un registro con foto | 200, 401, 404 |
| POST | `/api/registros` | Autenticado | Crear un registro (guarda al creador) | 201, 400, 401 |
| PUT | `/api/registros/{id}` | Autenticado | Actualizar: solo el dueño o un Admin | 200, 400, 401, 403, 404 |
| DELETE | `/api/registros/{id}` | **Solo Admin** | Eliminar un registro | 204, 401, 403, 404 |

### Seguridad

- **Contraseñas:** se guardan con hash BCrypt (con sal incluida); nunca en texto plano ni se devuelven en ninguna respuesta.
- **Token JWT (HS256):** claims `sub` (id), `email`, `role` y `jti`. Se validan emisor, audiencia, firma, algoritmo y
  tiempo de vida (`ClockSkew` = 0, vence exactamente a los 10 minutos).
- **401 vs 403:** `401` = no autenticado (sin token, alterado, vencido o credenciales incorrectas);
  `403` = autenticado pero sin permiso (DELETE con rol `Usuario`, o modificar un registro ajeno).
- **Login:** el mensaje de error es siempre el mismo (“Credenciales inválidas.”), sin indicar si falló el correo
  o la contraseña, y se gasta el mismo tiempo de BCrypt cuando el correo no existe.
- **Roles:** el registro público nunca crea Admins; el Admin inicial sale de la configuración (secretos).
- **Registros anteriores:** los creados antes de la autenticación no tienen dueño (`usuarioId` nulo); solo un Admin puede modificarlos.

## Validación

- `Titulo`: obligatorio, de 3 a 100 caracteres.
- `Descripcion`: obligatoria, máximo 500 caracteres.
- `FotoBase64`: opcional; si se envía debe ser Base64 válido y no superar 2 MB.
- `FechaCreacion`: la asigna el servidor.

Los errores de validación responden `400` con `ValidationProblemDetails`.
Las excepciones no controladas responden `500` con `ProblemDetails` sin exponer detalle interno.

## Pruebas

Colección de Postman: `docs/Bitacora.postman_collection.json` (carpetas 0, 1 y 2, ejecutar en orden).
Entorno: `docs/Bitacora.postman_environment.json` (`baseUrl`, `adminCorreo`, `adminPassword`).

1. Importe colección y entorno, seleccione el entorno **Bitacora Local** y escriba en él `adminCorreo` y `adminPassword`
   (los mismos que configuró en user-secrets; el archivo del repositorio los deja vacíos a propósito).
   Si Postman rechaza el certificado de desarrollo, ejecute `dotnet dev-certs https --trust` o desactive
   *SSL certificate verification* en la configuración de Postman para localhost.
2. Ejecute la carpeta **0. Preparación** (registra usuarios A y B y obtiene los tokens).
3. Ejecute la carpeta **1. Pruebas obligatorias**:

| Prueba | Resultado esperado |
|--------|--------------------|
| 1.1 Sin token | 401 |
| 1.2 Token de Usuario hace DELETE | 403 |
| 1.7 Token de Admin hace DELETE | 204 |
| 1.3 Usuario B modifica registro de A | 403 |
| 1.4 Token alterado | 401 |
| 1.5 Token vencido | 401 |
| 1.6 Credenciales incorrectas (contraseña o correo) | 401, mismo mensaje |

   **Token vencido (1.5):** la petición 0.5 guarda el primer token emitido en `tokenVencido`. Para que la prueba pase,
   ejecute 1.5 cuando hayan pasado más minutos que `ExpirationMinutes` desde ese login (con 1 minuto basta esperar 1 minuto).
   Si quiere repetirla, vacíe la variable `tokenVencido` de la colección y vuelva a ejecutar 0.5.
4. La carpeta **2. CRUD** repite las pruebas de la tarea 2.2, ahora con token (DELETE con el de Admin).
