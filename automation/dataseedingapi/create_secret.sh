#!/bin/bash

# SSL config

# https://cloud.google.com/endpoints/docs/openapi/enabling-ssl

# https://github.com/cloudendpoints/endpoints-tools/blob/599e12f787b148466f1dfecfb0306af3845426ba/start_esp/start_esp.py#L445

# parser.add_argument('-S', '--ssl_port', default=None, type=int, help='''
#   Expose a port for HTTPS requests.  Accepts both HTTP/1.x and HTTP/2
#   secure connections. Requires the certificate and key files
#   /etc/nginx/ssl/nginx.crt and /etc/nginx/ssl/nginx.key''')

# parser.add_argument('-t', '--tls_mutual_auth', action='store_true', help='''
#   Enable TLS mutual authentication for HTTPS backends.
#   Default value: Not enabled. Please provide the certificate and key files
#   /etc/nginx/ssl/backend.crt and /etc/nginx/ssl/backend.key.''')

# /etc/nginx/ssl/nginx.crt   - ca
# /etc/nginx/ssl/nginx.key   - ca
# /etc/nginx/ssl/backend.crt - server
# /etc/nginx/ssl/backend.key - server

SSL_FOLDER="../../private-data/soseedygrpc"

cp $SSL_FOLDER/ca.crt     ./nginx.crt
cp $SSL_FOLDER/ca.pem     ./nginx.key
cp $SSL_FOLDER/server.crt ./backend.crt
cp $SSL_FOLDER/server.pem ./backend.key

kubectl delete secret nginx-ssl > /dev/null
kubectl create secret generic nginx-ssl \
  --from-file=./nginx.crt \
  --from-file=./nginx.key \
  --from-file=./backend.crt \
  --from-file=./backend.key

# check with
# kubectl get secrets
