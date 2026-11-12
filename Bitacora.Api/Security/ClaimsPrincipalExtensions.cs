using System.Security.Claims;
using Bitacora.Application.Exceptions;
using Bitacora.Domain;

namespace Bitacora.Api.Security;

public static class ClaimsPrincipalExtensions
{
    public static int GetUserId(this ClaimsPrincipal principal)
    {
        var valor = principal.FindFirst("sub")?.Value ?? principal.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        return int.TryParse(valor, out var id)
            ? id
            : throw new UnauthorizedException("El token no contiene un identificador de usuario válido.");
    }

    public static string GetRole(this ClaimsPrincipal principal)
    {
        return principal.FindFirst("role")?.Value ?? principal.FindFirst(ClaimTypes.Role)?.Value ?? Roles.Usuario;
    }
}
