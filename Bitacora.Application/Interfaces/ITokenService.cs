using Bitacora.Domain.Entities;

namespace Bitacora.Application.Interfaces;

public record TokenResult(string Token, DateTime ExpiraEn);

public interface ITokenService
{
    TokenResult CreateToken(Usuario usuario);
}
