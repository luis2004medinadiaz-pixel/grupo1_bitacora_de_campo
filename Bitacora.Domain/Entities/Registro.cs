namespace Bitacora.Domain.Entities;

public class Registro
{
    public int Id { get; set; }

    public string Titulo { get; set; } = string.Empty;

    public string Descripcion { get; set; } = string.Empty;

    public string? FotoBase64 { get; set; }

    public DateTime FechaCreacion { get; set; }
}