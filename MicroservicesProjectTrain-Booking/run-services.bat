@echo off
echo Starting all IRCTC Microservices...

if not defined APP_SECURITY_JWT_SECRET (
  for /f "delims=" %%K in ('powershell -NoProfile -Command "$bytes = New-Object byte[] 32; $rng = [Security.Cryptography.RandomNumberGenerator]::Create(); try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }; [Convert]::ToBase64String($bytes)"') do set "APP_SECURITY_JWT_SECRET=%%K"
  echo Generated a temporary JWT signing key for this local run.
)

echo Starting Eureka Server...
start "Eureka Server" cmd /k "java -jar eureka-server\target\eureka-server-0.0.1-SNAPSHOT.jar"
timeout /t 15

echo Starting API Gateway...
start "API Gateway" cmd /k "java -jar api-gateway\target\api-gateway-0.0.1-SNAPSHOT.jar"

echo Starting User Service...
start "User Service" cmd /k "java -jar user-service\target\user-service-0.0.1-SNAPSHOT.jar"

echo Starting Train Service...
start "Train Service" cmd /k "java -jar train-service\target\train-service-0.0.1-SNAPSHOT.jar"

echo Starting Booking Service...
start "Booking Service" cmd /k "java -jar booking-service\target\booking-service-0.0.1-SNAPSHOT.jar"

echo All services are launching in separate windows!
