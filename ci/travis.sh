#!/bin/bash
set -e 
sh ci/download-deps.sh
echo "LOCAL IP: $TEST_POOL_IP"
echo "ENDPOINT_IP=\"$TEST_POOL_IP\"" > gradle.properties


cd StudyBits
docker build -t quindy:latest quindy/
docker run -v $HOME/.m2/:/root/m2 quindy:latest sh -c "mvn install -DskipTests && cp -r /root/.m2/repository /root/m2"

cd ..
./gradlew assemble
cd StudyBits
docker-compose up -d --build --force-recreate pool university-agent-rug university-agent-gent
if [ -n "$IN_TRAVIS" ]; then
    android-wait-for-emulator
fi
adb shell input keyevent 82 &

echo "Watining for dockers to start"
while [ -n "$(docker ps -a | grep starting)" ]; 
do
    sleep 1
done 

echo "Dockers are booted"


adb shell input keyevent 82 &
cd ..
./gradlew connectedCheck