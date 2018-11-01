#!/bin/bash
set -e 
bash ci/download-deps.sh
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

