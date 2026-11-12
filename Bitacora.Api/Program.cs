using System.Text;
using Bitacora.Api.Middleware;
using Bitacora.Api.Security;
using Bitacora.Application.Interfaces;
using Bitacora.Application.Services;
using Bitacora.Infrastructure.Data;
using Bitacora.Infrastructure.Repositories;
using Bitacora.Infrastructure.Security;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Authorization;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi;

var builder = WebApplication.CreateBuilder(args);

// ---------------------------------------------------------------------------
// Configuración JWT. La clave de firma NO está en el repositorio:
//   dotnet user-secrets set "Jwt:Key" "<mínimo 32 caracteres>"      (desarrollo)
//   variable de entorno Jwt__Key                                    (servidor)
// ---------------------------------------------------------------------------
var jwtSection = builder.Configuration.GetSection("Jwt");
var jwt = jwtSection.Get<JwtOptions>() ?? new JwtOptions();

if (string.IsNullOrWhiteSpace(jwt.Key) || Encoding.UTF8.GetByteCount(jwt.Key) < 32)
{
    throw new InvalidOperationException(
        "Falta la clave de firma JWT (Jwt:Key, mínimo 32 caracteres). " +
        "Configúrala con: dotnet user-secrets set \"Jwt:Key\" \"<clave>\" " +
        "o con la variable de entorno Jwt__Key.");
}

builder.Services.Configure<JwtOptions>(jwtSection);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "Bitácora API",
        Version = "v1",
        Description = "API REST para administrar los registros de la bitácora de campo. " +
                      "Protegida con JWT: use POST /api/auth/login y luego el botón Authorize."
    });

    // Esquema Bearer para probar desde Swagger (botón "Authorize").
    options.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        Description = "Pegue solo el token (Swagger agrega el prefijo 'Bearer ')."
    });

    options.AddSecurityRequirement(document => new OpenApiSecurityRequirement
    {
        [new OpenApiSecuritySchemeReference("Bearer", document)] = new List<string>()
    });
});

var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? "Data Source=bitacora.db";

builder.Services.AddDbContext<BitacoraDbContext>(options =>
    options.UseSqlite(connectionString));

builder.Services.AddScoped<IRegistroRepository, RegistroRepository>();
builder.Services.AddScoped<IRegistroService, RegistroService>();
builder.Services.AddScoped<IUsuarioRepository, UsuarioRepository>();
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddSingleton<IPasswordHasher, BcryptPasswordHasher>();
builder.Services.AddSingleton<ITokenService, JwtTokenService>();

// ---------------------------------------------------------------------------
// Autenticación: JWT Bearer validando emisor, audiencia, firma y tiempo de vida.
// ---------------------------------------------------------------------------
builder.Services
    .AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        // Mantiene los nombres cortos de los claims ("sub", "role", "email").
        options.MapInboundClaims = false;

        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidIssuer = jwt.Issuer,
            ValidateAudience = true,
            ValidAudience = jwt.Audience,
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwt.Key)),
            ValidateLifetime = true,
            RequireExpirationTime = true,
            // Sin tolerancia: el token vence exactamente en su fecha de expiración.
            ClockSkew = TimeSpan.Zero,
            // Solo se acepta el algoritmo con el que firmamos.
            ValidAlgorithms = new[] { SecurityAlgorithms.HmacSha256 },
            NameClaimType = "sub",
            RoleClaimType = "role"
        };

        options.Events = new JwtBearerEvents
        {
            // 401: sin token, token alterado o token vencido.
            OnChallenge = context =>
            {
                context.HandleResponse();

                var vencido = context.AuthenticateFailure is SecurityTokenExpiredException;
                var traiaToken = context.Request.Headers.ContainsKey("Authorization");

                string detalle;
                if (vencido)
                {
                    detalle = "El token ha vencido. Inicia sesión nuevamente.";
                    context.Response.Headers.WWWAuthenticate =
                        "Bearer error=\"invalid_token\", error_description=\"The token has expired\"";
                }
                else if (traiaToken)
                {
                    detalle = "El token no es válido.";
                    context.Response.Headers.WWWAuthenticate = "Bearer error=\"invalid_token\"";
                }
                else
                {
                    detalle = "Se requiere autenticación. Envía el encabezado Authorization: Bearer <token>.";
                    context.Response.Headers.WWWAuthenticate = "Bearer";
                }

                return ProblemResponse.WriteAsync(
                    context.Response,
                    context.Request.Path,
                    StatusCodes.Status401Unauthorized,
                    "No autenticado",
                    detalle);
            }
        };
    });

builder.Services.AddAuthorization();

// 403 con ProblemDetails (usuario autenticado pero sin el rol requerido).
builder.Services.AddSingleton<IAuthorizationMiddlewareResultHandler, ForbiddenProblemHandler>();

var app = builder.Build();

// Aplica las migraciones pendientes y crea el Admin inicial (si está configurado).
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<BitacoraDbContext>();
    db.Database.Migrate();

    var hasher = scope.ServiceProvider.GetRequiredService<IPasswordHasher>();
    var logger = scope.ServiceProvider.GetRequiredService<ILoggerFactory>().CreateLogger("Seed");
    await DbSeeder.SeedAdminAsync(
        db,
        hasher,
        builder.Configuration["Seed:AdminCorreo"],
        builder.Configuration["Seed:AdminPassword"],
        logger);
}

app.UseMiddleware<ExceptionHandlingMiddleware>();

app.UseHttpsRedirection();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// El orden importa: primero se identifica al usuario, luego se decide si puede pasar.
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
