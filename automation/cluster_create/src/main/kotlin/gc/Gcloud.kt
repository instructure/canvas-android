package gc

import bash.Bash
import com.google.api.services.container.model.CreateClusterRequest

object Gcloud {

    private fun quote(string: String): String {
        return "\"$string\""
    }

    fun generateKubeConfig(createClusterRequest: CreateClusterRequest) {
        val zone = quote(createClusterRequest.zone)
        val clusterName = createClusterRequest.cluster.name
        val name = quote(clusterName)
        Bash.execute("gcloud --quiet container clusters get-credentials $name --zone $zone")
        println("kubeconfig entry generated for $clusterName.")
        println()
    }

    fun listCluster(createClusterRequest: CreateClusterRequest) {
        val name = quote("name: ${createClusterRequest.cluster.name}")
        println(Bash.execute("gcloud --quiet container clusters list --filter=$name"))
    }
}
