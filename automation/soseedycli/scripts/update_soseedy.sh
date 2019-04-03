#!/usr/bin/env bash

DIR=`dirname "$BASH_SOURCE"`

SOSEEDY="$DIR/.."

"$SOSEEDY/gradlew" -p "$SOSEEDY" clean fatJar

cp "$SOSEEDY/build/libs/soseedycli-0.1.jar" "$DIR/soseedycli.jar"

TOOLS="$DIR/../../tools"
cp "$SOSEEDY/build/libs/soseedycli-0.1.jar" "$TOOLS/soseedycli.jar"
