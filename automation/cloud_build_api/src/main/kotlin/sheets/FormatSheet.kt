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

import com.google.api.services.sheets.v4.model.*

class FormatSheet(private val sheetId: Int) {
    private val white = Color().parseColor("#ffffff")
    private val darkBlue = Color().parseColor("#4dd0e1")
    private val lightBlue = Color().parseColor("#e0f7fa")
    private val darkGreen = Color().parseColor("#63d297")
    private val lightGreen = Color().parseColor("#e7f9ef")

    // https://developers.google.com/sheets/api/samples/sheet
    /** clear values and preserve format */
    private val userEnteredValue = "userEnteredValue"
    /** clear formatting and preserve values */
    private val userEnteredFormat = "userEnteredFormat"

    private val columnA = 0
    private val columnB = 1
    private val columnZ = 25

    private fun columnIndex(column: Char): Int {
        val index = column.toLowerCase().toInt() - 97 // 'a' starts at 97
        if (!IntRange(0, 25).contains(index)) throw RuntimeException("column is not within A-Z range! $column index: $index")
        return index
    }

    fun alignAllRight(): Request {
        // Right align all cells
        val cellData = CellData().setUserEnteredFormat(CellFormat().setHorizontalAlignment("RIGHT"))
        val gridRange = GridRange().setSheetId(sheetId)
                .setStartColumnIndex(columnA)
                .setEndColumnIndex(columnZ)
        return Request().setRepeatCell(RepeatCellRequest().setCell(cellData).setRange(gridRange).setFields(userEnteredFormat))
    }

    fun resizeColumnsFitToData(): Request {
        val dimensionRange = DimensionRange()
                .setDimension("COLUMNS")
                .setStartIndex(columnA)
                .setEndIndex(columnZ)
                .setSheetId(sheetId)

        return Request().setAutoResizeDimensions(AutoResizeDimensionsRequest().setDimensions(dimensionRange))
    }

    fun boldTitle(): Request {
        val cellFormat = CellFormat()
                .setTextFormat(TextFormat().setBold(true))
                .setHorizontalAlignment("CENTER")

        val cellData = CellData().setUserEnteredFormat(cellFormat)
        val gridRange = GridRange().setSheetId(sheetId)
                .setStartColumnIndex(columnA)
                .setEndColumnIndex(columnB)
                .setStartRowIndex(0)
                .setEndRowIndex(1)
        return Request().setRepeatCell(RepeatCellRequest().setCell(cellData).setRange(gridRange).setFields(userEnteredFormat))
    }

    fun cutAndPasteDate(): Request {
        // Cut and paste date from I1 to H1 *after* we've auto resized dimensions.
        val cutPasteRequest = CutPasteRequest()
        // I1 is [I,J) [0,1)
        // https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets#GridRange
        val columnI = columnIndex('I')
        cutPasteRequest.source = GridRange().setSheetId(sheetId)
                .setStartColumnIndex(columnI)
                .setEndColumnIndex(columnI + 1)
                .setStartRowIndex(0)
                .setEndRowIndex(1)
        cutPasteRequest.destination = GridCoordinate().setSheetId(sheetId)
                .setColumnIndex(columnIndex('H'))
                .setRowIndex(0)
        // https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets/request#PasteType
        return Request().setCutPaste(cutPasteRequest.setPasteType("PASTE_NORMAL"))
    }

    fun bandingQueueTime(): Request {
        val greenBandingProperties = BandingProperties()
                .setHeaderColor(darkGreen)
                .setFirstBandColor(white)
                .setSecondBandColor(lightGreen)

        // 'Queue Time'
        val bandedRange = BandedRange().setRange(GridRange()
                .setSheetId(sheetId)
                .setStartColumnIndex(columnIndex('B'))
                .setEndColumnIndex(columnIndex('E'))
                .setStartRowIndex(2 - 1).setEndRowIndex(4))
                .setRowProperties(greenBandingProperties)

        return Request().setAddBanding(AddBandingRequest().setBandedRange(bandedRange))

    }

    fun bandingAppTable(sheetLastRow: Int): Request {
        // BandedRange == alternating colors
        // https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets
        val blueBandingProperties = BandingProperties()
                .setHeaderColor(darkBlue)
                .setFirstBandColor(white)
                .setSecondBandColor(lightBlue)

        // 'App name, All, ...'
        val bandedRange = BandedRange().setRange(GridRange()
                .setSheetId(sheetId)
                .setStartColumnIndex(columnIndex('A'))
                .setEndColumnIndex(columnIndex('I'))
                .setStartRowIndex(7 - 1).setEndRowIndex(sheetLastRow))
                .setRowProperties(blueBandingProperties)

        return Request().setAddBanding(AddBandingRequest().setBandedRange(bandedRange))
    }
}
