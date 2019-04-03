package api.config

import com.squareup.wire.schema.Location
import com.squareup.wire.schema.internal.parser.ProtoFileElement
import com.squareup.wire.schema.internal.parser.ProtoParser
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object ParseProto {

    private val filePrefix = """
# Note: Auto generated file. Do not edit!
# https://github.com/googleapis/googleapis/blob/master/google/api/service.proto
type: google.api.Service
config_version: 3

# https://cloud.google.com/endpoints/docs/grpc/configure-endpoints
# YOUR_API_NAME.endpoints.YOUR_PROJECT_ID.cloud.goog
name: soseedy.endpoints.delta-essence-114723.cloud.goog

title: SoSeedy gRPC API
apis:
        """.trimIndent()

    private val fileSuffix = """
usage:
  rules:
  - selector: soseedy.SeedyGeneral.GetHealthCheck
    allow_unregistered_calls: true

endpoints:
- name: soseedy.endpoints.delta-essence-114723.cloud.goog
  target: "
        """.trimIndent()

    private fun parseProtoFile(file: File): ProtoFileElement {
        val path = file.canonicalPath
        val location = Location.get(path)
        val data = String(Files.readAllBytes(Paths.get(path)))
        return ProtoParser.parse(location, data)
    }

    private fun parseProtoServices(file: File): List<String> {
        val proto = parseProtoFile(file)

        val pkg = proto.packageName()
        val results = mutableListOf<String>()
        proto.services().forEach { service -> results.add("- name: $pkg.${service.name()}") }
        return results
    }

    fun generateApiConfig(): String {
        var result = filePrefix + "\n"

        File("../dataseedingapi/src/main/proto/").walkTopDown().forEach { file ->
            if (file.extension == "proto") {
                for (service in parseProtoServices(file)) {
                    result += service + "\n"
                }
            }
        }

        result += "\n" + fileSuffix + LoadBalancer.pollForIp() + "\""
        return result
    }

    // java -jar api_config.jar ../dataseedingapi/api_config.yaml
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.size != 1) {
            throw IllegalArgumentException("Expected path to api_config.yml")
        }

        val apiConfigFile = File(args.first())
        apiConfigFile.parentFile.mkdirs()

        Files.write(apiConfigFile.toPath(), generateApiConfig().toByteArray())
    }
}
