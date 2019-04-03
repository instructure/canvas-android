package gc.api

import com.google.api.services.container.model.Cluster
import com.google.api.services.container.model.CreateClusterRequest
import util.Constants
import gc.Gcloud
import gc.ParsedCluster
import util.WaitForOperation.waitForOperation

object GcCluster {

    private fun CreateClusterRequest.updateParent() {
        parent = "projects/$projectId/locations/$zone"
    }

    private fun CreateClusterRequest.clusterId(): String {
        return "$parent/clusters/${cluster.name}"
    }

    fun delete(createClusterRequest: CreateClusterRequest) {
        try {
            val delete = GcContainer.get.projects().locations().clusters().delete(
                    createClusterRequest.clusterId()).execute()

            println("Deleting cluster ${createClusterRequest.cluster.name}...")
            waitForOperation(delete)
        } catch (e: Exception) {
        }
    }

    fun printProjectLink(createClusterRequest: CreateClusterRequest) {
        println("View status at https://console.cloud.google.com/kubernetes/list?project=${createClusterRequest.projectId}")
        println()
    }

    fun create(createClusterRequest: CreateClusterRequest) {
        val create = GcContainer.get.Projects().Locations().Clusters().create(
                createClusterRequest.parent, createClusterRequest
        ).execute()

        println("Creating cluster ${createClusterRequest.cluster.name}...")
        waitForOperation(create)
        Gcloud.generateKubeConfig(createClusterRequest)
        Gcloud.listCluster(createClusterRequest)
    }

    fun createRequest(parsedCluster: ParsedCluster): CreateClusterRequest {
        val createClusterRequest = CreateClusterRequest()

        val cluster = Constants.jsonFactory.fromString(parsedCluster.json, Cluster::class.java)
        createClusterRequest.cluster = cluster
        createClusterRequest.projectId = parsedCluster.projectId
        createClusterRequest.zone = parsedCluster.zone
        createClusterRequest.updateParent()
        return createClusterRequest
    }
}
