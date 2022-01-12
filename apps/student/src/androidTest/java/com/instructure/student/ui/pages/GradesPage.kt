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

import com.instructure.espresso.OnViewWithId
import com.instructure.espresso.assertDisplayed
import com.instructure.espresso.assertNotDisplayed
import com.instructure.espresso.click
import com.instructure.espresso.page.BasePage
import com.instructure.espresso.page.onView
import com.instructure.espresso.page.plus
import com.instructure.espresso.page.withDescendant
import com.instructure.espresso.page.withId
import com.instructure.espresso.page.withText
import com.instructure.espresso.scrollTo
import com.instructure.espresso.swipeDown
import com.instructure.student.R

class GradesPage : BasePage(R.id.gradesPage) {

    private val swipeRefreshLayout by OnViewWithId(R.id.gradesRefreshLayout)
    private val gradesRecyclerView by OnViewWithId(R.id.gradesRecyclerView)
    private val emptyView by OnViewWithId(R.id.gradesEmptyView, autoAssert = false)

    fun assertCourseShownWithGrades(courseName: String, grade: String) {
        val courseNameMatcher = withId(R.id.gradesCourseNameText) + withText(courseName)
        val gradeMatcher = withId(R.id.scoreText) + withText(grade)

        onView(withId(R.id.gradeRow) + withDescendant(courseNameMatcher) + withDescendant(gradeMatcher))
            .scrollTo()
            .assertDisplayed()
    }

    fun assertCourseNotDisplayed(courseName: String) {
        val courseNameMatcher = withId(R.id.gradesCourseNameText) + withText(courseName)
        onView(courseNameMatcher).assertNotDisplayed()
    }

    fun refresh() {
        swipeRefreshLayout.swipeDown()
    }

    fun assertEmptyViewVisible() {
        emptyView.assertDisplayed()
    }

    fun assertRecyclerViewNotVisible() {
        gradesRecyclerView.assertNotDisplayed()
    }

    fun clickGradeRow(courseName: String) {
        onView(withId(R.id.gradesCourseNameText) + withText(courseName))
            .scrollTo()
            .click()
    }

    fun clickGradingPeriodSelector() {
        onView(withId(R.id.gradingPeriodSelector))
            .scrollTo()
            .click()
    }

    fun selectGradingPeriod(gradingPeriodName: String) {
        onView(withText(gradingPeriodName))
            .click()
    }

    fun assertSelectedGradingPeriod(gradingPeriodName: String) {
        onView(withId(R.id.gradingPeriodSelector) + withText(gradingPeriodName))
            .assertDisplayed()
    }
}