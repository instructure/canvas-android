/*
 * Copyright (C) 2025 - present Instructure, Inc.
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

package com.instructure.pandautils.features.dashboard.widget.courses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.domain.models.courses.CourseCardItem
import com.instructure.pandautils.domain.models.courses.GradeDisplay
import com.instructure.pandautils.domain.models.courses.GroupCardItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoursesWidgetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWidgetShowsLoadingShimmer() {
        val uiState = CoursesWidgetUiState(
            isLoading = true,
            courses = emptyList(),
            groups = emptyList()
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Favorite Courses and Groups").assertIsDisplayed()
        composeTestRule.onNodeWithText("All Courses").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsEmptyStateWhenNoCoursesOrGroups() {
        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = emptyList(),
            groups = emptyList()
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Favorite Courses and Groups").assertIsDisplayed()
        composeTestRule.onNodeWithText("Courses").assertDoesNotExist()
        composeTestRule.onNodeWithText("Groups").assertDoesNotExist()
    }

    @Test
    fun testWidgetShowsSingleCourse() {
        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = emptyList(),
            isCoursesExpanded = true
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Courses (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Introduction to Computer Science").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsMultipleCourses() {
        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            ),
            CourseCardItem(
                id = 2,
                name = "Advanced Mathematics",
                courseCode = "MATH 201",
                color = 0xFF4CAF50.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = emptyList(),
            isCoursesExpanded = true
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Courses (2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Introduction to Computer Science").assertIsDisplayed()
        composeTestRule.onNodeWithText("Advanced Mathematics").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsGroups() {
        val groups = listOf(
            GroupCardItem(
                id = 1,
                name = "Project Team Alpha",
                parentCourseName = "Introduction to Computer Science",
                parentCourseId = 1,
                color = 0xFF4CAF50.toInt(),
                memberCount = 5
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = emptyList(),
            groups = groups,
            isGroupsExpanded = true
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Groups (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Project Team Alpha").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsCoursesAndGroups() {
        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        val groups = listOf(
            GroupCardItem(
                id = 1,
                name = "Project Team Alpha",
                parentCourseName = "Introduction to Computer Science",
                parentCourseId = 1,
                color = 0xFF4CAF50.toInt(),
                memberCount = 5
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = groups,
            isCoursesExpanded = true,
            isGroupsExpanded = true
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Courses (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Groups (1)").assertIsDisplayed()
        composeTestRule.onNodeWithTag("CourseCard_1").assertIsDisplayed()
        composeTestRule.onNode(
            hasText("Introduction to Computer Science") and hasAnyAncestor(hasTestTag("CourseCard_1"))
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Project Team Alpha").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsGradeWhenEnabled() {
        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Percentage("85%"),
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = emptyList(),
            isCoursesExpanded = true,
            showGrades = true
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("85%").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsLetterGrade() {
        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Letter("A-"),
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = emptyList(),
            isCoursesExpanded = true,
            showGrades = true
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("A-").assertIsDisplayed()
    }

    @Test
    fun testWidgetHidesGradeWhenDisabled() {
        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Percentage("85%"),
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = emptyList(),
            isCoursesExpanded = true,
            showGrades = false
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("85%").assertDoesNotExist()
    }

    @Test
    fun testCoursesCollapsibleSectionToggle() {
        var toggleCalled = false

        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = emptyList(),
            isCoursesExpanded = true,
            onToggleCoursesExpanded = { toggleCalled = true }
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Courses (1)").performClick()

        assert(toggleCalled)
    }

    @Test
    fun testGroupsCollapsibleSectionToggle() {
        var toggleCalled = false

        val groups = listOf(
            GroupCardItem(
                id = 1,
                name = "Project Team Alpha",
                parentCourseName = "Introduction to Computer Science",
                parentCourseId = 1,
                color = 0xFF4CAF50.toInt(),
                memberCount = 5
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = emptyList(),
            groups = groups,
            isGroupsExpanded = true,
            onToggleGroupsExpanded = { toggleCalled = true }
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Groups (1)").performClick()

        assert(toggleCalled)
    }

    @Test
    fun testWidgetShowsSyncedIndicator() {
        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = true,
                isClickable = true
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = emptyList(),
            isCoursesExpanded = true
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Introduction to Computer Science").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsAnnouncementCount() {
        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 2,
                isSynced = false,
                isClickable = true
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = emptyList(),
            isCoursesExpanded = true
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    @Test
    fun testCoursesCollapseOnHeaderClick() {
        var isExpanded by mutableStateOf(true)

        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = CoursesWidgetUiState(
                    isLoading = false,
                    courses = courses,
                    groups = emptyList(),
                    isCoursesExpanded = isExpanded,
                    onToggleCoursesExpanded = { isExpanded = !isExpanded }
                ),
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("CourseCard_1").assertIsDisplayed()

        composeTestRule.onNodeWithText("Courses (1)").performClick()
        composeTestRule.waitForIdle()

        assert(!isExpanded)
        composeTestRule.onNodeWithTag("CourseCard_1").assertDoesNotExist()
    }

    @Test
    fun testCoursesExpandOnHeaderClick() {
        var isExpanded by mutableStateOf(false)

        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Introduction to Computer Science",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = CoursesWidgetUiState(
                    isLoading = false,
                    courses = courses,
                    groups = emptyList(),
                    isCoursesExpanded = isExpanded,
                    onToggleCoursesExpanded = { isExpanded = !isExpanded }
                ),
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("CourseCard_1").assertDoesNotExist()

        composeTestRule.onNodeWithText("Courses (1)").performClick()
        composeTestRule.waitForIdle()

        assert(isExpanded)
        composeTestRule.onNodeWithTag("CourseCard_1").assertIsDisplayed()
    }

    @Test
    fun testGroupsCollapseOnHeaderClick() {
        var isExpanded by mutableStateOf(true)

        val groups = listOf(
            GroupCardItem(
                id = 1,
                name = "Project Team Alpha",
                parentCourseName = "Introduction to Computer Science",
                parentCourseId = 1,
                color = 0xFF4CAF50.toInt(),
                memberCount = 5
            )
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = CoursesWidgetUiState(
                    isLoading = false,
                    courses = emptyList(),
                    groups = groups,
                    isGroupsExpanded = isExpanded,
                    onToggleGroupsExpanded = { isExpanded = !isExpanded }
                ),
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("GroupCard_1").assertIsDisplayed()

        composeTestRule.onNodeWithText("Groups (1)").performClick()
        composeTestRule.waitForIdle()

        assert(!isExpanded)
        composeTestRule.onNodeWithTag("GroupCard_1").assertDoesNotExist()
    }

    @Test
    fun testGroupsExpandOnHeaderClick() {
        var isExpanded by mutableStateOf(false)

        val groups = listOf(
            GroupCardItem(
                id = 1,
                name = "Project Team Alpha",
                parentCourseName = "Introduction to Computer Science",
                parentCourseId = 1,
                color = 0xFF4CAF50.toInt(),
                memberCount = 5
            )
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = CoursesWidgetUiState(
                    isLoading = false,
                    courses = emptyList(),
                    groups = groups,
                    isGroupsExpanded = isExpanded,
                    onToggleGroupsExpanded = { isExpanded = !isExpanded }
                ),
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("GroupCard_1").assertDoesNotExist()

        composeTestRule.onNodeWithText("Groups (1)").performClick()
        composeTestRule.waitForIdle()

        assert(isExpanded)
        composeTestRule.onNodeWithTag("GroupCard_1").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsMultipleColumnsOnTablet() {
        val courses = listOf(
            CourseCardItem(
                id = 1,
                name = "Course 1",
                courseCode = "CS 101",
                color = 0xFF2196F3.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            ),
            CourseCardItem(
                id = 2,
                name = "Course 2",
                courseCode = "CS 102",
                color = 0xFF4CAF50.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            ),
            CourseCardItem(
                id = 3,
                name = "Course 3",
                courseCode = "CS 103",
                color = 0xFFFF9800.toInt(),
                imageUrl = null,
                grade = GradeDisplay.Hidden,
                announcementCount = 0,
                isSynced = false,
                isClickable = true
            )
        )

        val uiState = CoursesWidgetUiState(
            isLoading = false,
            courses = courses,
            groups = emptyList(),
            isCoursesExpanded = true
        )

        composeTestRule.setContent {
            CoursesWidgetContent(
                uiState = uiState,
                columns = 3
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Course 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Course 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Course 3").assertIsDisplayed()
    }
}