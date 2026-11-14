# Tarea individual: comparativa Volley / OkHttp / Retrofit

## 1. Qué trae este paquete

| Archivo | Cambio |
|---|---|
| `Android/.../ComparativaActivity.java` y `res/layout/activity_comparativa.xml` | **Nuevos.** Pantalla con 3 botones, selector de escenario, tiempos y promedios. |
| `Bitacora.Api/Controllers/PruebaController.cs` | **Nuevo.** Endpoints de prueba (solo en Development): `lento`, `error500`, `no-autorizado`. |
| `Android/app/build.gradle` | + dependencia `com.android.volley:volley:1.2.1` |
| `AndroidManifest.xml` | + `ComparativaActivity` |
| `res/menu/menu_main.xml` y `MainActivity.java` | + opción de menú ⋮ **Comparativa de librerías** |
| `data/remote/ApiClient.java` | `readTimeout` de 60 s a **15 s** (para que el escenario de 20 s agote el tiempo) |
| `data/remote/RegistroApi.java` y `data/repository/RegistroRepository.java` | + `getFrom(@Url)` y `getAllFrom(path, cb)` para que Retrofit consulte las rutas de prueba reutilizando el repositorio |

## 2. Flujo de Git (en el clon que tiene acceso al repositorio del equipo)

```powershell
cd C:\Users\Johan\Desktop\grupo1_clone
git switch main
git pull origin main
git switch -c feature/comparativa-johan-lopez
```

Copia el contenido de este paquete encima del clon (misma estructura de carpetas) y haz **commits separados**:

```powershell
git add Bitacora.Api
git commit -m "Agrega endpoints de prueba para la comparativa (solo Development)"

git add Android/app/build.gradle Android/app/src/main/java/com/example/bitacora/data
git commit -m "Agrega Volley, timeout de 15 s y consulta por ruta en Retrofit"

git add Android
git commit -m "Agrega ComparativaActivity con Volley, OkHttp y Retrofit"

git add docs
git commit -m "Agrega guia y cuadros de la comparativa"

git push -u origin feature/comparativa-johan-lopez
```

Luego, en GitHub: **Compare & pull request** hacia `main` y pide revisión a otro integrante.

> Si los cambios de JWT todavía no están en `main` del repositorio del equipo, tu pull request los incluirá también. Lo ideal es que primero se integren y después partir de `main` actualizado.

## 3. Cómo probar

1. API con perfil **http**: `dotnet run --launch-profile http` (los endpoints de prueba solo existen en Development).
2. Ejecuta la app, inicia sesión y abre **⋮ → Comparativa de librerías**.
3. Para cada escenario, elige el escenario en el selector y pulsa los tres botones.
4. Para las capturas de Logcat: filtro `tag:COMPARATIVA`.

### Qué esperar en cada escenario (verifícalo y anota lo que realmente veas)

| Escenario | Cómo provocarlo | Volley | OkHttp | Retrofit |
|---|---|---|---|---|
| API detenida | Detén `dotnet run` | `NoConnectionError` (o `TimeoutError`) | `ConnectException` | mensaje "Sin conexión o tiempo de espera agotado" |
| Modo avión | Actívalo en el teléfono | `NoConnectionError` | `UnknownHostException` / `ConnectException` | igual que arriba |
| Tiempo agotado | Selector: *Tiempo agotado* | `TimeoutError` tras ~30 s (15 s × 2 intentos) | `SocketTimeoutException` tras ~15 s | mensaje de tiempo agotado tras ~15 s |
| 401 | Selector: *401 forzado*, o desmarca *Enviar token*, o espera a que venza | `AuthFailureError` (reintenta una vez) | `HTTP 401` en `onResponse` | el `AuthInterceptor` borra la sesión y **vuelve al login** |
| 500 | Selector: *Error 500* | `ServerError (HTTP 500)` | `HTTP 500` en `onResponse` (no es excepción) | "Error del servidor: 500" |

En ningún caso la app debe cerrarse. Si alguno la cierra, es un bug que hay que corregir antes de entregar.

**Tiempo promedio:** con el escenario *Normal* pulsa cada botón 5 veces; la pantalla calcula el promedio de cada librería. Usa **Reiniciar** antes de empezar. La primera ejecución suele ser más lenta (conexión nueva), puedes comentarlo en el análisis.

## 4. Cuadro comparativo (borrador: valida cada celda con tu código y tus mediciones)

| Criterio | Volley | OkHttp | Retrofit |
|---|---|---|---|
| Líneas de código | *cuenta las de `pruebaVolley` + `describirVolley`* | *cuenta las de `pruebaOkHttp` + `describirOkHttp` + `describirHttp`* | *cuenta las de `pruebaRetrofit` (+ interfaz y repositorio ya existentes)* |
| Envío de encabezados | Sobrescribir `getHeaders()` en cada request | `.header(...)` en cada `Request` (o un interceptor) | Una sola vez en el `AuthInterceptor` del cliente |
| Conversión de JSON | `JsonArrayRequest` entrega `JSONArray`; el mapeo a objetos es manual | Manual con Gson sobre `body.string()` | Automática con `GsonConverterFactory` |
| Hilo de la respuesta | Principal | **Secundario** (`runOnUiThread`) | Principal (en Android) |
| Manejo de errores | Jerarquía de `VolleyError` (`TimeoutError`, `NoConnectionError`, `ServerError`…) | `IOException` en `onFailure`; los códigos HTTP se revisan a mano en `onResponse` | `isSuccessful()` + `onFailure(Throwable)` |
| Reintentos y tiempos de espera | `DefaultRetryPolicy` integrada (timeout, reintentos, backoff) | Timeouts del cliente; solo reintenta fallos de conexión | Los del cliente OkHttp que usa por debajo |
| Facilidad de prueba y mantenimiento | *completa según lo que viviste* | *completa* | *completa* |
| Tiempo promedio (5 ejecuciones) | ___ ms | ___ ms | ___ ms |

**Recomendación:** escribe cuál usarían en el proyecto final y por qué, apoyándote en tus mediciones y en lo que observaste (por ejemplo: el token y el manejo del 401 centralizados en un interceptor, conversión automática del JSON, código más corto). Una recomendación sin evidencia propia pesa menos que una con datos.

## 5. Tabla de escenarios de falla para el PDF

| Escenario | Librería | Error que recibe el código | Mensaje que ve el usuario | Captura |
|---|---|---|---|---|
| API detenida | Volley | | | |
| | OkHttp | | | |
| | Retrofit | | | |
| *(repite para modo avión, tiempo agotado, 401 y 500)* | | | | |

## 6. Guion del video (3 a 4 min, cámara encendida)

1. (20 s) Presentación y objetivo de la tarea.
2. (60 s) Explica **con tus palabras** el código de Volley: `JsonArrayRequest`, `getHeaders()` con el token, `DefaultRetryPolicy`.
3. (60 s) Explica el de OkHttp: `Request` manual, `enqueue()`, Gson y por qué se usa `runOnUiThread`.
4. (60 s) Muestra al menos **dos escenarios de falla** (por ejemplo, API detenida y error 500) con los tres botones.
5. (20 s) Resultado del promedio y recomendación.

## 7. Antes de entregar

- Los endpoints de `PruebaController` son temporales: solo responden en Development, pero puedes borrarlos al terminar.
- Vuelve a dejar `readTimeout` en 60 s si el equipo lo necesita para subir fotos grandes.
