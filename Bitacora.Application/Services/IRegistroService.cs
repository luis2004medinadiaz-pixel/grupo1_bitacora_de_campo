using Bitacora.Application.DTOs;

namespace Bitacora.Application.Services;

public interface IRegistroService
{
    Task<IEnumerable<RegistroListItemDto>> GetAllAsync(CancellationToken cancellationToken = default);

    Task<RegistroResponseDto> GetByIdAsync(int id, CancellationToken cancellationToken = default);

    Task<RegistroResponseDto> CreateAsync(RegistroCreateDto dto, int usuarioId, CancellationToken cancellationToken = default);

    /// <summary>Un Admin puede modificar cualquier registro; un Usuario solo los suyos (si no, 403).</summary>
    Task<RegistroResponseDto> UpdateAsync(int id, RegistroUpdateDto dto, int usuarioId, string rol, CancellationToken cancellationToken = default);

    Task DeleteAsync(int id, CancellationToken cancellationToken = default);
}
