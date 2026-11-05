using System.ComponentModel.DataAnnotations;

namespace Bitacora.Application.Validation;

public class ValidBase64Attribute : ValidationAttribute
{
    private const long MaxBytes = 2 * 1024 * 1024;

    protected override ValidationResult? IsValid(object? value, ValidationContext validationContext)
    {
        if (value is null)
        {
            return ValidationResult.Success;
        }

        if (value is not string str || string.IsNullOrWhiteSpace(str))
        {
            return ValidationResult.Success;
        }

        try
        {
            var bytes = Convert.FromBase64String(str);
            if (bytes.Length > MaxBytes)
            {
                return new ValidationResult(ErrorMessage ?? "La foto no puede superar 2 MB.");
            }
        }
        catch (FormatException)
        {
            return new ValidationResult(ErrorMessage ?? "La foto debe estar en Base64 válido.");
        }

        return ValidationResult.Success;
    }
}