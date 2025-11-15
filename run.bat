@echo off
REM Edit the connector name if needed
set JAR=lib\mysql-connector-java-8.0.xx.jar
javac -cp .;%JAR% src\*.java
if %ERRORLEVEL% NEQ 0 pause && exit /B %ERRORLEVEL%
java -cp .;%JAR% FinanceApp
pause
