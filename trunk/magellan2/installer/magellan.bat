@echo off
if not exist "%JAVA_HOME%\bin\javaw.exe" goto noJavaHome
start "%JAVA_HOME%\bin\javaw.exe" -Xmx400m -Xms400m -jar "magellan-client.jar" %1
goto eof

:noJavaHome
start javaw -Xmx400m -Xms400m -jar "magellan-client.jar" %1
goto eof


:eof