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
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.R
import com.instructure.pandautils.features.dashboard.widget.courses.model.GroupCardItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroupCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        ContextKeeper.appContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testGroupCardDisplaysGroupName() {
        val groupCard = createSampleGroupCard()

        composeTestRule.setContent {
            GroupCard(
                groupCard = groupCard,
                onGroupClick = { _, _ -> },
                onMessageClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Test Group").assertIsDisplayed()
    }

    @Test
    fun testGroupCardDisplaysParentCourseName() {
        val groupCard = createSampleGroupCard(parentCourseName = "Computer Science 101")

        composeTestRule.setContent {
            GroupCard(
                groupCard = groupCard,
                onGroupClick = { _, _ -> },
                onMessageClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Computer Science 101").assertIsDisplayed()
    }

    @Test
    fun testGroupCardHidesParentCourseNameWhenNull() {
        val groupCard = createSampleGroupCard(parentCourseName = null)

        composeTestRule.setContent {
            GroupCard(
                groupCard = groupCard,
                onGroupClick = { _, _ -> },
                onMessageClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Computer Science 101").assertDoesNotExist()
    }

    @Test
    fun testGroupCardDisplaysMemberCount() {
        val groupCard = createSampleGroupCard(memberCount = 5)

        composeTestRule.setContent {
            GroupCard(
                groupCard = groupCard,
                onGroupClick = { _, _ -> },
                onMessageClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun testGroupCardDisplaysMemberCountSingular() {
        val groupCard = createSampleGroupCard(memberCount = 1)

        composeTestRule.setContent {
            GroupCard(
                groupCard = groupCard,
                onGroupClick = { _, _ -> },
                onMessageClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun testGroupCardDisplaysInboxIcon() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val groupCard = createSampleGroupCard()

        composeTestRule.setContent {
            GroupCard(
                groupCard = groupCard,
                onGroupClick = { _, _ -> },
                onMessageClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithContentDescription(context.getString(R.string.inbox))
            .assertIsDisplayed()
    }

    @Test
    fun testGroupCardDisplaysWithTestTag() {
        val groupCard = createSampleGroupCard(id = 123L)

        composeTestRule.setContent {
            GroupCard(
                groupCard = groupCard,
                onGroupClick = { _, _ -> },
                onMessageClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithTag("GroupCard_123").assertIsDisplayed()
    }

    @Test
    fun testGroupCardDisplaysMultipleMemberCount() {
        val groupCard = createSampleGroupCard(memberCount = 15)

        composeTestRule.setContent {
            GroupCard(
                groupCard = groupCard,
                onGroupClick = { _, _ -> },
                onMessageClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("15").assertIsDisplayed()
    }

    @Test
    fun testGroupCardDisplaysZeroMemberCount() {
        val groupCard = createSampleGroupCard(memberCount = 0)

        composeTestRule.setContent {
            GroupCard(
                groupCard = groupCard,
                onGroupClick = { _, _ -> },
                onMessageClick = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    private fun createSampleGroupCard(
        id: Long = 123L,
        name: String = "Test Group",
        parentCourseName: String? = "Computer Science 101",
        memberCount: Int = 5
    ): GroupCardItem {
        return GroupCardItem(
            id = id,
            name = name,
            parentCourseName = parentCourseName,
            memberCount = memberCount
        )
    }
}
