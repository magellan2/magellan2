#!/bin/sh
cd "$(dirname "$0")"
java -Xmx800m -jar "magellan-client.jar" "$@"
