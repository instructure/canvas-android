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
package com.instructure.pandautils.compose.features.studytools

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import com.instructure.pandautils.features.studytools.StudyToolState
import com.instructure.pandautils.features.studytools.StudyToolsActions
import com.instructure.pandautils.features.studytools.StudyToolsFeedback
import com.instructure.pandautils.features.studytools.composables.StudyToolContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyToolContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val summary = StudyToolResult.Summary("Photosynthesis turns light into sugar.")

    @Test
    fun assertSkeletonWhileGenerating() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Generating, actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsSkeleton").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsSummary").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolsRegenerate").assertDoesNotExist()
    }

    @Test
    fun assertSkeletonIsReplacedByTheSummary() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(summary), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsSkeleton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolsSummary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Photosynthesis turns light into sugar.").assertIsDisplayed()
    }

    @Test
    fun assertFooterOnlyShowsWithContent() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(summary), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsRegenerate").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithTag("studyToolsFeedbackLiked").assertIsDisplayed().assertHasClickAction()
        composeTestRule.onNodeWithTag("studyToolsFeedbackDisliked").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun assertFooterDoesNotShowForError() {
        composeTestRule.setContent {
            StudyToolContent(
                state = StudyToolState.Error("nope", retryable = false),
                actions = StudyToolsActions()
            )
        }

        composeTestRule.onNodeWithTag("studyToolsRegenerate").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolsFeedbackLiked").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolsFeedbackDisliked").assertDoesNotExist()
    }

    @Test
    fun assertFooterDoesNotShowForOffline() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Offline, actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsRegenerate").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolsFeedbackLiked").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolsFeedbackDisliked").assertDoesNotExist()
    }

    @Test
    fun assertRegenerateIsForwarded() {
        var regenerated = 0
        composeTestRule.setContent {
            StudyToolContent(
                state = StudyToolState.Ready(summary),
                actions = StudyToolsActions(onRegenerate = { regenerated++ })
            )
        }

        composeTestRule.onNodeWithTag("studyToolsRegenerate").performClick()

        assertEquals(1, regenerated)
    }

    @Test
    fun assertFeedbackVoteIsForwarded() {
        val votes = mutableListOf<StudyToolsFeedback>()
        composeTestRule.setContent {
            StudyToolContent(
                state = StudyToolState.Ready(summary),
                actions = StudyToolsActions(onFeedback = { votes.add(it) })
            )
        }

        composeTestRule.onNodeWithTag("studyToolsFeedbackDisliked").performClick()
        composeTestRule.onNodeWithTag("studyToolsFeedbackLiked").performClick()

        assertEquals(listOf(StudyToolsFeedback.DISLIKED, StudyToolsFeedback.LIKED), votes)
    }

    @Test
    fun assertRetryableErrorOffersRetry() {
        var retried = 0
        composeTestRule.setContent {
            StudyToolContent(
                state = StudyToolState.Error("Try again later", retryable = true),
                actions = StudyToolsActions(onToolRetry = { retried++ })
            )
        }

        composeTestRule.onNodeWithTag("studyToolError").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try again later").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().assertHasClickAction().performClick()

        // Retry must not spend a daily generation busting the cache - that is what Regenerate is for.
        assertEquals(1, retried)
    }

    @Test
    fun assertNonRetryableErrorDoesNotTellTheUserToTryAgain() {
        composeTestRule.setContent {
            StudyToolContent(
                state = StudyToolState.Error(message = null, retryable = false),
                actions = StudyToolsActions()
            )
        }

        composeTestRule.onNodeWithText("This couldn't be generated for this page.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun assertRegenerateFailureKeepsTheContentAndItsFooter() {
        composeTestRule.setContent {
            StudyToolContent(
                state = StudyToolState.Ready(summary, regenerateError = "You've reached today's limit"),
                actions = StudyToolsActions()
            )
        }

        composeTestRule.onNodeWithText("Photosynthesis turns light into sugar.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolRegenerateError").assertIsDisplayed()
        composeTestRule.onNodeWithText("You've reached today's limit").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsRegenerate").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun assertNoRegenerateErrorShownWhenThereIsNone() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(summary), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolRegenerateError").assertDoesNotExist()
    }

    @Test
    fun assertRateLimitErrorHasNoRetry() {
        composeTestRule.setContent {
            StudyToolContent(
                state = StudyToolState.Error("You've reached today's limit", retryable = false),
                actions = StudyToolsActions()
            )
        }

        composeTestRule.onNodeWithText("You've reached today's limit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun assertServerErrorHeaderIsShown() {
        composeTestRule.setContent {
            StudyToolContent(
                state = StudyToolState.Error("This page has no text to summarize.", "Nothing to read"),
                actions = StudyToolsActions()
            )
        }

        composeTestRule.onNodeWithText("Nothing to read").assertIsDisplayed()
        composeTestRule.onNodeWithText("This page has no text to summarize.").assertIsDisplayed()
    }

    @Test
    fun assertOfflineStateOffersRetry() {
        var retried = 0
        composeTestRule.setContent {
            StudyToolContent(
                state = StudyToolState.Offline,
                actions = StudyToolsActions(onToolRetry = { retried++ })
            )
        }

        composeTestRule.onNodeWithTag("studyToolOffline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed().assertHasClickAction().performClick()

        assertEquals(1, retried)
    }

    @Test
    fun assertNothingRendersForAToolWithoutContentYet() {
        composeTestRule.setContent {
            StudyToolContent(state = null, actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsSkeleton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolsRegenerate").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolError").assertDoesNotExist()
    }
}
