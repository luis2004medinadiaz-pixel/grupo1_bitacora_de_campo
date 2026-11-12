using Bitacora.Application.Interfaces;

namespace Bitacora.Infrastructure.Security;

/// <summary>Hash de contraseñas con BCrypt (sal aleatoria incluida en el propio hash).</summary>
public class BcryptPasswordHasher : IPasswordHasher
{
    private const int WorkFactor = 11;

    public string Hash(string password) => BCrypt.Net.BCrypt.HashPassword(password, WorkFactor);

    public bool Verify(string password, string hash)
    {
        try
        {
            return BCrypt.Net.BCrypt.Verify(password, hash);
        }
        catch (Exception)
        {
            // Hash corrupto o con formato inesperado: se trata como credencial inválida.
            return false;
        }
    }
}
