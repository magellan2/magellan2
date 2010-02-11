#!/bin/sh
cd "$(dirname "$0")"
java -Xmx400m -jar "magellan-client.jar" "$@"
