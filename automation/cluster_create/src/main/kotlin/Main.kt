import gc.ClustersTxt
import gc.api.GcCluster

object Main {
    fun deleteAndCreateCluster(clusterPath: String) {
        val createClusterRequest = GcCluster.createRequest(
                ClustersTxt.parse(clusterPath)
        )

        GcCluster.printProjectLink(createClusterRequest)
        GcCluster.delete(createClusterRequest) // delete takes ~5m
        GcCluster.create(createClusterRequest) // create takes ~4m
    }

    @JvmStatic
    fun main(args: Array<String>) {
        deleteAndCreateCluster("clusters.txt")
    }
}
