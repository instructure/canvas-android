/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */    package com.instructure.student.ui.pages

import com.instructure.espresso.OnViewWithContentDescription
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

class QRLoginPage : BasePage() {

    private val nextButton by OnViewWithId(R.id.next)
    private val qrCodeMessage by OnViewWithId(R.id.qrCodeExplanation)
    private val qrCodeScreenshot by OnViewWithContentDescription(R.string.qrCodeScreenshotContentDescription)

    fun clickForA11y() {
        qrCodeMessage.click()
    }

}