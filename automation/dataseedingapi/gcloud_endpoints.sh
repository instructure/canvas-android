#!/bin/bash

set -euxo pipefail

./gradlew --quiet generateProto
cp main.dsc api_descriptor.pb # required or gcloud will error
gcloud --quiet endpoints services deploy api_descriptor.pb api_config.yaml
