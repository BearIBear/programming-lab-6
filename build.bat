@echo off
echo Cleaning previous builds...
if exist out rmdir /S /Q out
if exist build_jar rmdir /S /Q build_jar
mkdir out
mkdir build_jar

echo Compiling Java source files...
javac -encoding utf-8 -cp "lib/*" -d out Main.java commands\*.java managers\*.java models\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b %errorlevel%
)

echo Extracting dependencies...
cd build_jar
jar xf ..\lib\gson-2.13.2.jar
jar xf ..\lib\jansi-2.4.2.jar
jar xf ..\lib\jline-3.30.7.jar

echo Copying compiled classes...
xcopy /E /Y ..\out\* . >nul

echo Removing dependency signatures to prevent security exceptions...
if exist META-INF\*.SF del /Q META-INF\*.SF
if exist META-INF\*.DSA del /Q META-INF\*.DSA
if exist META-INF\*.RSA del /Q META-INF\*.RSA

echo Creating manifest...
echo Main-Class: Main> manifest.txt
echo.>> manifest.txt

echo Creating fat jar...
jar cfm ..\app.jar manifest.txt *

cd ..
echo Cleaning up temporary files...
rmdir /S /Q out
rmdir /S /Q build_jar

echo Build complete! The single jar file is app.jar
