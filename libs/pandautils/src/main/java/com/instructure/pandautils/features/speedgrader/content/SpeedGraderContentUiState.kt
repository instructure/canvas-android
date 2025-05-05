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

import android.net.Uri
import android.os.Parcelable
import com.instructure.canvasapi2.models.Attachment
import kotlinx.parcelize.Parcelize

data class SpeedGraderContentUiState(
    val content: GradeableContent? = null
)

@Parcelize
sealed class GradeableContent : Parcelable
object NoSubmissionContent : GradeableContent()
object NoneContent : GradeableContent()
class ExternalToolContent(val url: String) : GradeableContent()
object OnPaperContent : GradeableContent()
object UnsupportedContent : GradeableContent()
class OtherAttachmentContent(val attachment: Attachment) : GradeableContent()
class PdfContent(val url: String) : GradeableContent()
class TextContent(val text: String) : GradeableContent()
class ImageContent(val url: String, val contentType: String) : GradeableContent()
class UrlContent(val url: String, val previewUrl: String?) : GradeableContent()
class DiscussionContent(val previewUrl: String?) : GradeableContent()
class StudentAnnotationContent(val submissionId: Long, val attempt: Long) : GradeableContent()
object AnonymousSubmissionContent : GradeableContent()

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