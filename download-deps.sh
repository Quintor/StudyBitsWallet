#!/bin/sh

LIBINDY_VERSION=1.6.6
ZIPFILE="libindy_android_x_86_$LIBINDY_VERSION.zip"
if [ ! -f "build/libindy_x86" ]; then
    echo "Downloading zipfile"
    wget -O "build/$ZIPFILE" "https://repo.sovrin.org/android/libindy/stable/$LIBINDY_VERSION/$ZIPFILE" 
    unzip build/$ZIPFILE -d build/ 
fi
cp $(find build/libindy_x86 -name libindy.so) app/src/main/jniLibs/x86 
