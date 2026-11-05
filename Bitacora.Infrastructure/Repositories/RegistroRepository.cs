using Bitacora.Application.Interfaces;
using Bitacora.Domain.Entities;
using Bitacora.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace Bitacora.Infrastructure.Repositories;

public class RegistroRepository : IRegistroRepository
{
    private readonly BitacoraDbContext _context;

    public RegistroRepository(BitacoraDbContext context)
    {
        _context = context;
    }

    public async Task<IEnumerable<Registro>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        return await _context.Registros
            .AsNoTracking()
            .OrderByDescending(r => r.FechaCreacion)
            .ToListAsync(cancellationToken);
    }

    public async Task<Registro?> GetByIdAsync(int id, CancellationToken cancellationToken = default)
    {
        return await _context.Registros.FindAsync(new object[] { id }, cancellationToken);
    }

    public async Task<Registro> AddAsync(Registro registro, CancellationToken cancellationToken = default)
    {
        _context.Registros.Add(registro);
        await _context.SaveChangesAsync(cancellationToken);
        return registro;
    }

    public async Task UpdateAsync(Registro registro, CancellationToken cancellationToken = default)
    {
        _context.Registros.Update(registro);
        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task DeleteAsync(Registro registro, CancellationToken cancellationToken = default)
    {
        _context.Registros.Remove(registro);
        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task<bool> ExistsAsync(int id, CancellationToken cancellationToken = default)
    {
        return await _context.Registros.AnyAsync(r => r.Id == id, cancellationToken);
    }
}