@echo off
setlocal

IF NOT EXIST "%JAVA_HOME%" goto missingjava

set BINDIR=%~dp0
set OUTDIR=%BINDIR%out
IF NOT EXIST "%OUTDIR%" goto missingout
:continue

"%JAVA_HOME%/bin/java" -cp lib/JavaOCR.jar;lib/jai_imageio.jar;lib/filterbuilder.jar;lib/htmllexer.jar;lib/htmlparser.jar;out/. BoardDownloader
goto end

:missingjava
echo You must install JAVA SDK, see http://java.sun.com/

:missingout
echo First run compile.bat!
goto continue

:end
pause