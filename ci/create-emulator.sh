#!/bin/sh
set -e
echo no | android create avd --force -n test -t android-24 --abi x86
emulator -avd test -netdelay none -netspeed full  -no-audio -no-window -no-accel &
android-wait-for-emulator

