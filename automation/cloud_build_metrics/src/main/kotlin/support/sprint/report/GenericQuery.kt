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



package support.sprint.report

import api.cloud.CloudApps
import util.formatDuration
import util.percent
import util.pretty
import util.weekRange
import java.util.*

interface GenericQuery {
    val datasetName: String
    val spreadsheetId: String
    val isBitrise: Boolean

    fun sprintReport(): ArrayList<List<String>> {
        val reportBody = ArrayList<List<String>>()

        val weekRange = weekRange(weeks = 1)
        val queueTimes = ArrayList<Long>()

        val cloudApps = CloudApps(isBitrise)
        val apps = cloudApps.getApps()

        for (app in apps) {
            val builds = CloudApps.getNormalBuilds(app, weekRange.limitAfter, weekRange.limitBefore)
            if (builds.isEmpty()) continue

            // Used to check each app has unique build ids
            val buildIds = ArrayList<String>()

            var appSuccessCount = 0

            val appTimeAll = ArrayList<Long>()
            val appTimeSuccessful = ArrayList<Long>()
            val appTimeFailed = ArrayList<Long>()

            for (build in builds) {
                buildIds.add(build.buildId)
                queueTimes.add(build.buildQueued)

                appTimeAll.add(build.buildDuration)

                if (build.buildSuccessful) {
                    appSuccessCount += 1
                    appTimeSuccessful.add(build.buildDuration)
                } else {
                    appTimeFailed.add(build.buildDuration)
                }
            }

            if (buildIds.distinct().size != buildIds.size) throw RuntimeException("Duplicate build ids detected!")

            // Avoid NaN
            if (appTimeAll.isEmpty()) appTimeAll.add(0)
            if (appTimeSuccessful.isEmpty()) appTimeSuccessful.add(0)
            if (appTimeFailed.isEmpty()) appTimeFailed.add(0)
            if (appTimeAll.isEmpty()) appTimeAll.add(0)
            if (queueTimes.isEmpty()) queueTimes.add(0)

            val appName = app.toNormalApp().name
            val appAverageTimeAll = formatDuration(appTimeAll.average())
            val appAverageTimeSuccessful = formatDuration(appTimeSuccessful.average())
            val appAverageTimeFailed = formatDuration(appTimeFailed.average())
            val appSuccessPercent = percent(appSuccessCount, builds.size)
            val appFastestSuccessfulBuild = formatDuration(appTimeSuccessful.min())
            val appSlowestBuild = formatDuration(appTimeAll.max())
            val appTotalBuilds = builds.size.toString()
            reportBody.add(listOf(
                    appName,
                    appAverageTimeAll,
                    appAverageTimeSuccessful,
                    appAverageTimeFailed,
                    appSuccessPercent,
                    appFastestSuccessfulBuild,
                    appSlowestBuild,
                    appTotalBuilds
            ))
        }

        // Avoid queueTimes NPE when there are no builds
        if (queueTimes.isEmpty()) queueTimes.add(0)
        val queueTimesAverage = formatDuration(queueTimes.average())
        val queueTimesMax = formatDuration(queueTimes.max())
        val queueTimesMin = formatDuration(queueTimes.min())

        val prettyAfter = weekRange.after.pretty()
        val prettyBefore = weekRange.before.pretty()

        val finalReport = ArrayList<List<String>>()
        val reportTitle = cloudApps.title
        finalReport.add(listOf("$reportTitle Report", "", "", "", "", "", "", "", "$prettyAfter - $prettyBefore "))
        finalReport.add(listOf("", "", "Queue Time"))
        finalReport.add(listOf("", "Average", "Max", "Min"))
        finalReport.add(listOf("", queueTimesAverage, queueTimesMax, queueTimesMin))
        finalReport.add(listOf(""))
        finalReport.add(listOf("", "", "Average Time"))
        finalReport.add(listOf("App name", "All", "Successful", "Failed", "Success %", "Fastest success", "Slowest build", "Total Builds"))
        finalReport.addAll(reportBody)

        return finalReport
    }
}
