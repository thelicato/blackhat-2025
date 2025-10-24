#!/usr/bin/env bash

###############################################################
# Place the apk in this folder and name it 'bh-demo-flag.apk' #
###############################################################

PACKAGE_NAME="com.blackhat.multistep"
APK_FILE="bh-demo-flag.apk"

echo "Installing app..."
while true; do
    adb install ./$APK_FILE
    if adb shell pm list packages | grep -xq "package:$PACKAGE_NAME"; then
        echo "Install command succeeded!"
        break
    else
        echo "Install command failed. Retrying in 3 seconds..."
        sleep 3
    fi
done