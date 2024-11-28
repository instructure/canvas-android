/*
 * Copyright (C) 2019 - present Instructure, Inc.
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

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.replaceText
import com.instructure.student.R
import org.hamcrest.Matcher

open class UrlSubmissionUploadPage : BasePage(R.id.urlSubmissionUpload) {

    private val editUrlView by OnViewWithId(R.id.editUrl)
    private val submitButton by OnViewWithId(R.id.menuSubmit)

    fun submitText(text: String) {
        editUrlView.replaceText(text)
        submitButton.perform(ClickAfterEnabledAction())
    }
}

/**
 * This ViewAction will wait until a view is enabled to click on it
 */
class ClickAfterEnabledAction: ViewAction {
    override fun getDescription(): String {
        return "Loops main thread until view is enabled, then clicks"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isDisplayed()
    }

    override fun perform(uiController: UiController?, view: View?) {
        uiController?.loopMainThreadUntilIdle()
        var flag = true
        do {
            if(view?.isEnabled == true) {
                view.performClick()
                flag = false
            }
            uiController?.loopMainThreadForAtLeast(100)
        } while (flag)
    }
}
