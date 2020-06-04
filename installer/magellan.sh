#!/bin/sh
##############################################################################
## Starts magellan with appropriate parameters
## You can adjust the amount of memory for magellan by changing the number
## after -Xmx (1200m means 1200 megabyte)
##############################################################################

cd "$(dirname "$0")"
java -Xmx1200m -jar "magellan-client.jar" "$@"
