@echo off
echo Cleaning previous builds...
if exist out_client rmdir /S /Q out_client
if exist build_jar_client rmdir /S /Q build_jar_client
mkdir out_client
mkdir build_jar_client

echo Compiling Java source files...
javac -encoding utf-8 -cp "lib/*" -d out_client MainClient.java client\managers\*.java models\*.java util\*.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b %errorlevel%
)

echo Extracting dependencies...
cd build_jar_client
for %%f in (..\lib\*.jar) do jar xf "%%f"

echo Copying compiled classes and resources...
xcopy /E /Y ..\out_client\* . >nul
if exist ..\log4j2.xml copy ..\log4j2.xml . >nul

echo Removing dependency signatures to prevent security exceptions...
if exist META-INF\*.SF del /Q META-INF\*.SF
if exist META-INF\*.DSA del /Q META-INF\*.DSA
if exist META-INF\*.RSA del /Q META-INF\*.RSA

echo Creating manifest...
echo Main-Class: MainClient> manifest.txt
echo.>> manifest.txt

echo Creating fat jar...
jar cfm ..\client.jar manifest.txt *

cd ..
echo Cleaning up temporary files...
rmdir /S /Q out_client
rmdir /S /Q build_jar_client

echo Build complete! The single jar file is client.jar
