namespace Bitacora.Domain.Entities;

public class Usuario
{
    public int Id { get; set; }

    /// <summary>Correo en minúsculas (único). Es el nombre de usuario para iniciar sesión.</summary>
    public string Correo { get; set; } = string.Empty;

    /// <summary>Hash BCrypt de la contraseña. Nunca se guarda ni se devuelve el texto plano.</summary>
    public string PasswordHash { get; set; } = string.Empty;

    /// <summary>"Admin" o "Usuario" (ver <see cref="Roles"/>).</summary>
    public string Rol { get; set; } = Roles.Usuario;

    public DateTime FechaCreacion { get; set; }
}
