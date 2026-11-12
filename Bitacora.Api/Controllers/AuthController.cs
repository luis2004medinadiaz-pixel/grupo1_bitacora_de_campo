using Bitacora.Application.DTOs;
using Bitacora.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Controllers;

[ApiController]
[Route("api/auth")]
[AllowAnonymous]
public class AuthController : ControllerBase
{
    private readonly IAuthService _auth;

    public AuthController(IAuthService auth)
    {
        _auth = auth;
    }

    /// <summary>Registra un usuario nuevo (siempre con rol "Usuario").</summary>
    [HttpPost("registro")]
    [ProducesResponseType(typeof(UsuarioResponseDto), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status409Conflict)]
    public async Task<ActionResult<UsuarioResponseDto>> Registro([FromBody] RegistroUsuarioDto dto, CancellationToken cancellationToken)
    {
        var usuario = await _auth.RegistrarAsync(dto, cancellationToken);
        return StatusCode(StatusCodes.Status201Created, usuario);
    }

    /// <summary>Inicia sesión y devuelve el token JWT, su fecha de vencimiento y el rol.</summary>
    [HttpPost("login")]
    [ProducesResponseType(typeof(AuthResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    public async Task<ActionResult<AuthResponseDto>> Login([FromBody] LoginDto dto, CancellationToken cancellationToken)
    {
        var respuesta = await _auth.LoginAsync(dto, cancellationToken);
        return Ok(respuesta);
    }
}
