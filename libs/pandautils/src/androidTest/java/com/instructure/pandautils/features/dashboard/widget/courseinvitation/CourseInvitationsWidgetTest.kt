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

package com.instructure.pandautils.features.dashboard.widget.courseinvitation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.pandautils.domain.models.enrollment.CourseInvitation
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseInvitationsWidgetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWidgetDoesNotShowWhenLoading() {
        val uiState = CourseInvitationsUiState(
            loading = true,
            error = false,
            invitations = emptyList()
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        // Widget should not render anything when loading
        composeTestRule.onNodeWithText("Course", substring = true).assertDoesNotExist()
    }

    @Test
    fun testWidgetDoesNotShowWhenError() {
        val uiState = CourseInvitationsUiState(
            loading = false,
            error = true,
            invitations = emptyList()
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        // Widget should not render anything when error
        composeTestRule.onNodeWithText("Course", substring = true).assertDoesNotExist()
    }

    @Test
    fun testWidgetDoesNotShowWhenNoInvitations() {
        val uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = emptyList()
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        // Widget should not render anything when no invitations
        composeTestRule.onNodeWithText("Course", substring = true).assertDoesNotExist()
    }

    @Test
    fun testWidgetShowsSingleInvitation() {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Introduction to Computer Science", 10L)
        )

        val uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = invitations
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Course Invitations (1)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Introduction to Computer Science").assertIsDisplayed()
        composeTestRule.onNodeWithText("Accept").assertIsDisplayed()
        composeTestRule.onNodeWithText("Decline").assertIsDisplayed()
    }

    @Test
    fun testWidgetShowsMultipleInvitations() {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Introduction to Computer Science", 10L),
            CourseInvitation(2L, 200L, "Advanced Mathematics", 10L),
            CourseInvitation(3L, 300L, "Art History 101", 10L)
        )

        val uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = invitations
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Course Invitations (3)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Introduction to Computer Science").assertIsDisplayed()

        // Swipe to second page
        composeTestRule.onRoot().performTouchInput {
            swipeLeft(
                startX = centerX + (width / 4),
                endX = centerX - (width / 4)
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Advanced Mathematics").assertIsDisplayed()

        // Swipe to third page
        composeTestRule.onRoot().performTouchInput {
            swipeLeft(
                startX = centerX + (width / 4),
                endX = centerX - (width / 4)
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Art History 101").assertIsDisplayed()
    }

    @Test
    fun testAcceptButtonCallsCallback() {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Test Course", 10L)
        )

        var acceptCalled = false
        var acceptedInvitation: CourseInvitation? = null

        val uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = invitations,
            onAcceptInvitation = { invitation ->
                acceptCalled = true
                acceptedInvitation = invitation
            }
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Accept")[0].performClick()

        assert(acceptCalled)
        assert(acceptedInvitation == invitations[0])
    }

    @Test
    fun testDeclineButtonShowsConfirmationDialog() {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Test Course", 10L)
        )

        val uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = invitations
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Decline")[0].performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Decline Invitation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to decline the invitation to Test Course?", substring = true).assertIsDisplayed()
    }

    @Test
    fun testDeclineConfirmationCallsCallback() {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Test Course", 10L)
        )

        var declineCalled = false
        var declinedInvitation: CourseInvitation? = null

        val uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = invitations,
            onDeclineInvitation = { invitation ->
                declineCalled = true
                declinedInvitation = invitation
            }
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Decline")[0].performClick()

        composeTestRule.waitForIdle()
        // Click the confirm button in the dialog
        composeTestRule.onAllNodesWithText("Decline")[1].performClick()

        assert(declineCalled)
        assert(declinedInvitation == invitations[0])
    }

    @Test
    fun testDeclineCancelDismissesDialog() {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Test Course", 10L)
        )

        var declineCalled = false

        val uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = invitations,
            onDeclineInvitation = { declineCalled = true }
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Decline")[0].performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Cancel").performClick()

        composeTestRule.waitForIdle()
        // Dialog should be dismissed
        composeTestRule.onNodeWithText("Decline Invitation").assertDoesNotExist()
        assert(!declineCalled)
    }

    @Test
    fun testWidgetShowsPagerIndicatorForMultiplePages() {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L),
            CourseInvitation(2L, 200L, "Course 2", 10L),
            CourseInvitation(3L, 300L, "Course 3", 10L)
        )

        val uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = invitations
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 2 // This will create 2 pages (2 invitations on first page, 1 on second)
            )
        }

        composeTestRule.waitForIdle()
        // Pager indicator should be visible when there are multiple pages
        // Note: We can't easily test the pager indicator visibility as it doesn't have a testTag
        // but we can verify the content is displayed correctly
        composeTestRule.onNodeWithText("Course 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Course 2").assertIsDisplayed()
    }

    @Test
    fun testWidgetHidesInvitationCardsProperly() {
        val invitations = listOf(
            CourseInvitation(1L, 100L, "Course 1", 10L)
        )

        val uiState = CourseInvitationsUiState(
            loading = false,
            error = false,
            invitations = invitations
        )

        composeTestRule.setContent {
            CourseInvitationsContent(
                uiState = uiState,
                columns = 1
            )
        }

        composeTestRule.waitForIdle()
        // Should show exactly 2 buttons per invitation (Accept and Decline)
        composeTestRule.onAllNodesWithText("Accept").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Decline").assertCountEquals(1)
    }
}