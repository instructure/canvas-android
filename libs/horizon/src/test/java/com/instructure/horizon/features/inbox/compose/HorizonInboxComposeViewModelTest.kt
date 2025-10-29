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
package com.instructure.horizon.features.inbox.compose

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.horizon.features.inbox.InboxEventHandler
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachment
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachmentState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HorizonInboxComposeViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val repository: HorizonInboxComposeRepository = mockk(relaxed = true)
    private val inboxEventHandler: InboxEventHandler = InboxEventHandler()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCourses = listOf(
        Course(id = 1L, name = "Course 1"),
        Course(id = 2L, name = "Course 2")
    )

    private val testRecipients = listOf(
        Recipient(stringId = "1", name = "Recipient 1"),
        Recipient(stringId = "2", name = "Recipient 2")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getAllInboxCourses(any()) } returns testCourses
        coEvery { repository.getRecipients(any(), any()) } returns testRecipients
        coEvery { repository.createConversation(any(), any(), any(), any(), any(), any()) } returns Unit
        coEvery { repository.invalidateConversationListCachedResponse() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test ViewModel initializes with course list`() = runTest {
        val viewModel = getViewModel()

        assertFalse(viewModel.uiState.value.coursePickerOptions.isEmpty())
        assertEquals(2, viewModel.uiState.value.coursePickerOptions.size)
        coVerify { repository.getAllInboxCourses(forceNetwork = true) }
    }

    @Test
    fun `Test single course is auto-selected`() = runTest {
        coEvery { repository.getAllInboxCourses(any()) } returns listOf(testCourses.first())

        val viewModel = getViewModel()

        assertNotNull(viewModel.uiState.value.selectedCourse)
        assertEquals(1L, viewModel.uiState.value.selectedCourse?.id)
    }

    @Test
    fun `Test multiple courses not auto-selected`() = runTest {
        val viewModel = getViewModel()

        assertNull(viewModel.uiState.value.selectedCourse)
    }

    @Test
    fun `Test course selection updates state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onCourseSelected(testCourses.first())

        assertEquals(1L, viewModel.uiState.value.selectedCourse?.id)
        assertNull(viewModel.uiState.value.courseErrorMessage)
    }

    @Test
    fun `Test recipient search query change triggers fetch`() = runTest {
        val viewModel = getViewModel()
        viewModel.uiState.value.onCourseSelected(testCourses.first())

        viewModel.uiState.value.onRecipientSearchQueryChanged(TextFieldValue("test"))

        assertEquals("test", viewModel.uiState.value.recipientSearchQuery.text)
    }

    @Test
    fun `Test recipient selection adds to list`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onRecipientSelected(testRecipients.first())

        assertTrue(viewModel.uiState.value.selectedRecipients.contains(testRecipients.first()))
        assertEquals("", viewModel.uiState.value.recipientSearchQuery.text)
        assertNull(viewModel.uiState.value.recipientErrorMessage)
    }

    @Test
    fun `Test recipient removal updates list`() = runTest {
        val viewModel = getViewModel()
        viewModel.uiState.value.onRecipientSelected(testRecipients.first())

        viewModel.onRecipientRemoved(testRecipients.first())

        assertFalse(viewModel.uiState.value.selectedRecipients.contains(testRecipients.first()))
    }

    @Test
    fun `Test send individually checkbox updates state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSendIndividuallyChanged(true)

        assertTrue(viewModel.uiState.value.isSendIndividually)
    }

    @Test
    fun `Test subject change updates state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Test Subject"))

        assertEquals("Test Subject", viewModel.uiState.value.subject.text)
        assertNull(viewModel.uiState.value.subjectErrorMessage)
    }

    @Test
    fun `Test body change updates state`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onBodyChanged(TextFieldValue("Test Body"))

        assertEquals("Test Body", viewModel.uiState.value.body.text)
        assertNull(viewModel.uiState.value.bodyErrorMessage)
    }

    @Test
    fun `Test send conversation validates required fields`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onSendConversation({})

        assertNotNull(viewModel.uiState.value.courseErrorMessage)
        assertNotNull(viewModel.uiState.value.recipientErrorMessage)
        assertNotNull(viewModel.uiState.value.subjectErrorMessage)
        assertNotNull(viewModel.uiState.value.bodyErrorMessage)
    }

    @Test
    fun `Test send conversation with valid data succeeds`() = runTest {
        val viewModel = getViewModel()
        viewModel.uiState.value.onCourseSelected(testCourses.first())
        viewModel.uiState.value.onRecipientSelected(testRecipients.first())
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onBodyChanged(TextFieldValue("Body"))

        var finished = false
        viewModel.uiState.value.onSendConversation { finished = true }

        coVerify { repository.createConversation(any(), any(), any(), any(), any(), any()) }
        assertTrue(finished)
    }

    @Test
    fun `Test send conversation validates attachments are uploaded`() = runTest {
        val viewModel = getViewModel()
        val failedAttachment = HorizonInboxAttachment(
            id = 1L,
            fileName = "test.pdf",
            fileSize = 1000L,
            filePath = "/path",
            state = HorizonInboxAttachmentState.Error
        )
        viewModel.uiState.value.onCourseSelected(testCourses.first())
        viewModel.uiState.value.onRecipientSelected(testRecipients.first())
        viewModel.uiState.value.onSubjectChanged(TextFieldValue("Subject"))
        viewModel.uiState.value.onBodyChanged(TextFieldValue("Body"))
        viewModel.uiState.value.onAttachmentsChanged(listOf(failedAttachment))

        viewModel.uiState.value.onSendConversation({})

        assertNotNull(viewModel.uiState.value.attachmentsErrorMessage)
    }

    @Test
    fun `Test attachment picker visibility toggle`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.onShowAttachmentPickerChanged(true)

        assertTrue(viewModel.uiState.value.showAttachmentPicker)
    }

    @Test
    fun `Test attachments change updates state`() = runTest {
        val viewModel = getViewModel()
        val attachment = HorizonInboxAttachment(
            id = 1L,
            fileName = "test.pdf",
            fileSize = 1000L,
            filePath = "/path",
            state = HorizonInboxAttachmentState.Success
        )

        viewModel.uiState.value.onAttachmentsChanged(listOf(attachment))

        assertEquals(1, viewModel.uiState.value.attachments.size)
        assertEquals("test.pdf", viewModel.uiState.value.attachments.first().fileName)
    }

    @Test
    fun `Test exit confirmation dialog visibility toggle`() = runTest {
        val viewModel = getViewModel()

        viewModel.uiState.value.updateShowExitConfirmationDialog(true)

        assertTrue(viewModel.uiState.value.showExitConfirmationDialog)
    }

    @Test
    fun `Test snackbar dismiss clears message`() = runTest {
        coEvery { repository.getAllInboxCourses(any()) } throws Exception("Error")

        val viewModel = getViewModel()
        assertNotNull(viewModel.uiState.value.snackbarMessage)

        viewModel.uiState.value.onDismissSnackbar()

        assertNull(viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `Test fetch recipients with valid course`() = runTest {
        val viewModel = getViewModel()
        viewModel.uiState.value.onCourseSelected(testCourses.first())
        viewModel.uiState.value.onRecipientSearchQueryChanged(TextFieldValue("test query"))

        // Wait for debounce
        delay(250)

        coVerify { repository.getRecipients(courseId = 1L, searchQuery = "test query", any()) }
    }

    private fun getViewModel(): HorizonInboxComposeViewModel {
        return HorizonInboxComposeViewModel(repository, context, inboxEventHandler)
    }
}
