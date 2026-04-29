@echo off
chcp 65001 >nul
setlocal

if "%XINGGUI_MODE%"=="" set XINGGUI_MODE=local-demo

echo ========================================
echo XingGui backend local demo launcher
echo ========================================
echo Backend URL : http://127.0.0.1:8080
echo Android URL : http://10.0.2.2:8080
echo Database    : 127.0.0.1:3306/xinggui
echo DB user     : %XINGGUI_DB_USER%  (empty means root)
echo Mode        : %XINGGUI_MODE%
echo Stop        : Press Ctrl+C in this window
echo.
echo Tip: Gradle showing "83%% EXECUTING" means the backend is running.
echo ========================================
echo.

call gradlew.bat :backend:run

endlocal
