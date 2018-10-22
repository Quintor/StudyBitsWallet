#!/bin/sh

LIBINDY_VERSION=1.6.6

wget -O build/tmp.zip "https://repo.sovrin.org/android/libindy/stable/$LIBINDY_VERSION/libindy_android_x86_$LIBINDY_VERSION.zip" 
unzip build/tmp.zip -d build/ 
cp $(find build/libindy_x86 -name libindy.so) app/src/main/jniLibs/x86 
