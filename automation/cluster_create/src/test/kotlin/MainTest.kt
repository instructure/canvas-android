import util.Constants
import org.junit.Test
import java.util.concurrent.TimeUnit

class MainTest {

    init {
        Constants.useMock = true
    }

    @Test
    fun testMain() {
        val server = MockServer.run()
        server.start()

        val clustersTxt = "./src/test/kotlin/fixtures/clusters.txt"

        try {
            Main.deleteAndCreateCluster(clustersTxt)
        } finally {
            server.stop(0,0, TimeUnit.SECONDS)
        }
    }
}
