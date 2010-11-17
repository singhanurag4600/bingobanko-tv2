@echo off
setlocal

IF NOT EXIST "%JAVA_HOME%" goto missingjava

set BINDIR=%~dp0
set OUTDIR=%BINDIR%out
IF NOT EXIST "%OUTDIR%" goto missingout
:continue

java -cp lib/JavaOCR.jar;lib/jai_imageio.jar;out/. GameApp
goto end

:missingjava
echo You must install JAVA SDK, see http://java.sun.com/

:missingout
echo First run compile.bat!
goto continue

:end
