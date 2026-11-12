# Guía para completar la entrega

Lo que ya está en el repositorio: código de la API y de la app Android, colección de Postman con las pruebas, y la
sección de OAuth 2.0 (`docs/Seccion-OAuth2.md`). Lo que falta producir **en su máquina** (requiere ejecutar el proyecto):

## 1. Antes de ejecutar
1. Configurar `user-secrets` (README de la raíz): `Jwt:Key`, `Seed:AdminCorreo`, `Seed:AdminPassword`.
2. `dotnet build` y `dotnet run --launch-profile https` (la migración se aplica sola).
3. Verificar que `git status` no muestre ningún secreto. **Nunca** haga commit de claves ni contraseñas; si ya subieron
   alguna al historial, hay que cambiarla (la clave del historial ya no es secreta) y limpiar el historial.

## 2. Contenido del PDF (capturas que hay que tomar)
| Sección | Qué incluir |
|---|---|
| Configuración JWT | Fragmento de `Program.cs` (AddAuthentication/AddJwtBearer) y de `appsettings.json` (sin la clave). Captura de `dotnet user-secrets list` **con la clave oculta**. |
| Token decodificado | Login en Swagger/Postman → pegar el token en jwt.io → captura de header y payload (`sub`, `email`, `role`, `iss`, `aud`, `exp`). **No** pegue tokens de cuentas reales de producción. |
| Hash de contraseña | Captura de la tabla `Usuarios` (por ejemplo con DB Browser for SQLite) mostrando `PasswordHash` con formato `$2a$11$...`. |
| Pruebas Postman | Captura del resultado de las 6 pruebas obligatorias (carpeta 1) pasando. |
| Swagger | Captura del botón *Authorize* con el esquema Bearer. |
| Android | Capturas de: login, registro, lista con sesión iniciada, botón Eliminar oculto con un Usuario y visible con Admin, mensaje al vencer la sesión (401), mensaje de “sin permiso” (403). |
| OAuth 2.0 | Copiar `docs/Seccion-OAuth2.md` (el diagrama puede exportarse desde mermaid.live). |

## 3. Guion sugerido para el video (3 a 5 min)
1. Registrar un usuario en la app y entrar (30 s).
2. Mostrar que sin token la API responde 401 (Postman 1.1) (30 s).
3. Usuario normal: crea un registro, no ve *Eliminar*; en Postman, DELETE con su token → 403 (45 s).
4. Otro usuario intenta editar el registro ajeno → 403, la app avisa y **no** cierra sesión (45 s).
5. Admin: ve *Eliminar* y borra → 204 (30 s).
6. Esperar el vencimiento (ExpirationMinutes=1) → la app regresa al login con mensaje (45 s).
7. Mostrar el repositorio sin secretos y el token en jwt.io (30 s).

## 4. Pruebas manuales recomendadas en el teléfono/emulador
- Cerrar sesión y reabrir la app: debe pedir login.
- Cerrar la app con sesión vigente y reabrir: entra directo.
- Apagar la API: la app muestra “Sin conexión” y **no** cierra sesión.
