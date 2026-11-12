namespace Bitacora.Domain;

/// <summary>Roles válidos del sistema. Se usan en el token JWT y en [Authorize(Roles = ...)].</summary>
public static class Roles
{
    public const string Admin = "Admin";
    public const string Usuario = "Usuario";
}
