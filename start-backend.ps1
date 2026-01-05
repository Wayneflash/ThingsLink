# IOT 后端服务启动脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   IOT 后端服务启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 启动 Docker 容器
Write-Host "[1/3] 启动 Docker 容器..." -ForegroundColor Yellow
Set-Location "D:\AICoding\IOT"
docker-compose up -d
Write-Host "等待容器就绪 10 秒..." -ForegroundColor Gray
Start-Sleep -Seconds 10
Write-Host ""

# 步骤2: 停止占用 8080 端口的进程
Write-Host "[2/3] 检查并停止占用 8080 端口的进程..." -ForegroundColor Yellow
try {
    $connections = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
    if ($connections) {
        $connections | ForEach-Object { 
            Write-Host "发现进程 PID: $($_.OwningProcess)，正在停止..." -ForegroundColor Gray
            Stop-Process -Id $_.OwningProcess -Force 
        }
        Start-Sleep -Seconds 2
    } else {
        Write-Host "8080 端口未被占用" -ForegroundColor Gray
    }
} catch {
    Write-Host "检查端口时出错，继续启动..." -ForegroundColor Gray
}
Write-Host ""

# 步骤3: 启动后端服务
Write-Host "[3/3] 启动后端服务..." -ForegroundColor Yellow
Set-Location "D:\AICoding\IOT\backend"

if (-not (Test-Path "target\iot-platform.jar")) {
    Write-Host "错误: 找不到 target\iot-platform.jar" -ForegroundColor Red
    Write-Host "请先编译: mvn clean package -DskipTests" -ForegroundColor Red
    pause
    exit 1
}

Write-Host "后端将在新窗口启动，请查看新窗口了解启动状态" -ForegroundColor Green
Write-Host ""

$javaPath = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot\bin\java.exe'
Start-Process $javaPath -ArgumentList "-jar", "target\iot-platform.jar" -WorkingDirectory "D:\AICoding\IOT\backend"

Start-Sleep -Seconds 2

Write-Host "========================================" -ForegroundColor Green
Write-Host "   启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "服务地址: http://localhost:8080" -ForegroundColor Cyan
Write-Host "请在新窗口查看启动日志" -ForegroundColor Gray
Write-Host "等待看到 'IoT Platform Started Successfully!' 即启动成功" -ForegroundColor Gray
Write-Host ""
Write-Host "按任意键关闭此窗口..." -ForegroundColor Gray
pause
