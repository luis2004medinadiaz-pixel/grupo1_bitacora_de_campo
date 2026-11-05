using Bitacora.Application.DTOs;
using Bitacora.Application.Services;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class RegistrosController : ControllerBase
{
    private readonly IRegistroService _service;

    public RegistrosController(IRegistroService service)
    {
        _service = service;
    }

    [HttpGet]
    [ProducesResponseType(typeof(IEnumerable<RegistroListItemDto>), StatusCodes.Status200OK)]
    public async Task<ActionResult<IEnumerable<RegistroListItemDto>>> GetAll(CancellationToken cancellationToken)
    {
        var registros = await _service.GetAllAsync(cancellationToken);
        return Ok(registros);
    }

    [HttpGet("{id:int}")]
    [ProducesResponseType(typeof(RegistroResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<RegistroResponseDto>> GetById(int id, CancellationToken cancellationToken)
    {
        var registro = await _service.GetByIdAsync(id, cancellationToken);
        return Ok(registro);
    }

    [HttpPost]
    [ProducesResponseType(typeof(RegistroResponseDto), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<RegistroResponseDto>> Create([FromBody] RegistroCreateDto dto, CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return ValidationProblem(ModelState);
        }

        var created = await _service.CreateAsync(dto, cancellationToken);
        return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
    }

    [HttpPut("{id:int}")]
    [ProducesResponseType(typeof(RegistroResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<ActionResult<RegistroResponseDto>> Update(int id, [FromBody] RegistroUpdateDto dto, CancellationToken cancellationToken)
    {
        if (!ModelState.IsValid)
        {
            return ValidationProblem(ModelState);
        }

        var updated = await _service.UpdateAsync(id, dto, cancellationToken);
        return Ok(updated);
    }

    [HttpDelete("{id:int}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Delete(int id, CancellationToken cancellationToken)
    {
        await _service.DeleteAsync(id, cancellationToken);
        return NoContent();
    }
}