/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */package com.instructure.pandautils.features.speedgrader.content

import android.graphics.Color
import android.net.Uri
import android.os.Parcelable
import androidx.annotation.ColorInt
import com.instructure.canvasapi2.models.Attachment
import com.instructure.pandautils.features.grades.SubmissionStateLabel
import kotlinx.parcelize.Parcelize
import java.util.Date

data class SpeedGraderContentUiState(
    val content: GradeableContent? = null,
    val assigneeId: Long? = null,
    val userName: String? = null,
    val userUrl: String? = null,
    val submissionState: SubmissionStateLabel = SubmissionStateLabel.NONE,
    val dueDate: Date? = null,
    val attachmentSelectorUiState: SelectorUiState = SelectorUiState(),
    val attemptSelectorUiState: SelectorUiState = SelectorUiState(),
    @ColorInt val courseColor: Int = Color.GREEN
)

@Parcelize
sealed class GradeableContent : Parcelable
data object NoSubmissionContent : GradeableContent()
data object NoneContent : GradeableContent()
class ExternalToolContent(val url: String) : GradeableContent()
data object OnPaperContent : GradeableContent()
data object UnsupportedContent : GradeableContent()
class OtherAttachmentContent(val attachment: Attachment) : GradeableContent()
class PdfContent(
    val url: String,
    val courseId: Long? = null,
    val assigneeId: Long? = null
) : GradeableContent()
class TextContent(val text: String) : GradeableContent()
class ImageContent(val url: String, val contentType: String) : GradeableContent()
class UrlContent(val url: String, val previewUrl: String?) : GradeableContent()
class DiscussionContent(val previewUrl: String?) : GradeableContent()
class StudentAnnotationContent(val submissionId: Long, val attempt: Long) : GradeableContent()
data object AnonymousSubmissionContent : GradeableContent()

class QuizContent(
    val courseId: Long,
    val assignmentId: Long,
    val studentId: Long,
    val url: String,
    val pendingReview: Boolean
) : GradeableContent()

class MediaContent(
    val uri: Uri,
    val contentType: String? = null,
    val thumbnailUrl: String? = null,
    val displayName: String? = null
) : GradeableContent()

data class SelectorItem(
    val id: Long,
    val title: String,
    val subtitle: String? = null
)

data class SelectorUiState(
    val items: List<SelectorItem> = emptyList(),
    val selectedItemId: Long? = null,
    val onItemSelected: (Long) -> Unit = {}
)
