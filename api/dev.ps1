#!/usr/bin/env pwsh
# Local Development Script for NAIS Auth API

param(
    [Parameter(Position=0)]
    [string]$Command = "help"
)

function Show-Help {
    Write-Host ""
    Write-Host "NAIS Auth API Local Development Script" -ForegroundColor Green
    Write-Host "======================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Usage: .\dev.ps1 [command]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor Cyan
    Write-Host "  build    - Build the SAM application" -ForegroundColor White
    Write-Host "  start    - Start the local API server" -ForegroundColor White
    Write-Host "  restart  - Rebuild and restart the server" -ForegroundColor White
    Write-Host "  clean    - Clean build artifacts" -ForegroundColor White
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Cyan
    Write-Host "  .\dev.ps1 build     # Build the application" -ForegroundColor Gray
    Write-Host "  .\dev.ps1 start     # Start local server at http://127.0.0.1:8080" -ForegroundColor Gray
    Write-Host "  .\dev.ps1 restart   # Rebuild and restart" -ForegroundColor Gray
    Write-Host ""
}

function Build-App {
    Write-Host "Building SAM application for local development..." -ForegroundColor Yellow
    sam build --template-file template-local.yaml
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Build failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "Build completed successfully!" -ForegroundColor Green
}

function Start-Server {
    Write-Host "Starting local API server on port 8080..." -ForegroundColor Yellow
    Write-Host "API will be available at: http://127.0.0.1:8080" -ForegroundColor Cyan
    Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Gray
    Write-Host ""
    # Use built template and skip pulling Docker images to avoid network issues
    sam local start-api --skip-pull-image --env-vars env.json --port 8080
}

function Restart-Server {
    Write-Host "Rebuilding and restarting local API server..." -ForegroundColor Yellow
    Build-App
    if ($LASTEXITCODE -eq 0) {
        Start-Server
    }
}

function Clean-Build {
    Write-Host "Cleaning build artifacts..." -ForegroundColor Yellow
    if (Test-Path ".aws-sam") {
        Remove-Item -Recurse -Force ".aws-sam"
        Write-Host "Cleaned .aws-sam directory" -ForegroundColor Green
    } else {
        Write-Host "No build artifacts to clean" -ForegroundColor Gray
    }
}

# Main script logic
switch ($Command.ToLower()) {
    "build" { Build-App }
    "start" { Start-Server }
    "restart" { Restart-Server }
    "clean" { Clean-Build }
    "help" { Show-Help }
    default { 
        Write-Host "Unknown command: $Command" -ForegroundColor Red
        Show-Help 
    }
}