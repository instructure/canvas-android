#!/bin/bash

set -euxo pipefail

# cfssl is a TLS toolkit from cloudflare.
# cfssl is actively developed with over 100 contributors
# https://github.com/cloudflare/cfssl
# https://blog.cloudflare.com/how-to-build-your-own-public-key-infrastructure/
# https://coreos.com/os/docs/latest/generate-self-signed-certificates.html
go get -u github.com/cloudflare/cfssl/cmd/cfssl
go get -u github.com/cloudflare/cfssl/cmd/cfssljson

# cfssl print-defaults config > ca-config.json # manually edited
# cfssl print-defaults csr > ca-csr.json # manually edited
# cfssl print-defaults csr > server-csr.json # manually edited
# cfssl print-defaults csr > client-csr.json # manually edited

# See mutual auth generation script:
# https://github.com/grpc/grpc-java/tree/master/examples

# Generate certs in the secrets folder
PRIVATE_DIR="../../../private-data/soseedygrpc"
cp *.json "$PRIVATE_DIR"
cd $PRIVATE_DIR
rm -rf *.crl *.key *.crt *.csr *.pem

# CA
cfssl gencert -initca ca-csr.json | cfssljson -bare ca
mv ca.pem ca.crt
openssl pkcs8 -topk8 -nocrypt -in ca-key.pem -out ca.pem

# Server
cfssl gencert -ca=ca.crt -ca-key=ca-key.pem -config=ca-config.json -profile=server server-csr.json | cfssljson -bare server
mv server.pem server.crt
openssl pkcs8 -topk8 -nocrypt -in server-key.pem -out server.pem

# Client
cfssl gencert -ca=ca.crt -ca-key=ca-key.pem -config=ca-config.json -profile=client client-csr.json | cfssljson -bare client
mv client.pem client.crt
openssl pkcs8 -topk8 -nocrypt -in client-key.pem -out client.pem

# netty only supports PKCS8 keys. openssl is used to convert from PKCS1 to PKCS8
# http://netty.io/wiki/sslcontextbuilder-and-private-key.html
