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

package com.instructure.student.ui.pages.offline

import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.pandautils.R

class ManageOfflineContentPage : BasePage(R.id.manageOfflineContentPage) {

    private val toolbar by OnViewWithId(R.id.toolbar)

    fun selectEntireCourseForSync(courseName: String) {
        onView(withId(R.id.checkbox) + hasSibling(withId(R.id.title) + withText(courseName))).click()
    }

    fun clickOnSyncButton() {
        onView(withId(R.id.syncButton)).click()
    }
}
