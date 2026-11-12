using Bitacora.Application.DTOs;
using Bitacora.Application.Exceptions;
using Bitacora.Application.Interfaces;
using Bitacora.Domain;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.Services;

public class AuthService : IAuthService
{
    // Mensaje único: nunca se revela si falló el correo o la contraseña.
    private const string MensajeCredenciales = "Credenciales inválidas.";

    // Hash falso para gastar el mismo tiempo de BCrypt cuando el correo no existe
    // (evita distinguir "correo inexistente" de "contraseña errónea" midiendo el tiempo).
    private static string? _hashFalso;

    private readonly IUsuarioRepository _usuarios;
    private readonly IPasswordHasher _hasher;
    private readonly ITokenService _tokens;

    public AuthService(IUsuarioRepository usuarios, IPasswordHasher hasher, ITokenService tokens)
    {
        _usuarios = usuarios;
        _hasher = hasher;
        _tokens = tokens;
    }

    public async Task<UsuarioResponseDto> RegistrarAsync(RegistroUsuarioDto dto, CancellationToken cancellationToken = default)
    {
        var correo = Normalizar(dto.Correo);

        if (await _usuarios.ExistsByCorreoAsync(correo, cancellationToken))
        {
            throw new ConflictException("El correo ya está registrado.");
        }

        // El registro público SIEMPRE crea usuarios con rol "Usuario".
        // El rol Admin nunca se puede pedir desde el exterior.
        var usuario = new Usuario
        {
            Correo = correo,
            PasswordHash = _hasher.Hash(dto.Password),
            Rol = Roles.Usuario,
            FechaCreacion = DateTime.UtcNow
        };

        var creado = await _usuarios.AddAsync(usuario, cancellationToken);

        return new UsuarioResponseDto { Id = creado.Id, Correo = creado.Correo, Rol = creado.Rol };
    }

    public async Task<AuthResponseDto> LoginAsync(LoginDto dto, CancellationToken cancellationToken = default)
    {
        var usuario = await _usuarios.GetByCorreoAsync(Normalizar(dto.Correo), cancellationToken);

        _hashFalso ??= _hasher.Hash(Guid.NewGuid().ToString("N"));
        var passwordValida = _hasher.Verify(dto.Password, usuario?.PasswordHash ?? _hashFalso);

        if (usuario is null || !passwordValida)
        {
            throw new UnauthorizedException(MensajeCredenciales);
        }

        var token = _tokens.CreateToken(usuario);

        return new AuthResponseDto
        {
            Token = token.Token,
            ExpiraEn = token.ExpiraEn,
            Rol = usuario.Rol,
            Correo = usuario.Correo,
            UsuarioId = usuario.Id
        };
    }

    private static string Normalizar(string correo) => correo.Trim().ToLowerInvariant();
}
