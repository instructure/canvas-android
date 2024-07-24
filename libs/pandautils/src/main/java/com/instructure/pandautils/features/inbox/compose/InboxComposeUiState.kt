package com.instructure.pandautils.features.inbox.compose

import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Recipient

data class InboxComposeUiState(
    var contextPickerUiState: ContextPickerUiState,
    var recipientsState: RecipientPickerUiState,
    var screenOption: InboxComposeScreenOptions = InboxComposeScreenOptions.None,
    var sendIndividual: Boolean = false,
    var subject: String = "",
    var body: String = "",
)

sealed class InboxComposeActionHandler {
    data object OpenContextPicker : InboxComposeActionHandler()
    data object OpenRecipientPicker : InboxComposeActionHandler()
    data object CancelClicked : InboxComposeActionHandler()
    data class SendClicked(val sendIndividual: Boolean, val subject: String, val body: String) : InboxComposeActionHandler()
    data class SubjectChanged(val subject: String) : InboxComposeActionHandler()
    data class BodyChanged(val body: String) : InboxComposeActionHandler()
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
    var roles: List<String> = emptyList(),
    var selectedRecipients: List<Recipient> = emptyList(),
    var screenOption: RecipientPickerScreenOption = RecipientPickerScreenOption.Roles,
    var isLoading: Boolean = false,
)

sealed class RecipientPickerActionHandler {
    data class RecipientClicked(val recipient: Recipient) : RecipientPickerActionHandler()
    data object DoneClicked : RecipientPickerActionHandler()
}

sealed class RecipientPickerScreenOption {
    data object Roles : RecipientPickerScreenOption()
    data object Recipients : RecipientPickerScreenOption()
}