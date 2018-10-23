#!/bin/bash
set -e 
sh download-deps.sh


ip -o addr show
export TEST_POOL_IP=$(ip -o addr show | grep -E "eth0.*inet " | grep -E -o  -e "[0-9]*(\.[0-9]*){3}" | head -1)
echo "LOCAL IP: $TEST_POOL_IP"

echo "ENDPOINT_IP=\"$TEST_POOL_IP\"" > gradle.properties

./gradlew assemble

cd StudyBits
docker build -t quindy:latest quindy/
docker-compose up -d --build --force-recreate pool university-agent-rug university-agent-gent

docker run -v $HOME/.m2/:/root/m2 quindy:latest sh -c "mvn install -DskipTests && cp -r /root/.m2/repository /root/m2"

echo "Watining for dockers to start"
while [ -n "$(docker ps -a | grep starting)" ]; 
do
    sleep 1
done 

echo "Dockers are booted"

android-wait-for-emulator

cd ..
./gradlew connectedCheck
