package com.instructure.horizon.features.inbox.compose

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.Recipient
import com.instructure.horizon.features.inbox.attachment.HorizonInboxAttachment

data class HorizonInboxComposeUiState(
    val coursePickerOptions: List<Course> = emptyList(),
    val selectedCourse: Course? = null,
    val isRecipientPickerLoading: Boolean = false,
    val recipientSearchQuery: TextFieldValue = TextFieldValue(""),
    val recipientPickerOptions: List<Recipient> = emptyList(),
    val selectedRecipients: List<Recipient> = emptyList(),
    val isConversationSendLoading: Boolean = false,
    val isSendIndividually: Boolean = false,
    val subject: TextFieldValue = TextFieldValue(""),
    val body: TextFieldValue = TextFieldValue(""),
    val isSendLoading: Boolean = false,
    val onCourseSelected: (Course) -> Unit = {},
    val onRecipientSearchQueryChanged: (TextFieldValue) -> Unit = {},
    val onRecipientSelected: (Recipient) -> Unit = {},
    val onRecipientRemoved: (Recipient) -> Unit = {},
    val onSendConversation: (onFinished: () -> Unit) -> Unit = {},
    val onSendIndividuallyChanged: (Boolean) -> Unit = {},
    val onSubjectChanged: (TextFieldValue) -> Unit = {},
    val onBodyChanged: (TextFieldValue) -> Unit = {},
    val courseErrorMessage: String? = null,
    val recipientErrorMessage: String? = null,
    val subjectErrorMessage: String? = null,
    val bodyErrorMessage: String? = null,
    val attachmentsErrorMessage: String? = null,
    val snackbarMessage: String? = null,
    val onDismissSnackbar: () -> Unit = {},
    val showAttachmentPicker: Boolean = false,
    val onShowAttachmentPickerChanged: (Boolean) -> Unit = {},
    val attachments: List<HorizonInboxAttachment> = emptyList(),
    val onAttachmentsChanged: (List<HorizonInboxAttachment>) -> Unit = {}
)
