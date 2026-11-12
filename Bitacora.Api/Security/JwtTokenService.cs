using System.Security.Claims;
using System.Text;
using Bitacora.Application.Interfaces;
using Bitacora.Domain.Entities;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.JsonWebTokens;
using Microsoft.IdentityModel.Tokens;

namespace Bitacora.Api.Security;

public class JwtTokenService : ITokenService
{
    private readonly JwtOptions _options;

    public JwtTokenService(IOptions<JwtOptions> options)
    {
        _options = options.Value;
    }

    public TokenResult CreateToken(Usuario usuario)
    {
        var ahora = DateTime.UtcNow;
        var expira = ahora.AddMinutes(_options.ExpirationMinutes);

        var llave = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_options.Key));

        var descriptor = new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(new[]
            {
                new Claim("sub", usuario.Id.ToString()),
                new Claim("email", usuario.Correo),
                new Claim("role", usuario.Rol),
                new Claim("jti", Guid.NewGuid().ToString())
            }),
            Issuer = _options.Issuer,
            Audience = _options.Audience,
            NotBefore = ahora,
            IssuedAt = ahora,
            Expires = expira,
            SigningCredentials = new SigningCredentials(llave, SecurityAlgorithms.HmacSha256)
        };

        var token = new JsonWebTokenHandler().CreateToken(descriptor);
        return new TokenResult(token, expira);
    }
}
