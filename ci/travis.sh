#!/bin/bash
set -e 
sh ci/download-deps.sh
echo "LOCAL IP: $TEST_POOL_IP"
echo "ENDPOINT_IP=\"$TEST_POOL_IP\"" > gradle.properties

cd StudyBits
docker build -t quindy:latest quindy/

# Install the quindy artifact in the local m2 repo
docker run -v $HOME/.m2/:/root/m2 quindy:latest sh -c "mvn install -DskipTests && cp -r /root/.m2/repository /root/m2"


cd ..
# Pre-assemble the artifact before starting runtime dependencies
./gradlew assemble

