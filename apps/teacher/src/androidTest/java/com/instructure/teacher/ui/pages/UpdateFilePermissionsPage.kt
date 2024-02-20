/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
 *
 */

package com.instructure.teacher.ui.pages

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertChecked
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.teacher.R
import com.instructure.teacher.features.modules.list.ui.file.FileAvailability
import com.instructure.teacher.features.modules.list.ui.file.FileVisibility

class UpdateFilePermissionsPage : BasePage() {

    private val closeButton by OnViewWithId(R.id.closeButton)
    private val saveButton by OnViewWithId(R.id.updateButton)
    private val availabilityRadioGroup by OnViewWithId(R.id.availabilityRadioGroup)
    private val publishRadioButton by OnViewWithId(R.id.publish)
    private val unpublishRadioButton by OnViewWithId(R.id.unpublish)
    private val hideRadioButton by OnViewWithId(R.id.hide)
    private val scheduleRadioButton by OnViewWithId(R.id.schedule)
    private val visibilityRadioGroup by OnViewWithId(R.id.visibilityRadioGroup)
    private val inheritRadioButton by OnViewWithId(R.id.visibilityInherit)
    private val contextRadioButton by OnViewWithId(R.id.visibilityContext)
    private val institutionRadioButton by OnViewWithId(R.id.visibilityInstitution)
    private val publicRadioButton by OnViewWithId(R.id.visibilityPublic)
    private val scheduleLayout by OnViewWithId(R.id.scheduleLayout)
    private val availableFromDate by OnViewWithId(R.id.availableFromDate)

    fun assertFileAvailability(fileAvailability: FileAvailability) {
        when (fileAvailability) {
            FileAvailability.PUBLISHED -> publishRadioButton.assertChecked()
            FileAvailability.UNPUBLISHED -> unpublishRadioButton.assertChecked()
            FileAvailability.HIDDEN -> hideRadioButton.assertChecked()
            FileAvailability.SCHEDULED -> scheduleRadioButton.assertChecked()
        }
    }

    fun assertFileVisibility(fileVisibility: FileVisibility) {
        when (fileVisibility) {
            FileVisibility.INHERIT -> inheritRadioButton.assertChecked()
            FileVisibility.CONTEXT -> contextRadioButton.assertChecked()
            FileVisibility.INSTITUTION -> institutionRadioButton.assertChecked()
            FileVisibility.PUBLIC -> publicRadioButton.assertChecked()
        }
    }

    fun clickCloseButton() {
        closeButton.click()
    }

    fun clickSaveButton() {
        saveButton.click()
    }

    fun clickPublishRadioButton() {
        publishRadioButton.click()
    }

    fun clickUnpublishRadioButton() {
        unpublishRadioButton.click()
    }

    fun clickHideRadioButton() {
        hideRadioButton.click()
    }

    fun clickScheduleRadioButton() {
        scheduleRadioButton.click()
    }

    fun clickInheritRadioButton() {
        inheritRadioButton.click()
    }

    fun clickContextRadioButton() {
        contextRadioButton.click()
    }

    fun clickInstitutionRadioButton() {
        institutionRadioButton.click()
    }

    fun clickPublicRadioButton() {
        publicRadioButton.click()
    }

    fun assertScheduleLayoutDisplayed() {
        scheduleLayout.assertDisplayed()
    }

    fun assertScheduleLayoutNotDisplayed() {
        scheduleLayout.assertNotDisplayed()
    }

    fun clickAvailableFromTitle() {
        waitForView(withText("Date")).click()
    }

    fun dialogPositiveButton() {
        onViewWithText(android.R.string.ok).click()
    }
}