@echo off
echo Cleaning previous builds...
if exist out_server rmdir /S /Q out_server
if exist build_jar_server rmdir /S /Q build_jar_server
mkdir out_server
mkdir build_jar_server

echo Compiling Java source files...
javac --release 17 -encoding utf-8 -cp "lib/*" -d out_server MainServer.java server\commands\*.java server\managers\*.java models\*.java util\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b %errorlevel%
)

echo Extracting dependencies...
cd build_jar_server
for %%f in (..\lib\*.jar) do jar xf "%%f"

echo Copying compiled classes and resources...
xcopy /E /Y ..\out_server\* . >nul
if exist ..\log4j2.xml copy ..\log4j2.xml . >nul

echo Removing dependency signatures to prevent security exceptions...
if exist META-INF\*.SF del /Q META-INF\*.SF
if exist META-INF\*.DSA del /Q META-INF\*.DSA
if exist META-INF\*.RSA del /Q META-INF\*.RSA

echo Creating manifest...
echo Main-Class: MainServer> manifest.txt
echo.>> manifest.txt

echo Creating fat jar...
jar cfm ..\server.jar manifest.txt *

cd ..
echo Cleaning up temporary files...
rmdir /S /Q out_server
rmdir /S /Q build_jar_server

echo Build complete! The single jar file is server.jar
