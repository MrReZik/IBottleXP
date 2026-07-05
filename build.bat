@echo off
setlocal EnableExtensions DisableDelayedExpansion

title IBottleXP Build

echo ============================================================
echo   IBottleXP Plugin - Auto Build
echo ============================================================
echo.

set "ROOT=%~dp0"
set "TOOLS=%ROOT%.tools"
set "MAVEN_DIR=%TOOLS%\maven"
set "JDK_DIR=%TOOLS%\jdk"

if not exist "%TOOLS%" mkdir "%TOOLS%"

:: ============================================================
:: 1. Check Java
:: ============================================================
echo [1/4] Checking Java 21+...

set "JAVA_CMD=java"
set "JAVA_HOME_SET="

if exist "%JDK_DIR%\bin\java.exe" (
    set "JAVA_CMD=%JDK_DIR%\bin\java.exe"
    set "JAVA_HOME=%JDK_DIR%"
    set "JAVA_HOME_SET=1"
    echo [OK] Local JDK found in .tools\jdk
    goto :java_ok
)

where java >nul 2>&1
if errorlevel 1 goto :java_download

java -version 2>&1 | findstr /r "version \"2[1-9]\." >nul
if not errorlevel 1 (
    echo [OK] System Java 21+ found.
    goto :java_ok
)

java -version 2>&1 | findstr /r "version \"[3-9][0-9]\." >nul
if not errorlevel 1 (
    echo [OK] System Java 21+ found.
    goto :java_ok
)

:java_download
echo [INFO] Java 21+ not found. Downloading Temurin JDK 21...
set "JDK_ZIP=%TOOLS%\jdk21.zip"
set "JDK_URL=https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.5+11/OpenJDK21U-jdk_x64_windows_hotspot_21.0.5_11.zip"

powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; (New-Object Net.WebClient).DownloadFile('%JDK_URL%','%JDK_ZIP%')"
if errorlevel 1 (
    echo [ERROR] Failed to download JDK. Install Java 21 manually: https://adoptium.net
    pause & exit /b 1
)

echo Extracting JDK...
powershell -NoProfile -Command "Expand-Archive -Path '%JDK_ZIP%' -DestinationPath '%TOOLS%\jdk_tmp' -Force"
if errorlevel 1 (
    echo [ERROR] Failed to extract JDK.
    pause & exit /b 1
)

for /d %%D in ("%TOOLS%\jdk_tmp\jdk-*") do (
    xcopy /E /I /Q "%%D" "%JDK_DIR%" >nul
)
rmdir /s /q "%TOOLS%\jdk_tmp"
del /q "%JDK_ZIP%"

set "JAVA_CMD=%JDK_DIR%\bin\java.exe"
set "JAVA_HOME=%JDK_DIR%"
set "JAVA_HOME_SET=1"
echo [OK] JDK 21 installed in .tools\jdk

:java_ok
echo.

:: ============================================================
:: 2. Check Maven
:: ============================================================
echo [2/4] Checking Maven...

set "MVN_CMD="

if exist "%MAVEN_DIR%\bin\mvn.cmd" (
    set "MVN_CMD=%MAVEN_DIR%\bin\mvn.cmd"
    echo [OK] Local Maven found in .tools\maven
    goto :maven_ok
)

where mvn >nul 2>&1
if not errorlevel 1 (
    set "MVN_CMD=mvn"
    echo [OK] System Maven found.
    goto :maven_ok
)

echo [INFO] Maven not found. Downloading Maven 3.9.9...
set "MVN_VER=3.9.9"
set "MVN_ZIP=%TOOLS%\maven.zip"
set "MVN_URL=https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"

powershell -NoProfile -Command "[Net.ServicePointManager]::SecurityProtocol='Tls12'; (New-Object Net.WebClient).DownloadFile('%MVN_URL%','%MVN_ZIP%')"
if errorlevel 1 (
    echo [ERROR] Failed to download Maven. Install manually: https://maven.apache.org
    pause & exit /b 1
)

echo Extracting Maven...
powershell -NoProfile -Command "Expand-Archive -Path '%MVN_ZIP%' -DestinationPath '%TOOLS%\maven_tmp' -Force"
if errorlevel 1 (
    echo [ERROR] Failed to extract Maven.
    pause & exit /b 1
)

for /d %%D in ("%TOOLS%\maven_tmp\apache-maven-*") do (
    xcopy /E /I /Q "%%D" "%MAVEN_DIR%" >nul
)
rmdir /s /q "%TOOLS%\maven_tmp"
del /q "%MVN_ZIP%"

set "MVN_CMD=%MAVEN_DIR%\bin\mvn.cmd"
echo [OK] Maven 3.9.9 installed in .tools\maven

:maven_ok
echo.

:: ============================================================
:: 3. Build
:: ============================================================
echo [3/4] Building plugin...
echo.

cd /d "%ROOT%"

if defined JAVA_HOME_SET (
    set "PATH=%JDK_DIR%\bin;%PATH%"
)

"%MVN_CMD%" clean package -q
if errorlevel 1 (
    echo.
    echo ============================================================
    echo   [ERROR] Build failed! Check output above for details.
    echo ============================================================
    pause & exit /b 1
)

:: ============================================================
:: 4. Copy JAR to project root
:: ============================================================
echo [4/4] Copying JAR...

set "JAR_SRC=%ROOT%target\IBottleXP-1.0.0.jar"
set "JAR_DEST=%ROOT%IBottleXP-1.0.0.jar"

if exist "%JAR_SRC%" (
    copy /Y "%JAR_SRC%" "%JAR_DEST%" >nul
    echo.
    echo ============================================================
    echo   [DONE] Build successful!
    echo.
    echo   Output: IBottleXP-1.0.0.jar
    echo.
    echo   Copy this file into your server's plugins\ folder
    echo   and restart the server.
    echo ============================================================
) else (
    echo [WARN] JAR not found at expected path: %JAR_SRC%
)

echo.
pause
