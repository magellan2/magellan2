@echo off
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Starts magellan with appropriate parameters
:: You can adjust the amount of memory for magellan by changing the number
:: after -Xmx
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

if not exist "%JAVA_HOME%\bin\javaw.exe" goto noJavaHome
start "Magellan" "%JAVA_HOME%\bin\javaw.exe" -Xmx800m -jar "magellan-client.jar" %1
goto eof

:noJavaHome
start javaw -Xmx800m -jar "magellan-client.jar" %1
goto eof

:eof