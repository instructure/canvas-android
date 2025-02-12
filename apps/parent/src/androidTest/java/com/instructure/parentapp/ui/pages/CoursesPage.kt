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
 */

package com.instructure.parentapp.ui.pages

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.instructure.canvasapi2.models.Course
import com.instructure.composeTest.hasSiblingWithText
import com.instructure.dataseeding.model.CourseApiModel
import com.instructure.pandares.R


class CoursesPage(private val composeTestRule: ComposeTestRule) {

    fun assertCourseItemDisplayed(course: Course) {
        composeTestRule.onNodeWithText(course.name)
            .performScrollTo()
            .assertIsDisplayed()
        course.courseCode?.let {
            composeTestRule.onNode(hasSiblingWithText(course.name).and(hasText(it)), true)
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    fun assertCourseItemDisplayed(course: CourseApiModel) {
        composeTestRule.onNodeWithText(course.name)
            .performScrollTo()
            .assertIsDisplayed()
        course.courseCode.let {
            composeTestRule.onNode(hasSiblingWithText(course.name).and(hasText(it)), true)
                .performScrollTo()
                .assertIsDisplayed()
        }
    }

    fun assertEmptyContentDisplayed() {
        composeTestRule.onNodeWithText("No Courses", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Your student’s courses might not be published yet.", useUnmergedTree = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(R.drawable.ic_panda_book.toString())
            .assertIsDisplayed()
    }

    fun assertGradeTextDisplayed(courseName: String, gradeText: String) {
        composeTestRule.onNode(hasSiblingWithText(courseName).and(hasText(gradeText)), true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun assertCourseCodeTextDisplayed(courseName: String, courseCode: String) {
        composeTestRule.onNode(hasSiblingWithText(courseName).and(hasText(courseCode)).and(
            hasTestTag("courseCodeText")
        ), true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun assertGradeTextIsNotDisplayed(courseName: String) {
        composeTestRule.onNode(hasSiblingWithText(courseName).and(hasTestTag("gradeText")), true)
            .assertIsNotDisplayed()
    }

    fun clickCourseItem(courseName: String) {
        composeTestRule.onNodeWithText(courseName)
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
    }
}
