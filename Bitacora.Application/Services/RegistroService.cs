using Bitacora.Application.DTOs;
using Bitacora.Application.Exceptions;
using Bitacora.Application.Interfaces;
using Bitacora.Application.Validation;
using Bitacora.Domain;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.Services;

public class RegistroService : IRegistroService
{
    private readonly IRegistroRepository _repository;

    public RegistroService(IRegistroRepository repository)
    {
        _repository = repository;
    }

    public async Task<IEnumerable<RegistroListItemDto>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        var registros = await _repository.GetAllAsync(cancellationToken);
        return registros.Select(r => new RegistroListItemDto
        {
            Id = r.Id,
            Titulo = r.Titulo,
            Descripcion = r.Descripcion,
            FechaCreacion = r.FechaCreacion,
            UsuarioId = r.UsuarioId
        });
    }

    public async Task<RegistroResponseDto> GetByIdAsync(int id, CancellationToken cancellationToken = default)
    {
        var registro = await _repository.GetByIdAsync(id, cancellationToken);
        if (registro is null)
        {
            throw new NotFoundException($"No se encontró el registro con id {id}.");
        }

        return ToResponse(registro);
    }

    public async Task<RegistroResponseDto> CreateAsync(RegistroCreateDto dto, int usuarioId, CancellationToken cancellationToken = default)
    {
        var registro = new Registro
        {
            Titulo = dto.Titulo.Trim(),
            Descripcion = dto.Descripcion.Trim(),
            FotoBase64 = Base64Photo.Normalize(dto.FotoBase64),
            FechaCreacion = DateTime.UtcNow,
            UsuarioId = usuarioId
        };

        var created = await _repository.AddAsync(registro, cancellationToken);
        return ToResponse(created);
    }

    public async Task<RegistroResponseDto> UpdateAsync(int id, RegistroUpdateDto dto, int usuarioId, string rol, CancellationToken cancellationToken = default)
    {
        var registro = await _repository.GetByIdAsync(id, cancellationToken);
        if (registro is null)
        {
            throw new NotFoundException($"No se encontró el registro con id {id}.");
        }

        // Autorización por propiedad: solo el creador o un Admin pueden modificar.
        if (rol != Roles.Admin && registro.UsuarioId != usuarioId)
        {
            throw new ForbiddenException("No tienes permiso para modificar un registro creado por otro usuario.");
        }

        registro.Titulo = dto.Titulo.Trim();
        registro.Descripcion = dto.Descripcion.Trim();
        registro.FotoBase64 = Base64Photo.Normalize(dto.FotoBase64);

        await _repository.UpdateAsync(registro, cancellationToken);
        return ToResponse(registro);
    }

    public async Task DeleteAsync(int id, CancellationToken cancellationToken = default)
    {
        var registro = await _repository.GetByIdAsync(id, cancellationToken);
        if (registro is null)
        {
            throw new NotFoundException($"No se encontró el registro con id {id}.");
        }

        await _repository.DeleteAsync(registro, cancellationToken);
    }

    private static RegistroResponseDto ToResponse(Registro registro) => new()
    {
        Id = registro.Id,
        Titulo = registro.Titulo,
        Descripcion = registro.Descripcion,
        FotoBase64 = registro.FotoBase64,
        FechaCreacion = registro.FechaCreacion,
        UsuarioId = registro.UsuarioId
    };
}