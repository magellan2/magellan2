#!/bin/sh
##############################################################################
## Starts magellan with appropriate parameters
## You can adjust the amount of memory for magellan by changing the number
## after -Xmx
##############################################################################

cd "$(dirname "$0")"
java -Xmx800m -jar "magellan-client.jar" "$@"
