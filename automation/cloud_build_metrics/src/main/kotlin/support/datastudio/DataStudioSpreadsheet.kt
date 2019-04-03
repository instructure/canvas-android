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



package support.datastudio

import api.bitrise.BitriseAppObject
import api.bitrise.BitriseApps
import com.google.api.services.sheets.v4.model.ValueRange
import normal.toNormalBuild
import sheets.SheetClient
import sheets.SheetsV4.sheetsService
import sheets.executeWithRetry
import support.BitriseApp
import tasks.Task
import util.*
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Updates data studio spreadsheet
 */
class DataStudioSpreadsheet(private val platform: Spreadsheet) : Task {
    override fun execute() {
        updateSpreadsheet()
    }

    private fun sheetTitle(): String {
        return platform.sheetTitle
    }

    private fun buildRowFromRange(range: WeekRange): List<String> {
        return when (platform.type) {
            MobileTeam.iOS -> buildRow(range, listOf(BitriseApp.iOS.Automation.jest))
            MobileTeam.Android -> buildRow(range, BitriseApp.pullRequestApps)
        }
    }

    private fun buildRow(range: WeekRange,
                         bitriseApps: List<BitriseAppObject> = listOf()): List<String> {
        val date = range.after
        val yearWeek = range.after.yearWeek(space = false)
        var durationSum = 0L
        var successful = 0
        var failed = 0

        bitriseApps.forEach { app ->
            val builds = BitriseApps.getBuilds(app,
                    limitDateAfter = range.limitAfter,
                    limitDateBefore = range.limitBefore,
                    page = true).mapNotNull { it.toNormalBuild() }
            println("  ${builds.size} builds - ${app.title}")
            for (build in builds) {
                durationSum += build.buildDuration

                if (build.buildSuccessful) {
                    successful += 1
                } else {
                    failed += 1
                }
            }
        }

        // Calculate metrics based on the processed builds.
        val total = successful + failed
        val durationAverage = if (total > 0) {
            (durationSum / total).toString()
        } else {
            "0"
        }
        val successPercent = if (total > 0) {
            percent(successful, total, percentSymbol = false)
        } else {
            "0"
        }

        val dateFormat = DateTimeFormatter.ofPattern("MMM d")
        val weekName = dateFormat.format(range.after) + " - " + dateFormat.format(range.before)

        println("${range.after} - ${range.before} Total builds: $total")

        return listOf(date.toString(), yearWeek, weekName, durationAverage, successful.toString(), failed.toString(),
                total.toString(), successPercent)
    }

    private fun buildRows(firstRun: Boolean = false): List<List<Any>> {
        val rows = mutableListOf<List<String>>()
        val weekCount = platform.weekCount
        if (firstRun) {
            rows.add(listOf("Date", "YearWeek", "WeekName", "Duration", "Successful", "Failed",
                    "Total", "Success_%"))

            weekRanges(weekCount).forEach { range -> rows.add(buildRowFromRange(range)) }
            return rows
        }

        rows.add(buildRowFromRange(weekRange(1)))

        return rows
    }

    private fun shouldReplaceRow(lastUpdate: LocalDate): Boolean {
        val lastYearWeek = lastUpdate.yearWeek()
        val currentYearWeek = LocalDate.now(ZoneOffset.UTC).yearWeek()
        return lastYearWeek == currentYearWeek
    }

    private fun installLastUpdatedFormula() {
        val dateColumn = "A:A"
        val lastDateFormula = "=INDEX($dateColumn,COUNTA($dateColumn))"
        val lastCellFormula = "=COUNTA($dateColumn)"
        val content = ValueRange().setValues(listOf(
                listOf("Last Updated", lastDateFormula, lastCellFormula)))

        // https://developers.google.com/sheets/api/reference/rest/v4/ValueInputOption
        sheetsService.spreadsheets().values()
                .update(
                        platform.spreadsheetId,
                        "'" + sheetTitle() + "'!I1:K1",
                        content
                ).setValueInputOption(platform.userEntered)
                .executeWithRetry()
    }

    private fun appendRows(values: List<List<Any>>) {
        val body = ValueRange().setValues(values)
        val range = "'" + sheetTitle() + "'!A:Z" // ='Sheet1'!A:Z

        sheetsService.spreadsheets().values().append(platform.spreadsheetId, range, body)
                .setValueInputOption(platform.userEntered)
                .executeWithRetry()
    }

    private fun updateRows(values: List<List<Any>>) {
        val body = ValueRange().setValues(values)
        val range = "'" + sheetTitle() + "'!A${getLastRow()}:Z" // ='Sheet1'!A:Z

        sheetsService.spreadsheets().values().update(platform.spreadsheetId, range, body)
                .setValueInputOption(platform.userEntered)
                .executeWithRetry()
    }

    private fun getCell(cell: String): String {
        val response = sheetsService.spreadsheets().values().get(platform.spreadsheetId, "'" + sheetTitle() + "'!$cell")
                .executeWithRetry()
        return response.getValues().first().first() as String
    }

    private fun getLastUpdate(): LocalDate {
        return LocalDate.parse(getCell("J1"))
    }

    private fun getLastRow(): String {
        return getCell("K1")
    }

    private fun updateSpreadsheet() {
        val client = SheetClient(platform.spreadsheetId, sheetTitle())
        val firstRun = client.getSheet() == null

        if (firstRun) {
            client.createSheet()
            appendRows(buildRows(firstRun = firstRun))
            installLastUpdatedFormula()
            return
        }

        val lastUpdate = getLastUpdate()

        println("Last updated: $lastUpdate")

        // replace existing week if it's the same as the current week
        if (shouldReplaceRow(lastUpdate)) {
            println("Update rows!")
            updateRows(buildRows())
        } else { // append on new weeks
            println("Append rows!")
            appendRows(buildRows())
        }
    }
}
