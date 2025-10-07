/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.ui.pages.classic.k5

import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.matchers.WaitForViewMatcher
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withParent
import com.instructure.espresso.page.withText
import com.instructure.student.R


class ElementaryCoursePage : BasePage(R.id.elementaryCoursePage) {

    fun assertTitleCorrect(courseName: String) {
        WaitForViewMatcher.waitForView(withParent(R.id.toolbar) + withText(courseName)).assertDisplayed()
    }
}