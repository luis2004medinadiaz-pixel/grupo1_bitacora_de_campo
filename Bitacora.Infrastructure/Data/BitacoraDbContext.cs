using Bitacora.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace Bitacora.Infrastructure.Data;

public class BitacoraDbContext : DbContext
{
    public BitacoraDbContext(DbContextOptions<BitacoraDbContext> options) : base(options)
    {
    }

    public DbSet<Registro> Registros => Set<Registro>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Registro>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Titulo).IsRequired().HasMaxLength(100);
            entity.Property(e => e.Descripcion).IsRequired().HasMaxLength(500);
            entity.Property(e => e.FotoBase64).HasColumnType("TEXT");
            entity.Property(e => e.FechaCreacion).IsRequired();
        });
    }
}