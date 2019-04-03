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

import api.bitrise.BitriseAppObject
import api.bitrise.BitriseApps
import api.bitrise.BitriseBuildObject
import util.pretty
import util.prettyDateTimeYear
import util.weekRange
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.ZonedDateTime

object BuildErrorReport : Task {

    private fun tmpDirForApp(appSlug: String): Path {
        return Paths.get(System.getProperty("java.io.tmpdir"), appSlug)
    }

    fun downloadErrorLog(appSlug: String,
                         build: BitriseBuildObject,
                         deleteCache: Boolean = false): Path {
        val tmpDir = tmpDirForApp(appSlug)
        if (deleteCache) tmpDir.toFile().deleteRecursively()
        tmpDir.toFile().mkdirs()

        val buildLog = tmpDir.resolve(build.slug)

        if (buildLog.exists()) { // cached
            return buildLog
        }

        println("Downloading: ${build.slug} to: $tmpDir")
        val log = BitriseApps.getBuildLog(appSlug, build)
        Files.write(buildLog, log)
        return buildLog
    }

    private fun downloadErrorLogs(app: BitriseAppObject,
                                  deleteCache: Boolean = false,
                                  limitAfter: ZonedDateTime,
                                  limitBefore: ZonedDateTime): List<String> {
        val builds = BitriseApps.getBuilds(app, limitAfter, limitBefore, true)

        val failedBuilds = mutableListOf<BitriseBuildObject>()
        val successfulBuilds = mutableListOf<BitriseBuildObject>()

        builds.forEach { build ->
            when (build.status_text) {
                "error" -> failedBuilds.add(build)
                "success" -> successfulBuilds.add(build)
            }
        }

        val totalBuilds = failedBuilds.size + successfulBuilds.size
        println("\"${app.title}\" has ${failedBuilds.size} failed builds out of $totalBuilds builds")

        // get logs from build
        val buildSlugs = ArrayList<String>(failedBuilds.size)
        for (build in failedBuilds) {
            val slug = build.slug
            buildSlugs.add(slug)


            downloadErrorLog(app.slug, build, deleteCache)
        }

        return buildSlugs
    }

    private fun parseLogs(app: BitriseAppObject, buildSlugs: List<String>) {
        /*

+------------------------------------------------------------------------------+

| (5) ios-auto-provision                                                       |
+------------------------------------------------------------------------------+
| id: ios-auto-provision                                                       |
| version: 0.9.2                                                               |
| collection: https://github.com/bitrise-io/bitrise-steplib.git                |
| toolkit: bash                                                                |
| time: 2017-11-29T08:34:08-08:00                                              |
+------------------------------------------------------------------------------+
|                                                                              |

<data>

+---+---------------------------------------------------------------+----------+
| x | ios-auto-provision (exit code: 1)                             | 100 sec  |
+---+---------------------------------------------------------------+----------+
| Issue tracker: ...github.com/bitrise-steplib/steps-ios-auto-provision/issues |
| Source: https://github.com/bitrise-steplib/steps-ios-auto-provision          |
+---+---------------------------------------------------------------+----------+

+---+---------------------------------------------------------------+----------+
| x | ios-auto-provision (exit code: 1)                             | 100 sec  |
+---+---------------------------------------------------------------+----------+

---

+---+---------------------------------------------------------------+----------+
| [31;1mx[0m | [31;1mios-auto-provision (exit code: 1)[0m                             | 100 sec  |
+---+---------------------------------------------------------------+----------+

*/
        val tmpDir = tmpDirForApp(app.slug)
        val failureRegex = Regex("^\\| .*x.* \\| (.*) \\| (.*)|$")

        println("Parsing errors from ${buildSlugs.size} logs")
        for (slug in buildSlugs) {

            val buildLog = tmpDir.resolve(slug)

            if (buildLog.exists()) {
                val lines = Files.readAllLines(buildLog, Charsets.UTF_8)
                lines
                        .mapIndexedNotNull { index, string ->
                            val stepName = failureRegex.find(string)?.groups?.get(1)?.value
                            if (!detailedErrors) return@mapIndexedNotNull stepName

                            val errorLines: String?
                            var result: String? = null

                            if (stepName != null) {
                                errorLines = lines.slice(IntRange(index - 20, index - 3))
                                        .reduce { acc, line ->
                                            // detect step headers
                                            if (line.startsWith("+--") || line.startsWith("| ")) {
                                                acc
                                            } else if (line.isNotBlank()) {
                                                "$acc\n  $line"
                                            } else {
                                                acc
                                            }
                                        }
                                result = stepName + "\n" + errorLines
                            }
                            result
                        }.distinctBy { it.split("\n").first() }
                        .forEach { println("https://www.bitrise.io/build/$slug, $it"); }
            } else {
                throw RuntimeException("Missing build log for $slug on app ${app.slug}")
            }
        }
    }

    private fun buildErrorReport(app: BitriseAppObject,
                                 deleteCache: Boolean = false,
                                 limitAfter: ZonedDateTime,
                                 limitBefore: ZonedDateTime) {
        val buildSlugs = downloadErrorLogs(app, deleteCache, limitAfter, limitBefore)
        parseLogs(app, buildSlugs)
    }

    private fun Path.exists(): Boolean {
        return this.toFile().exists()
    }

    override fun execute() {
        val targetAppSlug = "57b22415f35222e3"
        val appsInOrg = BitriseApps.getAppsForOrg()

        println("Found ${appsInOrg.size} apps in org")
        val app = appsInOrg.first { it.slug == targetAppSlug }

        println("Building error report for ${app.title}")
        val weekStart = LocalDate.parse("Monday, January 7 2019", prettyDateTimeYear)
        val range = weekRange(52, weekStart)

        println("${range.after.pretty()} - ${range.before.pretty()}")
        buildErrorReport(app,
                deleteCache = false,
                limitAfter = range.limitAfter,
                limitBefore = range.limitBefore)
    }

    // Prints detailed stack trace for failed step.
    var detailedErrors = false

    @JvmStatic
    fun main(args: Array<String>) {
        this.execute()
    }
}
