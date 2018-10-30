#!/bin/sh
set -e
echo no | android create avd --force -n test -t android-24 --abi x86 -no-accel
emulator -avd test -netdelay none -netspeed full  -no-audio -no-window &
android-wait-for-emulator

