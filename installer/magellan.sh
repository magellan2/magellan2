#!/bin/sh
##############################################################################
## This is no longer the preferred way to start Magellan!
##############################################################################

echo "------------------------------------------------------"
echo "This is no longer the preferred way to start Magellan!"
echo "Use magellan_launcher"
echo "If you need to use vm options like -Xmx1000m, edit the file magellan_launcher.vmoptions."
echo "------------------------------------------------------"
echo

# The magellan_launcher is not included in the release ZIP.
#./magellan_launcher
#exit

# Running Magellan with the java command works perfectly.
cd "$(dirname "$0")"
java -Xmx1200m -jar "magellan-client.jar" "$@"
