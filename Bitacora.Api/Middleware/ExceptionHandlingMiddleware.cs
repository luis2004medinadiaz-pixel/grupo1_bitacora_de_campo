using Bitacora.Api.Security;
using Bitacora.Application.Exceptions;

namespace Bitacora.Api.Middleware;

public class ExceptionHandlingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ExceptionHandlingMiddleware> _logger;

    public ExceptionHandlingMiddleware(RequestDelegate next, ILogger<ExceptionHandlingMiddleware> logger)
    {
        _next = next;
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await _next(context);
        }
        catch (Exception ex)
        {
            var (status, title) = ex switch
            {
                NotFoundException => (StatusCodes.Status404NotFound, "Recurso no encontrado"),
                ForbiddenException => (StatusCodes.Status403Forbidden, "Acceso prohibido"),
                UnauthorizedException => (StatusCodes.Status401Unauthorized, "No autenticado"),
                ConflictException => (StatusCodes.Status409Conflict, "Conflicto"),
                _ => (StatusCodes.Status500InternalServerError, "Error interno del servidor")
            };

            if (status == StatusCodes.Status500InternalServerError)
            {
                _logger.LogError(ex, "Unhandled exception processing {Method} {Path}", context.Request.Method, context.Request.Path);
            }
            else
            {
                _logger.LogWarning("{Method} {Path} -> {Status}: {Message}", context.Request.Method, context.Request.Path, status, ex.Message);
            }

            if (status == StatusCodes.Status401Unauthorized)
            {
                context.Response.Headers.WWWAuthenticate = "Bearer";
            }

            var detail = status == StatusCodes.Status500InternalServerError
                ? "Ocurrió un error inesperado. Intente nuevamente más tarde."
                : ex.Message;

            await ProblemResponse.WriteAsync(context.Response, context.Request.Path, status, title, detail);
        }
    }
}
