package com.instructure.pandautils.features.inbox.compose

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InboxComposeViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val inboxComposeRepository: InboxComposeRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    @Test
    fun `Test initial state`() {
        val viewmodel = getViewModel()
        val uiState = viewmodel.uiState.value

        assertEquals(uiState.contextPickerUiState.selectedContext, null)
        assertEquals(uiState.recipientPickerUiState.selectedRecipients, emptyList<Recipient>())
        assertEquals(uiState.screenOption, InboxComposeScreenOptions.None)
        assertEquals(uiState.sendIndividual, false)
        assertEquals(uiState.subject, TextFieldValue(""))
        assertEquals(uiState.body, TextFieldValue(""))
        assertEquals(uiState.screenState, ScreenState.Data)
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

        viewModel.handleAction(ContextPickerActionHandler.ContextClicked(Course(id = courseId)))

        assertEquals(recipients[0], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.STUDENTENROLLMENT]?.first())
        assertEquals(recipients[1], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.TEACHERENROLLMENT]?.first())
        assertEquals(recipients[2], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.OBSERVERENROLLMENT]?.first())
        assertEquals(recipients[3], viewModel.uiState.value.recipientPickerUiState.recipientsByRole[EnrollmentType.TAENROLLMENT]?.first())

    }

    //region Context Picker action handler
    @Test
    fun `Close action handler`() {
        val viewmodel = getViewModel()
        viewmodel.uiState.value.onDismiss = mockk(relaxed = true)
        viewmodel.handleAction(InboxComposeActionHandler.Close)

        coVerify(exactly = 1) { viewmodel.uiState.value.onDismiss() }
    }

    @Test
    fun `Open Context Picker action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(InboxComposeActionHandler.OpenContextPicker)

        assertEquals(viewmodel.uiState.value.screenOption, InboxComposeScreenOptions.ContextPicker)
    }

    @Test
    fun `Remove Recipient action handler`() {
        val recipient1 = Recipient(stringId = "1")
        val recipient2 = Recipient(stringId = "2")
        val viewmodel = getViewModel()
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(recipient1))
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(recipient2))

        assertEquals(viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.size, 2)
        assertEquals(viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.contains(recipient1), true)
        assertEquals(viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.contains(recipient2), true)

        viewmodel.handleAction(InboxComposeActionHandler.RemoveRecipient(recipient1))

        assertEquals(viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.size, 1)
        assertEquals(viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.contains(recipient1), false)
        assertEquals(viewmodel.uiState.value.recipientPickerUiState.selectedRecipients.contains(recipient2), true)
    }

    @Test
    fun `Open Recipient Picker action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(InboxComposeActionHandler.OpenRecipientPicker)

        assertEquals(viewmodel.uiState.value.screenOption, InboxComposeScreenOptions.RecipientPicker)
    }

    @Test
    fun `Body Changed action handler`() {
        val viewmodel = getViewModel()
        val expected = TextFieldValue("expected")

        assertEquals(viewmodel.uiState.value.body, TextFieldValue(""))

        viewmodel.handleAction(InboxComposeActionHandler.BodyChanged(expected))

        assertEquals(viewmodel.uiState.value.body, expected)
    }

    @Test
    fun `Subject Changed action handler`() {
        val viewmodel = getViewModel()
        val expected = TextFieldValue("expected")

        assertEquals(viewmodel.uiState.value.subject, TextFieldValue(""))

        viewmodel.handleAction(InboxComposeActionHandler.SubjectChanged(expected))

        assertEquals(viewmodel.uiState.value.subject, expected)
    }

    @Test
    fun `Send Individual Changed action handler`() {
        val viewmodel = getViewModel()
        val expected = true

        assertEquals(viewmodel.uiState.value.sendIndividual, false)

        viewmodel.handleAction(InboxComposeActionHandler.SendIndividualChanged(expected))

        assertEquals(viewmodel.uiState.value.sendIndividual, expected)
    }

    @Test
    fun `Send Message action handler`() {
        val viewmodel = getViewModel()
        viewmodel.uiState.value.onDismiss = mockk(relaxed = true)
        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(mockk(relaxed = true)))

        viewmodel.handleAction(InboxComposeActionHandler.SendClicked)

        coVerify(exactly = 1) { viewmodel.uiState.value.onDismiss() }
        coVerify(exactly = 1) { inboxComposeRepository.createConversation(any(), any(), any(), any(), any(), any()) }
    }
    //endregion

    //region Context Picker action handler
    @Test
    fun `Done Clicked action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(ContextPickerActionHandler.DoneClicked)

        assertEquals(viewmodel.uiState.value.screenOption, InboxComposeScreenOptions.None)
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
        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(context))

        assertEquals(viewmodel.uiState.value.contextPickerUiState.selectedContext, context)
        assertEquals(viewmodel.uiState.value.screenOption, InboxComposeScreenOptions.None)

        coVerify(exactly = 1) { inboxComposeRepository.getRecipients(any(), context, any()) }
    }
    //endregion

    //region Recipient Picker action handler
    @Test
    fun `Recipient Done Clicked action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(mockk(relaxed = true)))
        viewmodel.handleAction(RecipientPickerActionHandler.DoneClicked)

        assertEquals(viewmodel.uiState.value.recipientPickerUiState.screenOption, RecipientPickerScreenOption.Roles)
        assertEquals(viewmodel.uiState.value.screenOption, InboxComposeScreenOptions.None)
    }

    @Test
    fun `Recipient Back Clicked action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(mockk(relaxed = true)))
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientBackClicked)

        assertEquals(viewmodel.uiState.value.recipientPickerUiState.screenOption, RecipientPickerScreenOption.Roles)
    }

    @Test
    fun `Role Clicked action handler`() {
        val viewmodel = getViewModel()
        val role: EnrollmentType = mockk(relaxed = true)
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(role))

        assertEquals(viewmodel.uiState.value.recipientPickerUiState.screenOption, RecipientPickerScreenOption.Recipients)
    }

    @Test
    fun `Recipient Clicked action handler`() {
        val viewmodel = getViewModel()
        val expected: Recipient = mockk(relaxed = true)
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(expected))

        assertEquals(viewmodel.uiState.value.recipientPickerUiState.selectedRecipients, listOf(expected))
        assertEquals(viewmodel.uiState.value.recipientPickerUiState.selectedRecipients, listOf(expected))
    }
    //endregion

    private fun getViewModel(): InboxComposeViewModel {
        return InboxComposeViewModel(inboxComposeRepository)
    }
}