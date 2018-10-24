#!/bin/sh
set -e
echo no | android create avd --force -n test -t android-24 --abi google_apis/armeabi-v7a
emulator -avd test  -no-window &


