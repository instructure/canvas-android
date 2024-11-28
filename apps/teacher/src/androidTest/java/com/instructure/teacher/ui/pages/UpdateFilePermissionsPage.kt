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

import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertChecked
import com.instructure.espresso.assertDisabled
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertEnabled
import com.instructure.espresso.assertHasText
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.pages.BasePage
import com.instructure.espresso.pages.onView
import com.instructure.espresso.pages.onViewWithId
import com.instructure.espresso.pages.onViewWithText
import com.instructure.espresso.pages.waitForViewWithClassName
import com.instructure.espresso.pages.waitForViewWithId
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeUp
import com.instructure.teacher.R
import org.hamcrest.Matchers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class UpdateFilePermissionsPage : BasePage() {

    private val updateButton by OnViewWithId(R.id.updateButton)
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

    fun clickUpdateButton() {
        updateButton.click()
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

    fun clickScheduleRadioButton() {
        waitForViewWithId(R.id.schedule).click()
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

    fun setFromDateTime(calendar: Calendar) {
        availableFromDate.perform(click())
        waitForViewWithClassName(Matchers.equalTo(DatePicker::class.java.name)).perform(PickerActions.setDate(calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)))
        onViewWithId(android.R.id.button1).click()
        availableFromTime.perform(click())
        onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))
        onViewWithId(android.R.id.button1).click()
    }

    fun setUntilDateTime(calendar: Calendar) {
        availableUntilDate.perform(click())
        waitForViewWithClassName(Matchers.equalTo(DatePicker::class.java.name)).perform(PickerActions.setDate(calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)))
        onViewWithId(android.R.id.button1).click()
        availableUntilTime.perform(click())
        onView(withClassName(Matchers.equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)))
        onViewWithId(android.R.id.button1).click()
    }
}