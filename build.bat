@echo off
echo Building Intelligent Ad Allocation Engine...

REM Find MySQL connector jar (check for common versions)
set CONNECTOR_JAR=
for %%f in (mysql-connector*.jar mysql-connector*) do (
    set CONNECTOR_JAR=%%f
    goto found_jar
)

:found_jar
if "%CONNECTOR_JAR%"=="" (
    echo Warning: No MySQL connector found - compilation may fail
    set CONNECTOR_JAR=mysql-connector-java-8.0.11.jar
)

echo Using connector: %CONNECTOR_JAR%

REM Create output directory
if not exist "build" mkdir build
if not exist "build\classes" mkdir build\classes

echo Compiling Java files...

REM Compile all Java files
javac -cp "%CONNECTOR_JAR%" -d build\classes ^
    app\*.java ^
    auth\*.java ^
    cli\*.java ^
    context\*.java ^
    dsa\heap\*.java ^
    dsa\graph\*.java ^
    dsa\trie\*.java ^
    dsa\slidingwindow\*.java ^
    engine\*.java ^
    exceptions\*.java ^
    logs\*.java ^
    manager\*.java ^
    model\*.java ^
    repository\*.java ^
    service\*.java ^
    utils\*.java ^
    database\*.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo To run the application:
    echo java -cp "build\classes;%CONNECTOR_JAR%" app.Main
) else (
    echo Compilation failed!
    pause
    exit /b 1
)

pause
