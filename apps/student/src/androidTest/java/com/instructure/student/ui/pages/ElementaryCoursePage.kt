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

package com.instructure.student.ui.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.instructure.espresso.assertHasText
import com.instructure.espresso.page.BasePage
import com.instructure.student.R
import org.hamcrest.Matchers


class ElementaryCoursePage : BasePage(R.id.elementaryCoursePage) {

    fun assertTitleCorrect(courseName: String) {
        Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.courseName), ViewMatchers.isDisplayed())).assertHasText(courseName)
    }
}