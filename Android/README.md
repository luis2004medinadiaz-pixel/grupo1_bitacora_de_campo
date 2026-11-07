# Bitácora Android (Tarea 2.3)

Cliente Android (Java) que consume la API de la Bitácora de campo (Tarea 2.2).

## Requisitos
- Android Studio (Giraffe o superior).
- SDK Android 36.
- La API corriendo en la máquina (http://localhost:5080). En el emulador se accede con 10.0.2.2.

## Cómo abrir
1. Abre Android Studio → Open → selecciona la carpeta `Android`.
2. Espera a que Gradle sincronice.
3. Ejecuta en un emulador o dispositivo.

## Configuración
- URL base: `http://10.0.2.2:5080/` (emulador). Para dispositivo físico, cambia a la IP de tu máquina en `ApiClient.BASE_URL`.
- HTTP sin cifrar permitido solo para desarrollo (ver `network_security_config.xml`).

## Estructura
- `ui`: Activities y adaptador RecyclerView.
- `viewmodel`: ViewModel con LiveData y estados.
- `data/remote`: interfaz Retrofit y cliente OkHttp.
- `data/repository`: repositorio (única clase que llama a la API).

## Funciones
- Listado con RecyclerView y pull-to-refresh (botón actualizar).
- Detalle con foto decodificada desde Base64.
- Formulario de creación/edición: foto con cámara (reducida a 1024px, JPEG 70%) y descripción por voz.
- Eliminación con diálogo de confirmación.
- Estados: cargando, lista vacía, error, sin conexión (con Reintentar).