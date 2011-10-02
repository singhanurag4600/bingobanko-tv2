@echo off
setlocal

IF NOT EXIST "%JAVA_HOME%" goto missingjava

set BINDIR=%~dp0
set OUTDIR=%BINDIR%out
IF NOT EXIST "%OUTDIR%" goto createdirs
:continue

"%JAVA_HOME%/bin/javac" -cp lib/JavaOCR.jar;lib/jai_imageio.jar;lib/filterbuilder.jar;lib/htmllexer.jar;lib/htmlparser.jar src/*.java -d out
echo SUCCESS! Du kan nu starte programmet "downloader.bat" for at hente plader,
echo og "gameclient.bat" n†r der er bingobanko i TV'et.
pause

goto end

:missingjava
echo You must install JAVA SDK, see http://java.sun.com/

:createdirs
md %OUTDIR%
goto continue

:end
