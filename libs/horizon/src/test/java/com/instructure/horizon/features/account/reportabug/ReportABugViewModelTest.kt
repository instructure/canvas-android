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
package com.instructure.horizon.features.account.reportabug

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.apis.ErrorReportAPI
import com.instructure.canvasapi2.models.ErrorReportResult
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.R
import com.instructure.horizon.features.account.AccountEvent
import com.instructure.horizon.features.account.AccountEventHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportABugViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: ReportABugRepository = mockk(relaxed = true)
    private val apiPrefs: ApiPrefs = mockk(relaxed = true)
    private val accountEventHandler: AccountEventHandler = AccountEventHandler()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUser = User(
        id = 1L,
        name = "Test User",
        email = "test@example.com"
    )

    private val testErrorReportResult = ErrorReportResult(
        id = 12345L,
        logged = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { apiPrefs.user } returns testUser
        every { context.getString(R.string.reportAProblemTopicRequired) } returns "Topic is required"
        every { context.getString(R.string.reportAProblemSubjectRequired) } returns "Subject is required"
        every { context.getString(R.string.reportAProblemDescriptionRequired) } returns "Description is required"
        every { context.getString(R.string.reportAProblemSuccessMessage) } returns "Your ticket was submitted."
        every { context.getString(R.string.reportAProblemErrorMessage) } returns "Your ticket couldn't be submitted. Please try again."
        every { context.getString(R.string.reportAProblemTopicSuggestion) } returns "Suggestion or comment"
        every { context.getString(R.string.reportAProblemTopicGeneralHelp) } returns "General help"
        every { context.getString(R.string.reportAProblemTopicMinorIssue) } returns "Minor issue"
        every { context.getString(R.string.reportAProblemTopicUrgentIssue) } returns "Urgent issue"
        every { context.getString(R.string.reportAProblemTopicCriticalError) } returns "Critical system error"

        coEvery { repository.submitErrorReport(any(), any(), any(), any()) } returns testErrorReportResult
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state has callbacks populated`() = runTest {
        val viewModel = getViewModel()

        assertNotNull(viewModel.uiState.value.onTopicSelected)
        assertNotNull(viewModel.uiState.value.onTopicMenuOpenChanged)
        assertNotNull(viewModel.uiState.value.onSubjectChanged)
        assertNotNull(viewModel.uiState.value.onDescriptionChanged)
        assertNotNull(viewModel.uiState.value.onSubmit)
        assertNotNull(viewModel.uiState.value.onSnackbarDismissed)
    }

    @Test
    fun `initial state has empty form fields`() = runTest {
        val viewModel = getViewModel()

        assertNull(viewModel.uiState.value.selectedTopic)
        assertEquals("", viewModel.uiState.value.subject.text)
        assertEquals("", viewModel.uiState.value.description.text)
    }

    @Test
    fun `initial state is not loading`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `updating topic sets selectedTopic`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")

        assertEquals("Minor issue", viewModel.uiState.value.selectedTopic)
    }

    @Test
    fun `updating topic clears topicError`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSubmit()
        viewModel.uiState.value.onTopicSelected("Minor issue")

        assertNull(viewModel.uiState.value.topicError)
    }

    @Test
    fun `updating topic menu open state changes isTopicMenuOpen`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicMenuOpenChanged(true)
        assertTrue(viewModel.uiState.value.isTopicMenuOpen)

        viewModel.uiState.value.onTopicMenuOpenChanged(false)
        assertFalse(viewModel.uiState.value.isTopicMenuOpen)
    }

    @Test
    fun `updating subject sets subject`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Test Subject"))

        assertEquals("Test Subject", viewModel.uiState.value.subject.text)
    }

    @Test
    fun `updating subject clears subjectError`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSubmit()
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Test Subject"))

        assertNull(viewModel.uiState.value.subjectError)
    }

    @Test
    fun `updating description sets description`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Test Description"))

        assertEquals("Test Description", viewModel.uiState.value.description.text)
    }

    @Test
    fun `updating description clears descriptionError`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSubmit()
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Test Description"))

        assertNull(viewModel.uiState.value.descriptionError)
    }

    @Test
    fun `submit with empty topic shows topicError`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertEquals("Topic is required", viewModel.uiState.value.topicError)
    }

    @Test
    fun `submit with blank subject shows subjectError`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertEquals("Subject is required", viewModel.uiState.value.subjectError)
    }

    @Test
    fun `submit with blank description shows descriptionError`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onSubmit()

        assertEquals("Description is required", viewModel.uiState.value.descriptionError)
    }

    @Test
    fun `submit with all fields invalid shows all errors`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSubmit()

        assertEquals("Topic is required", viewModel.uiState.value.topicError)
        assertEquals("Subject is required", viewModel.uiState.value.subjectError)
        assertEquals("Description is required", viewModel.uiState.value.descriptionError)
    }

    @Test
    fun `submit with valid fields does not show errors`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertNull(viewModel.uiState.value.topicError)
        assertNull(viewModel.uiState.value.subjectError)
        assertNull(viewModel.uiState.value.descriptionError)
    }

    @Test
    fun `Suggestion or comment maps to COMMENT severity`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Suggestion or comment")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        coVerify {
            repository.submitErrorReport(
                subject = any(),
                description = any(),
                email = any(),
                severity = ErrorReportAPI.Severity.COMMENT.tag
            )
        }
    }

    @Test
    fun `General help maps to NOT_URGENT severity`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("General help")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        coVerify {
            repository.submitErrorReport(
                subject = any(),
                description = any(),
                email = any(),
                severity = ErrorReportAPI.Severity.NOT_URGENT.tag
            )
        }
    }

    @Test
    fun `Minor issue maps to WORKAROUND_POSSIBLE severity`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        coVerify {
            repository.submitErrorReport(
                subject = any(),
                description = any(),
                email = any(),
                severity = ErrorReportAPI.Severity.WORKAROUND_POSSIBLE.tag
            )
        }
    }

    @Test
    fun `Urgent issue maps to BLOCKING severity`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Urgent issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        coVerify {
            repository.submitErrorReport(
                subject = any(),
                description = any(),
                email = any(),
                severity = ErrorReportAPI.Severity.BLOCKING.tag
            )
        }
    }

    @Test
    fun `Critical system error maps to CRITICAL severity`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Critical system error")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        coVerify {
            repository.submitErrorReport(
                subject = any(),
                description = any(),
                email = any(),
                severity = ErrorReportAPI.Severity.CRITICAL.tag
            )
        }
    }

    @Test
    fun `successful submit sets isLoading to true during call`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successful submit calls repository with correct parameters`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Test Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Test Description"))
        viewModel.uiState.value.onSubmit()

        coVerify {
            repository.submitErrorReport(
                subject = "Test Subject",
                description = "Test Description",
                email = "test@example.com",
                severity = ErrorReportAPI.Severity.WORKAROUND_POSSIBLE.tag
            )
        }
    }

    @Test
    fun `successful submit sets shouldNavigateBack to true`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertTrue(viewModel.uiState.value.shouldNavigateBack)
    }

    @Test
    fun `successful submit sets isLoading to false after completion`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successful submit uses email from ApiPrefs`() = runTest {
        val user = testUser.copy(email = "custom@example.com")
        every { apiPrefs.user } returns user
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        coVerify {
            repository.submitErrorReport(
                subject = any(),
                description = any(),
                email = "custom@example.com",
                severity = any()
            )
        }
    }

    @Test
    fun `failed submit sets snackbarMessage on error`() = runTest {
        coEvery { repository.submitErrorReport(any(), any(), any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertEquals("Your ticket couldn't be submitted. Please try again.", viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `failed submit sets isLoading to false after error`() = runTest {
        coEvery { repository.submitErrorReport(any(), any(), any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `failed submit does not set shouldNavigateBack`() = runTest {
        coEvery { repository.submitErrorReport(any(), any(), any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertFalse(viewModel.uiState.value.shouldNavigateBack)
    }

    @Test
    fun `failed submit does not post event to AccountEventHandler`() = runTest {
        val events = mutableListOf<AccountEvent>()
        val job = launch {
            accountEventHandler.events.collect { events.add(it) }
        }

        coEvery { repository.submitErrorReport(any(), any(), any(), any()) } throws Exception("Network error")
        val viewModel = getViewModel()

        viewModel.uiState.value.onTopicSelected("Minor issue")
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onDescriptionChanged(TextFieldValue("Description"))
        viewModel.uiState.value.onSubmit()

        assertEquals(0, events.size)

        job.cancel()
    }

    private fun getViewModel(): ReportABugViewModel {
        return ReportABugViewModel(
            context,
            repository,
            apiPrefs,
            accountEventHandler
        )
    }
}
