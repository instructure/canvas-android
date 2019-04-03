import com.instructure.soseedy.HealthCheckRequest
import com.instructure.soseedy.SeedyGeneralGrpc
import org.junit.Test

class DeployedServerTest {

    @Test
    fun testServerAndClient() {
        val channel = Client.buildSecureChannel("soseedy.endpoints.delta-essence-114723.cloud.goog", 80)
        val blockingStub = SeedyGeneralGrpc.newBlockingStub(channel)
        blockingStub.getHealthCheck(HealthCheckRequest.getDefaultInstance())
    }
}
