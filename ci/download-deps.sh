#!/bin/bash

function download_dep_for_arch() {
    echo "ARCH: $1"
    ARCH="$1"
    echo "ARCH: $ARCH"
    LIBINDY_VERSION=1.6.6
    ZIPFILE=libindy_android_"$ARCH"_"$LIBINDY_VERSION".zip

    echo "$ZIPFILE"
    mkdir -p build
    if [ ! -d "build/libindy_$ARCH" ]; then
        echo "Downloading zipfile"
        wget -O "build/$ZIPFILE" "https://repo.sovrin.org/android/libindy/stable/$LIBINDY_VERSION/$ZIPFILE" 
        unzip build/$ZIPFILE -d build/ 
    fi
    cp $(find build/libindy_$ARCH -name libindy.so) "app/src/main/jniLibs/$ARCH"
}

download_dep_for_arch x86
download_dep_for_arch x86_64
