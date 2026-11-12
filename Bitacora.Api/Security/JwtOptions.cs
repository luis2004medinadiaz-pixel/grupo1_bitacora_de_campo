namespace Bitacora.Api.Security;

public class JwtOptions
{
    public string Issuer { get; set; } = string.Empty;

    public string Audience { get; set; } = string.Empty;

    /// <summary>Clave de firma HMAC. NUNCA va en el repositorio: user-secrets o variable de entorno Jwt__Key.</summary>
    public string Key { get; set; } = string.Empty;

    public int ExpirationMinutes { get; set; } = 10;
}
