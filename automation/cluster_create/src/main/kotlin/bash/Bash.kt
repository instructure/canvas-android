package bash

import util.Constants
import java.lang.ProcessBuilder.Redirect.PIPE

object Bash {

    fun execute(cmd: String): String {
        if (Constants.useMock) return ""
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
