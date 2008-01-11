#!/bin/sh
cd "$(dirname "$0")"
java -Xmx400m -Xms200m -jar "magellan-client.jar" "$@"
