#!/bin/sh
cd "$(dirname "$0")"
java -jar "Uninstaller/uninstaller.jar" > "$HOME/magellan.uninstall.log"
sleep 10
