using Bitacora.Domain.Entities;

namespace Bitacora.Application.Interfaces;

public interface IUsuarioRepository
{
    Task<Usuario?> GetByCorreoAsync(string correo, CancellationToken cancellationToken = default);

    Task<bool> ExistsByCorreoAsync(string correo, CancellationToken cancellationToken = default);

    Task<Usuario> AddAsync(Usuario usuario, CancellationToken cancellationToken = default);
}
