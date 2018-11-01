#!/bin/bash

adb logcat *:S STUDYBITS:V TestRunner:V & LOGCAT_PID=$!;
./gradlew connectedCheck --info ;
if [ -n "$LOGCAT_PID" ] ; then kill $LOGCAT_PID; fi
