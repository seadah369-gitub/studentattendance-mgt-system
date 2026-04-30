@echo off
echo ============================================
echo  Student Attendance Management System
echo ============================================
echo.

:: Set Java 21 path
set "JAVA_HOME=C:\Users\hp\.jdk\jdk-21.0.10"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using Java: %JAVA_HOME%
echo.

:: Check if jar already exists
if exist "target\StudentAttendanceSystem-1.0.0-jar-with-dependencies.jar" (
    echo JAR found. Skipping build...
    goto RUN
)

:: Build
echo Building project...
call mvn clean package -q
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo BUILD FAILED. Make sure Maven is installed.
    echo Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

:RUN
echo Starting application...
java -jar target\StudentAttendanceSystem-1.0.0-jar-with-dependencies.jar
pause
