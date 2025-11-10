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
package com.instructure.horizon.ui.features.dashboard.course

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.dashboard.DashboardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardButtonRoute
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardItemState
import com.instructure.horizon.features.dashboard.widget.DashboardPaginatedWidgetCardState
import com.instructure.horizon.features.dashboard.widget.course.DashboardCourseSection
import com.instructure.horizon.features.dashboard.widget.course.DashboardCourseUiState
import com.instructure.horizon.features.dashboard.widget.course.card.CardClickAction
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardModuleItemState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardParentProgramState
import com.instructure.horizon.features.dashboard.widget.course.card.DashboardCourseCardState
import com.instructure.horizon.model.LearningObjectType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class HorizonDashboardCourseSectionUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProgramAndCourseCards() {
        val state = DashboardCourseUiState(
            state = DashboardItemState.SUCCESS,
            programs = DashboardPaginatedWidgetCardState(
                listOf(
                    DashboardPaginatedWidgetCardItemState(
                        title = "Program 1",
                        route = DashboardPaginatedWidgetCardButtonRoute.HomeRoute("")
                    )
                )
            ),
            courses = listOf(
                DashboardCourseCardState(
                    parentPrograms = listOf(
                        DashboardCourseCardParentProgramState(
                            programName = "Program 11",
                            programId = "1",
                            onClickAction = CardClickAction.Action {}
                        ),
                        DashboardCourseCardParentProgramState(
                            programName = "Program 12",
                            programId = "2",
                            onClickAction = CardClickAction.Action {}
                        )
                    ),
                    title = "Course 1",
                    moduleItem = DashboardCourseCardModuleItemState(
                        moduleItemTitle = "Module Item 1",
                        moduleItemType = LearningObjectType.PAGE,
                        dueDate = Date(),
                        estimatedDuration = "5 min",
                        onClickAction = CardClickAction.Action {}
                    )
                ),
                DashboardCourseCardState(
                    title = "Course 2",
                    moduleItem = DashboardCourseCardModuleItemState(
                        moduleItemTitle = "Module Item 2",
                        moduleItemType = LearningObjectType.ASSIGNMENT,
                        dueDate = Date(),
                        estimatedDuration = "10 min",
                        onClickAction = CardClickAction.Action {}
                    )
                )
            )
        )
        composeTestRule.setContent {
            val mainNavController = rememberNavController()
            val homeNavController = rememberNavController()
            DashboardCourseSection(state, mainNavController,homeNavController)
        }

        composeTestRule.onNodeWithText("Program 1").assertExists()

        composeTestRule.onNodeWithText("Course 1").performScrollTo().assertExists()
        composeTestRule.onNodeWithText("Program 11", true).assertExists()
        composeTestRule.onNodeWithText("Program 12", true).assertExists()
        composeTestRule.onNodeWithText("Module Item 1").assertExists().assertHasClickAction()
        composeTestRule.onNodeWithText("5 min").assertExists().assertHasClickAction()
        composeTestRule.onNodeWithText("Page").assertExists().assertHasClickAction()

        composeTestRule.onNodeWithText("Course 2").performScrollTo().assertExists()
        composeTestRule.onNodeWithText("Module Item 2").assertExists().assertHasClickAction()
        composeTestRule.onNodeWithText("10 min").assertExists().assertHasClickAction()
        composeTestRule.onNodeWithText("Assignment").assertExists().assertHasClickAction()
    }

}