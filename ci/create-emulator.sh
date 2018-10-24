#!/bin/sh
echo no | android create avd --force -n test -t google_apis-24 --abi armeabi-v7a
emulator -avd test  -no-window &


