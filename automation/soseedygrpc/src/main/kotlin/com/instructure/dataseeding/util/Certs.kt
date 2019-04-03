package com.instructure.dataseeding.util

import java.io.File

object Certs {

    private fun getResourceAsFile(name: String): File? {
        val resource = this::class.java.getResourceAsStream("/$name") ?: return null

        val text = resource
                .bufferedReader().use {
                    it.readText()
                }

        val file = createTempFile()
        file.deleteOnExit()
        file.appendText(text)
        return file
    }

    private fun get(name: String): File {
        val resource = getResourceAsFile(name)
        if (resource != null) return resource

        val file = File("./src/main/resources/$name")
        if (file.exists()) return file

        throw RuntimeException("Unable to find $name as resource or file")
    }

    var caCert = get("ca.crt")
    var serverCert = get("server.crt")
    var serverPrivateKey = get("server.pem")
    var clientCert = get("client.crt")
    var clientPrivateKey = get("client.pem")
}
