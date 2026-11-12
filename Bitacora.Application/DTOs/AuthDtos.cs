using System.ComponentModel.DataAnnotations;

namespace Bitacora.Application.DTOs;

public class RegistroUsuarioDto
{
    [Required(ErrorMessage = "El correo es obligatorio.")]
    [EmailAddress(ErrorMessage = "El correo no tiene un formato válido.")]
    [StringLength(254, ErrorMessage = "El correo no puede superar 254 caracteres.")]
    public string Correo { get; set; } = string.Empty;

    [Required(ErrorMessage = "La contraseña es obligatoria.")]
    [StringLength(72, MinimumLength = 8, ErrorMessage = "La contraseña debe tener entre 8 y 72 caracteres.")]
    [RegularExpression(@"^(?=.*[A-Za-z])(?=.*\d).+$", ErrorMessage = "La contraseña debe incluir al menos una letra y un número.")]
    public string Password { get; set; } = string.Empty;
}

public class LoginDto
{
    [Required(ErrorMessage = "El correo es obligatorio.")]
    [StringLength(254)]
    public string Correo { get; set; } = string.Empty;

    [Required(ErrorMessage = "La contraseña es obligatoria.")]
    [StringLength(200)]
    public string Password { get; set; } = string.Empty;
}

public class UsuarioResponseDto
{
    public int Id { get; set; }

    public string Correo { get; set; } = string.Empty;

    public string Rol { get; set; } = string.Empty;
}

public class AuthResponseDto
{
    public string Token { get; set; } = string.Empty;

    /// <summary>Fecha y hora (UTC) en que vence el token.</summary>
    public DateTime ExpiraEn { get; set; }

    public string Rol { get; set; } = string.Empty;

    public string Correo { get; set; } = string.Empty;

    public int UsuarioId { get; set; }
}
