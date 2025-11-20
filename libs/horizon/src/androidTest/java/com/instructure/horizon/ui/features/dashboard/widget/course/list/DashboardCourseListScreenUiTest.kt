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
package com.instructure.horizon.ui.features.dashboard.widget.course.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.horizon.features.dashboard.widget.course.list.DashboardCourseListCourseState
import com.instructure.horizon.features.dashboard.widget.course.list.DashboardCourseListFilterOption
import com.instructure.horizon.features.dashboard.widget.course.list.DashboardCourseListParentProgramState
import com.instructure.horizon.features.dashboard.widget.course.list.DashboardCourseListScreen
import com.instructure.horizon.features.dashboard.widget.course.list.DashboardCourseListUiState
import com.instructure.horizon.horizonui.platform.LoadingState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardCourseListScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createTestCourses(count: Int): List<DashboardCourseListCourseState> {
        return (1..count).map { index ->
            DashboardCourseListCourseState(
                parentPrograms = emptyList(),
                name = "Course $index",
                courseId = index.toLong(),
                progress = (index * 10).toDouble() % 100
            )
        }
    }

    @Test
    fun testEmptyStateDisplaysEmptyMessage() {
        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = emptyList()
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("Nothing here yet")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Adjust your filters to see more.")
            .assertIsDisplayed()
    }

    @Test
    fun testCoursesAreDisplayed() {
        val courses = listOf(
            DashboardCourseListCourseState(
                parentPrograms = emptyList(),
                name = "Math 101",
                courseId = 1L,
                progress = 50.0
            ),
            DashboardCourseListCourseState(
                parentPrograms = emptyList(),
                name = "Science 201",
                courseId = 2L,
                progress = 75.0
            )
        )

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("Math 101")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Science 201")
            .assertIsDisplayed()
    }

    @Test
    fun testCourseProgressIsDisplayed() {
        val courses = listOf(
            DashboardCourseListCourseState(
                parentPrograms = emptyList(),
                name = "Math 101",
                courseId = 1L,
                progress = 50.0
            )
        )

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("50%", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun testParentProgramsAreDisplayed() {
        val courses = listOf(
            DashboardCourseListCourseState(
                parentPrograms = listOf(
                    DashboardCourseListParentProgramState(
                        programName = "Computer Science Degree",
                        programId = "cs-101"
                    )
                ),
                name = "Math 101",
                courseId = 1L,
                progress = 50.0
            )
        )

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("Computer Science Degree", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun testFilterOptionsAreDisplayed() {
        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = createTestCourses(5),
            visibleCourseCount = 3,
            selectedFilterOption = DashboardCourseListFilterOption.All
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("All courses")
            .assertIsDisplayed()
    }

    @Test
    fun testCourseCountIsDisplayed() {
        val courses = createTestCourses(15)

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("15")
            .assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonIsDisplayedWhenMoreCoursesExist() {
        val courses = createTestCourses(15)

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithTag("collapsableContent")
            .onChild()
            .performScrollToNode(hasText("Show more"))
        composeTestRule.onNodeWithText("Show more")
            .assertIsDisplayed()
    }

    @Test
    fun testShowMoreButtonIsNotDisplayedWhenAllCoursesAreVisible() {
        val courses = createTestCourses(3)

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("Show more")
            .assertDoesNotExist()
    }

    @Test
    fun testShowMoreButtonIsNotDisplayedWhenLessCoursesExist() {
        val courses = createTestCourses(2)

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("Show more")
            .assertDoesNotExist()
    }

    @Test
    fun testShowMoreButtonClickCallsCallback() {
        var showMoreCalled = false
        val courses = createTestCourses(15)

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3,
            onShowMoreCourses = { showMoreCalled = true }
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithTag("collapsableContent")
            .onChild()
            .performScrollToNode(hasText("Show more"))
        composeTestRule.onNodeWithText("Show more")
            .assertIsDisplayed()
            .performClick()

        assert(showMoreCalled) { "Show more callback should be called when button is clicked" }
    }

    @Test
    fun testOnlyVisibleCoursesAreDisplayed() {
        val courses = createTestCourses(15)

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithTag("collapsableContent")
            .onChild()
            .performScrollToNode(hasText("Course 1"))
        composeTestRule.onNodeWithText("Course 1")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("collapsableContent")
            .onChild()
            .performScrollToNode(hasText("Course 2"))
        composeTestRule.onNodeWithText("Course 2")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("collapsableContent")
            .onChild()
            .performScrollToNode(hasText("Course 3"))
        composeTestRule.onNodeWithText("Course 3")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Course 4")
            .assertDoesNotExist()

        composeTestRule.onNodeWithText("Course 5")
            .assertDoesNotExist()
    }

    @Test
    fun testMoreCoursesDisplayedAfterIncreasingVisibleCount() {
        val courses = createTestCourses(15)

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 6
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithTag("collapsableContent")
            .onChild()
            .performScrollToNode(hasText("Course 1"))
        composeTestRule.onNodeWithText("Course 1")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("collapsableContent")
            .onChild()
            .performScrollToNode(hasText("Course 6"))
        composeTestRule.onNodeWithText("Course 6")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Course 7")
            .assertDoesNotExist()
    }

    @Test
    fun testShowMoreButtonDisappearsWhenAllCoursesAreShown() {
        val courses = createTestCourses(5)

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 5
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("Course 1")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("collapsableContent")
            .onChild()
            .performScrollToNode(hasText("Course 5"))
        composeTestRule.onNodeWithText("Course 5")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Show more")
            .assertDoesNotExist()
    }

    @Test
    fun testTitleIsDisplayed() {
        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = createTestCourses(5),
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("All courses")
            .assertIsDisplayed()
    }

    @Test
    fun testMultipleParentProgramsAreDisplayed() {
        val courses = listOf(
            DashboardCourseListCourseState(
                parentPrograms = listOf(
                    DashboardCourseListParentProgramState(
                        programName = "Program A",
                        programId = "prog-a"
                    ),
                    DashboardCourseListParentProgramState(
                        programName = "Program B",
                        programId = "prog-b"
                    )
                ),
                name = "Math 101",
                courseId = 1L,
                progress = 50.0
            )
        )

        val state = DashboardCourseListUiState(
            loadingState = LoadingState(isLoading = false),
            courses = courses,
            visibleCourseCount = 3
        )

        composeTestRule.setContent {
            val navController = rememberNavController()
            DashboardCourseListScreen(state, navController)
        }

        composeTestRule.onNodeWithText("Program A", substring = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Program B", substring = true)
            .assertIsDisplayed()
    }
}
