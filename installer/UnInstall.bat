@echo off
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: starts izpack's uninstaller with administrator rights
:: 
:: implementation remark: once izpack 5.0 implements the 
:: runAsAdministrator attribute for the shortcut tag, we can get rid of
:: the first part of the script.
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
echo Trying to uninstall magellan.
echo You will probably asked to grant adminstration rights to cmd.exe.
echo Please accept.
echo
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: BatchGotAdmin (Run as Admin code starts)
REM --> Check for permissions
>nul 2>&1 "%SYSTEMROOT%\system32\cacls.exe" "%SYSTEMROOT%\system32\config\system"
REM --> If error flag set, we do not have admin.
if '%errorlevel%' NEQ '0' (
echo Requesting administrative privileges...
goto UACPrompt
) else ( goto gotAdmin )
:UACPrompt
echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
echo UAC.ShellExecute "%~s0", "", "", "runas", 1 >> "%temp%\getadmin.vbs"
"%temp%\getadmin.vbs"
exit /B
:gotAdmin
if exist "%temp%\getadmin.vbs" ( del "%temp%\getadmin.vbs" )
pushd "%CD%"
CD /D "%~dp0"
:: BatchGotAdmin (Run as Admin code ends)
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

echo Starting uninstall.jar

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
if "%OS%"=="Windows_NT" GOTO WinNT
GOTO Win9x

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:WinNT
start javaw -Dswing.noxp=true -jar Uninstaller/uninstaller.jar > uninstall.log
goto END

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:Win9x
javaw -Dswing.noxp=true -jar Uninstaller/uninstaller.jar

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:END

pause -1
::eof
