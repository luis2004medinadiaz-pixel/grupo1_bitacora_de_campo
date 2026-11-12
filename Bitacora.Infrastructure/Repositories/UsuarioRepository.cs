using Bitacora.Application.Exceptions;
using Bitacora.Application.Interfaces;
using Bitacora.Domain.Entities;
using Bitacora.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace Bitacora.Infrastructure.Repositories;

public class UsuarioRepository : IUsuarioRepository
{
    private readonly BitacoraDbContext _context;

    public UsuarioRepository(BitacoraDbContext context)
    {
        _context = context;
    }

    public async Task<Usuario?> GetByCorreoAsync(string correo, CancellationToken cancellationToken = default)
    {
        return await _context.Usuarios
            .AsNoTracking()
            .FirstOrDefaultAsync(u => u.Correo == correo, cancellationToken);
    }

    public async Task<bool> ExistsByCorreoAsync(string correo, CancellationToken cancellationToken = default)
    {
        return await _context.Usuarios.AnyAsync(u => u.Correo == correo, cancellationToken);
    }

    public async Task<Usuario> AddAsync(Usuario usuario, CancellationToken cancellationToken = default)
    {
        _context.Usuarios.Add(usuario);
        try
        {
            await _context.SaveChangesAsync(cancellationToken);
        }
        catch (DbUpdateException)
        {
            // Dos registros simultáneos con el mismo correo: el índice único lo rechaza.
            throw new ConflictException("El correo ya está registrado.");
        }

        return usuario;
    }
}
