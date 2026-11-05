using Bitacora.Application.Exceptions;
using Microsoft.AspNetCore.Mvc;

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
            _logger.LogError(ex, "Unhandled exception processing {Method} {Path}", context.Request.Method, context.Request.Path);

            var (status, title) = ex switch
            {
                NotFoundException => (StatusCodes.Status404NotFound, "Recurso no encontrado"),
                _ => (StatusCodes.Status500InternalServerError, "Error interno del servidor")
            };

            var problem = new ProblemDetails
            {
                Status = status,
                Title = title,
                Detail = status == StatusCodes.Status500InternalServerError
                    ? "Ocurrió un error inesperado. Intente nuevamente más tarde."
                    : ex.Message,
                Instance = context.Request.Path
            };

            context.Response.StatusCode = status;
            context.Response.ContentType = "application/problem+json";
            await context.Response.WriteAsJsonAsync(problem);
        }
    }
}