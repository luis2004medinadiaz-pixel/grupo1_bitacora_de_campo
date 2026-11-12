using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Authorization.Policy;

namespace Bitacora.Api.Security;

/// <summary>
/// Responde 403 con ProblemDetails cuando el usuario SÍ está autenticado pero su rol no alcanza
/// (por ejemplo, un "Usuario" intentando DELETE). Los casos sin token o con token inválido
/// siguen el flujo normal de JwtBearer y responden 401.
/// </summary>
public class ForbiddenProblemHandler : IAuthorizationMiddlewareResultHandler
{
    private readonly AuthorizationMiddlewareResultHandler _default = new();

    public Task HandleAsync(
        RequestDelegate next,
        HttpContext context,
        AuthorizationPolicy policy,
        PolicyAuthorizationResult authorizeResult)
    {
        if (authorizeResult.Forbidden)
        {
            return ProblemResponse.WriteAsync(
                context.Response,
                context.Request.Path,
                StatusCodes.Status403Forbidden,
                "Acceso prohibido",
                "Tu usuario no tiene permiso para realizar esta acción.");
        }

        return _default.HandleAsync(next, context, policy, authorizeResult);
    }
}
