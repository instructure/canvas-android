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
package com.instructure.student.ui.pages

import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.instructure.espresso.Searchable
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.matchers.WaitForViewMatcher
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.student.R

class AnnouncementListPage(searchable: Searchable) : DiscussionListPage(searchable) {

    fun assertToolbarTitle() {
        WaitForViewMatcher.waitForView(withParent(R.id.discussionListToolbar) + withText(R.string.announcements)).assertDisplayed()
    }

    fun assertAnnouncementTitleVisible(title: String) {
        onView(withText(title) + isDisplayed()).assertDisplayed()
    }
}