/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 */

package com.instructure.student.ui.pages.classic.offline

import android.widget.TextView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import com.instructure.canvas.espresso.containsTextCaseInsensitive
import com.instructure.canvas.espresso.getView
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertContainsText
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertVisibility
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.waitForView
import com.instructure.espresso.page.withAncestor
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.pandautils.R

class SyncProgressPage : BasePage(R.id.syncProgressPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)

    fun assertDownloadProgressSuccessDetails() {
        onView(withId(R.id.downloadProgress)).assertDisplayed()
        onView(withId(R.id.errorTitle)).assertVisibility(ViewMatchers.Visibility.GONE)
        waitForDownloadSuccess()
    }

    fun waitForDownloadStarting() {
        waitForView(withId(R.id.downloadProgressText) + containsTextCaseInsensitive("Downloading")).assertDisplayed()
    }

    private fun waitForDownloadSuccess() {
        waitForView(withId(R.id.downloadProgressText) + containsTextCaseInsensitive("Success! Downloaded")).assertDisplayed()
    }

    fun assertCourseSyncedSuccessfully(courseName: String) {
        onView(withId(R.id.courseName) + withText(courseName) + withAncestor(R.id.syncProgressPage)).assertDisplayed()
        onView(withId(R.id.successIndicator) + withParent(withId(R.id.actionContainer) + hasSibling(withId(R.id.courseName) + withText(courseName)))).assertVisibility(ViewMatchers.Visibility.VISIBLE)
    }

    fun expandCollapseCourse(courseName: String) {
        onView(withId(R.id.toggleButton) + hasSibling(withId(R.id.courseName) + withText(courseName))).click()
    }

    fun assertCourseTabSynced(tabName: String) {
        onView(withId(R.id.successIndicator) + withParent(withId(R.id.actionContainer) + hasSibling(withId(R.id.tabTitle) + withText(tabName)))).assertVisibility(ViewMatchers.Visibility.VISIBLE)
    }

    fun getCourseSize(courseName: String): Int {
        val courseSizeView = onView(withId(R.id.courseSize) + hasSibling(withId(R.id.courseName) + withText(courseName)))
        val courseSizeText = (courseSizeView.getView() as TextView).text.toString()
        return courseSizeText.split(" ")[0].toInt()
    }

    fun assertSumOfCourseSizes(expectedSize: Int) {
        if(expectedSize > 999) {
            val convertedSumSize = convertKiloBytesToMegaBytes(expectedSize)
            onView(withId(R.id.downloadProgressText)).assertContainsText(convertedSumSize.toString())
        }
        else {
            onView(withId(R.id.downloadProgressText)).assertContainsText(expectedSize.toString())
        }
    }

    private fun convertKiloBytesToMegaBytes(kilobytes: Int): Double {
        return kilobytes / 1000.0
    }
}
