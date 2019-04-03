#!/bin/bash
set -euxo pipefail

./gradlew clean fatJar

cp ./build/libs/*-all-*.jar ./api_config.jar
