@echo off

REM The following call can be used to run the uninstaller in debug mode.
REM In debug mode, exceptions are printed directly to stderr.
REM
REM *Important*: please copy this batch file to the installation directory and
REM              set the working directory there, otherwise the uninstaller will
REM              not work.
REM
REM 
REM The JAR file user.jar contains your custom code.
REM If you start the uninstaller from your IDE, you can remove the user.jar 
REM classpath entry to execute your latest code.
REM
REM To test the unattended uninstallation, please pass the argument -q 

IF NOT exist .install4j\i4jruntime.jar goto error
cd .install4j 

echo *********************************************************
echo Please note that some files cannot be deleted by the 
echo debug uninstaller.
echo *********************************************************

java @VM_PARAMETERS@ -cp i4jruntime.jar;user.jar;user\* -Dinstall4j.debug=true -Dinstall4j.logToStderr=true com.install4j.runtime.installer.Uninstaller @ARGUMENTS@

pause
exit

:error

echo *********************************************************
echo ERROR: Please copy this batch file to the installation
echo directory and start it there.
echo *********************************************************
pause
