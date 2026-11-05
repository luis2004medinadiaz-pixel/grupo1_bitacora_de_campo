using Bitacora.Application.DTOs;

namespace Bitacora.Application.Services;

public interface IRegistroService
{
    Task<IEnumerable<RegistroListItemDto>> GetAllAsync(CancellationToken cancellationToken = default);

    Task<RegistroResponseDto> GetByIdAsync(int id, CancellationToken cancellationToken = default);

    Task<RegistroResponseDto> CreateAsync(RegistroCreateDto dto, CancellationToken cancellationToken = default);

    Task<RegistroResponseDto> UpdateAsync(int id, RegistroUpdateDto dto, CancellationToken cancellationToken = default);

    Task DeleteAsync(int id, CancellationToken cancellationToken = default);
}