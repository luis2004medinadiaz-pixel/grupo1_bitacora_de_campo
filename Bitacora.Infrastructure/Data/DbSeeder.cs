using Bitacora.Application.Interfaces;
using Bitacora.Domain;
using Bitacora.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace Bitacora.Infrastructure.Data;

public static class DbSeeder
{
    /// <summary>
    /// Crea el usuario Admin inicial a partir de la configuración (user-secrets o variables de entorno).
    /// El correo y la contraseña del Admin NO están en el repositorio.
    /// </summary>
    public static async Task SeedAdminAsync(
        BitacoraDbContext db,
        IPasswordHasher hasher,
        string? correo,
        string? password,
        ILogger logger)
    {
        if (string.IsNullOrWhiteSpace(correo) || string.IsNullOrWhiteSpace(password))
        {
            logger.LogWarning(
                "No se creó el Admin inicial: configure Seed:AdminCorreo y Seed:AdminPassword (user-secrets o variables de entorno).");
            return;
        }

        var correoNormalizado = correo.Trim().ToLowerInvariant();

        if (await db.Usuarios.AnyAsync(u => u.Correo == correoNormalizado))
        {
            return;
        }

        db.Usuarios.Add(new Usuario
        {
            Correo = correoNormalizado,
            PasswordHash = hasher.Hash(password),
            Rol = Roles.Admin,
            FechaCreacion = DateTime.UtcNow
        });

        await db.SaveChangesAsync();
        logger.LogInformation("Usuario Admin inicial creado ({Correo}).", correoNormalizado);
    }
}
