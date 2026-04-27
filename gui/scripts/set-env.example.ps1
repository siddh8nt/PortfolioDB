# Set DB environment variables before launching the app.
# Run this file in the SAME PowerShell terminal where you will run Maven:
#   .\scripts\set-env.example.ps1

# Replace these placeholder values with your active DB credentials.
$env:DB_URL="jdbc:mysql://interchange.proxy.rlwy.net:47479/portfoliodb?useSSL=true&requireSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USER="<your_db_user>"
$env:DB_PASSWORD="<your_db_password>"

Write-Host "DB environment variables loaded in this terminal session." -ForegroundColor Green
Write-Host "Current DB_URL: $env:DB_URL" -ForegroundColor DarkGray
Write-Host "Current DB_USER: $env:DB_USER" -ForegroundColor DarkGray
Write-Host "Now run: mvn clean compile" -ForegroundColor Cyan
Write-Host "Then run: mvn exec:java" -ForegroundColor Cyan
