using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;

QuestPDF.Settings.License = LicenseType.Community;

var imgPath = @"D:\Documentos\Proyecto predeterminado\Bitacora\docs\images\swagger.png";
var outPath = @"D:\Documentos\Proyecto predeterminado\Bitacora\docs\INFORME.pdf";

Document.Create(container =>
{
    container.Page(page =>
    {
        page.Size(PageSizes.A4);
        page.Margin(2, Unit.Centimetre);
        page.PageColor(Colors.White);
        page.DefaultTextStyle(x => x.FontSize(11));

        page.Header().Text("Informe - Tarea 2.2 API REST Bitácora")
            .SemiBold().FontSize(16).FontColor(Colors.Blue.Medium);

        page.Content().Column(col =>
        {
            col.Spacing(12);

            col.Item().Text("1. Diagrama de capas y dependencias").Bold().FontSize(13);
            col.Item().Text("Bitacora.Api --> Bitacora.Application");
            col.Item().Text("Bitacora.Api --> Bitacora.Infrastructure");
            col.Item().Text("Bitacora.Infrastructure --> Bitacora.Application");
            col.Item().Text("Bitacora.Infrastructure --> Bitacora.Domain");
            col.Item().Text("Bitacora.Application --> Bitacora.Domain");
            col.Item().Text("Bitacora.Domain --> (ninguna dependencia)");
            col.Item().Text("Regla: todas las capas dependen hacia el dominio. El dominio no conoce a ningún otro proyecto.");

            col.Item().Text("2. Captura de Swagger").Bold().FontSize(13);
            if (File.Exists(imgPath))
            {
                col.Item().Image(imgPath).FitWidth();
            }
            else
            {
                col.Item().Text("(Imagen no encontrada)");
            }

            col.Item().Text("3. Pruebas en Postman").Bold().FontSize(13);
            col.Item().Text("Colección: docs/Bitacora.postman_collection.json");
            col.Item().Text("Entorno: docs/Bitacora.postman_environment.json (variable baseUrl = http://localhost:5080).");
            col.Item().Text("• GET /api/registros exitoso (200), sin foto.");
            col.Item().Text("• GET /api/registros/{id} exitoso (200) y fallido (404).");
            col.Item().Text("• POST /api/registros exitoso (201 con Location) y fallido (400).");
            col.Item().Text("• PUT /api/registros/{id} exitoso (200), fallido (400) y fallido (404).");
            col.Item().Text("• DELETE /api/registros/{id} exitoso (204) y fallido (404).");

            col.Item().Text("4. Capturas de pruebas en Postman").Bold().FontSize(13);
            var postmanImages = new[]
            {
                @"D:\Documentos\Proyecto predeterminado\Bitacora\docs\images\postman-exito1.PNG",
                @"D:\Documentos\Proyecto predeterminado\Bitacora\docs\images\postman-exito2.PNG",
                @"D:\Documentos\Proyecto predeterminado\Bitacora\docs\images\postman-exito3.PNG",
                @"D:\Documentos\Proyecto predeterminado\Bitacora\docs\images\postman-error1.PNG",
                @"D:\Documentos\Proyecto predeterminado\Bitacora\docs\images\postman-error2.PNG",
            };
            foreach (var img in postmanImages)
            {
                if (File.Exists(img))
                {
                    col.Item().Image(img).FitWidth();
                    col.Item().Text(Path.GetFileName(img)).FontSize(9).Italic();
                }
            }

            col.Item().Text("5. Distribución del trabajo").Bold().FontSize(13);
            col.Item().Table(table =>
            {
                table.ColumnsDefinition(columns =>
                {
                    columns.RelativeColumn(2);
                    columns.RelativeColumn(1.5f);
                    columns.RelativeColumn(3);
                });

                table.Header(header =>
                {
                    header.Cell().Text("Integrante").Bold();
                    header.Cell().Text("N° de cuenta").Bold();
                    header.Cell().Text("Responsabilidad").Bold();
                });

                var rows = new[]
                {
                    ("Angel Manuel Rivera", "202330010471", "Estructura de la solución, proyectos y referencias."),
                    ("Anthony Joel Gonzales Juárez", "202310010114", "Entidad de dominio y DTOs con validaciones."),
                    ("Brayan wilfredo Torres aguilar", "201820030041", "Repositorio EF Core, DbContext y migraciones."),
                    ("Johan Moises Lopez Moran", "202220010502", "Controladores, middleware de errores y configuración Swagger."),
                    ("Luis David Diaz Medina", "202210010189", "Colección de Postman, README y documentación."),
                };

                foreach (var (name, cuenta, resp) in rows)
                {
                    table.Cell().Text(name);
                    table.Cell().Text(cuenta);
                    table.Cell().Text(resp);
                }
            });
        });

        page.Footer().AlignCenter().Text(x =>
        {
            x.Span("Bitácora API - Tarea 2.2");
        });
    });
}).GeneratePdf(outPath);

Console.WriteLine($"PDF generado: {outPath}");