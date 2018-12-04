#!/bin/sh
set -e
echo no | android create avd --force -n test -t android-24 --abi armeabi-v7a
emulator -avd test -netdelay none -netspeed full -no-window &
# Purposefully not waiting for emulator now, first going for other build stuff to give more startup time
# android-wait-for-emulator
