@echo off
chcp 65001 >nul
echo ========================================
echo 星轨后端服务启动脚本
echo ========================================
echo.
echo 正在启动后端服务...
echo 端口: 8080
echo 数据库: xinggui
echo.
echo 请保持此窗口打开，不要关闭！
echo 按 Ctrl+C 可以停止后端服务
echo ========================================
echo.

gradlew.bat :backend:run
