package com.instructure.pandautils.features.inbox.compose

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.pandautils.compose.composables.SelectContextUiState
import java.util.EnumMap

data class InboxComposeUiState(
    val selectContextUiState: SelectContextUiState = SelectContextUiState(),
    val recipientPickerUiState: RecipientPickerUiState = RecipientPickerUiState(),
    val screenOption: InboxComposeScreenOptions = InboxComposeScreenOptions.None,
    val sendIndividual: Boolean = false,
    val subject: TextFieldValue = TextFieldValue(""),
    val body: TextFieldValue = TextFieldValue(""),
    val attachments: List<AttachmentCardItem> = emptyList(),
    val screenState: ScreenState = ScreenState.Data,
    val showConfirmationDialog: Boolean = false,
) {
    val isSendButtonEnabled: Boolean
        get() = selectContextUiState.selectedCanvasContext != null &&
                recipientPickerUiState.selectedRecipients.isNotEmpty() &&
                subject.text.isNotEmpty() && body.text.isNotEmpty() &&
                attachments.all { it.status == AttachmentStatus.UPLOADED }
}

sealed class InboxComposeViewModelAction {
    data object NavigateBack: InboxComposeViewModelAction()
    data object OpenAttachmentPicker: InboxComposeViewModelAction()
    data class ShowScreenResult(val message: String): InboxComposeViewModelAction()
}

sealed class InboxComposeActionHandler {
    data object OpenContextPicker: InboxComposeActionHandler()
    data object OpenRecipientPicker: InboxComposeActionHandler()
    data class RemoveRecipient(val recipient: Recipient): InboxComposeActionHandler()
    data object Close: InboxComposeActionHandler()
    data class CancelDismissDialog(val isShow: Boolean): InboxComposeActionHandler()
    data object SendClicked : InboxComposeActionHandler()
    data class SendIndividualChanged(val sendIndividual: Boolean) : InboxComposeActionHandler()
    data class SubjectChanged(val subject: TextFieldValue) : InboxComposeActionHandler()
    data class BodyChanged(val body: TextFieldValue) : InboxComposeActionHandler()
    data object AddAttachmentSelected : InboxComposeActionHandler()
    data class RemoveAttachment(val attachment: AttachmentCardItem) : InboxComposeActionHandler()
    data class OpenAttachment(val attachment: AttachmentCardItem) : InboxComposeActionHandler()
}

sealed class InboxComposeScreenOptions {
    data object None : InboxComposeScreenOptions()
    data object ContextPicker : InboxComposeScreenOptions()
    data object RecipientPicker : InboxComposeScreenOptions()
}

sealed class ContextPickerActionHandler {
    data class ContextClicked(val context: CanvasContext) : ContextPickerActionHandler()
    data object RefreshCalled : ContextPickerActionHandler()
    data object DoneClicked : ContextPickerActionHandler()
}

data class RecipientPickerUiState(
    val recipientsByRole: EnumMap<EnrollmentType, List<Recipient>> = EnumMap(EnrollmentType::class.java),
    val selectedRole: EnrollmentType? = null,
    val recipientsToShow: List<Recipient> = emptyList(),
    val allRecipientsToShow: Recipient? = null,
    val selectedRecipients: List<Recipient> = emptyList(),
    val searchValue: TextFieldValue = TextFieldValue(""),
    val screenOption: RecipientPickerScreenOption = RecipientPickerScreenOption.Roles,
    val screenState: ScreenState = ScreenState.Data,
)

sealed class RecipientPickerActionHandler {
    data class RoleClicked(val role: EnrollmentType) : RecipientPickerActionHandler()
    data object RecipientBackClicked : RecipientPickerActionHandler()
    data class RecipientClicked(val recipient: Recipient) : RecipientPickerActionHandler()
    data object DoneClicked : RecipientPickerActionHandler()
    data object RefreshCalled : RecipientPickerActionHandler()
    data class SearchValueChanged(val searchText: TextFieldValue) : RecipientPickerActionHandler()
}

sealed class RecipientPickerScreenOption {
    data object Roles : RecipientPickerScreenOption()
    data object Recipients : RecipientPickerScreenOption()
}

sealed class ScreenState {
    data object Loading: ScreenState()
    data object Data: ScreenState()
    data object Empty: ScreenState()
    data object Error: ScreenState()
}