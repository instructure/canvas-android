import com.google.api.services.container.model.Operation
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import util.Constants
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine

object MockServer {
    fun buildOp(call: ApplicationCall): Operation {
        val projectId = call.parameters["projectId"]
        val zoneId = call.parameters["zoneId"]
        val operationId = call.parameters["operationId"] ?: "operation-1530417048993-155f9b85"
        val operation = Operation()
        operation.status = "DONE"
        operation.selfLink = "https://container.googleapis.com/v1/projects/$projectId/zones/$zoneId/operations/$operationId"

        return operation
    }

    fun run(): NettyApplicationEngine {
        return embeddedServer(Netty, Constants.localPort) {
            install(ContentNegotiation) {
                // Fix: IllegalArgumentException: number type formatted as a JSON number cannot use @JsonString annotation
                val gson = GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create()
                register(ContentType.Application.Json, GsonConverter(gson))
            }
            routing {
                post("/v1/projects/{projectId}/locations/{zoneId}/clusters") {
                    call.respond(buildOp(call))
                }

                get("/v1/projects/{projectId}/locations/{zoneId}/operations/{operationId}") {
                    call.respond(buildOp(call))
                }

                post("/{...}") {
                    println("Unknown POST " + call.request.uri)
                    call.respond("unknown post")
                }

                get("/{...}") {
                    println("Unknown GET " + call.request.uri)
                    call.respond("unknown get")
                }
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        run().start(wait = true)
    }
}
