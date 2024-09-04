package com.instructure.pandautils.features.inbox.compose

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.work.WorkInfo
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.DataResult
import com.instructure.pandautils.R
import com.instructure.pandautils.room.appdatabase.daos.AttachmentDao
import com.instructure.pandautils.utils.FileDownloader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.STUDENTENROLLMENT.rawValue()))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TEACHERENROLLMENT.rawValue()))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.OBSERVERENROLLMENT.rawValue()))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TAENROLLMENT.rawValue())))
        )
        coEvery { inboxComposeRepository.getRecipients(any(), any(), any()) } returns DataResult.Success(recipients)
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        viewModel.handleAction(ContextPickerActionHandler.ContextClicked(Course(id = courseId)))

        assertEquals(recipients[0], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.STUDENTENROLLMENT]?.first())
        assertEquals(recipients[1], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.TEACHERENROLLMENT]?.first())
        assertEquals(recipients[2], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.OBSERVERENROLLMENT]?.first())
        assertEquals(recipients[3], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.TAENROLLMENT]?.first())

    }

    @Test
    fun `Test Recipient list on Role selection`() {
        val viewModel = getViewModel()
        val courseId: Long = 1
        val recipients = listOf(
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.STUDENTENROLLMENT.rawValue()))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TEACHERENROLLMENT.rawValue()))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.OBSERVERENROLLMENT.rawValue()))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TAENROLLMENT.rawValue()))
            )
        )
        coEvery { inboxComposeRepository.getRecipients(any(), any(), any()) } returns DataResult.Success(recipients)
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        viewModel.handleAction(ContextPickerActionHandler.ContextClicked(Course(id = courseId)))
        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.STUDENTENROLLMENT))

        assertEquals(recipients[0], viewModel.uiState.value.recipientPickerUiState.recipientsToShow.first())
    }

    @Test
    fun `Test if All Recipients show up`() {
        val courseId: Long = 1
        val course = Course(id = courseId, name = "Course")
        val recipients = listOf(
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.STUDENTENROLLMENT.rawValue()))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TEACHERENROLLMENT.rawValue()))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.OBSERVERENROLLMENT.rawValue()))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TAENROLLMENT.rawValue()))
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

        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.STUDENTENROLLMENT))
        val expectedAllStudentsRecipient = Recipient(
            stringId = "${course.contextId}_students",
            name = "All in Students"
        )
        assertEquals(expectedAllStudentsRecipient, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RecipientBackClicked)
        assertEquals(expectedAllCourseRecipient, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.TEACHERENROLLMENT))
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
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.STUDENTENROLLMENT.rawValue()))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TEACHERENROLLMENT.rawValue()))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.OBSERVERENROLLMENT.rawValue()))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TAENROLLMENT.rawValue()))
            )
        )
        coEvery { inboxComposeRepository.getRecipients(any(), any(), any()) } returns DataResult.Success(recipients)
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        viewModel.handleAction(ContextPickerActionHandler.ContextClicked(course))

        assertEquals(null, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.STUDENTENROLLMENT))
        assertEquals(null, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RecipientBackClicked)
        assertEquals(null, viewModel.uiState.value.recipientPickerUiState.allRecipientsToShow)

        viewModel.handleAction(RecipientPickerActionHandler.RoleClicked(EnrollmentType.STUDENTENROLLMENT))
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
        val attachmentCardItem = AttachmentCardItem(Attachment(), AttachmentStatus.UPLOADED)
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
        val attachmentCardItem = AttachmentCardItem(attachment, AttachmentStatus.UPLOADED)

        viewModel.handleAction(InboxComposeActionHandler.OpenAttachment(attachmentCardItem))

        coVerify(exactly = 1) { fileDownloader.downloadFileToDevice(attachment) }
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
        val context = Course()
        coEvery { inboxComposeRepository.canSendToAll(any()) } returns DataResult.Success(false)
        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(context))

        assertEquals(context, viewmodel.uiState.value.selectContextUiState.selectedCanvasContext)
        assertEquals(InboxComposeScreenOptions.None, viewmodel.uiState.value.screenOption)

        coVerify(exactly = 1) { inboxComposeRepository.getRecipients(any(), context, any()) }
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

        coVerify(exactly = 1) { inboxComposeRepository.getRecipients("", course, true) }
    }

    @Test
    fun `Add Recipient action handler`() {
        val recipient = Recipient(stringId = "1")
        val viewmodel = getViewModel()
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(recipient))

        assertEquals(listOf(recipient), viewmodel.uiState.value.recipientPickerUiState.selectedRecipients)
    }

    @Test
    fun `Search value changed action handler`() {
        val searchValue = TextFieldValue("searchValue")
        val courseId: Long = 1
        val course = Course(id = courseId)
        val recipients = listOf(
            Recipient(stringId = "1", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.STUDENTENROLLMENT.rawValue()))),
            Recipient(stringId = "2", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TEACHERENROLLMENT.rawValue()))),
            Recipient(stringId = "3", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.OBSERVERENROLLMENT.rawValue()))),
            Recipient(stringId = "4", commonCourses = hashMapOf(courseId.toString() to arrayOf(EnrollmentType.TAENROLLMENT.rawValue())))
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
        assertEquals(listOf(recipients.first()), viewmodel.uiState.value.recipientPickerUiState.recipientsToShow)
    }
    //endregion

    private fun getViewModel(fileDownloader: FileDownloader = mockk(relaxed = true)): InboxComposeViewModel {
        return InboxComposeViewModel(context, fileDownloader, inboxComposeRepository, attachmentDao)
    }
}