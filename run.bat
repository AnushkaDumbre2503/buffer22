@echo off
echo Starting Intelligent Ad Allocation Engine...

REM Check if build exists
if not exist "build\classes\app\Main.class" (
    echo Application not compiled yet!
    echo Please run build.bat first to compile the application.
    pause
    exit /b 1
)

REM Find MySQL connector jar (check for common versions)
set CONNECTOR_JAR=
for %%f in (mysql-connector*.jar mysql-connector*) do (
    set CONNECTOR_JAR=%%f
    goto found_jar
)

:found_jar
if "%CONNECTOR_JAR%"=="" (
    echo Warning: No MySQL connector found!
    set CONNECTOR_JAR=mysql-connector-java-8.0.11.jar
)

echo Using connector: %CONNECTOR_JAR%

REM Run the application
java -cp "build\classes;%CONNECTOR_JAR%" app.Main

pause
