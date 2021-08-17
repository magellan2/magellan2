#! /bin/sh

# The following call can be used to run the uninstaller in debug mode.
# In debug mode, exceptions are printed directly to stderr.
#
# *Important*: please copy this shell script to the installation directory and
#              set the working directory there, otherwise the uninstaller will
#              not work.
#
# 
# The JAR file user.jar contains your custom code.
# If you start the uninstaller from your IDE, you can remove the user.jar 
# classpath entry to execute your latest code.
#
# To test the unattended uninstallation, please pass the argument -q 

cd .install4j

if [ $? -ne 0 ]; then
    echo '*********************************************************'
    echo ERROR: Please copy this batch file to the installation
    echo directory and start it there.
    echo '*********************************************************'
    exit
fi

echo '*********************************************************'
echo Please note that some files cannot be deleted by the 
echo debug uninstaller.
echo '*********************************************************'

java @VM_PARAMETERS@ -cp 'i4jruntime.jar:user.jar:user/*' -Dinstall4j.debug=true -Dinstall4j.logToStderr=true com.install4j.runtime.installer.Uninstaller @ARGUMENTS@
