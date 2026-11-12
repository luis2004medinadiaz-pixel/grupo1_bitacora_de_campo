namespace Bitacora.Domain.Entities;

public class Registro
{
    public int Id { get; set; }

    public string Titulo { get; set; } = string.Empty;

    public string Descripcion { get; set; } = string.Empty;

    public string? FotoBase64 { get; set; }

    public DateTime FechaCreacion { get; set; }

    /// <summary>
    /// Usuario que creó el registro. Es nulo en los registros creados antes de agregar la
    /// autenticación; esos registros solo los puede modificar un Admin.
    /// </summary>
    public int? UsuarioId { get; set; }
}
