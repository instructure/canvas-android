#!/usr/bin/env bash

DIR=`dirname "$BASH_SOURCE"`

FLANK="$DIR/../../../flank/test_runner"

"$FLANK/gradlew" -p "$FLANK" clean assemble fatJar

cp "$FLANK"/build/libs/flank-*.jar "$DIR/flank.jar"
