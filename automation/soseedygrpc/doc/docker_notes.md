## Docker notes

Check no random docker env vars are set
- `env | grep -i DOCKER_`

- Authenticate to gcloud container registry
 - https://cloud.google.com/container-registry/docs/quickstart
 - `gcloud auth configure-docker`
- Build the jar
  - `./gradlew fatJar`
- Build the image
  - `docker build -t gcr.io/delta-essence-114723/soseedy .`
- Publish the image
  - `docker push gcr.io/delta-essence-114723/soseedy`
- Run with
  - `docker run -p 50051:50051 gcr.io/delta-essence-114723/soseedy`
- Verify port with
  - `nc -w 5 -z localhost 50051`
