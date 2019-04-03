# Cluster Create

- Visit Kubernetes clusters page. Replace `PROJECT_ID` with your project id.
  - https://console.cloud.google.com/kubernetes/list?project=PROJECT_ID
- Press `CREATE CLUSTER` button. Configure as desired
- Scroll to bottom. Click on `REST` in `Equivalent REST or command line`
- Copy the `Equivalent REST request` text and save as `clusters.txt`
- Build the jar with `./build.sh`
- Deploy with `java -jar cluster_create.jar`

Note that GCloud CLI must be installed and authorized. Here's how that works in CI using a service account.

```
#!/usr/bin/env bash
set -e

KEY_FILE="$HOME/.config/gcloud/application_default_credentials.json"

echo "$GCLOUD_KEY" | base64 --decode > "$KEY_FILE"

gcloud config set project "$PROJECT_ID"
gcloud auth activate-service-account --key-file "$KEY_FILE"
```

# links

- https://cloud.google.com/sdk/gcloud/reference/container/clusters/create
- https://cloud.google.com/sdk/gcloud/reference/alpha/container/clusters/create
- https://cloud.google.com/kubernetes-engine/docs/reference/rest/?hl=en_US
- https://developers.google.com/api-client-library/java/apis/container/v1
- http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.google.apis%22%20AND%20a%3A%22google-api-services-container%22
- https://github.com/bootstraponline/gcloud_cli/blob/master/google-cloud-sdk/lib/googlecloudsdk/third_party/apis/container_v1.json
