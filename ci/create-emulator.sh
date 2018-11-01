#!/bin/sh
set -e
echo no | android create avd --force -n test -t android-24 --abi armeabi-v7a
emulator -avd test -netdelay none -netspeed full -no-window &
#android-wait-for-emulator
sleep 5
