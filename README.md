# Bitácora API

API REST en C# con ASP.NET Core para administrar los registros de una bitácora de campo.
Organizada por capas según los principios de arquitectura limpia.

## Estructura de la solución

```
Bitacora.slnx
├── Bitacora.Domain            # Entidad Registro y reglas de negocio. Sin dependencias.
├── Bitacora.Application       # DTOs, interfaz IRegistroRepository, servicios/casos de uso.
├── Bitacora.Infrastructure    # EF Core (SQLite) e implementación del repositorio.
└── Bitacora.Api               # Controladores, configuración, DI y middleware.
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

## Ejecución

```bash
cd Bitacora.Api
dotnet run
```

La API queda disponible en `http://localhost:5080` (o el puerto indicado en `Properties/launchSettings.json`).
Documentación Swagger UI: `http://localhost:5080/swagger`.

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

| Método | Ruta | Acción | Respuestas |
|--------|------|--------|------------|
| GET | `/api/registros` | Listar registros (sin foto) | 200 |
| GET | `/api/registros/{id}` | Consultar un registro con foto | 200, 404 |
| POST | `/api/registros` | Crear un registro | 201, 400 |
| PUT | `/api/registros/{id}` | Actualizar un registro | 200, 400, 404 |
| DELETE | `/api/registros/{id}` | Eliminar un registro | 204, 404 |

## Validación

- `Titulo`: obligatorio, de 3 a 100 caracteres.
- `Descripcion`: obligatoria, máximo 500 caracteres.
- `FotoBase64`: opcional; si se envía debe ser Base64 válido y no superar 2 MB.
- `FechaCreacion`: la asigna el servidor.

Los errores de validación responden `400` con `ValidationProblemDetails`.
Las excepciones no controladas responden `500` con `ProblemDetails` sin exponer detalle interno.

## Pruebas

Colección de Postman: `docs/Bitacora.postman_collection.json`.
Entorno: `docs/Bitacora.postman_environment.json` (variable `baseUrl`).
