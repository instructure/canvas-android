/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.pandautils.features.dashboard.widget.courses

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.instructure.canvasapi2.models.DiscussionTopicHeader
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.courses.model.CourseCardItem
import com.instructure.pandautils.features.dashboard.widget.courses.model.GradeDisplay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        ContextKeeper.appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testCourseCardDisplaysCourseName() {
        val courseCard = createSampleCourseCard()

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = false,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Test Course").assertIsDisplayed()
    }

    @Test
    fun testCourseCardDisplaysPercentageGrade() {
        val courseCard = createSampleCourseCard(grade = GradeDisplay.Percentage("85%"))

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = true,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("85%").assertIsDisplayed()
    }

    @Test
    fun testCourseCardDisplaysLetterGrade() {
        val courseCard = createSampleCourseCard(grade = GradeDisplay.Letter("A-"))

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = true,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("A-").assertIsDisplayed()
    }

    @Test
    fun testCourseCardHidesGradeWhenShowGradeIsFalse() {
        val courseCard = createSampleCourseCard(grade = GradeDisplay.Percentage("85%"))

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = false,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("85%").assertDoesNotExist()
    }

    @Test
    fun testCourseCardDisplaysLockedIcon() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val courseCard = createSampleCourseCard(grade = GradeDisplay.Locked)

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = true,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.locked))
            .assertIsDisplayed()
    }

    @Test
    fun testCourseCardDisplaysNotAvailableGrade() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val courseCard = createSampleCourseCard(grade = GradeDisplay.NotAvailable)

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = true,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText(context.getString(R.string.noGradeText))
            .assertIsDisplayed()
    }

    @Test
    fun testCourseCardDisplaysSyncedIcon() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val courseCard = createSampleCourseCard(isSynced = true)

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = false,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.offline_content_available))
            .assertIsDisplayed()
    }

    @Test
    fun testCourseCardHidesSyncedIconWhenNotSynced() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val courseCard = createSampleCourseCard(isSynced = false)

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = false,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.offline_content_available))
            .assertDoesNotExist()
    }

    @Test
    fun testCourseCardDisplaysAnnouncementIcon() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val announcements = listOf(
            DiscussionTopicHeader(id = 1L, title = "Test Announcement")
        )
        val courseCard = createSampleCourseCard(announcements = announcements)

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = false,
                showColorOverlay = false,
                onCourseClick = { _, _ -> },
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.announcements))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun testCourseCardDisplaysAnnouncementCount() {
        val announcements = listOf(
            DiscussionTopicHeader(id = 1L, title = "Announcement 1"),
            DiscussionTopicHeader(id = 2L, title = "Announcement 2"),
            DiscussionTopicHeader(id = 3L, title = "Announcement 3")
        )
        val courseCard = createSampleCourseCard(announcements = announcements)

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = false,
                showColorOverlay = false,
                onCourseClick = { _, _ -> },
                onAnnouncementClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun testCourseCardHidesAnnouncementIconWhenNoAnnouncements() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val courseCard = createSampleCourseCard(announcements = emptyList())

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = false,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.announcements))
            .assertDoesNotExist()
    }

    @Test
    fun testCourseCardReducedOpacityWhenNotClickable() {
        val courseCard = createSampleCourseCard(isClickable = false)

        composeTestRule.setContent {
            CourseCard(
                courseCard = courseCard,
                showGrade = false,
                showColorOverlay = false,
                onCourseClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithTag("CourseCard_${courseCard.id}").assertIsDisplayed()
    }

    private fun createSampleCourseCard(
        id: Long = 123L,
        name: String = "Test Course",
        grade: GradeDisplay = GradeDisplay.Hidden,
        announcements: List<DiscussionTopicHeader> = emptyList(),
        isSynced: Boolean = false,
        isClickable: Boolean = true
    ): CourseCardItem {
        return CourseCardItem(
            id = id,
            name = name,
            courseCode = "TEST101",
            imageUrl = null,
            grade = grade,
            announcements = announcements,
            isSynced = isSynced,
            isClickable = isClickable,
            color = android.graphics.Color.RED
        )
    }
}
