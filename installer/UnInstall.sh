#!/bin/bash
cd "$(dirname "$0")"
java -jar Uninstaller/uninstaller.jar > $HOME/magellan.uninstall.log
