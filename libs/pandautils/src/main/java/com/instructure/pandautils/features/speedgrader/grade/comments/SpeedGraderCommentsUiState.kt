package com.instructure.pandautils.features.speedgrader.grade.comments

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import com.instructure.pandautils.views.RecordingMediaType
import java.io.File
import java.util.Date


data class SpeedGraderCommentsUiState(
    val comments: List<SpeedGraderComment> = emptyList(),
    val commentText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
    val showAttachmentTypeDialog: Boolean = false,
    val fileSelectorDialogData: SpeedGraderFileSelectorDialogData? = null,
    val showRecordFloatingView: RecordingMediaType? = null,
)

data class SpeedGraderComment(
    val id: String = "",
    val authorName: String = "",
    val authorId: String = "",
    val authorAvatarUrl: String? = null,
    val content: String = "",
    val createdAt: Date? = null,
    val isOwnComment: Boolean = false,
    val attachments: List<SpeedGraderCommentAttachment> = emptyList(),
    val mediaObject: SpeedGraderMediaObject? = null,
    val isPending: Boolean = false,
    val isFailed: Boolean = false,
)

data class SpeedGraderCommentAttachment(
    val id: String = "",
    val title: String = "",
    val displayName: String = "",
    val contentType: String = "",
    val size: String = "",
    val url: String = "",
    val thumbnailUrl: String? = null,
    val createdAt: Date? = null,
)

data class SpeedGraderMediaObject(
    val id: String,
    val mediaDownloadUrl: String?,
    val title: String?,
    val mediaType: MediaType?,
    val thumbnailUrl: String?,
    val contentType: String?
)

enum class MediaType {
    AUDIO,
    VIDEO
}

data class SpeedGraderFileSelectorDialogData(
    val assignmentId: Long,
    val courseId: Long,
    val userId: Long,
    val attempt: Long?
)

sealed class SpeedGraderCommentsAction {
    data class CommentFieldChanged(val commentText: String, val saveDraft: Boolean = true) : SpeedGraderCommentsAction()
    data object AddCommentLibraryClicked : SpeedGraderCommentsAction()
    data object AddAttachmentClicked : SpeedGraderCommentsAction()
    data object AttachmentTypeSelectorDialogClosed : SpeedGraderCommentsAction()
    data object AttachmentRecordDialogClosed : SpeedGraderCommentsAction()
    data object SendCommentClicked : SpeedGraderCommentsAction()
    data class RetryCommentUpload(val comment: SpeedGraderComment) : SpeedGraderCommentsAction()
    data object RecordAudioClicked : SpeedGraderCommentsAction()
    data object RecordVideoClicked : SpeedGraderCommentsAction()
    data object ChooseFilesClicked : SpeedGraderCommentsAction()
    data object FileUploadDialogClosed : SpeedGraderCommentsAction()
    data class FilesSelected(val filePaths: List<String>) : SpeedGraderCommentsAction()
    data class FileUploadStarted(val workInfoLiveData: LiveData<WorkInfo>) : SpeedGraderCommentsAction()
    data class MediaRecorded(val file: File) : SpeedGraderCommentsAction()
}
