@echo off
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: This is no longer the preferred way to start Magellan!
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

echo ------------------------------------------------------
echo This is no longer the preferred way to start Magellan!
echo Use magellan_launcher.exe
echo If you need to use vm options like -Xmx1000m, edit the file magellan_launcher.vmoptions.
echo ------------------------------------------------------

.\magellan_launcher.exe

pause

goto eof


if not exist "%JAVA_HOME%\bin\javaw.exe" goto noJavaHome
echo JavaHome
start "Magellan" "%JAVA_HOME%\bin\javaw.exe" -Xmx800m -jar "magellan-client.jar" %1
goto eof

:noJavaHome
echo NoJavaHome
start javaw -Xmx1200m -jar "magellan-client.jar" %1
goto eof

:eof