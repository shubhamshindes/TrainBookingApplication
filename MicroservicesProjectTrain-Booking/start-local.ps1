<#
.SYNOPSIS
    Starts the IRCTC microservices project for local development.

.DESCRIPTION
    1. Brings up MySQL + Redis via Docker (docker-compose.infra.yml)
    2. Waits until both are healthy
    3. Builds the entire project with Maven (skipping tests)
    4. Launches each Spring Boot service in a separate PowerShell window

.NOTES
    Prerequisites: Java 17+, Maven 3.x, Docker Desktop (running)
    Run from the project root:
        .\start-local.ps1
#>

param(
    [switch]$SkipBuild,         # Pass -SkipBuild to skip Maven build step
    [switch]$InfraOnly,         # Pass -InfraOnly to start only MySQL + Redis
    [switch]$StopInfra          # Pass -StopInfra to tear down infra containers
)

$Root = $PSScriptRoot
$ErrorActionPreference = "Stop"

# ── Colour helpers ────────────────────────────────────────────────────────────
function Write-Step  { param($msg) Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Write-OK    { param($msg) Write-Host "    [OK] $msg" -ForegroundColor Green }
function Write-Warn  { param($msg) Write-Host "    [!!] $msg" -ForegroundColor Yellow }
function Write-Fail  { param($msg) Write-Host "    [XX] $msg" -ForegroundColor Red; exit 1 }

# ── Stop infra ────────────────────────────────────────────────────────────────
if ($StopInfra) {
    Write-Step "Stopping infrastructure containers..."
    docker compose -f "$Root\docker-compose.infra.yml" down
    Write-OK "Infrastructure stopped."
    exit 0
}

# ── Pre-flight checks ─────────────────────────────────────────────────────────
Write-Step "Pre-flight checks"

if (-not (Get-Command java -ErrorAction SilentlyContinue)) { Write-Fail "java not found in PATH" }
Write-OK "Java: $(java -version 2>&1 | Select-Object -First 1)"

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { Write-Fail "mvn not found in PATH" }
Write-OK "Maven: $(mvn -version 2>&1 | Select-Object -First 1)"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { Write-Fail "docker not found in PATH" }
Write-OK "Docker: $(docker --version)"

# ── Start infra ───────────────────────────────────────────────────────────────
Write-Step "Starting infrastructure (MySQL + Redis via Docker)..."
docker compose -f "$Root\docker-compose.infra.yml" up -d
if ($LASTEXITCODE -ne 0) { Write-Fail "Failed to start Docker infra." }

# ── Wait for MySQL ────────────────────────────────────────────────────────────
Write-Step "Waiting for MySQL to become healthy..."
$maxWait = 120   # seconds
$elapsed = 0
$ready   = $false

while ($elapsed -lt $maxWait) {
    $status = docker inspect --format="{{.State.Health.Status}}" irctc-mysql-local 2>$null
    if ($status -eq "healthy") { $ready = $true; break }
    Write-Host "    MySQL status: $status (${elapsed}s elapsed)..." -ForegroundColor DarkGray
    Start-Sleep -Seconds 5
    $elapsed += 5
}
if (-not $ready) { Write-Fail "MySQL did not become healthy within ${maxWait}s." }
Write-OK "MySQL is healthy."

# ── Wait for Redis ────────────────────────────────────────────────────────────
Write-Step "Waiting for Redis to become healthy..."
$elapsed = 0
$ready   = $false

while ($elapsed -lt 60) {
    $status = docker inspect --format="{{.State.Health.Status}}" irctc-redis-local 2>$null
    if ($status -eq "healthy") { $ready = $true; break }
    Write-Host "    Redis status: $status (${elapsed}s elapsed)..." -ForegroundColor DarkGray
    Start-Sleep -Seconds 3
    $elapsed += 3
}
if (-not $ready) { Write-Fail "Redis did not become healthy within 60s." }
Write-OK "Redis is healthy."

if ($InfraOnly) {
    Write-Host "`nInfra-only mode. Services not started." -ForegroundColor Yellow
    Write-Host "  MySQL  -> localhost:3306  (dbs: user_db, train_db, booking_db, user: root, pass: Boss@8055)"
    Write-Host "  Redis  -> localhost:6379"
    exit 0
}

# ── Maven build ───────────────────────────────────────────────────────────────
if (-not $SkipBuild) {
    Write-Step "Building common-utils (shared library)..."
    Push-Location "$Root\common-utils"
    mvn install -DskipTests -q
    if ($LASTEXITCODE -ne 0) { Write-Fail "common-utils build failed." }
    Pop-Location
    Write-OK "common-utils installed."

    Write-Step "Building all services..."
    Push-Location $Root
    mvn install -DskipTests -q
    if ($LASTEXITCODE -ne 0) { Write-Fail "Maven build failed." }
    Pop-Location
    Write-OK "All services built."
} else {
    Write-Warn "Skipping Maven build (-SkipBuild flag set)."
}

# ── Helper: launch a service in a new terminal window ────────────────────────
function Start-Service {
    param(
        [string]$Name,
        [string]$Dir,
        [string]$MainClass = ""
    )

    $jarPattern = "$Root\$Dir\target\*.jar"
    $jar = Get-ChildItem -Path $jarPattern -Exclude "*sources*","*javadoc*" |
           Where-Object { $_.Name -notmatch "original" } |
           Sort-Object LastWriteTime -Descending |
           Select-Object -First 1

    if (-not $jar) { Write-Fail "No JAR found for $Name in $Dir\target\" }

    $cmd = "java -jar `"$($jar.FullName)`""
    Write-OK "Launching $Name  ->  $($jar.Name)"

    Start-Process powershell -ArgumentList `
        "-NoExit", "-Command", `
        "Write-Host '--- $Name ---' -ForegroundColor Cyan; $cmd"
}

# ── Launch services in order ──────────────────────────────────────────────────
Write-Step "Launching services..."

Start-Service -Name "eureka-server"   -Dir "eureka-server"
Write-Host "    Waiting 20s for Eureka to start..." -ForegroundColor DarkGray
Start-Sleep -Seconds 20

Start-Service -Name "api-gateway"     -Dir "api-gateway"
Start-Sleep -Seconds 5

Start-Service -Name "user-service"    -Dir "user-service"
Start-Service -Name "train-service"   -Dir "train-service"
Start-Service -Name "booking-service" -Dir "booking-service"

# ── Done ──────────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "=========================================================" -ForegroundColor Green
Write-Host "  All services launched! Endpoints:" -ForegroundColor Green
Write-Host "  Eureka Dashboard  -> http://localhost:8761" -ForegroundColor Green
Write-Host "  API Gateway       -> http://localhost:8080" -ForegroundColor Green
Write-Host "  user-service      -> http://localhost:8081" -ForegroundColor Green
Write-Host "  train-service     -> http://localhost:8082" -ForegroundColor Green
Write-Host "  booking-service   -> http://localhost:8083" -ForegroundColor Green
Write-Host "=========================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  To stop infrastructure containers later, run:" -ForegroundColor DarkGray
Write-Host "    .\start-local.ps1 -StopInfra" -ForegroundColor DarkGray
Write-Host ""
