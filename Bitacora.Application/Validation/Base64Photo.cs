namespace Bitacora.Application.Validation;

public static class Base64Photo
{
    private const string Base64Marker = ";base64,";

    public static string? Normalize(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        var trimmed = value.Trim();

        var markerIndex = trimmed.IndexOf(Base64Marker, StringComparison.OrdinalIgnoreCase);
        if (trimmed.StartsWith("data:", StringComparison.OrdinalIgnoreCase) && markerIndex >= 0)
        {
            trimmed = trimmed[(markerIndex + Base64Marker.Length)..];
        }

        var cleaned = new string(trimmed.Where(c => !char.IsWhiteSpace(c)).ToArray());
        return cleaned.Length == 0 ? null : cleaned;
    }
}
