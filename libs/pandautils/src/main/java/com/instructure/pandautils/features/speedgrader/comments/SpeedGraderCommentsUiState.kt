package com.instructure.pandautils.features.speedgrader.comments

import androidx.compose.ui.text.input.TextFieldValue
import com.instructure.pandautils.views.RecordingMediaType
import java.io.File


data class SpeedGraderCommentsUiState(
    val comments: List<SpeedGraderComment> = emptyList(),
    val commentText: TextFieldValue = TextFieldValue(""),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val showAttachmentTypeDialog: Boolean = false,
    val showRecordFloatingView: RecordingMediaType? = null,
)

data class SpeedGraderComment(
    val id: String = "",
    val authorName: String = "",
    val authorId: String = "",
    val authorAvatarUrl: String = "",
    val content: String = "",
    val createdAt: String = "",
    val isOwnComment: Boolean = false,
    val attachments: List<SpeedGraderCommentAttachment> = emptyList(),
    val isPending: Boolean = false
)

data class SpeedGraderCommentAttachment(
    val id: String = "",
    val title: String = "",
    val displayName: String = "",
    val contentType: String = "",
    val size: String = "",
    val url: String = "",
    val thumbnailUrl: String? = null,
    val createdAt: String = "",
)

sealed class SpeedGraderCommentsAction {
    data class CommentFieldChanged(val commentText: TextFieldValue) : SpeedGraderCommentsAction()
    data object AddCommentLibraryClicked : SpeedGraderCommentsAction()
    data object AddAttachmentClicked : SpeedGraderCommentsAction()
    data object AttachmentTypeSelectorDialogClosed : SpeedGraderCommentsAction()
    data object AttachmentRecordDialogClosed : SpeedGraderCommentsAction()
    data object SendCommentClicked : SpeedGraderCommentsAction()
    data object RecordAudioClicked : SpeedGraderCommentsAction()
    data object RecordVideoClicked : SpeedGraderCommentsAction()
    data object ChooseFilesClicked : SpeedGraderCommentsAction()
    data class MediaRecorded(val file: File) : SpeedGraderCommentsAction()
}
