/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.pages

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import kotlin.math.roundToInt

class HorizonDashboardPage(private val composeTestRule: ComposeTestRule) {
    fun assertNotStartedProgramDisplayed(programName: String) {
        composeTestRule.onNodeWithText(programName)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(programName)
            .onParent()
            .onChildren()
            .filterToOne(hasText("Welcome! View your program to enroll in your first course."))
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(programName)
            .onParent()
            .onChildren()
            .filterToOne(hasAnyDescendant(hasText("Program details")))
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    fun clickProgramDetails(programName: String) {
        composeTestRule.onNodeWithText(programName)
            .onParent()
            .onChildren()
            .filterToOne(hasAnyDescendant(hasText("Program details")))
            .assertIsDisplayed()
            .performClick()
    }

    fun assertCourseCardDisplayed(
        courseName: String,
        programNames: List<String> = emptyList(),
        progress: Double? = null,
        moduleItemName: String? = null
    ) {
        val courseCardParent = composeTestRule.onNodeWithText(courseName)
            .assertIsDisplayed()
            .assertHasClickAction()
            .onParent()
            .onParent()

        courseCardParent.onChildren()
            .filterToOne(hasAnyDescendant(hasText(courseName)))
            .assertIsDisplayed()

        programNames.forEach { programName ->
            courseCardParent.onChildren()
                .filterToOne(hasAnyDescendant(hasText(programName, substring = true)))
                .assertIsDisplayed()
        }

        if (progress != null) {
            courseCardParent.onChildren()
                .filterToOne(hasAnyDescendant(hasText(progress.roundToInt().toString(), substring = true)))
                .assertIsDisplayed()
        }

        if (moduleItemName != null) {
            courseCardParent.onChildren()
                .filterToOne(hasAnyDescendant(hasText(moduleItemName)))
                .onChildren().onFirst()
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    fun clickCourseCard(courseName: String) {
        composeTestRule.onNodeWithText(courseName)
            .performClick()
    }

    fun clickCourseCardModuleItem(courseName: String, moduleItemName: String) {
        composeTestRule.onNodeWithText(courseName)
            .assertIsDisplayed()
            .assertHasClickAction()
            .onParent()
            .onParent()
            .onChildren()
            .filterToOne(hasAnyDescendant(hasText(moduleItemName)))
            .onChildren().onFirst()
            .assertIsDisplayed()
            .performClick()
    }

    fun selectCourseCardAtIndex(index: Int, courseCount: Int) {
        composeTestRule.onNodeWithContentDescription(
            "Course Card ${index + 1} of ${courseCount}"
        ).performClick()
    }

    fun clickInboxButton() {
        composeTestRule.onNodeWithContentDescription("Inbox").performClick()
    }

    fun clickNotificationButton() {
        composeTestRule.onNodeWithContentDescription("Notifications").performClick()
    }

    fun clickNotebookButton() {
        composeTestRule.onNodeWithContentDescription("Notebook").performClick()
    }
}