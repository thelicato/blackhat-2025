#!/usr/bin/env bash

###############################################################
# Place the apk in this folder and name it 'bh-demo-flag.apk' #
###############################################################

adb shell pm uninstall com.blackhat.multistep
./setup.sh