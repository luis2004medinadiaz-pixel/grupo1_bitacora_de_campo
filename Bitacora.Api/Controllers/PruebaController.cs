using Bitacora.Application.Exceptions;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Controllers;

/// <summary>
/// ENDPOINTS TEMPORALES para la tarea comparativa Volley / OkHttp / Retrofit.
/// Solo responden en Development; en cualquier otro entorno devuelven 404.
/// Se eliminan al terminar la tarea.
/// </summary>
[ApiController]
[Route("api/prueba")]
public class PruebaController : ControllerBase
{
    private readonly IWebHostEnvironment _env;

    public PruebaController(IWebHostEnvironment env)
    {
        _env = env;
    }

    /// <summary>Responde tarde (por defecto 20 s) para provocar un tiempo de espera agotado en el cliente.</summary>
    [HttpGet("lento")]
    [Authorize]
    public async Task<IActionResult> Lento([FromQuery] int segundos = 20, CancellationToken cancellationToken = default)
    {
        if (!_env.IsDevelopment()) return NotFound();

        await Task.Delay(TimeSpan.FromSeconds(Math.Clamp(segundos, 1, 120)), cancellationToken);
        return Ok(Array.Empty<object>());
    }

    /// <summary>Provoca un error interno (500).</summary>
    [HttpGet("error500")]
    [Authorize]
    public IActionResult Error500()
    {
        if (!_env.IsDevelopment()) return NotFound();

        throw new InvalidOperationException("Error de prueba provocado a propósito para la comparativa.");
    }

    /// <summary>Responde siempre 401, aunque el token sea válido (para repetir el escenario sin esperar a que venza).</summary>
    [HttpGet("no-autorizado")]
    [AllowAnonymous]
    public IActionResult NoAutorizado()
    {
        if (!_env.IsDevelopment()) return NotFound();

        throw new UnauthorizedException("Respuesta 401 forzada para la comparativa.");
    }
}
