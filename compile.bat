@echo off
setlocal

IF NOT EXIST "%JAVA_HOME%" goto missingjava

set BINDIR=%~dp0
set OUTDIR=%BINDIR%out
IF NOT EXIST "%OUTDIR%" goto createdirs
:continue

javac -cp lib/JavaOCR.jar;lib/jai_imageio.jar src/*.java -d out
goto end

:missingjava
echo You must install JAVA SDK, see http://java.sun.com/

:createdirs
md %OUTDIR%
goto continue

:end
