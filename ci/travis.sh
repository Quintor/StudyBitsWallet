#!/bin/bash
set -e 
sh ci/download-deps.sh
echo "LOCAL IP: $TEST_POOL_IP"
echo "ENDPOINT_IP=\"$TEST_POOL_IP\"" > gradle.properties

# Pre-assemble the artifact before starting runtime dependencies
./gradlew assemble

