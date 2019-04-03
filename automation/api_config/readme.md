# Api Config

Automates generation of the `api_config.yml` required for deployment on cloud endpoints.

# Design

Google's protobuf parser expects compiled binaries from the protoc. There's no support for parsing `.proto` files dierctly.
Square has implemented a new Java proto file parser for Java that's able to parse uncompiled protobuf.

The API config project is responsible for walking the folder tree in `dataseedingapi/src/main/proto/`,
 enumerating all proto files, parsing out the services using `wired`, and finally generating the `api_config.yaml` file.

- https://stackoverflow.com/a/47635433
- https://github.com/square/wire

## Links

- https://cloud.google.com/endpoints/docs/grpc/get-started-grpc-kubernetes-engine
- https://cloud.google.com/endpoints/docs/grpc/configure-endpoints
