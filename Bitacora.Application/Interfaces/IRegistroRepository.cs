using Bitacora.Domain.Entities;

namespace Bitacora.Application.Interfaces;

public interface IRegistroRepository
{
    Task<IEnumerable<Registro>> GetAllAsync(CancellationToken cancellationToken = default);

    Task<Registro?> GetByIdAsync(int id, CancellationToken cancellationToken = default);

    Task<Registro> AddAsync(Registro registro, CancellationToken cancellationToken = default);

    Task UpdateAsync(Registro registro, CancellationToken cancellationToken = default);

    Task DeleteAsync(Registro registro, CancellationToken cancellationToken = default);

    Task<bool> ExistsAsync(int id, CancellationToken cancellationToken = default);
}