package api.config

import bash.Bash
import java.util.concurrent.TimeUnit

object LoadBalancer {

    private fun getIp(): String {
        // Note: Java API isn't used due to lack of GCP token refresh https://github.com/kubernetes-client/java/issues/290
        // kubectl get service esp-grpc-soseedy -o=jsonpath='{.status.loadBalancer.ingress[0].ip}'
        return Bash.execute("kubectl get service esp-grpc-soseedy -o=jsonpath='{.status.loadBalancer.ingress[0].ip}'")
    }

    private val ipRegex = Regex("""\d+\.\d+\.\d+\.\d+""")

    fun pollForIp(): String {
        val waitTime = TimeUnit.MINUTES.toMillis(10)
        val endTime = System.currentTimeMillis() + waitTime

        do {
            val ip = getIp()
            if (ip.matches(ipRegex)) return ip
            Thread.sleep(10_000)
        } while (System.currentTimeMillis() < endTime)

        throw RuntimeException("No ip address found.")
    }
}
