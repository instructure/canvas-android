package com.instructure.dataseeding.util

import io.grpc.netty.GrpcSslContexts
import io.netty.handler.ssl.ClientAuth
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslProvider
import java.io.File
import java.net.InetSocketAddress
import javax.net.ssl.SSLException

object Config {

    const val port = 50051
    val exampleDotCom = "example.com"

    fun serverSslContext(serverCert: File,
                         serverPrivateKey: File,
                         caCert: File): SslContext {
        val sslClientContextBuilder = SslContextBuilder.forServer(serverCert, serverPrivateKey)
        sslClientContextBuilder.trustManager(caCert)
        sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE)

        return GrpcSslContexts.configure(sslClientContextBuilder, SslProvider.OPENSSL).build()
    }

    @Throws(SSLException::class)
    fun clientSslContext(trustCertCollection: File,
                         clientCertChain: File,
                         clientPrivateKey: File): SslContext {
        val builder = GrpcSslContexts.forClient()
        builder.trustManager(trustCertCollection)
        builder.keyManager(clientCertChain, clientPrivateKey)

        return builder.build()
    }
}
