#!/bin/sh

LIBINDY_VERSION=1.6.6
ZIPFILE="libindy_android_x86_$LIBINDY_VERSION.zip"

mkdir -p build
if [ ! -d "build/libindy_x86" ]; then
    echo "Downloading zipfile"
    wget -O "build/$ZIPFILE" "https://repo.sovrin.org/android/libindy/stable/$LIBINDY_VERSION/$ZIPFILE" 
    unzip build/$ZIPFILE -d build/ 
fi
cp $(find build/libindy_x86 -name libindy.so) app/src/main/jniLibs/x86 
