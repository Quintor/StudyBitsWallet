#!/bin/bash
set -e 
sh ci/download-deps.sh
echo "LOCAL IP: $TEST_POOL_IP"
echo "ENDPOINT_IP=\"$TEST_POOL_IP\"" > gradle.properties

echo "MEMORYCHECK1"
ps aux --sort -rss | head -20


cd StudyBits
docker build -t quindy:latest quindy/
docker run -v $HOME/.m2/:/root/m2 quindy:latest sh -c "mvn install -DskipTests && cp -r /root/.m2/repository /root/m2"

echo "MEMORYCHECK2"
ps aux --sort -rss | head -20


cd ..
./gradlew assemble

echo "MEMORYCHECK3"
ps aux --sort -rss | head -20

cd StudyBits
docker-compose up -d --build --force-recreate pool university-agent-rug university-agent-gent

echo "MEMORYCHECK4"
ps aux --sort -rss | head -20


echo "Ran docker-compose up"
travis_wait android-wait-for-emulator
echo "Waited for emulator"
travis_wait adb shell input keyevent 82 &

echo "MEMORYCHECK5"
ps aux --sort -rss | head -20


echo "Waiting for dockers to start"
while [ -n "$(docker ps -a | grep starting)" ]; 
do
    echo $(docker ps -a | grep starting)
    sleep 5
done 

echo "Dockers are booted"

echo "MEMORYCHECK6"
ps aux --sort -rss | head -20


adb shell input keyevent 82 &
cd ..
echo "repeating to check if we get here due to log buffer issues"
echo "Starting test"
echo "Starting test"
echo "Starting test"
echo "Starting test"
echo "Starting test"
echo "Starting test"
echo "Starting test"
echo "Starting test"
echo "Starting test"
echo "Starting test"
echo "Starting test"
echo "Starting test"
travis_wait 30 ./gradlew connectedCheck
