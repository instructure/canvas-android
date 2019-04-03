## iOS

When making changes to the API consult https://github.com/instructure/ios/blob/master/documentation/DATASEEDING.md to ensure that the data seeding API will continue to work for iOS UI automation.

---

Kubernetes doesn't work with latest tag:

- https://github.com/kubernetes/kubernetes/issues/33664

must write tool to refer to image as hash, then use apply to update
without losing the ephemeral IP address.

Google container API should allow discovering the latest image hash.

```
 - kubectl set image deployment/myapp myapp=image:<new_tag_version>
 - kubectl apply -f deployment_manifest.yaml
```

--

debug failed deployments:

```
kubectl get pod

kubectl logs esp-grpc-soseedy-548d44bbb4-q5x8x -c esp
kubectl logs esp-grpc-soseedy-548d44bbb4-q5x8x -c soseedy

Error from server (BadRequest): container "esp" in pod "esp-grpc-soseedy-6fc96fc676-7wvd6" is waiting to start: trying and failing to pull image
 ✘  ~/code/android-uno/automation/dataseedingapi   dockerfile ●  kubectl logs esp-grpc-soseedy-6fc96fc676-7wvd6 -c soseedy
Server started on port 50051
# https://console.cloud.google.com/gcr/images/endpoints-release/GLOBAL/endpoints-runtime?gcrImageListsize=50&gcrImageListsort=-uploaded

kubectl delete -f deployment_manifest.yaml
kubectl create -f deployment_manifest.yaml --save-config
kubectl apply -f deployment_manifest.yaml

$ kubectl get service

$ kubectl get pod
NAME                                READY     STATUS              RESTARTS   AGE
esp-grpc-soseedy-548d44bbb4-svfr8   0/2       ContainerCreating   0          30s
```

Note: plaintext grpc with allow unregistered calls works

next - try ssl + mutual auth
