import com.instructure.dataseeding.util.Certs
import com.instructure.dataseeding.util.Config
import io.grpc.ManagedChannel
import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder
import java.net.InetSocketAddress

object Client {
    fun buildPlaintextChannel(hostname: String, port: Int): ManagedChannel {
        return NettyChannelBuilder.forAddress(InetSocketAddress(hostname, port))
                .overrideAuthority(Config.exampleDotCom)
                .usePlaintext()
                .build()
    }

    fun buildSecureChannel(hostname: String, port: Int): ManagedChannel {
        val sslContext = Config.clientSslContext(
                Certs.caCert,
                Certs.clientCert,
                Certs.clientPrivateKey)

        return NettyChannelBuilder.forAddress(InetSocketAddress(hostname, port))
                .negotiationType(NegotiationType.TLS)
                .sslContext(sslContext)
                .overrideAuthority(Config.exampleDotCom)
                .build()
    }
}
