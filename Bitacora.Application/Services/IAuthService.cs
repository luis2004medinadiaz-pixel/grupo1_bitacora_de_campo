using Bitacora.Application.DTOs;

namespace Bitacora.Application.Services;

public interface IAuthService
{
    Task<UsuarioResponseDto> RegistrarAsync(RegistroUsuarioDto dto, CancellationToken cancellationToken = default);

    Task<AuthResponseDto> LoginAsync(LoginDto dto, CancellationToken cancellationToken = default);
}
