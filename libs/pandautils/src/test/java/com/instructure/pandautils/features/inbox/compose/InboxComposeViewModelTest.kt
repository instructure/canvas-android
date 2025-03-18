/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.features.inbox.compose

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Conversation
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Message
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.features.inbox.utils.AttachmentCardItem
import com.instructure.pandautils.features.inbox.utils.AttachmentStatus
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptions
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDefaultValues
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsDisabledFields
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsHiddenFields
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsMode
import com.instructure.pandautils.features.inbox.utils.InboxComposeOptionsPreviousMessages
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.utils.FileDownloader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class InboxComposeViewModelTest {
    private val context: Context = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val inboxComposeRepository: InboxComposeRepository = mockk(relaxed = true)
    private val attachmentDao: AttachmentDao = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        ContextKeeper.appContext = context

        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        coEvery { inboxComposeRepository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { inboxComposeRepository.getGroups(any()) } returns DataResult.Success(emptyList())
        coEvery { inboxComposeRepository.getRecipients(any(), any(), any()) } returns DataResult.Success(emptyList())
        coEvery { context.getString(R.string.messageSentSuccessfully) } returns "Message sent successfully."
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `Test initial state`() {
        val viewmodel = getViewModel()
        val uiState = viewmodel.uiState.value

        assertEquals(null, uiState.selectContextUiState.selectedCanvasContext)
        assertEquals(emptyList<Recipient>(), uiState.recipientPickerUiState.selectedRecipients)
        assertEquals(InboxComposeScreenOptions.None, uiState.screenOption)
        assertEquals(false, uiState.sendIndividual)
        assertEquals(TextFieldValue(""), uiState.subject)
        assertEquals(TextFieldValue(""), uiState.body)
        assertEquals(ScreenState.Data, uiState.screenState)
    }

    @Test
    fun `Signature footer added on init`() {
        coEvery { inboxComposeRepository.getInboxSignature() } returns "Signature"
        val viewmodel = getViewModel()
        val uiState = viewmodel.uiState.value

        assertEquals("\n\n---\nSignature", uiState.body.text)
    }

    @Test
    fun `Signature footer not added on init when it is blank`() {
        coEvery { inboxComposeRepository.getInboxSignature() } returns ""
        val viewmodel = getViewModel()
        val uiState = viewmodel.uiState.value

        assertEquals("", uiState.body.text)
    }

    @Test
    fun `Load available contexts on init`() {
        val viewmodel = getViewModel()

        coVerify(exactly = 1) { inboxComposeRepository.getCourses(any()) }
        coVerify(exactly = 1) { inboxComposeRepository.getGroups(any()) }
    }

    @Test
    fun `Load Recipients on Context selection`() {
        val viewModel = getViewModel()
        val courseId: Long = 1
        val recipients = listOf(
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.StudentEnrollment.rawValue))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.ObserverEnrollment.rawValue))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TaEnrollment.rawValue)))
        )
        coEvery { inboxComposeRepository.getRecipients(any(), any(), any()) } returns DataResult.Success(recipients)
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        viewModel.handleAction(ContextPickerActionHandler.ContextClicked(Course(id = courseId)))

        assertEquals(recipients[0], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.StudentEnrollment]?.first())
        assertEquals(recipients[1], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.TeacherEnrollment]?.first())
        assertEquals(recipients[2], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.ObserverEnrollment]?.first())
        assertEquals(recipients[3], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.TaEnrollment]?.first())

    }

    @Test
    fun `Test Recipient list on Role selection`() {
        val viewModel = getViewModel()
        val courseId: Long = 1
        val recipients = listOf(
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.StudentEnrollment.rawValue))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.ObserverEnrollment.rawValue))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TaEnrollment.rawValue))
            )
        )
        coEvery { inboxComposeRepository.getRecipients(any(), any(), any()) } returns DataResult.Success(recipients)
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        viewModel.handleAction(ContextPickerActionHandler.ContextClicked(Course(id = courseId)))
        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.StudentEnrollment))

        assertEquals(recipients[0], viewModel.uiState.value.recipientPickerUiState.recipientsToShow.first())
    }

    @Test
    fun `Test if All Recipients show up`() = runTest {
        val courseId: Long = 1
        val course = Course(id = courseId, name = "Course")
        val recipients = listOf(
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.StudentEnrollment.rawValue))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.ObserverEnrollment.rawValue))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TaEnrollment.rawValue))
            )
        )
        coEvery { inboxComposeRepository.getRecipients(any(), any(), any()) } returns DataResult.Success(recipients)
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(true)
        coEvery { inboxComposeRepository.getCourses(any()) } returns DataResult.Success(emptyList())
        coEvery { inboxComposeRepository.getGroups(any()) } returns DataResult.Success(emptyList())
        val viewModel = getViewModel()

        viewModel.handleAction(ContextPickerActionHandler.ContextClicked(course))

        val expectedAllCourseRecipient = Recipient(
            stringId = course.contextId,
            name = "All in Course"
        )
        assertEquals(expectedAllCourseRecipient, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.StudentEnrollment))
        val expectedAllStudentsRecipient = Recipient(
            stringId = "${course.contextId}_students",
            name = "All in Students"
        )
        assertEquals(expectedAllStudentsRecipient, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RecipientBackClicked)

        //Wait for debounce
        delay(500)

        assertEquals(expectedAllCourseRecipient, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.TeacherEnrollment))
        val expectedAllTeachersRecipient = Recipient(
            stringId = "${course.contextId}_teachers",
            name = "All in Teachers"
        )
        assertEquals(expectedAllTeachersRecipient, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)
    }

    @Test
    fun `Test if All Recipients is not allowed to show`() {
        val viewModel = getViewModel()
        val courseId: Long = 1
        val course = Course(id = courseId, name = "Course")
        val recipients = listOf(
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.StudentEnrollment.rawValue))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.ObserverEnrollment.rawValue))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TaEnrollment.rawValue))
            )
        )
        coEvery { inboxComposeRepository.getRecipients(any(), any(), any()) } returns DataResult.Success(recipients)
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        viewModel.handleAction(ContextPickerActionHandler.ContextClicked(course))

        assertEquals(null, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.StudentEnrollment))
        assertEquals(null, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RecipientBackClicked)
        assertEquals(null, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.StudentEnrollment))
        assertEquals(null, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)
    }

    //region Inbox Compose action handler
    @Test
    fun `Cancel action handler`() {
        val viewModel = getViewModel()
        assertEquals(false, viewModel.uiState.value.showConfirmationDialog)

        viewModel.handleAction(InboxComposeActionHandler.CancelDismissDialog(true))
        assertEquals(true, viewModel.uiState.value.showConfirmationDialog)

        viewModel.handleAction(InboxComposeActionHandler.CancelDismissDialog(false))
        assertEquals(false, viewModel.uiState.value.showConfirmationDialog)
    }

    @Test
    fun `Open Context Picker action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(InboxComposeActionHandler.OpenContextPicker)

        assertEquals(InboxComposeScreenOptions.ContextPicker, viewmodel.uiState.value.screenOption)
    }

    @Test
    fun `Remove Recipient action handler`() {
        val recipient1 = Recipient(stringId = "1")
        val recipient2 = Recipient(stringId = "2")
        val viewmodel = getViewModel()
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(recipient1))
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(recipient2))

        assertEquals(2, viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.size)
        assertEquals(true, viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.contains(recipient1))
        assertEquals(true, viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.contains(recipient2))

        viewmodel.handleAction(InboxComposeActionHandler.RemoveRecipient(recipient1))

        assertEquals(1, viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.size)
        assertEquals(false, viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.contains(recipient1))
        assertEquals(true, viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.contains(recipient2))
    }

    @Test
    fun `Open Recipient Picker action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(InboxComposeActionHandler.OpenRecipientPicker)

        assertEquals(InboxComposeScreenOptions.RecipientPicker, viewmodel.uiState.value.screenOption)
    }

    @Test
    fun `Body Changed action handler`() {
        val viewmodel = getViewModel()
        val expected = TextFieldValue("expected")

        assertEquals(TextFieldValue(""), viewmodel.uiState.value.body)

        viewmodel.handleAction(InboxComposeActionHandler.BodyChanged(expected))

        assertEquals(expected, viewmodel.uiState.value.body)
    }

    @Test
    fun `Subject Changed action handler`() {
        val viewmodel = getViewModel()
        val expected = TextFieldValue("expected")

        assertEquals(TextFieldValue(""), viewmodel.uiState.value.subject)

        viewmodel.handleAction(InboxComposeActionHandler.SubjectChanged(expected))

        assertEquals(expected, viewmodel.uiState.value.subject)
    }

    @Test
    fun `Send Individual Changed action handler`() {
        val viewmodel = getViewModel()
        val expected = true

        assertEquals(false ,viewmodel.uiState.value.sendIndividual)

        viewmodel.handleAction(InboxComposeActionHandler.SendIndividualChanged(expected))

        assertEquals(expected, viewmodel.uiState.value.sendIndividual)
    }

    @Test
    fun `Send Message action handler`() = runTest {
        val viewmodel = getViewModel()

        val events = mutableListOf<InboxComposeViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewmodel.events.toList(events)
        }

        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(mockk(relaxed = true)))

        viewmodel.handleAction(InboxComposeActionHandler.SendClicked)

        coVerify(exactly = 1) { inboxComposeRepository.createConversation(any(), any(), any(), any(), any(), any()) }
        assertEquals(3, events.size)
        assertEquals(InboxComposeViewModelAction.UpdateParentFragment, events[0])
        assertEquals(InboxComposeViewModelAction.ShowScreenResult(context.getString(R.string.messageSentSuccessfully)), events[1])
        assertEquals(InboxComposeViewModelAction.NavigateBack, events[2])
    }

    @Test
    fun `Close Compose Screen`() = runTest {
        val viewmodel = getViewModel()

        val events = mutableListOf<InboxComposeViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewmodel.events.toList(events)
        }

        viewmodel.handleAction(InboxComposeActionHandler.Close)

        assertEquals(InboxComposeViewModelAction.NavigateBack, events.last())
    }

    @Test
    fun `Check if Compose content had been changed`() {
        val viewModel = getViewModel()
        assertEquals(false, viewModel.composeContentHasChanged())

        viewModel.handleAction(InboxComposeActionHandler.SubjectChanged(TextFieldValue("Subject")))
        assertEquals(true, viewModel.composeContentHasChanged())
        viewModel.handleAction(InboxComposeActionHandler.SubjectChanged(TextFieldValue("")))
        assertEquals(false, viewModel.composeContentHasChanged())

        viewModel.handleAction(InboxComposeActionHandler.BodyChanged(TextFieldValue("Body")))
        assertEquals(true, viewModel.composeContentHasChanged())
        viewModel.handleAction(InboxComposeActionHandler.BodyChanged(TextFieldValue("")))
        assertEquals(false, viewModel.composeContentHasChanged())

        viewModel.handleAction(InboxComposeActionHandler.SendIndividualChanged(true))
        assertEquals(true, viewModel.composeContentHasChanged())
        viewModel.handleAction(InboxComposeActionHandler.SendIndividualChanged(false))
        assertEquals(false, viewModel.composeContentHasChanged())

        val recipient = Recipient(stringId = "1")
        viewModel.handleAction(InboxComposeActionHandler.AddRecipient(recipient))
        assertEquals(true, viewModel.composeContentHasChanged())
        viewModel.handleAction(InboxComposeActionHandler.RemoveRecipient(recipient))
        assertEquals(false, viewModel.composeContentHasChanged())

        viewModel.handleAction(ContextPickerActionHandler.ContextClicked(Course()))
        assertEquals(true, viewModel.composeContentHasChanged())
    }

    @Test
    fun `Attachment selector dialog opens`() = runTest {
        val viewmodel = getViewModel()

        val events = mutableListOf<InboxComposeViewModelAction>()
        backgroundScope.launch(testDispatcher) {
            viewmodel.events.toList(events)
        }

        viewmodel.handleAction(InboxComposeActionHandler.AddAttachmentSelected)

        assertEquals(InboxComposeViewModelAction.OpenAttachmentPicker, events.last())
    }

    @Test
    fun `Attachment removed`() {
        val viewmodel = getViewModel()
        val attachment = Attachment()
        val attachmentEntity = com.instructure.pandautils.room.appdatabase.entities.AttachmentEntity(attachment)
        val attachmentCardItem = AttachmentCardItem(Attachment(), AttachmentStatus.UPLOADED, false)
        val uuid = UUID.randomUUID()
        coEvery { attachmentDao.findByParentId(uuid.toString()) } returns listOf(attachmentEntity)
        viewmodel.updateAttachments(uuid, WorkInfo(UUID.randomUUID(), WorkInfo.State.SUCCEEDED, setOf("")))

        assertEquals(1, viewmodel.uiState.value.attachments.size)

        viewmodel.handleAction(InboxComposeActionHandler.RemoveAttachment(attachmentCardItem))

        assertEquals(0, viewmodel.uiState.value.attachments.size)
    }

    @Test
    fun `Download attachment on selection`() {
        val fileDownloader: FileDownloader = mockk(relaxed = true)
        val viewModel = getViewModel(fileDownloader)
        val attachment = Attachment()
        val attachmentCardItem = AttachmentCardItem(attachment, AttachmentStatus.UPLOADED, false)

        viewModel.handleAction(InboxComposeActionHandler.OpenAttachment(attachmentCardItem))

        coVerify(exactly = 1) { fileDownloader.downloadFileToDevice(attachment) }
    }

    @Test
    fun `Add recipient action handler`() {
        val viewmodel = getViewModel()
        val recipient = Recipient(stringId = "1")

        assertEquals(0, viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.size)

        viewmodel.handleAction(InboxComposeActionHandler.AddRecipient(recipient))

        assertEquals(1, viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.size)
        assertEquals(true, viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.contains(recipient))
    }

    @Test
    fun `Inline search value changed`() = runTest {
        val viewmodel = getViewModel()
        val searchValue = TextFieldValue("searchValue")
        val courseId = 1L
        val canvasContext: CanvasContext = Course(id = courseId)
        val recipients = listOf(
            Recipient(stringId = "1"),
            Recipient(stringId = "2"),
            Recipient(stringId = "3"),
        )

        coEvery { inboxComposeRepository.getRecipients(searchValue.text, canvasContext.contextId, any()) } returns DataResult.Success(recipients)

        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(canvasContext))
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(recipients.first()))
        viewmodel.handleAction(InboxComposeActionHandler.SearchRecipientQueryChanged(searchValue))

        //Wait for debounce
        delay(500)

        assertEquals(true, viewmodel.uiState.value.inlineRecipientSelectorState.isShowResults)
        assertEquals(listOf(recipients[1], recipients[2]), viewmodel.uiState.value.inlineRecipientSelectorState.searchResults)

        viewmodel.handleAction(InboxComposeActionHandler.HideSearchResults)

        assertEquals(false, viewmodel.uiState.value.inlineRecipientSelectorState.isShowResults)
    }

    @Test
    fun `Hide search results`() = runTest {
        val viewmodel = getViewModel()
        val searchValue = TextFieldValue("searchValue")
        val courseId = 1L
        val canvasContext: CanvasContext = Course(id = courseId)
        val recipients = listOf(
            Recipient(stringId = "1"),
            Recipient(stringId = "2"),
            Recipient(stringId = "3"),
        )

        coEvery { inboxComposeRepository.getRecipients(searchValue.text, canvasContext.contextId, any()) } returns DataResult.Success(recipients)

        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(canvasContext))
        viewmodel.handleAction(InboxComposeActionHandler.SearchRecipientQueryChanged(searchValue))

        //Wait for debounce
        delay(500)

        assertEquals(true, viewmodel.uiState.value.inlineRecipientSelectorState.isShowResults)

        viewmodel.handleAction(InboxComposeActionHandler.HideSearchResults)

        assertEquals(false, viewmodel.uiState.value.inlineRecipientSelectorState.isShowResults)
    }

    //endregion

    //region Context Picker action handler
    @Test
    fun `Done Clicked action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(ContextPickerActionHandler.DoneClicked)

        assertEquals(InboxComposeScreenOptions.None, viewmodel.uiState.value.screenOption)
    }

    @Test
    fun `Refresh Called action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(ContextPickerActionHandler.RefreshCalled)

        coVerify(exactly = 1) { inboxComposeRepository.getCourses(true) }
        coVerify(exactly = 1) { inboxComposeRepository.getGroups(true) }
    }

    @Test
    fun `Context Clicked action handler`() {
        val viewmodel = getViewModel()
        val courseId = 1L
        val context = Course(id = courseId)
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(context))

        assertEquals(context, viewmodel.uiState.value.selectContextUiState.selectedCanvasContext)
        assertEquals(InboxComposeScreenOptions.None, viewmodel.uiState.value.screenOption)

        coVerify(exactly = 1) { inboxComposeRepository.getRecipients(any(), context.contextId, any()) }
    }
    //endregion

    //region Recipient Picker action handler
    @Test
    fun `Recipient Done Clicked action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(mockk(relaxed = true)))
        viewmodel.handleAction(RecipientPickerActionHandler.DoneClicked)

        assertEquals(RecipientPickerScreenOption.Roles, viewmodel.uiState.value.recipientPickerUiState.screenOption)
        assertEquals(InboxComposeScreenOptions.None, viewmodel.uiState.value.screenOption)
    }

    @Test
    fun `Recipient Back Clicked action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(mockk(relaxed = true)))
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientBackClicked)

        assertEquals(RecipientPickerScreenOption.Roles, viewmodel.uiState.value.recipientPickerUiState.screenOption)
    }

    @Test
    fun `Role Clicked action handler`() {
        val viewmodel = getViewModel()
        val role: EnrollmentType = mockk(relaxed = true)
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(role))

        assertEquals(RecipientPickerScreenOption.Recipients, viewmodel.uiState.value.recipientPickerUiState.screenOption)
    }

    @Test
    fun `Recipient Clicked action handler`() {
        val viewmodel = getViewModel()
        val expected: Recipient = mockk(relaxed = true)
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(expected))

        assertEquals(listOf(expected), viewmodel.uiState.value.recipientPickerUiState.selectedRecipients)
        assertEquals(listOf(expected), viewmodel.uiState.value.recipientPickerUiState.selectedRecipients)
    }

    @Test
    fun `Refresh action handler`() {
        val course = Course()
        coEvery { inboxComposeRepository.getCourses(any()) } returns DataResult.Success(listOf(course))
        coEvery { inboxComposeRepository.getGroups(any()) } returns DataResult.Success(emptyList())
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        val viewmodel = getViewModel()

        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(course))
        viewmodel.handleAction(RecipientPickerActionHandler.RefreshCalled)

        coVerify(exactly = 1) { inboxComposeRepository.getRecipients("", course.contextId, true) }
    }

    @Test
    fun `Search value changed action handler`() = runTest {
        val searchValue = TextFieldValue("searchValue")
        val courseId: Long = 1
        val course = Course(id = courseId)
        val recipients = listOf(
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.StudentEnrollment.rawValue))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TeacherEnrollment.rawValue))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.ObserverEnrollment.rawValue))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TaEnrollment.rawValue)))
        )
        coEvery { inboxComposeRepository.getCourses(any()) } returns DataResult.Success(listOf(course))
        coEvery { inboxComposeRepository.getGroups(any()) } returns DataResult.Success(emptyList())
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        coEvery { inboxComposeRepository.getRecipients("", any(), any()) } returns DataResult.Success(recipients)
        val viewmodel = getViewModel()

        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(course))
        assertEquals(recipients, viewmodel.uiState.value.recipientPickerUiState.recipientsToShow)

        coEvery { inboxComposeRepository.getRecipients(searchValue.text, any(), any()) } returns DataResult.Success(listOf(recipients.first()))
        viewmodel.handleAction(RecipientPickerActionHandler.SearchValueChanged(searchValue))

        //Wait for debounce
        delay(500)

        assertEquals(listOf(recipients.first()), viewmodel.uiState.value.recipientPickerUiState.recipientsToShow)
    }
    //endregion

    // region Arguments

    @Test
    fun `Argument values are populated to ViewModel`() {
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

        val mode = InboxComposeOptionsMode.REPLY
        val conversation = Conversation(id = 2)
        val messages = listOf(Message(id = 2), Message(id = 3))
        val contextCode = "course_1"
        val contextName = "Course 1"
        val recipients = listOf(Recipient(stringId = "1"))
        val subject = "Test subject"
        val body = "Test body"
        val attachments = listOf(Attachment())
        coEvery { savedStateHandle.get<InboxComposeOptions>(InboxComposeOptions.COMPOSE_PARAMETERS) } returns InboxComposeOptions(
            mode = mode,
            previousMessages = InboxComposeOptionsPreviousMessages(conversation, messages),
            defaultValues = InboxComposeOptionsDefaultValues(
                contextCode = contextCode,
                contextName = contextName,
                recipients = recipients,
                subject = subject,
                body = body,
                attachments = attachments
            )
        )
        val viewmodel = InboxComposeViewModel(savedStateHandle, context, mockk(relaxed = true), inboxComposeRepository, attachmentDao)
        val uiState = viewmodel.uiState.value

        assertEquals(mode, uiState.inboxComposeMode)
        assertEquals(conversation, uiState.previousMessages?.conversation)
        assertEquals(messages, uiState.previousMessages?.previousMessages)
        assertEquals(contextName, uiState.selectContextUiState.selectedCanvasContext?.name)
        assertEquals(contextCode, uiState.selectContextUiState.selectedCanvasContext?.contextId)
        assertEquals(recipients, uiState.recipientPickerUiState.selectedRecipients)
        assertEquals(subject, uiState.subject.text)
        assertEquals(body, uiState.body.text)
        assertEquals(attachments, uiState.attachments.map { it.attachment })
    }

    @Test
    fun `Argument disabled fields are populated to ViewModel`() {
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

        coEvery { savedStateHandle.get<InboxComposeOptions>(InboxComposeOptions.COMPOSE_PARAMETERS) } returns InboxComposeOptions(
            disabledFields = InboxComposeOptionsDisabledFields(
                isContextDisabled = true,
                isRecipientsDisabled = true,
                isSendIndividualDisabled = true,
                isSubjectDisabled = true,
                isBodyDisabled = true,
                isAttachmentDisabled = true
            )
        )
        val viewmodel = InboxComposeViewModel(savedStateHandle, context, mockk(relaxed = true), inboxComposeRepository, attachmentDao)
        val disabledFields = viewmodel.uiState.value.disabledFields

        assertEquals(true, disabledFields.isContextDisabled)
        assertEquals(true, disabledFields.isRecipientsDisabled)
        assertEquals(true, disabledFields.isSendIndividualDisabled)
        assertEquals(true, disabledFields.isSubjectDisabled)
        assertEquals(true, disabledFields.isBodyDisabled)
        assertEquals(true, disabledFields.isAttachmentDisabled)
    }

    @Test
    fun `Argument hidden fields are populated to ViewModel`() {
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

        coEvery { savedStateHandle.get<InboxComposeOptions>(InboxComposeOptions.COMPOSE_PARAMETERS) } returns InboxComposeOptions(
            hiddenFields = InboxComposeOptionsHiddenFields(
                isContextHidden = true,
                isRecipientsHidden = true,
                isSendIndividualHidden = true,
                isSubjectHidden= true,
                isBodyHidden = true,
                isAttachmentHidden = true
            )
        )
        val viewmodel = InboxComposeViewModel(savedStateHandle, context, mockk(relaxed = true), inboxComposeRepository, attachmentDao)
        val hiddenFields = viewmodel.uiState.value.hiddenFields

        assertEquals(true, hiddenFields.isContextHidden)
        assertEquals(true, hiddenFields.isRecipientsHidden)
        assertEquals(true, hiddenFields.isSendIndividualHidden)
        assertEquals(true, hiddenFields.isSubjectHidden)
        assertEquals(true, hiddenFields.isBodyHidden)
        assertEquals(true, hiddenFields.isAttachmentHidden)
    }

    // endregion

    // region External state modification

    @Test
    fun `Test dismiss dialog logic`() {
        val viewmodel = getViewModel()

        assertEquals(false, viewmodel.uiState.value.showConfirmationDialog)
        assertEquals(true, viewmodel.uiState.value.enableCustomBackHandler)

        viewmodel.cancelDismissDialog(true)

        assertEquals(true, viewmodel.uiState.value.showConfirmationDialog)
        assertEquals(false, viewmodel.uiState.value.enableCustomBackHandler)

        viewmodel.cancelDismissDialog(false)

        assertEquals(false, viewmodel.uiState.value.showConfirmationDialog)
        assertEquals(true, viewmodel.uiState.value.enableCustomBackHandler)

    }

    @Test
    fun `Test dismissing context picker screen`() {
        val viewmodel = getViewModel()
        assertEquals(InboxComposeScreenOptions.None, viewmodel.uiState.value.screenOption)

        viewmodel.handleAction(InboxComposeActionHandler.OpenContextPicker)
        assertEquals(InboxComposeScreenOptions.ContextPicker, viewmodel.uiState.value.screenOption)

        viewmodel.closeContextPicker()
        assertEquals(InboxComposeScreenOptions.None, viewmodel.uiState.value.screenOption)
    }

    @Test
    fun `Test back to roles on recipient picker screen`() {
        val viewmodel = getViewModel()
        val role: EnrollmentType = mockk(relaxed = true)
        assertEquals(InboxComposeScreenOptions.None, viewmodel.uiState.value.screenOption)

        viewmodel.handleAction(InboxComposeActionHandler.OpenRecipientPicker)
        assertEquals(InboxComposeScreenOptions.RecipientPicker, viewmodel.uiState.value.screenOption)
        assertEquals(RecipientPickerScreenOption.Roles, viewmodel.uiState.value.recipientPickerUiState.screenOption)
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(role))
        assertEquals(RecipientPickerScreenOption.Recipients, viewmodel.uiState.value.recipientPickerUiState.screenOption)

        viewmodel.recipientPickerBackToRoles()
        assertEquals(InboxComposeScreenOptions.RecipientPicker, viewmodel.uiState.value.screenOption)
        assertEquals(RecipientPickerScreenOption.Roles, viewmodel.uiState.value.recipientPickerUiState.screenOption)
    }

    @Test
    fun `Test done button on recipient screen`() {
        val viewmodel = getViewModel()
        val role: EnrollmentType = mockk(relaxed = true)
        assertEquals(InboxComposeScreenOptions.None, viewmodel.uiState.value.screenOption)

        viewmodel.handleAction(InboxComposeActionHandler.OpenRecipientPicker)
        assertEquals(InboxComposeScreenOptions.RecipientPicker, viewmodel.uiState.value.screenOption)
        assertEquals(RecipientPickerScreenOption.Roles, viewmodel.uiState.value.recipientPickerUiState.screenOption)
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(role))
        assertEquals(RecipientPickerScreenOption.Recipients, viewmodel.uiState.value.recipientPickerUiState.screenOption)

        viewmodel.recipientPickerDone()
        assertEquals(InboxComposeScreenOptions.None, viewmodel.uiState.value.screenOption)
        assertEquals(RecipientPickerScreenOption.Roles, viewmodel.uiState.value.recipientPickerUiState.screenOption)
    }

    // endregion

    // region SendIndividual

    @Test
    fun `Test Send individual over 100 recipients`() {
        val viewmodel = getViewModel()
        val over100RecipientGroup = Recipient(stringId = "all", name = "Test", userCount = 110)
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(over100RecipientGroup))

        assertEquals(true, viewmodel.uiState.value.isSendIndividualEnabled)
        assertEquals(false, viewmodel.uiState.value.sendIndividual)

        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(over100RecipientGroup))

        assertEquals(false, viewmodel.uiState.value.isSendIndividualEnabled)
        assertEquals(false, viewmodel.uiState.value.sendIndividual)

        viewmodel.handleAction(InboxComposeActionHandler.SendIndividualChanged(true))

        assertEquals(true, viewmodel.uiState.value.isSendIndividualEnabled)
        assertEquals(true, viewmodel.uiState.value.sendIndividual)

        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(over100RecipientGroup))

        assertEquals(true, viewmodel.uiState.value.isSendIndividualEnabled)
        assertEquals(true, viewmodel.uiState.value.sendIndividual)
    }

    // endregion

    private fun getViewModel(fileDownloader: FileDownloader = mockk(relaxed = true)): InboxComposeViewModel {
        return InboxComposeViewModel(SavedStateHandle(), context, fileDownloader, inboxComposeRepository, attachmentDao)
    }
}