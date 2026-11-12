# Bitácora Android (Tareas 2.3 y 2.4: sesión con JWT)

Cliente Android (Java) que consume la API de la Bitácora de campo y maneja la sesión con JWT.

## Requisitos
- Android Studio (Giraffe o superior), SDK Android 36.
- La API corriendo con el perfil `http` (`dotnet run --launch-profile http`, escucha en el puerto 5080).
  Antes debe tener configurados sus secretos (ver README de la raíz).
- Un usuario: se puede crear desde la pantalla **Regístrate** de la app, o usar el Admin configurado en la API.

## Configuración
- `ApiClient.BASE_URL`: `http://10.0.2.2:5080/` (emulador) o la IP de la PC (dispositivo físico).
- **Si cambia la IP**, actualícela también en `res/xml/network_security_config.xml`.
  Por defecto todo el tráfico en texto plano está bloqueado; solo se permite HTTP hacia el emulador y la IP de pruebas
  (solo desarrollo; en producción se usa HTTPS).

## Sesión y seguridad
- **Pantallas:** inicio de sesión y registro, con validación en el cliente (correo válido, contraseña de 8 a 72 caracteres
  con letra y número, confirmación igual). La app arranca en el login; si hay una sesión vigente pasa directo a la lista.
- **Token:** se guarda con `EncryptedSharedPreferences` (clave AES-256 en el Android Keystore) en `data/local/SessionManager`.
  Con `SharedPreferences` comunes el token quedaría en texto plano en un XML dentro del almacenamiento de la app; en un
  teléfono con root, o con una copia de seguridad, cualquiera podría leerlo y usarlo para hacerse pasar por el usuario.
- **Interceptor OkHttp** (`AuthInterceptor`): agrega `Authorization: Bearer <token>` a cada petición (menos login y registro).
- **401:** el interceptor borra la sesión y todas las pantallas protegidas (`BaseAuthActivity`) regresan al login con un mensaje.
  Además, al abrir una pantalla se comprueba la fecha de vencimiento guardada.
- **403:** solo se muestra “No tienes permiso para realizar esta acción”; la sesión **no** se cierra.
- **Cerrar sesión:** menú (⋮) de la pantalla principal; borra el token guardado.
- **Rol:** el botón *Eliminar* solo se muestra a los Admin y *Editar* solo al dueño del registro o a un Admin.
  Es solo comodidad de la interfaz: la API valida siempre.
- **Registros:** el log de red está en nivel `BASIC` (no registra cuerpos ni encabezados, así que contraseñas y token no
  aparecen en Logcat) y se oculta el encabezado `Authorization`. `allowBackup` está desactivado.

## Estructura
- `ui`, `viewmodel`: pantallas y estados. `LoginActivity`, `RegisterActivity`, `BaseAuthActivity`.
- `data/local`: `SessionManager` (token cifrado).
- `data/remote`: interfaces Retrofit (`RegistroApi`, `AuthApi`), `ApiClient`, `AuthInterceptor`.
- `data/repository`: repositorios (únicas clases que llaman a la API).
