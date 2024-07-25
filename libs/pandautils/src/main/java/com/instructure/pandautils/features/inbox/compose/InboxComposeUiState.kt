package com.instructure.pandautils.features.inbox.compose

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient

data class InboxComposeUiState(
    var selectedContext: CanvasContext? = null,
    var selectedRecipients: List<Recipient> = emptyList(),
    var screenOption: InboxComposeScreenOptions = InboxComposeScreenOptions.None,
    var sendIndividual: Boolean = false,
    var subject: TextFieldValue = TextFieldValue(""),
    var body: TextFieldValue = TextFieldValue(""),
)

sealed class InboxComposeActionHandler {
    data object OpenContextPicker : InboxComposeActionHandler()
    data object OpenRecipientPicker : InboxComposeActionHandler()
    data class RemoveRecipient(val recipient: Recipient) : InboxComposeActionHandler()
    data object CancelClicked : InboxComposeActionHandler()
    data object SendClicked : InboxComposeActionHandler()
    data class SendIndividualChanged(val sendIndividual: Boolean) : InboxComposeActionHandler()
    data class SubjectChanged(val subject: TextFieldValue) : InboxComposeActionHandler()
    data class BodyChanged(val body: TextFieldValue) : InboxComposeActionHandler()
}

sealed class InboxComposeScreenOptions {
    data object None : InboxComposeScreenOptions()
    data object ContextPicker : InboxComposeScreenOptions()
    data object RecipientPicker : InboxComposeScreenOptions()
}
data class ContextPickerUiState(
    var courses: List<CanvasContext> = emptyList(),
    var groups: List<CanvasContext> = emptyList(),
    var selectedContext: CanvasContext? = null,
    var isLoading: Boolean = false,
)

sealed class ContextPickerActionHandler {
    data class ContextClicked(val context: CanvasContext) : ContextPickerActionHandler()
    data object RefreshCalled : ContextPickerActionHandler()
    data object DoneClicked : ContextPickerActionHandler()
}

data class RecipientPickerUiState(
    var recipients: List<Recipient> = emptyList(),
    var roles: List<Recipient.Enrollment> = emptyList(),
    var selectedRecipients: List<Recipient> = emptyList(),
    var screenOption: RecipientPickerScreenOption = RecipientPickerScreenOption.Roles,
    var isLoading: Boolean = false,
)

sealed class RecipientPickerActionHandler {
    data class RoleClicked(val role: Recipient.Enrollment) : RecipientPickerActionHandler()
    data class RecipientClicked(val recipient: Recipient) : RecipientPickerActionHandler()
    data object DoneClicked : RecipientPickerActionHandler()
}

sealed class RecipientPickerScreenOption {
    data object Roles : RecipientPickerScreenOption()
    data object Recipients : RecipientPickerScreenOption()
}