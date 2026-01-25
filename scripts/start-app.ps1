# UniApp Mobile Application Startup Script
# PowerShell version for better error handling

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Start UniApp Mobile Application" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get script directory
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir

Write-Host "Project Path: $projectRoot" -ForegroundColor Gray
Write-Host "Note: Ensure backend service and Docker are already started" -ForegroundColor Gray
Write-Host ""

# Change to project root
Set-Location $projectRoot

# Check if iot-mobile-app directory exists
$mobileAppDir = Join-Path $projectRoot "iot-mobile-app"
if (-not (Test-Path $mobileAppDir)) {
    Write-Host "[ERROR] iot-mobile-app directory not found!" -ForegroundColor Red
    Write-Host "Current directory: $projectRoot" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Navigate to UniApp directory
Set-Location $mobileAppDir
Write-Host "[INFO] Changed to: $mobileAppDir" -ForegroundColor Green
Write-Host ""

# Check if package.json exists
if (-not (Test-Path "package.json")) {
    Write-Host "[ERROR] package.json not found!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if npm is available
try {
    $npmVersion = npm --version
    Write-Host "[INFO] npm version: $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERROR] npm command not found!" -ForegroundColor Red
    Write-Host "Please install Node.js and npm first." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check if node_modules exists
if (-not (Test-Path "node_modules")) {
    Write-Host "[ERROR] node_modules directory not found!" -ForegroundColor Red
    Write-Host "Please run: npm install" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Set environment variables
$env:NODE_ENV = "development"
$env:UNI_PLATFORM = "h5"
Write-Host "[INFO] Environment variables set: NODE_ENV=$env:NODE_ENV, UNI_PLATFORM=$env:UNI_PLATFORM" -ForegroundColor Green
Write-Host ""

# Try to start the development server
Write-Host "[INFO] Starting UniApp development server..." -ForegroundColor Cyan
Write-Host ""

$vueCliService = Join-Path $mobileAppDir "node_modules\@vue\cli-service\bin\vue-cli-service.js"

if (Test-Path $vueCliService) {
    Write-Host "[INFO] Found: vue-cli-service.js" -ForegroundColor Green
    Write-Host "[INFO] Server will be available at http://localhost:8081/" -ForegroundColor Yellow
    Write-Host "[INFO] Press Ctrl+C to stop the server" -ForegroundColor Yellow
    Write-Host ""
    
    try {
        # Start the server (this will block until Ctrl+C)
        node $vueCliService uni-serve
    } catch {
        Write-Host ""
        Write-Host "[ERROR] Failed to start development server!" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
} else {
    Write-Host "[ERROR] vue-cli-service.js not found!" -ForegroundColor Red
    Write-Host "Expected path: $vueCliService" -ForegroundColor Red
    Write-Host "Please run: npm install" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "[INFO] Development server stopped." -ForegroundColor Green
Read-Host "Press Enter to exit"
