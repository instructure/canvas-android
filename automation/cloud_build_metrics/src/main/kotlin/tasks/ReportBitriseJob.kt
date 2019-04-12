//
// Copyright (C) 2018-present Instructure, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//



package tasks

import api.bitrise.BitriseApps
import util.defaultBitriseTxt
import tasks.BuildErrorReport.downloadErrorLog
import java.nio.file.Files
import java.nio.file.Paths

// Reads bitrise job ids from txt file and reports status
object ReportBitriseJob : Task {

    override fun execute() {
        val buildSlugs = Files.readAllLines(Paths.get(defaultBitriseTxt))

        // Write failed: Broken pipe // https://www.bitrise.io/build/a260c64aa59835cb
        // INSTRUMENTATION_ABORTED: System has crashed. // https://www.bitrise.io/build/28a2d3e5bc76a723
        // INSTRUMENTATION_STATUS: stack=

        val failures = listOf(
                "Write failed: Broken pipe",
                "INSTRUMENTATION_ABORTED:",
                "INSTRUMENTATION_STATUS: stack="
        )

        val appSlug = "693f666c209a029b"
        val total = buildSlugs.size
        val status = mutableMapOf<String, Int>()
        var logFailures = 0
        buildSlugs.forEach buildSlugsForEach@{ buildSlug ->
            val build = BitriseApps.getBuild(appSlug, buildSlug).data
            println("https://www.bitrise.io/build/${build.slug} - ${build.status_text}")

            val value: Int = (status[build.status_text] ?: 0) + 1
            status[build.status_text] = value

            // Check logs on successful builds to ensure they're really successful
            if (build.status_text == "success") {
                val logPath = downloadErrorLog(appSlug, build)
                val logData = Files.readAllLines(logPath)

                logData.forEach { line ->
                    if (line.indexOfAny(failures) != -1) {
                        println("  log failure detected")
                        logFailures += 1
                        return@buildSlugsForEach
                    }
                }
            } else {
                logFailures += 1
            }
        }

        println()
        println("Total builds: $total")

        println("Bitrise build status")
        status.forEach { key, value -> println("$key: $value") }
        println()

        println("Log failures")
        println("Success: ${total - logFailures}")
        println("Failure: $logFailures")
    }

    // start: 2018-02-15 20:55:12
    // end:   2018-02-16 06:25:53
    //
    // Chronic.parse('20:55:12') - Chronic.parse('06:25:53')
    // => -34241.0
    //
    // Chronic.output(34241.0)
    // "9 hrs 30 mins 41 secs" for 100x espresso @ 10 concurrency
    @JvmStatic
    fun main(args: Array<String>) {
        execute()
    }
}
