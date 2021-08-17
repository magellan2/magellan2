#! /bin/sh

# The following call can be used to run the installer in debug mode.
# In debug mode, exceptions are printed directly to stderr.
#
# *Important*: please set the working directory to the directory where this
#              shell script is located, otherwise the installer will not work.
#
# 
# The JAR file user.jar contains your custom code.
# If you start the installer from your IDE, you can remove the user.jar 
# classpath entry to execute your latest code.
#
# To test the unattended installation, please pass the argument -q 

java @VM_PARAMETERS@ -cp 'i4jruntime.jar:user.jar:user/*' -Dinstall4j.debug=true -Dinstall4j.logToStderr=true com.install4j.runtime.installer.Installer @ARGUMENTS@