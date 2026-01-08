/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.teacher.ui.pages.classic

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withChild
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.WaitForViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertHasText
import com.instructure.espresso.click
import com.instructure.espresso.matchers.WaitForViewMatcher.waitForViewToBeClickable
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.teacher.R

class FileChooserPage : BasePage() {
    private val cameraButton by OnViewWithId(R.id.fromCamera)
    private val galleryButton by OnViewWithId(R.id.fromGallery)
    private val deviceButton by OnViewWithId(R.id.fromDevice)
    private val chooseFileTitle by OnViewWithId(R.id.chooseFileTitle)
    private val chooseFileSubtitle by OnViewWithId(R.id.chooseFileSubtitle)
    private val fileChooserTitle by WaitForViewWithId(R.id.alertTitle)

    fun assertFileChooserDetails() {
        chooseFileTitle.assertDisplayed().assertHasText(R.string.chooseFile)
        chooseFileSubtitle.assertDisplayed().assertHasText(R.string.chooseFileForUploadSubtext)
        cameraButton.assertDisplayed()
        galleryButton.assertDisplayed()
        deviceButton.assertDisplayed()
    }

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
        onView(withText(R.string.upload)).click()
    }

    fun clickTurnIn() {
        onView(withText(R.string.turnIn)).click()
    }

    fun clickCancel() {
        onView(withText(R.string.cancel)).click()
    }

    fun removeFile(filename: String) {
        val fileItemMatcher = withId(R.id.fileItem) + withDescendant(withId(R.id.fileName) + containsTextCaseInsensitive(filename))

        val removeMatcher = withId(R.id.removeFile) + ViewMatchers.isDescendantOfA(fileItemMatcher)
        waitForViewToBeClickable(removeMatcher).scrollTo().click()
    }

    fun assertDialogTitle(title: String) {
        fileChooserTitle.assertHasText(title)
    }

    fun assertFileDisplayed(filename: String) {
        val fileNameMatcher = withId(R.id.fileName) + withText(filename)
        onView(fileNameMatcher).assertDisplayed()
        onView(withId(R.id.fileSize) + hasSibling(fileNameMatcher)).assertDisplayed()
        onView(withId(R.id.fileIcon)  + withParent(withId(R.id.iconWrapper) + hasSibling(withId(R.id.content) + withChild(fileNameMatcher)))).assertDisplayed()
        onView(withId(R.id.removeFile)  + hasSibling(withId(R.id.content) + withChild(fileNameMatcher))).assertDisplayed()
    }

    fun assertFileNotDisplayed(filename: String) {
        onView(withId(R.id.fileName) + withText(filename)).check(doesNotExist())
    }
}