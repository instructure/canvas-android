#!/bin/bash
set -euxo pipefail

./gradlew clean assemble fatJar

cp ./build/libs/cluster_create-all-*.jar ./cluster_create.jar
