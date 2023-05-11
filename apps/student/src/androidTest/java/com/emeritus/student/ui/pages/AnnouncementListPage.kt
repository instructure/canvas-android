/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.emeritus.student.ui.pages

import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.matchers.WaitForViewMatcher
import com.instructure.espresso.page.*
import com.emeritus.student.R

class AnnouncementListPage : BasePage(R.id.discussionListPage) {

    fun assertToolbarTitle() {
        WaitForViewMatcher.waitForView(withParent(R.id.discussionListToolbar) + withText(R.string.announcements)).assertDisplayed()
    }

    fun assertAnnouncementTitleVisible(title: String) {
        onView(withText(title) + isDisplayed()).assertDisplayed()
    }
}