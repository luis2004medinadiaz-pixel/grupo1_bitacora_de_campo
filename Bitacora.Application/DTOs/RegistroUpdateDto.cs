using System.ComponentModel.DataAnnotations;
using Bitacora.Application.Validation;

namespace Bitacora.Application.DTOs;

public class RegistroUpdateDto
{
    [Required(ErrorMessage = "El título es obligatorio.")]
    [StringLength(100, MinimumLength = 3, ErrorMessage = "El título debe tener entre 3 y 100 caracteres.")]
    public string Titulo { get; set; } = string.Empty;

    [Required(ErrorMessage = "La descripción es obligatoria.")]
    [MaxLength(500, ErrorMessage = "La descripción no puede superar 500 caracteres.")]
    public string Descripcion { get; set; } = string.Empty;

    [ValidBase64(ErrorMessage = "La foto debe ser Base64 válido y no superar 2 MB.")]
    public string? FotoBase64 { get; set; }
}