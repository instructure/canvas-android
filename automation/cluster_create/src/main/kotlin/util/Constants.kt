package util

import com.google.api.client.googleapis.GoogleUtils
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory

object Constants {
    var apiVersion = ""
    // https://cloud.google.com/kubernetes-engine/docs/reference/rest/v1beta1/projects.locations.clusters/create
    val httpTransport by lazy {
        // GoogleNetHttpTransport.newTrustedTransport() + ApiVersionConnectionFactory
        NetHttpTransport.Builder()
                .trustCertificates(GoogleUtils.getCertificateTrustStore())
                .setConnectionFactory(ApiVersionConnectionFactory(proxy = null))
                .build()!!
    }
    val jsonFactory = JacksonFactory.getDefaultInstance()!!
    private val defaultCredential = GoogleCredential.getApplicationDefault()
    val scopedCredential = defaultCredential.createScoped(
            listOf("https://www.googleapis.com/auth/cloud-platform"))!!

    const val localPort = 9090
    const val localhost = "http://localhost:$localPort"
    var useMock = false
}
