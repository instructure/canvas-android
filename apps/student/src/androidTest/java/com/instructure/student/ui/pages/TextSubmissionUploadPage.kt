/*
 * Copyright (C) 2023 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.ui.pages

import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import com.instructure.canvas.espresso.explicitClick
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.OnViewWithText
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.student.R

class TextSubmissionUploadPage : BasePage(R.id.textSubmissionUpload) {

    val submitButton by OnViewWithId(R.id.menuSubmit)
    val contentRceView by OnViewWithId(R.id.rce_webView)
    val textEntryLabel by OnViewWithText(R.string.textEntry)

    fun typeText(textToType: String) {
        contentRceView.click()
        contentRceView.perform(typeTextIntoFocusedView(textToType))
    }

    fun clickOnSubmitButton() {
        submitButton.perform(explicitClick())
    }
}
