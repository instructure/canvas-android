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


package sheets

import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClientRequest
import com.google.api.services.sheets.v4.model.*
import sheets.SheetsV4.sheetsService
import util.utcYearWeek
import java.io.IOException
import java.time.ZonedDateTime

class SheetClient(private val spreadsheetId: String, private val sheetTitle: String) {

    fun getSheet(): Sheet? {
        val spreadsheet = sheetsService.Spreadsheets().get(spreadsheetId).executeWithRetry()
        // sheet titles are case insensitive. 'android' and 'Android' are the same
        return spreadsheet.sheets
                .firstOrNull { sheetTitle.equals(it.properties.title, ignoreCase = true) }
    }

    /** Create a new sheet if it doesn't already exist */
    fun createSheet(): Sheet {
        var sheet = getSheet()

        // addSheet request will fail if the sheet name already exists.
        if (sheet != null) return sheet

        val addSheetRequest = AddSheetRequest().setProperties(
                SheetProperties().setTitle(sheetTitle))

        val content = BatchUpdateSpreadsheetRequest()
        content.requests = listOf(
                Request().setAddSheet(addSheetRequest)
        )
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, content).executeWithRetry()
        // response.replies.first().addSheet doesn't include the sheet object so fetch from the server
        sheet = getSheet()
        if (sheet == null) throw RuntimeException("Sheet not found after addSheetRequest!")
        return sheet
    }

    private fun reformatSheet(sheet: Sheet, sheetLastRow: Int) {
        val sheetId = sheet.properties.sheetId

        // Adding banding to a cell that already has it will error.
        // bandedRanges is null when there are no ranges.
        val deleteBandedRanges = sheet.bandedRanges?.map { Request().setDeleteBanding(DeleteBandingRequest().setBandedRangeId(it.bandedRangeId)) }

        val formatSheet = FormatSheet(sheetId)

        var requests = listOf(
                formatSheet.resizeColumnsFitToData(),
                formatSheet.cutAndPasteDate(),
                formatSheet.alignAllRight(),
                formatSheet.bandingQueueTime(),
                formatSheet.bandingAppTable(sheetLastRow),
                formatSheet.boldTitle()
        )

        if (deleteBandedRanges != null) requests = deleteBandedRanges + requests

        val content = BatchUpdateSpreadsheetRequest().setRequests(requests)
        sheetsService.spreadsheets().batchUpdate(spreadsheetId, content).executeWithRetry()
    }

    fun updateSheet(values: List<List<Any>>) {
        val sheet = createSheet()

        // https://developers.google.com/sheets/api/reference/rest/v4/ValueInputOption
        val valueInputOption = "USER_ENTERED"
        val body = ValueRange().setValues(values)
        val range = "'$sheetTitle'!A:Z" // ='Sheet1'!A:Z

        // Clear all existing content
        sheetsService.spreadsheets().values().clear(spreadsheetId, range, ClearValuesRequest()).executeWithRetry()

        sheetsService.spreadsheets().values().update(spreadsheetId, range, body)
                .setValueInputOption(valueInputOption)
                .executeWithRetry()

        reformatSheet(sheet, values.size)
    }

    companion object {
        fun createSheetClient(spreadsheetId: String): SheetClient {
            val sheetTitle = ZonedDateTime.now().utcYearWeek()
            return SheetClient(spreadsheetId, sheetTitle)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val spreadsheetId = "1t0LtMr746QjBVv_NpYeuu5KuQ-J1YwJI_Tv1wOT3fjI"
            val sheetClient = createSheetClient(spreadsheetId)
            // Used for debugging style changes
            sheetClient.reformatSheet(sheetClient.getSheet()!!, 18)
        }
    }
}

// note: only batch requests retry by default
// https://github.com/google/google-api-java-client/blob/4fc8c099d9db5646770868cc1bc9a33c9225b3c7/google-api-client/src/main/java/com/google/api/client/googleapis/batch/BatchRequest.java#L222
fun <T> AbstractGoogleJsonClientRequest<T>.executeWithRetry(): T {
    var lastErr: IOException? = null

    for (i in 1..3) {
        try {
            return this.execute()
        } catch (err: IOException) {
            lastErr = err
            System.err.println("Request failed, retrying ${i}x $err")
        }
    }

    throw IOException("Request failed", lastErr)
}
