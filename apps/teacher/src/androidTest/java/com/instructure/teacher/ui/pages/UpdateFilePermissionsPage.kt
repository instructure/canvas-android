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
import com.instructure.espresso.assertDisabled
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertEnabled
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onViewWithText
import com.instructure.espresso.page.waitForViewWithId
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeUp
import com.instructure.teacher.R
import java.text.SimpleDateFormat
import java.util.Date

class UpdateFilePermissionsPage : BasePage() {

    private val saveButton by OnViewWithId(R.id.updateButton)
    private val publishRadioButton by OnViewWithId(R.id.publish)
    private val unpublishRadioButton by OnViewWithId(R.id.unpublish)
    private val hideRadioButton by OnViewWithId(R.id.hide)
    private val scheduleRadioButton by OnViewWithId(R.id.schedule)
    private val inheritRadioButton by OnViewWithId(R.id.visibilityInherit)
    private val contextRadioButton by OnViewWithId(R.id.visibilityContext)
    private val institutionRadioButton by OnViewWithId(R.id.visibilityInstitution)
    private val publicRadioButton by OnViewWithId(R.id.visibilityPublic)
    private val scheduleLayout by OnViewWithId(R.id.scheduleLayout)
    private val availableFromDate by OnViewWithId(R.id.availableFromDate)
    private val availableFromTime by OnViewWithId(R.id.availableFromTime)
    private val availableUntilDate by OnViewWithId(R.id.availableUntilDate)
    private val availableUntilTime by OnViewWithId(R.id.availableUntilTime)

    fun assertFilePublished() {
        publishRadioButton.assertChecked()
    }

    fun assertFileUnpublished() {
        unpublishRadioButton.assertChecked()
    }

    fun assertFileHidden() {
        hideRadioButton.assertChecked()
    }

    fun assertFileScheduled() {
        scheduleRadioButton.assertChecked()
    }

    fun assertFileVisibilityInherit() {
        inheritRadioButton.assertChecked()
    }

    fun assertFileVisibilityContext() {
        contextRadioButton.assertChecked()
    }

    fun assertFileVisibilityInstitution() {
        institutionRadioButton.assertChecked()
    }

    fun assertFileVisibilityPublic() {
        publicRadioButton.assertChecked()
    }

    fun clickSaveButton() {
        saveButton.click()
    }

    fun clickPublishRadioButton() {
        waitForViewWithId(R.id.publish).click()
    }

    fun clickUnpublishRadioButton() {
        waitForViewWithId(R.id.unpublish).click()
    }

    fun clickHideRadioButton() {
        waitForViewWithId(R.id.hide).click()
    }

    fun assertScheduleLayoutDisplayed() {
        scheduleLayout.assertDisplayed()
    }

    fun assertScheduleLayoutNotDisplayed() {
        scheduleLayout.assertNotDisplayed()
    }

    fun assertUnlockDate(unlockDate: Date) {
        val dateString = SimpleDateFormat("MMM d, YYYY").format(unlockDate)
        val timeString = SimpleDateFormat("h:mm a").format(unlockDate)

        waitForViewWithId(R.id.availableFromDate).scrollTo().assertDisplayed()
        availableFromDate.assertHasText(dateString)
        availableFromTime.assertHasText(timeString)
    }

    fun assertLockDate(lockDate: Date) {
        val dateString = SimpleDateFormat("MMM d, YYYY").format(lockDate)
        val timeString = SimpleDateFormat("h:mm a").format(lockDate)

        waitForViewWithId(R.id.availableUntilDate).scrollTo().assertDisplayed()
        availableUntilDate.assertHasText(dateString)
        availableUntilTime.assertHasText(timeString)
    }

    fun assertVisibilityDisabled() {
        inheritRadioButton.assertDisabled()
        contextRadioButton.assertDisabled()
        institutionRadioButton.assertDisabled()
        publicRadioButton.assertDisabled()
    }

    fun assertVisibilityEnabled() {
        inheritRadioButton.assertEnabled()
        contextRadioButton.assertEnabled()
        institutionRadioButton.assertEnabled()
        publicRadioButton.assertEnabled()
    }

    fun swipeUpBottomSheet() {
        onViewWithText(R.string.edit_permissions).swipeUp()
        Thread.sleep(1000)
    }
}