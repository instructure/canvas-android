package bash

import java.lang.ProcessBuilder.Redirect.PIPE

object Bash {

    fun execute(cmd: String): String {
        println(cmd)

        val process = ProcessBuilder("/bin/bash", "-c", cmd)
                .redirectOutput(PIPE)
                .redirectError(PIPE)
                .start()

        val result = process.waitForResult()

        if (process.failed()) {
            System.err.println("Error: ${result.stderr}")
            throw RuntimeException("Command bash.failed: $cmd")
        }

        return result.stdout.trim()
    }
}
