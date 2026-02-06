@echo off
echo ========================================
echo   学习平台系统启动脚本
echo ========================================
echo.

echo [1/3] 检查端口占用情况...
netstat -an | find "8084"
if %ERRORLEVEL% == 0 (
    echo.
    echo ❌ 端口8084已被占用 !
    echo 请先停止占用端口8084的进程
    echo 检查: netstat -ano | find "8084"
    echo.
    goto :end
)

echo.
echo [2/3] 启动后端服务 (端口8084)...
start /B mvnw spring-boot:run
echo 后端服务启动中，请等待30秒...
timeout /t 30 /nobreak > nul

echo.
echo [3/3] 检查后端服务状态...
netstat -an | find "8084"
if %ERRORLEVEL% == 0 (
    echo ✅ 后端服务启动成功 !
    echo.
    echo ========================================
    echo   系统启动完成！
    echo ========================================
    echo.
    echo 🌐 前端访问地址: http://localhost:3000
    echo 🔌 API接口地址: http://localhost:8084
    echo.
    echo 测试账户:
    echo   邮箱: test@example.com
    echo   密码: 123456
    echo.
) else (
    echo ❌ 后端服务启动失败 !
    echo 请检查日志文件: backend.log
)

:end
echo.
pause