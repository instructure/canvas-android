package gc.api

import com.google.api.services.container.Container
import util.Constants.httpTransport
import util.Constants.jsonFactory
import util.Constants.localhost
import util.Constants.scopedCredential
import util.Constants.useMock

object GcContainer {

    val get: Container by lazy {
        val builder = Container.Builder(httpTransport, jsonFactory, scopedCredential)
                .setApplicationName("cluster_create")

        if (useMock) builder.rootUrl = localhost

        builder.build()
    }
}
