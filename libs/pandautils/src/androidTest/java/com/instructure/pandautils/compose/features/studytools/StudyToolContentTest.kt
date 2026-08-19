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

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.instructure.pandautils.domain.models.studytools.Flashcard
import com.instructure.pandautils.domain.models.studytools.QuizQuestion
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import com.instructure.pandautils.features.studytools.StudyToolState
import com.instructure.pandautils.features.studytools.StudyToolsActions
import com.instructure.pandautils.features.studytools.StudyToolsFeedback
import com.instructure.pandautils.features.studytools.composables.StudyToolContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyToolContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val summary = StudyToolResult.Summary("Photosynthesis turns light into sugar.")

    private val quiz = StudyToolResult.Quiz(
        listOf(
            QuizQuestion("What is 2 + 2?", listOf("3", "4", "5"), correctAnswerIndex = 1),
            QuizQuestion("What is 3 + 3?", listOf("5", "6", "7"), correctAnswerIndex = 1)
        )
    )

    private val flashcards = StudyToolResult.Flashcards(
        listOf(
            Flashcard("What is the capital of France?", "Paris"),
            Flashcard("What is 2 + 2?", "4")
        )
    )

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

    @Test
    fun assertQuizRendersFirstQuestionAndItsAnswers() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithText("What is 2 + 2?").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_2").assertIsDisplayed()
    }

    @Test
    fun assertLandscapeShowsAnswersPairedIntoTwoColumns() {
        composeTestRule.setContent {
            // Copy the real ambient configuration rather than constructing a bare Configuration(),
            // so unrelated fields Compose relies on (density, font scale, ...) stay valid - only
            // orientation is overridden.
            val landscapeConfiguration = Configuration(LocalConfiguration.current).apply {
                orientation = Configuration.ORIENTATION_LANDSCAPE
            }
            CompositionLocalProvider(LocalConfiguration provides landscapeConfiguration) {
                StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
            }
        }

        val answer0Top = composeTestRule.onNodeWithTag("studyToolsQuizAnswer_0").fetchSemanticsNode().boundsInRoot.top
        val answer1Top = composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").fetchSemanticsNode().boundsInRoot.top
        val answer2Top = composeTestRule.onNodeWithTag("studyToolsQuizAnswer_2").fetchSemanticsNode().boundsInRoot.top

        // Answers 0 and 1 share the first row; answer 2 starts a new row below it.
        assertEquals(answer0Top, answer1Top, 0.5f)
        assertTrue(answer2Top > answer0Top)
    }

    @Test
    fun assertPortraitKeepsAnswersInOneColumn() {
        composeTestRule.setContent {
            val portraitConfiguration = Configuration(LocalConfiguration.current).apply {
                orientation = Configuration.ORIENTATION_PORTRAIT
            }
            CompositionLocalProvider(LocalConfiguration provides portraitConfiguration) {
                StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
            }
        }

        val answer0Top = composeTestRule.onNodeWithTag("studyToolsQuizAnswer_0").fetchSemanticsNode().boundsInRoot.top
        val answer1Top = composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").fetchSemanticsNode().boundsInRoot.top

        assertTrue(answer1Top > answer0Top)
    }

    @Test
    fun assertSelectingAQuizAnswerMarksItSelected() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").performClick()

        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").assertIsSelected()
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_0").assertIsNotSelected()
    }

    @Test
    fun assertCheckAnswerIsDisabledUntilAnAnswerIsPicked() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").assertIsNotEnabled()

        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").performClick()

        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").assertIsEnabled()
    }

    @Test
    fun assertCheckingRevealsCorrectAndIncorrectAndLocksTheQuestion() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
        }

        // Answer 0 ("3") is wrong; answer 1 ("4") is correct.
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_0").performClick()
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick()

        // `selected` tracks the pick, not correctness; `stateDescription` is what actually
        // distinguishes correct from incorrect - assertIsSelected() alone can't tell them apart.
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_0")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Incorrect"))
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1")
            .assertIsNotSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Correct"))

        // Locked: the untouched distractor is disabled and picking it after checking does nothing.
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_2").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_2").performClick()
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_2").assertIsNotSelected()
    }

    @Test
    fun assertPickingTheCorrectAnswerMarksItSelectedAndCorrect() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
        }

        // Answer 1 ("4") is correct and is the one picked - the case the row's `selected` param
        // must get right too, not just the picked-wrong case covered above.
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").performClick()
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick()

        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1")
            .assertIsSelected()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Correct"))
    }

    @Test
    fun assertNextQuestionAdvancesAndResetsSelection() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").performClick()
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick() // Check answer
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick() // Next question

        composeTestRule.onNodeWithText("What is 3 + 3?").assertIsDisplayed()
        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_0").assertIsNotSelected()
    }

    @Test
    fun assertQuizProgressAdvances() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithText("Question 1 of 2").assertIsDisplayed()

        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").performClick()
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick() // Check answer
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick() // Next question

        composeTestRule.onNodeWithText("Question 2 of 2").assertIsDisplayed()
    }

    @Test
    fun assertCheckingTheLastQuestionShowsCompletionInsteadOfADeadEndButton() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(quiz), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").performClick()
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick() // Check answer
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick() // Next question

        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").performClick()
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick() // Check final answer

        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolsQuizCompleted").assertIsDisplayed()
    }

    @Test
    fun assertASingleQuestionQuizReachesCompletionOnTheFirstCheck() {
        val singleQuestionQuiz = StudyToolResult.Quiz(
            listOf(QuizQuestion("What is 2 + 2?", listOf("3", "4", "5"), correctAnswerIndex = 1))
        )
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(singleQuestionQuiz), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsQuizAnswer_1").performClick()
        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").performClick()

        composeTestRule.onNodeWithTag("studyToolsQuizActionButton").assertDoesNotExist()
        composeTestRule.onNodeWithTag("studyToolsQuizCompleted").assertIsDisplayed()
    }

    @Test
    fun assertFlashcardShowsItsQuestionByDefault() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(flashcards), actions = StudyToolsActions())
        }

        // The card is a single clearAndSetSemantics node - its face text is exposed as
        // contentDescription, not as a separately-matchable text node.
        composeTestRule.onNodeWithContentDescription("What is the capital of France?").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Paris").assertDoesNotExist()
    }

    @Test
    fun assertTappingTheFlashcardFlipsItToItsAnswer() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(flashcards), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsFlashcard").performClick()

        composeTestRule.onNodeWithContentDescription("Paris").assertIsDisplayed()
    }

    @Test
    fun assertSwipingTheDeckShowsTheNextCard() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(flashcards), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsFlashcardsPager").performTouchInput { swipeLeft() }

        composeTestRule.onNodeWithContentDescription("What is 2 + 2?").assertIsDisplayed()
    }

    @Test
    fun assertFlipStatePersistsAfterNavigatingAwayAndBack() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(flashcards), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsFlashcard").performClick() // flip card 1 to its answer
        composeTestRule.onNodeWithTag("studyToolsFlashcardsPager").performTouchInput { swipeLeft() } // to card 2
        composeTestRule.onNodeWithTag("studyToolsFlashcardsPager").performTouchInput { swipeRight() } // back to card 1

        composeTestRule.onNodeWithContentDescription("Paris").assertIsDisplayed()
    }

    @Test
    fun assertFlippingOneCardDoesNotFlipTheNextCard() {
        composeTestRule.setContent {
            StudyToolContent(state = StudyToolState.Ready(flashcards), actions = StudyToolsActions())
        }

        composeTestRule.onNodeWithTag("studyToolsFlashcard").performClick() // flip card 1 to its answer
        composeTestRule.onNodeWithTag("studyToolsFlashcardsPager").performTouchInput { swipeLeft() } // to card 2

        // Card 2 must still show its own question - flip state is per-card, not shared.
        composeTestRule.onNodeWithContentDescription("What is 2 + 2?").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("4").assertDoesNotExist()
    }
}
