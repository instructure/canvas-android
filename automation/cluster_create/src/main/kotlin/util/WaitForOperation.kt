package util

import com.google.api.services.container.model.Operation
import gc.api.GcContainer
import java.util.concurrent.TimeUnit

object WaitForOperation {
    private const val DONE = "DONE"

    private fun Operation.id(): String {
        return selfLink.substringAfter("/${Constants.apiVersion}/").replace("/zones/", "/locations/")
    }

    // https://github.com/bootstraponline/gcloud_cli/blob/master/google-cloud-sdk/lib/googlecloudsdk/third_party/apis/container_v1.json
    // https://github.com/bootstraponline/gcloud_cli/blob/45d69d20f7e8e15304c92861317a8e27a63c7852/google-cloud-sdk/lib/surface/container/clusters/create.py#L330
    // https://github.com/bootstraponline/gcloud_cli/blob/45d69d20f7e8e15304c92861317a8e27a63c7852/google-cloud-sdk/lib/googlecloudsdk/api_lib/container/api_adapter.py#L695
    //
    // Poll container Operation until its status is done or timeout reached.
    //
    // operationRef - operation resource
    fun waitForOperation(operationRef: Operation) {
        val timeout = TimeUnit.MINUTES.toMillis(20)
        val pollPeriod = TimeUnit.SECONDS.toMillis(1)

        val stopWatch = StopWatch().start()
        var op = operationRef

        while (timeout > (System.currentTimeMillis() - stopWatch.startTime)) {
            val operationId = operationRef.id()
            op = GcContainer.get.projects().locations().operations().get(operationId).execute()

            if (op.status == DONE) {
                println("Operation ${op.operationType} succeeded after ${stopWatch.check()}")
                println()
                break
            }

            Thread.sleep(pollPeriod)
        }

        if (op.status != DONE) {
            throw RuntimeException("Timed out after ${stopWatch.check()} waiting for operation $op")
        }
    }
}
