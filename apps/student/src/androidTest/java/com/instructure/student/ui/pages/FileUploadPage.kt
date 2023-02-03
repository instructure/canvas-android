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
 *
 */
package com.instructure.student.ui.pages

import android.widget.Button
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForViewToBeClickable
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.student.R
import org.hamcrest.core.AllOf.allOf

class FileUploadPage : BasePage() {
    private val cameraButton by OnViewWithId(R.id.fromCamera)
    private val galleryButton by OnViewWithId(R.id.fromGallery)
    private val deviceButton by OnViewWithId(R.id.fromDevice)
    private val chooseFileTitle by OnViewWithId(R.id.chooseFileTitle)
    private val chooseFileSubtitle by OnViewWithId(R.id.chooseFileSubtitle)

    fun chooseCamera() {
        cameraButton.scrollTo().click()
    }

    fun chooseGallery() {
        galleryButton.scrollTo().click()
    }

    fun chooseDevice() {
        deviceButton.scrollTo().click()
    }

    fun clickUpload() {
        onView(allOf(isAssignableFrom(Button::class.java), withText(R.string.upload))).click()
    }

    fun clickTurnIn() {
        onView(withText(R.string.turnIn)).click()
    }

    fun removeFile(filename: String) {
        val fileItemMatcher = withId(R.id.fileItem) + withDescendant(withId(R.id.fileName) + withText(filename))

        val removeMatcher = withId(R.id.removeFile) + ViewMatchers.isDescendantOfA(fileItemMatcher)
        waitForViewToBeClickable(removeMatcher).scrollTo().click()
    }

    fun assertDialogTitle(title: String) {
        onViewWithText(title).assertDisplayed()
    }

    fun assertFileDisplayed(filename: String) {
        onView(withId(R.id.fileName) + withText(filename))
    }

    fun assertFileNotDisplayed(filename: String) {
        onView(withId(R.id.fileName) + withText(filename)).check(doesNotExist())
    }
}