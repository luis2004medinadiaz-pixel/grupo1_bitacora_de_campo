namespace Bitacora.Application.DTOs;

public class RegistroResponseDto
{
    public int Id { get; set; }

    public string Titulo { get; set; } = string.Empty;

    public string Descripcion { get; set; } = string.Empty;

    public string? FotoBase64 { get; set; }

    public DateTime FechaCreacion { get; set; }

    public int? UsuarioId { get; set; }
}

public class RegistroListItemDto
{
    public int Id { get; set; }

    public string Titulo { get; set; } = string.Empty;

    public string Descripcion { get; set; } = string.Empty;

    public DateTime FechaCreacion { get; set; }

    public int? UsuarioId { get; set; }
}
