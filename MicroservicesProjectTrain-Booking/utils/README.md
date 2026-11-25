# Shared code layout

`common-utils/` is the only shared Java library in this repository. It contains
cross-service JWT configuration, JWT issue/validation helpers, security filter
configuration, and the stable API error response contract.

The root `utils/` directory is documentation only; it is not a Spring Boot
service or a second common-utils module. Service-specific infrastructure such
as Redis caching belongs to the service that uses it. The user service owns the
Redis cache because it is the only service that currently caches data.

## JWT configuration

All services that consume `common-utils` must receive the same
`APP_SECURITY_JWT_SECRET` environment variable. Set it to a Base64-encoded key
of at least 32 random bytes. The optional `APP_SECURITY_JWT_EXPIRATION` setting
accepts a Spring duration such as `30m` or `24h` and defaults to `24h`.

Generate a development key in PowerShell with:

```powershell
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
$env:APP_SECURITY_JWT_SECRET = [Convert]::ToBase64String($bytes)
```

Never commit the generated value. Keep the same key configured for every
service instance so tokens issued by the user service can be verified by the
other services.
