#!/bin/bash

# A script to undo the damage caused by pact-jvm-consumer's encoding of various elements in the produced json file.
echo "arg0 = $0"
echo "arg1 = $1"
inputfile=$1

sed -i ".bak" 's/\\u003d/=/g' "$inputfile"
sed -i ".bak" 's/, /\&/g' "$inputfile"
sed -i ".bak" 's/\\u0026/\&/g' "$inputfile"
sed -i ".bak" 's/%2C+/\&/g' "$inputfile"
sed -i ".bak" 's/%5B%5D%3D/\[\]=/g' "$inputfile"

