@echo off
chcp 65001 >nul
echo ========================================
echo 修复首页和通知功能
echo ========================================
echo.
echo 问题：
echo 1. /devices/statistics 返回 405（需要重启后端）
echo 2. /notification/list 返回 404（需要创建表+重启后端）
echo.

echo [1/2] 检查数据库连接...
docker exec iot-mysql mysql -uroot -proot123456 -e "SELECT 1" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 数据库连接失败！请确保Docker容器 iot-mysql 正在运行
    pause
    exit /b 1
)
echo ✅ 数据库连接正常

echo.
echo [2/2] 创建通知表...
docker exec -i iot-mysql mysql -uroot -proot123456 iot_platform < migration_add_notification_table.sql
if %errorlevel% neq 0 (
    echo ❌ 创建表失败！
    pause
    exit /b 1
)
echo ✅ 数据库表创建成功

echo.
echo ========================================
echo 数据库表创建完成！
echo ========================================
echo.
echo ⚠️  重要：必须重启后端服务才能生效！
echo.
echo 下一步：
echo 1. 停止当前运行的后端服务
echo 2. 重新启动后端服务
echo     - 如果使用JAR：java -jar backend/target/iot-platform.jar
echo     - 如果使用IDE：重新运行 IotPlatformApplication
echo 3. 等待后端启动完成（查看日志确认无错误）
echo 4. 刷新前端页面（Ctrl+F5强制刷新）
echo 5. 测试功能：
echo    - 首页应该能正常加载统计数据
echo    - 点击通知图标应该能正常显示通知列表
echo.
pause
