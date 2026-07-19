@echo off
setlocal EnableExtensions

set "BASE_DIR=%~dp0"
set "PROPERTIES_FILE=%BASE_DIR%.mvn\wrapper\maven-wrapper.properties"

if not exist "%PROPERTIES_FILE%" (
  echo Missing Maven Wrapper properties: "%PROPERTIES_FILE%" 1>&2
  exit /b 1
)

for /f "tokens=1,* delims==" %%A in ('findstr /b "distributionUrl=" "%PROPERTIES_FILE%"') do set "DISTRIBUTION_URL=%%B"
if "%DISTRIBUTION_URL%"=="" (
  echo distributionUrl is missing from "%PROPERTIES_FILE%" 1>&2
  exit /b 1
)

for %%A in ("%DISTRIBUTION_URL%") do set "ARCHIVE_NAME=%%~nxA"
set "DISTRIBUTION_NAME=%ARCHIVE_NAME:.zip=%"
set "MAVEN_DIRECTORY_NAME=%DISTRIBUTION_NAME:-bin=%"
if "%MAVEN_USER_HOME%"=="" set "MAVEN_USER_HOME=%USERPROFILE%\.m2"
set "WRAPPER_HOME=%MAVEN_USER_HOME%\wrapper\dists\%DISTRIBUTION_NAME%"
set "MAVEN_HOME=%WRAPPER_HOME%\%MAVEN_DIRECTORY_NAME%"

if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runMaven

mkdir "%WRAPPER_HOME%" 2>nul
set "ARCHIVE=%WRAPPER_HOME%\%ARCHIVE_NAME%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing -Uri '%DISTRIBUTION_URL%' -OutFile '%ARCHIVE%'"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force -Path '%ARCHIVE%' -DestinationPath '%WRAPPER_HOME%'"
if errorlevel 1 exit /b 1

:runMaven
call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
