using Bitacora.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace Bitacora.Infrastructure.Data;

public class BitacoraDbContext : DbContext
{
    public BitacoraDbContext(DbContextOptions<BitacoraDbContext> options) : base(options)
    {
    }

    public DbSet<Registro> Registros => Set<Registro>();

    public DbSet<Usuario> Usuarios => Set<Usuario>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Usuario>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Correo).IsRequired().HasMaxLength(254);
            entity.HasIndex(e => e.Correo).IsUnique();
            entity.Property(e => e.PasswordHash).IsRequired().HasMaxLength(100);
            entity.Property(e => e.Rol).IsRequired().HasMaxLength(20);
            entity.Property(e => e.FechaCreacion).IsRequired();
        });

        modelBuilder.Entity<Registro>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Titulo).IsRequired().HasMaxLength(100);
            entity.Property(e => e.Descripcion).IsRequired().HasMaxLength(500);
            entity.Property(e => e.FotoBase64).HasColumnType("TEXT");
            entity.Property(e => e.FechaCreacion).IsRequired();

            // Autor del registro (opcional para conservar los registros anteriores a la autenticación).
            entity.HasOne<Usuario>()
                .WithMany()
                .HasForeignKey(e => e.UsuarioId)
                .OnDelete(DeleteBehavior.SetNull);
        });
    }
}
