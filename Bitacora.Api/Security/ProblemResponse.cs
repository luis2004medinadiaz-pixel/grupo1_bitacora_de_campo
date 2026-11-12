using System.Text.Json;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Security;

public static class ProblemResponse
{
    public static Task WriteAsync(HttpResponse response, string path, int status, string title, string detail)
    {
        response.StatusCode = status;

        var problem = new ProblemDetails
        {
            Status = status,
            Title = title,
            Detail = detail,
            Instance = path
        };

        return response.WriteAsJsonAsync(problem, (JsonSerializerOptions?)null, "application/problem+json");
    }
}
