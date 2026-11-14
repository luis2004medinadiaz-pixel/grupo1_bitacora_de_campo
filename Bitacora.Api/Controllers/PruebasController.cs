using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Controllers;

// TEMPORAL: solo para la tarea de comparativa Volley / OkHttp / Retrofit.
// Eliminar este controlador cuando termine la prueba de escenarios de falla.
[ApiController]
[Route("api/pruebas")]
[Authorize]
public class PruebasController : ControllerBase
{
    // Responde después de 15 s, más que el tiempo de espera de 5 s de la app.
    [HttpGet("lento")]
    public async Task<IActionResult> Lento(CancellationToken cancellationToken)
    {
        await Task.Delay(TimeSpan.FromSeconds(15), cancellationToken);
        return Ok(Array.Empty<object>());
    }

    // Lanza una excepción: el middleware la convierte en un 500.
    [HttpGet("error")]
    public IActionResult Error()
    {
        throw new InvalidOperationException("Error de prueba provocado a propósito.");
    }
}
