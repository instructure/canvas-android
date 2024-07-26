package com.instructure.pandautils.features.inbox.compose

import androidx.compose.ui.text.input.TextFieldValue
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
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

        assertEquals(uiState.selectedContext, null)
        assertEquals(uiState.selectedRecipients, emptyList<Recipient>())
        assertEquals(uiState.screenOption, InboxComposeScreenOptions.None)
        assertEquals(uiState.sendIndividual, false)
        assertEquals(uiState.subject, TextFieldValue(""))
        assertEquals(uiState.body, TextFieldValue(""))
        assertEquals(uiState.isSending, false)
    }

    @Test
    fun `Load available contexts on init`() {
        val viewmodel = getViewModel()

        coVerify(exactly = 1) { inboxComposeRepository.getCourses(any()) }
        coVerify(exactly = 1) { inboxComposeRepository.getGroups(any()) }

    }

    //region Context Picker action handler
    @Test
    fun `Cancel action handler`() {
        val viewmodel = getViewModel()
        val activity: FragmentActivity = mockk(relaxed = true)
        viewmodel.handleAction(InboxComposeActionHandler.CancelClicked, activity)

        coVerify(exactly = 1) { activity.supportFragmentManager.popBackStack() }
    }

    @Test
    fun `Open Context Picker action handler`() {
        val viewmodel = getViewModel()
        val activity: FragmentActivity = mockk(relaxed = true)
        viewmodel.handleAction(InboxComposeActionHandler.OpenContextPicker, activity)

        assertEquals(viewmodel.uiState.value.screenOption, InboxComposeScreenOptions.ContextPicker)
    }

    @Test
    fun `Remove Recipient action handler`() {
        val recipient1 = Recipient(stringId = "1")
        val recipient2 = Recipient(stringId = "2")
        val viewmodel = getViewModel()
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(recipient1))
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(recipient2))

        assertEquals(viewmodel.uiState.value.selectedRecipients.size, 2)
        assertEquals(viewmodel.uiState.value.selectedRecipients.contains(recipient1), true)
        assertEquals(viewmodel.uiState.value.selectedRecipients.contains(recipient2), true)

        val activity: FragmentActivity = mockk(relaxed = true)
        viewmodel.handleAction(InboxComposeActionHandler.RemoveRecipient(recipient1), activity)

        assertEquals(viewmodel.uiState.value.selectedRecipients.size, 1)
        assertEquals(viewmodel.uiState.value.selectedRecipients.contains(recipient1), false)
        assertEquals(viewmodel.uiState.value.selectedRecipients.contains(recipient2), true)
    }

    @Test
    fun `Open Recipient Picker action handler`() {
        val viewmodel = getViewModel()
        val activity: FragmentActivity = mockk(relaxed = true)
        viewmodel.handleAction(InboxComposeActionHandler.OpenRecipientPicker, activity)

        assertEquals(viewmodel.uiState.value.screenOption, InboxComposeScreenOptions.RecipientPicker)
    }

    @Test
    fun `Body Changed action handler`() {
        val viewmodel = getViewModel()
        val expected = TextFieldValue("expected")

        assertEquals(viewmodel.uiState.value.body, TextFieldValue(""))

        val activity: FragmentActivity = mockk(relaxed = true)
        viewmodel.handleAction(InboxComposeActionHandler.BodyChanged(expected), activity)

        assertEquals(viewmodel.uiState.value.body, expected)
    }

    @Test
    fun `Subject Changed action handler`() {
        val viewmodel = getViewModel()
        val expected = TextFieldValue("expected")

        assertEquals(viewmodel.uiState.value.subject, TextFieldValue(""))

        val activity: FragmentActivity = mockk(relaxed = true)
        viewmodel.handleAction(InboxComposeActionHandler.SubjectChanged(expected), activity)

        assertEquals(viewmodel.uiState.value.subject, expected)
    }

    @Test
    fun `Send Individual Changed action handler`() {
        val viewmodel = getViewModel()
        val expected = true

        assertEquals(viewmodel.uiState.value.sendIndividual, false)

        val activity: FragmentActivity = mockk(relaxed = true)
        viewmodel.handleAction(InboxComposeActionHandler.SendIndividualChanged(expected), activity)

        assertEquals(viewmodel.uiState.value.sendIndividual, expected)
    }

    @Test
    fun `Send Message action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(ContextPickerActionHandler.ContextClicked(mockk(relaxed = true)))

        val activity: FragmentActivity = mockk(relaxed = true)
        viewmodel.handleAction(InboxComposeActionHandler.SendClicked, activity)

        coVerify(exactly = 1) { activity.supportFragmentManager.popBackStack() }
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

        assertEquals(viewmodel.uiState.value.selectedContext, context)
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

        assertEquals(viewmodel.recipientPickerUiState.value.screenOption, RecipientPickerScreenOption.Roles)
        assertEquals(viewmodel.uiState.value.screenOption, InboxComposeScreenOptions.None)
    }

    @Test
    fun `Recipient Back Clicked action handler`() {
        val viewmodel = getViewModel()
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(mockk(relaxed = true)))
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientBackClicked)

        assertEquals(viewmodel.recipientPickerUiState.value.screenOption, RecipientPickerScreenOption.Roles)
    }

    @Test
    fun `Role Clicked action handler`() {
        val viewmodel = getViewModel()
        val role: Recipient.Enrollment = mockk(relaxed = true)
        viewmodel.handleAction(RecipientPickerActionHandler.RoleClicked(role))

        assertEquals(viewmodel.recipientPickerUiState.value.screenOption, RecipientPickerScreenOption.Recipients)
    }

    @Test
    fun `Recipient Clicked action handler`() {
        val viewmodel = getViewModel()
        val expected: Recipient = mockk(relaxed = true)
        viewmodel.handleAction(RecipientPickerActionHandler.RecipientClicked(expected))

        assertEquals(viewmodel.recipientPickerUiState.value.selectedRecipients, listOf(expected))
        assertEquals(viewmodel.uiState.value.selectedRecipients, listOf(expected))
    }
    //endregion

    private fun getViewModel(): InboxComposeViewModel {
        return InboxComposeViewModel(inboxComposeRepository)
    }
}