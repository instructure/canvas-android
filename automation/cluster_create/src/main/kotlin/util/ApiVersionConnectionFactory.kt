package util

import com.google.api.client.http.javanet.DefaultConnectionFactory
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL

class ApiVersionConnectionFactory(proxy: Proxy?) : DefaultConnectionFactory(proxy) {
    override fun openConnection(url: URL?): HttpURLConnection {
        // /v1/projects/delta-essence-114723/locations/us-central1-a/clusters/cluster-1  =>
        // /v1beta1/projects/delta-essence-114723/locations/us-central1-a/clusters/cluster-1
        var newUrl: URL? = null
        if (url != null) {
            // /v1/projects/... => v1/projects/ => v1
            val v1 = url.path.drop(1).substringBefore("/")
            newUrl = URL(url.toString().replaceFirst("/$v1/", "/${Constants.apiVersion}/"))
        }
        return super.openConnection(newUrl)
    }
}
