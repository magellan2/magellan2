@echo off

REM The following call can be used to run the installer in debug mode.
REM In debug mode, exceptions are printed directly to stderr.
REM
REM *Important*: please set the working directory to the directory where this
REM              batch file is located, otherwise the installer will not work.
REM
REM 
REM The JAR file user.jar contains your custom code.
REM If you start the installer from your IDE, you can remove the user.jar 
REM classpath entry to execute your latest code.
REM
REM To test the unattended installation, please pass the argument -q 

java @VM_PARAMETERS@ -cp i4jruntime.jar;user.jar;user\* -Dinstall4j.debug=true -Dinstall4j.logToStderr=true com.install4j.runtime.installer.Installer @ARGUMENTS@

pause