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
import api.cloud.CloudApps
import com.google.common.collect.ListMultimap
import com.google.common.collect.MultimapBuilder
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Generates a local job activity report.
 * Useful for discovering obsolete jobs.
 * Copy and paste output into Google sheets for better visibility.
 */
object BuildActivityReport : Task {

    private fun lastBuildRelativeTime(zonedDate: String): Pair<Long, String> {
        val buildDate = ZonedDateTime.parse(zonedDate)

        val now = ZonedDateTime.now()

        val minutes = ChronoUnit.MINUTES.between(buildDate, now)
        val hours = ChronoUnit.HOURS.between(buildDate, now)
        val days = ChronoUnit.DAYS.between(buildDate, now)
        val months = ChronoUnit.MONTHS.between(buildDate, now)

        val unit = when {
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            days <= 30 -> "$days days ago"
            else -> "$months months ago"
        }

        return Pair(minutes, unit)
    }

    private fun sortedMap(): ListMultimap<Long, String> {
        return MultimapBuilder.treeKeys().linkedListValues().build<Long, String>()
    }

    /**
     * Generate an activity report for Bitrise.
     * TSV for copying into Google Sheets
     *
     * candroid	6 hours ago
     * iOS JavaScript WIP	6 hours ago
     * android-parent-robo	17 hours ago
     */
    private fun jobActivityReport(cloud: Any) {
        val isBitrise = cloud == BitriseApps
        if (!isBitrise) throw RuntimeException("Unknown cloud ${cloud::class}")

        val map = sortedMap()
        val cloudApps = CloudApps(isBitrise)
        val apps = cloudApps.getApps()

        for (app in apps) {
            val normalApp = app.toNormalApp()

            val (minutes, unit) = when (app) {
                is BitriseAppObject -> {
                    val firstBuild = BitriseApps.getBuilds(app, null, null, false).first()
                    lastBuildRelativeTime(firstBuild.triggered_at)
                }
                else -> {
                    throw RuntimeException("Unknown app")
                }
            }

            map.put(minutes, "${normalApp.name}\t$unit")
        }

        val cloudName = cloudApps.title
        println("$cloudName app count\t${apps.size}")
        for (value in map.values()) {
            println(value)
        }
        println()
    }

    override fun execute() {
        jobActivityReport(BitriseApps)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        this.execute()
    }
}
