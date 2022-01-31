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

./magellan_launcher

exit

cd "$(dirname "$0")"
java -Xmx1200m -jar "magellan-client.jar" "$@"
