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
 *
 */
package com.instructure.pandautils.features.studytools

import android.content.res.Resources
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Enrollment
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.AnalyticsParamConstants
import com.instructure.pandautils.R
import com.instructure.pandautils.domain.models.studytools.Flashcard
import com.instructure.pandautils.domain.models.studytools.QuizQuestion
import com.instructure.pandautils.domain.models.studytools.StudyTool
import com.instructure.pandautils.domain.models.studytools.StudyToolOption
import com.instructure.pandautils.domain.models.studytools.StudyToolResult
import com.instructure.pandautils.domain.models.studytools.StudyToolsError
import com.instructure.pandautils.domain.models.studytools.StudyToolsOutcome
import com.instructure.pandautils.domain.usecase.studytools.GetStudyToolsUseCase
import com.instructure.pandautils.domain.usecase.studytools.RunStudyToolUseCase
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NetworkStateProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class StudyToolsViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private val savedStateHandle: SavedStateHandle = mockk(relaxed = true)
    private val resources: Resources = mockk(relaxed = true)
    private val getStudyToolsUseCase: GetStudyToolsUseCase = mockk(relaxed = true)
    private val runStudyToolUseCase: RunStudyToolUseCase = mockk(relaxed = true)
    private val networkStateProvider: NetworkStateProvider = mockk(relaxed = true)
    private val analytics: Analytics = mockk(relaxed = true)

    private val course = Course(id = 1L, enrollments = mutableListOf(Enrollment(type = Enrollment.EnrollmentType.Student)))

    private val tools = listOf(
        StudyToolOption(StudyTool.SUMMARIZE, "Summarize"),
        StudyToolOption(StudyTool.QUIZ, "Quiz me"),
        StudyToolOption(StudyTool.FLASHCARDS, "Flashcards")
    )

    private val summary = StudyToolResult.Summary("This page is about photosynthesis.")
    private val quiz = StudyToolResult.Quiz(
        listOf(QuizQuestion("What is 2 + 2?", listOf("3", "4", "5"), correctAnswerIndex = 1))
    )
    private val flashcards = StudyToolResult.Flashcards(
        listOf(Flashcard("What is the capital of France?", "Paris"))
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { networkStateProvider.isOnline() } returns true
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns course

        coEvery { getStudyToolsUseCase(any()) } returns StudyToolsOutcome.Success(tools)
        coEvery { runStudyToolUseCase(any()) } returns StudyToolsOutcome.Success(summary)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    private fun getViewModel(pageUrl: String = "my-page"): StudyToolsViewModel {
        val viewModel = StudyToolsViewModel(
            savedStateHandle,
            getStudyToolsUseCase,
            runStudyToolUseCase,
            networkStateProvider,
            resources,
            analytics
        )
        viewModel.onPageResolved(pageUrl)
        return viewModel
    }

    @Test
    fun `the icon shows once the server reports tools for the page`() {
        val viewModel = getViewModel()

        assertTrue(viewModel.uiState.value.available)
    }

    @Test
    fun `the icon stays hidden when the course has no tools`() {
        coEvery { getStudyToolsUseCase(any()) } returns StudyToolsOutcome.Success(emptyList())

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.available)
    }

    @Test
    fun `the icon stays hidden when the availability check fails`() {
        coEvery { getStudyToolsUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.TemporarilyUnavailable("down"))

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.available)
    }

    @Test
    fun `the availability check is skipped while offline`() {
        every { networkStateProvider.isOnline() } returns false

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.available)
    }

    @Test
    fun `the panel is closed until the icon is tapped`() {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.panelVisible)
    }

    @Test
    fun `tapping the icon opens the panel and loads the tools`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.actions.onOpen()

        assertEquals(StudyToolsScreenState.Content(tools), viewModel.uiState.value.screenState)
    }

    @Test
    fun `opening the panel reuses the availability result instead of refetching`() {
        val viewModel = getViewModel()

        viewModel.uiState.value.actions.onOpen()

        assertEquals(StudyToolsScreenState.Content(tools), viewModel.uiState.value.screenState)
        coVerify(exactly = 1) { getStudyToolsUseCase(any()) }
    }

    @Test
    fun `retry refetches even though the availability result was cached`() {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()

        viewModel.uiState.value.actions.onRetry()

        coVerify(exactly = 2) { getStudyToolsUseCase(any()) }
    }

    @Test
    fun `an empty tool list shows the no-tools state rather than an empty menu`() {
        coEvery { getStudyToolsUseCase(any()) } returns StudyToolsOutcome.Success(emptyList())
        val viewModel = getViewModel()

        viewModel.uiState.value.actions.onOpen()

        assertEquals(StudyToolsScreenState.NoToolsAvailable, viewModel.uiState.value.screenState)
    }

    @Test
    fun `an unavailable error shows the no-tools state, not an error`() {
        coEvery { getStudyToolsUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.Unavailable("not enabled"))
        val viewModel = getViewModel()

        viewModel.uiState.value.actions.onOpen()

        assertEquals(StudyToolsScreenState.NoToolsAvailable, viewModel.uiState.value.screenState)
    }

    @Test
    fun `a transient failure shows a retryable error carrying the server message`() {
        coEvery { getStudyToolsUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.ContentProblem("Cannot read this page", "No readable content"))
        val viewModel = getViewModel()

        viewModel.uiState.value.actions.onOpen()

        assertEquals(
            StudyToolsScreenState.Error("Cannot read this page", "No readable content"),
            viewModel.uiState.value.screenState
        )
    }

    @Test
    fun `going offline between opening and loading shows the offline state`() {
        val viewModel = getViewModel()
        every { networkStateProvider.isOnline() } returns false

        viewModel.uiState.value.actions.onOpen()

        assertEquals(StudyToolsScreenState.Offline, viewModel.uiState.value.screenState)
    }

    @Test
    fun `retry reloads the tools after a failure`() {
        coEvery { getStudyToolsUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.TemporarilyUnavailable("down"))
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()

        coEvery { getStudyToolsUseCase(any()) } returns StudyToolsOutcome.Success(tools)
        viewModel.uiState.value.actions.onRetry()

        assertEquals(StudyToolsScreenState.Content(tools), viewModel.uiState.value.screenState)
    }

    @Test
    fun `selecting a tool records it so the header can swap`() {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()

        viewModel.uiState.value.actions.onToolSelected(tools[1])

        assertEquals(tools[1], viewModel.uiState.value.selectedTool)
    }

    @Test
    fun `back returns from a tool to the menu`() {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()
        viewModel.uiState.value.actions.onToolSelected(tools[0])

        viewModel.uiState.value.actions.onBack()

        assertNull(viewModel.uiState.value.selectedTool)
    }

    @Test
    fun `the ai disclosure opens and closes`() {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()

        viewModel.uiState.value.actions.onAiDisclosureClick()
        assertTrue(viewModel.uiState.value.aiDisclosureVisible)

        viewModel.uiState.value.actions.onAiDisclosureDismiss()
        assertFalse(viewModel.uiState.value.aiDisclosureVisible)
    }

    /**
     * Canary for [StudyToolsViewModel.resetPanel]: the VM is Fragment-scoped and outlives a panel
     * close, so dismissing must explicitly clear panel-only state rather than relying on the whole
     * object being discarded, as the old nested-null-as-closed model did for free.
     */
    @Test
    fun `dismissing the panel clears its state so it reopens fresh`() {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()
        viewModel.uiState.value.actions.onToolSelected(tools[0])

        viewModel.uiState.value.actions.onDismiss()
        assertFalse(viewModel.uiState.value.panelVisible)

        viewModel.uiState.value.actions.onOpen()
        assertNull(viewModel.uiState.value.selectedTool)
    }

    @Test
    fun `a callback arriving after dismissal cannot resurrect the panel`() {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()
        val staleActions = viewModel.uiState.value.actions

        viewModel.uiState.value.actions.onDismiss()
        staleActions.onAiDisclosureClick()

        assertFalse(viewModel.uiState.value.panelVisible)
    }

    @Test
    fun `the availability check uses the resolved page url`() {
        val params = mutableListOf<GetStudyToolsUseCase.Params>()
        coEvery { getStudyToolsUseCase(capture(params)) } returns StudyToolsOutcome.Success(tools)

        getViewModel(pageUrl = "my-page")

        assertEquals(1L, params.first().courseId)
        assertEquals("my-page", params.first().pageUrl)
    }

    @Test
    fun `a group context never offers study tools`() {
        every { savedStateHandle.get<CanvasContext>(Const.CANVAS_CONTEXT) } returns
            CanvasContext.getGenericContext(CanvasContext.Type.GROUP, 7L, "Group")

        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.available)
    }

    @Test
    fun `the availability check retries automatically once connectivity returns`() {
        val onlineState = MutableLiveData(false)
        every { networkStateProvider.isOnlineLiveData } returns onlineState
        every { networkStateProvider.isOnline() } returns false

        val viewModel = getViewModel()
        assertFalse(viewModel.uiState.value.available)

        every { networkStateProvider.isOnline() } returns true
        onlineState.value = true

        assertTrue(viewModel.uiState.value.available)
    }

    @Test
    fun `a rate-limited menu load is not retryable`() {
        coEvery { getStudyToolsUseCase(any()) } returns StudyToolsOutcome.Failure(StudyToolsError.RateLimited("Daily limit reached"))
        val viewModel = getViewModel()

        viewModel.uiState.value.actions.onOpen()

        val state = viewModel.uiState.value.screenState as StudyToolsScreenState.Error
        assertFalse(state.retryable)
    }

    @Test
    fun `a temporarily-unavailable menu load is retryable`() {
        coEvery { getStudyToolsUseCase(any()) } returns StudyToolsOutcome.Failure(StudyToolsError.TemporarilyUnavailable("Try again"))
        val viewModel = getViewModel()

        viewModel.uiState.value.actions.onOpen()

        val state = viewModel.uiState.value.screenState as StudyToolsScreenState.Error
        assertTrue(state.retryable)
    }

    @Test
    fun `a stale retry response cannot overwrite a newer one`() {
        val responses = listOf(
            CompletableDeferred<StudyToolsOutcome<List<StudyToolOption>>>(),
            CompletableDeferred<StudyToolsOutcome<List<StudyToolOption>>>(),
            CompletableDeferred<StudyToolsOutcome<List<StudyToolOption>>>()
        )
        var callIndex = 0
        coEvery { getStudyToolsUseCase(any()) } coAnswers { responses[callIndex++].await() }

        // Call 0: the availability check kicked off by onPageResolved inside getViewModel(). Left
        // pending - irrelevant to this test.
        val viewModel = getViewModel()

        // Call 1: availableTools is still null (call 0 hasn't resolved), so onOpen() falls through
        // to loadStudyTools().
        viewModel.uiState.value.actions.onOpen()
        // Call 2: retrying cancels call 1's still-pending job before starting this one.
        viewModel.uiState.value.actions.onRetry()

        responses[2].complete(StudyToolsOutcome.Success(tools))
        assertEquals(StudyToolsScreenState.Content(tools), viewModel.uiState.value.screenState)

        // Call 1 finally "arrives" after being cancelled - it must not clobber call 2's result.
        responses[1].complete(StudyToolsOutcome.Failure(StudyToolsError.TemporarilyUnavailable(null)))
        assertEquals(StudyToolsScreenState.Content(tools), viewModel.uiState.value.screenState)
    }

    @Test
    fun `selecting summarize generates the summary`() {
        val viewModel = openSummarize()

        assertEquals(StudyToolState.Ready(summary), viewModel.uiState.value.toolState)
    }

    @Test
    fun `the skeleton shows until the summary arrives`() {
        val pending = CompletableDeferred<StudyToolsOutcome<StudyToolResult>>()
        coEvery { runStudyToolUseCase(any()) } coAnswers { pending.await() }
        val viewModel = openSummarize()

        assertEquals(StudyToolState.Generating, viewModel.uiState.value.toolState)

        pending.complete(StudyToolsOutcome.Success(summary))

        assertEquals(StudyToolState.Ready(summary), viewModel.uiState.value.toolState)
    }

    @Test
    fun `the first generation echoes the tool prompt and does not bust the cache`() {
        val params = mutableListOf<RunStudyToolUseCase.Params>()
        coEvery { runStudyToolUseCase(capture(params)) } returns StudyToolsOutcome.Success(summary)

        openSummarize()

        assertEquals(1L, params.single().courseId)
        assertEquals("my-page", params.single().pageUrl)
        assertEquals(StudyTool.SUMMARIZE, params.single().tool)
        assertEquals("Summarize", params.single().prompt)
        assertFalse(params.single().regenerate)
    }

    @Test
    fun `regenerate asks the server to bust its 24h cache`() {
        val params = mutableListOf<RunStudyToolUseCase.Params>()
        coEvery { runStudyToolUseCase(capture(params)) } returns StudyToolsOutcome.Success(summary)
        val viewModel = openSummarize()

        viewModel.uiState.value.actions.onRegenerate()

        assertEquals(2, params.size)
        assertTrue(params.last().regenerate)
    }

    @Test
    fun `regenerate replaces the content and clears the previous vote`() {
        val viewModel = openSummarize()
        viewModel.uiState.value.actions.onFeedback(StudyToolsFeedback.LIKED)

        val regenerated = StudyToolResult.Summary("A different summary.")
        coEvery { runStudyToolUseCase(any()) } returns StudyToolsOutcome.Success(regenerated)
        viewModel.uiState.value.actions.onRegenerate()

        assertEquals(StudyToolState.Ready(regenerated), viewModel.uiState.value.toolState)
    }

    @Test
    fun `a failed regenerate keeps the summary the user was reading`() {
        val viewModel = openSummarize()

        coEvery { runStudyToolUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.RateLimited("You've reached today's limit"))
        viewModel.uiState.value.actions.onRegenerate()

        assertEquals(
            StudyToolState.Ready(summary, regenerateError = "You've reached today's limit"),
            viewModel.uiState.value.toolState
        )
    }

    @Test
    fun `a failed regenerate keeps the vote already cast`() {
        val viewModel = openSummarize()
        viewModel.uiState.value.actions.onFeedback(StudyToolsFeedback.LIKED)

        coEvery { runStudyToolUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.RateLimited("nope"))
        viewModel.uiState.value.actions.onRegenerate()

        assertEquals(
            StudyToolState.Ready(summary, StudyToolsFeedback.LIKED, regenerateError = "nope"),
            viewModel.uiState.value.toolState
        )
    }

    @Test
    fun `going offline before a regenerate keeps the content instead of blanking it`() {
        every { resources.getString(R.string.studyTools_offline) } returns "You're offline."
        val viewModel = openSummarize()
        every { networkStateProvider.isOnline() } returns false

        viewModel.uiState.value.actions.onRegenerate()

        assertEquals(
            StudyToolState.Ready(summary, regenerateError = "You're offline."),
            viewModel.uiState.value.toolState
        )
    }

    @Test
    fun `a regenerate failure without a server message falls back to local copy`() {
        every { resources.getString(R.string.studyTools_genericError) } returns "Temporarily unavailable."
        val viewModel = openSummarize()

        coEvery { runStudyToolUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.TemporarilyUnavailable(null))
        viewModel.uiState.value.actions.onRegenerate()

        assertEquals(
            StudyToolState.Ready(summary, regenerateError = "Temporarily unavailable."),
            viewModel.uiState.value.toolState
        )
    }

    @Test
    fun `a regenerate failure keeps the server header instead of dropping it`() {
        val viewModel = openSummarize()

        coEvery { runStudyToolUseCase(any()) } returns StudyToolsOutcome.Failure(
            StudyToolsError.ContentProblem("This page has no text to summarize.", "Nothing to read")
        )
        viewModel.uiState.value.actions.onRegenerate()

        assertEquals(
            StudyToolState.Ready(summary, regenerateError = "Nothing to read This page has no text to summarize."),
            viewModel.uiState.value.toolState
        )
    }

    @Test
    fun `a successful regenerate clears a previous regenerate error`() {
        val viewModel = openSummarize()
        coEvery { runStudyToolUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.RateLimited("nope"))
        viewModel.uiState.value.actions.onRegenerate()

        coEvery { runStudyToolUseCase(any()) } returns StudyToolsOutcome.Success(summary)
        viewModel.uiState.value.actions.onRegenerate()

        assertEquals(StudyToolState.Ready(summary), viewModel.uiState.value.toolState)
    }

    @Test
    fun `retrying a generation that produced nothing leaves the server cache alone`() {
        coEvery { runStudyToolUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.TemporarilyUnavailable("down"))
        val viewModel = openSummarize()

        val params = mutableListOf<RunStudyToolUseCase.Params>()
        coEvery { runStudyToolUseCase(capture(params)) } returns StudyToolsOutcome.Success(summary)
        viewModel.uiState.value.actions.onToolRetry()

        assertFalse(params.single().regenerate)
        assertEquals(StudyToolState.Ready(summary), viewModel.uiState.value.toolState)
    }

    @Test
    fun `regenerate does nothing once the panel is closed`() {
        val viewModel = openSummarize()
        val staleActions = viewModel.uiState.value.actions

        viewModel.uiState.value.actions.onDismiss()
        staleActions.onRegenerate()

        assertFalse(viewModel.uiState.value.panelVisible)
        coVerify(exactly = 1) { runStudyToolUseCase(any()) }
    }

    @Test
    fun `selecting quiz generates the quiz`() {
        coEvery { runStudyToolUseCase(any()) } returns StudyToolsOutcome.Success(quiz)

        val viewModel = openQuiz()

        assertEquals(StudyToolState.Ready(quiz), viewModel.uiState.value.toolState)
        coVerify(exactly = 1) { runStudyToolUseCase(match { it.tool == StudyTool.QUIZ }) }
    }

    @Test
    fun `selecting flashcards generates the flashcards`() {
        coEvery { runStudyToolUseCase(any()) } returns StudyToolsOutcome.Success(flashcards)

        val viewModel = openFlashcards()

        assertEquals(StudyToolState.Ready(flashcards), viewModel.uiState.value.toolState)
        coVerify(exactly = 1) { runStudyToolUseCase(match { it.tool == StudyTool.FLASHCARDS }) }
    }

    @Test
    fun `an upstream outage offers a retry`() {
        coEvery { runStudyToolUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.TemporarilyUnavailable("Try again later"))

        val viewModel = openSummarize()

        assertEquals(
            StudyToolState.Error("Try again later", retryable = true),
            viewModel.uiState.value.toolState
        )
    }

    @Test
    fun `the daily limit is reported without a retry`() {
        coEvery { runStudyToolUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.RateLimited("You've reached today's limit"))

        val viewModel = openSummarize()

        assertEquals(
            StudyToolState.Error("You've reached today's limit", retryable = false),
            viewModel.uiState.value.toolState
        )
    }

    @Test
    fun `an unreadable page surfaces both the server header and body`() {
        coEvery { runStudyToolUseCase(any()) } returns StudyToolsOutcome.Failure(
            StudyToolsError.ContentProblem("This page has no text to summarize.", "Nothing to read")
        )

        val viewModel = openSummarize()

        assertEquals(
            StudyToolState.Error("This page has no text to summarize.", "Nothing to read", retryable = false),
            viewModel.uiState.value.toolState
        )
    }

    @Test
    fun `retrying a failed generation can succeed`() {
        coEvery { runStudyToolUseCase(any()) } returns
            StudyToolsOutcome.Failure(StudyToolsError.TemporarilyUnavailable("down"))
        val viewModel = openSummarize()

        coEvery { runStudyToolUseCase(any()) } returns StudyToolsOutcome.Success(summary)
        viewModel.uiState.value.actions.onRegenerate()

        assertEquals(StudyToolState.Ready(summary), viewModel.uiState.value.toolState)
    }

    @Test
    fun `going offline before generating never fires the request`() {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()
        every { networkStateProvider.isOnline() } returns false

        viewModel.uiState.value.actions.onToolSelected(tools[0])

        assertEquals(StudyToolState.Offline, viewModel.uiState.value.toolState)
        coVerify(exactly = 0) { runStudyToolUseCase(any()) }
    }

    @Test
    fun `going back to the menu drops the generated content`() {
        val viewModel = openSummarize()

        viewModel.uiState.value.actions.onBack()

        assertNull(viewModel.uiState.value.selectedTool)
        assertNull(viewModel.uiState.value.toolState)
    }

    @Test
    fun `a result for an abandoned tool cannot replace what is on screen`() {
        val pending = CompletableDeferred<StudyToolsOutcome<StudyToolResult>>()
        coEvery { runStudyToolUseCase(any()) } coAnswers { pending.await() }
        val viewModel = openSummarize()

        viewModel.uiState.value.actions.onBack()
        pending.complete(StudyToolsOutcome.Success(summary))

        assertNull(viewModel.uiState.value.toolState)
    }

    @Test
    fun `a vote is recorded and reported to pendo`() {
        val viewModel = openSummarize()

        viewModel.uiState.value.actions.onFeedback(StudyToolsFeedback.DISLIKED)

        assertEquals(
            StudyToolState.Ready(summary, StudyToolsFeedback.DISLIKED),
            viewModel.uiState.value.toolState
        )
        verify(exactly = 1) { analytics.logEvent(AnalyticsEventConstants.STUDY_ASSIST_FEEDBACK, any()) }
    }

    @Test
    fun `repeating the same vote is not counted twice`() {
        val viewModel = openSummarize()

        viewModel.uiState.value.actions.onFeedback(StudyToolsFeedback.LIKED)
        viewModel.uiState.value.actions.onFeedback(StudyToolsFeedback.LIKED)

        verify(exactly = 1) { analytics.logEvent(AnalyticsEventConstants.STUDY_ASSIST_FEEDBACK, any()) }
    }

    @Test
    fun `changing the vote reports it again`() {
        val viewModel = openSummarize()

        viewModel.uiState.value.actions.onFeedback(StudyToolsFeedback.LIKED)
        viewModel.uiState.value.actions.onFeedback(StudyToolsFeedback.DISLIKED)

        assertEquals(
            StudyToolState.Ready(summary, StudyToolsFeedback.DISLIKED),
            viewModel.uiState.value.toolState
        )
        verify(exactly = 2) { analytics.logEvent(AnalyticsEventConstants.STUDY_ASSIST_FEEDBACK, any()) }
    }

    @Test
    fun `voting before the content arrives is ignored`() {
        coEvery { runStudyToolUseCase(any()) } coAnswers {
            CompletableDeferred<StudyToolsOutcome<StudyToolResult>>().await()
        }
        val viewModel = openSummarize()

        viewModel.uiState.value.actions.onFeedback(StudyToolsFeedback.LIKED)

        assertEquals(StudyToolState.Generating, viewModel.uiState.value.toolState)
        verify(exactly = 0) { analytics.logEvent(AnalyticsEventConstants.STUDY_ASSIST_FEEDBACK, any()) }
    }

    @Test
    fun `the vote values match the ones canvas web reports`() {
        assertEquals("vote", AnalyticsParamConstants.VOTE)
        assertEquals("liked", StudyToolsFeedback.LIKED.vote)
        assertEquals("disliked", StudyToolsFeedback.DISLIKED.vote)
    }

    /**
     * The event payload is what lands in the shared Pendo funnel with Canvas web, so the key and value
     * the ViewModel actually writes matter as much as the event name. Bundle is an android.jar stub in
     * plain JVM tests (`returnDefaultValues = true`), so its writes are intercepted rather than read back.
     */
    @Test
    fun `the feedback event is keyed on vote and carries the wire value`() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putString(any(), any()) } just Runs
        val viewModel = openSummarize()

        viewModel.uiState.value.actions.onFeedback(StudyToolsFeedback.DISLIKED)

        verify(exactly = 1) { anyConstructed<Bundle>().putString("vote", "disliked") }
    }

    private fun openSummarize(): StudyToolsViewModel {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()
        viewModel.uiState.value.actions.onToolSelected(tools[0])
        return viewModel
    }

    private fun openQuiz(): StudyToolsViewModel {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()
        viewModel.uiState.value.actions.onToolSelected(tools[1])
        return viewModel
    }

    private fun openFlashcards(): StudyToolsViewModel {
        val viewModel = getViewModel()
        viewModel.uiState.value.actions.onOpen()
        viewModel.uiState.value.actions.onToolSelected(tools[2])
        return viewModel
    }
}
