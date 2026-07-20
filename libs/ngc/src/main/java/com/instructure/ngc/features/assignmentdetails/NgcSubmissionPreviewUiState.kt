/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.ngc.features.assignmentdetails

import android.net.Uri
import com.instructure.canvasapi2.models.Assignment.SubmissionType
import com.instructure.canvasapi2.models.Attachment
import com.instructure.canvasapi2.models.Submission
import com.pspdfkit.annotations.Annotation

/**
 * Runtime state driving the [NGCSubmissionPreview] card. Produced by [NgcSubmissionPreviewViewModel]
 * from a [NgcSubmissionPreviewPlan]: the synchronous plans map straight through, while the file-backed
 * plans ([NgcSubmissionPreviewPlan.Pdf] / [NgcSubmissionPreviewPlan.TextFile]) resolve to [Loading]
 * first and then to their final state once the file has been downloaded.
 */
sealed interface NgcSubmissionPreviewUiState {
    data object Loading : NgcSubmissionPreviewUiState

    /** No inline preview is possible (LTI, quiz, discussion, annotation, or resolution failure). */
    data object NoPreview : NgcSubmissionPreviewUiState

    /**
     * A file upload whose first file can't be rendered inline; we list the files as items instead.
     * The list may itself overflow the preview window, in which case a "N additional files" row is
     * appended for the ones that don't fit.
     */
    data class NoFilePreview(val fileNames: List<String>) : NgcSubmissionPreviewUiState

    /**
     * Number of files that aren't shown in this preview (neither previewed nor listed). For a
     * single-file preview (image/pdf/media/text file), this is every other uploaded file; rendered
     * as a "N additional files" row beneath the preview. 0 when nothing else is hidden.
     */
    data class Image(val uri: Uri, val displayName: String, val additionalFileCount: Int = 0) : NgcSubmissionPreviewUiState
    data class Pdf(val uri: Uri, val additionalFileCount: Int = 0) : NgcSubmissionPreviewUiState
    data class Media(
        val uri: Uri,
        val thumbnailUrl: String,
        val contentType: String,
        val displayName: String,
        val additionalFileCount: Int = 0,
    ) : NgcSubmissionPreviewUiState

    /** Rendered in a fixed-height WebView. Used for both text entry (HTML) and text file uploads. */
    data class Text(
        val content: String,
        val contentType: String,
        val useInAppFormatting: Boolean,
        val baseUrl: String?,
        val additionalFileCount: Int = 0,
    ) : NgcSubmissionPreviewUiState

    /** A submitted URL, loaded into a fixed-height WebView. */
    data class Url(val url: String) : NgcSubmissionPreviewUiState

    /** A `student_annotation` submission: the raw PDF plus the annotations drawn on top of it. */
    data class AnnotatedPdf(val uri: Uri, val annotations: List<Annotation>) : NgcSubmissionPreviewUiState
}

/**
 * Pure description of what to preview for a submission, with no I/O. [mapSubmissionPreviewPlan]
 * derives it from the [Submission]; [NgcSubmissionPreviewViewModel] turns it into a
 * [NgcSubmissionPreviewUiState]. Kept separate from the UI state so the type-selection logic
 * (including the file-picking order) is unit-testable without downloading anything.
 */
sealed interface NgcSubmissionPreviewPlan {
    data object NoPreview : NgcSubmissionPreviewPlan
    data class NoFilePreview(val fileNames: List<String>) : NgcSubmissionPreviewPlan
    data class Image(val url: String, val displayName: String, val additionalFileCount: Int = 0) : NgcSubmissionPreviewPlan
    data class Media(
        val url: String,
        val thumbnailUrl: String,
        val contentType: String,
        val displayName: String,
        val additionalFileCount: Int = 0,
    ) : NgcSubmissionPreviewPlan

    /** Text entry submission body (HTML). */
    data class TextEntry(val html: String, val baseUrl: String?) : NgcSubmissionPreviewPlan
    data class Url(val url: String) : NgcSubmissionPreviewPlan

    /** File-backed: must be downloaded before it can be rendered. */
    data class Pdf(val url: String, val additionalFileCount: Int = 0) : NgcSubmissionPreviewPlan
    data class TextFile(val url: String, val contentType: String, val additionalFileCount: Int = 0) : NgcSubmissionPreviewPlan

    /**
     * A `student_annotation` submission. Resolved to a downloaded annotated PDF via a CanvaDoc
     * session (see [NgcAnnotatedPdfUseCase]) before rendering, so it carries only the identifiers.
     */
    data class AnnotatedPdf(val submissionId: Long, val attempt: Long) : NgcSubmissionPreviewPlan
}

/**
 * Decides how to preview [submission]. Types without a possible inline preview (quizzes, external
 * tools, discussions, Studio LTI launches) map to [NgcSubmissionPreviewPlan.NoPreview]; student
 * annotations resolve to an annotated PDF ([NgcSubmissionPreviewPlan.AnnotatedPdf]). For file
 * uploads the decision hinges on the **first** attachment: if it can be rendered inline we preview
 * it and report the remaining files as `additionalFileCount`; otherwise we list the files via
 * [NgcSubmissionPreviewPlan.NoFilePreview] (even if a later file would have been previewable).
 *
 * @param baseUrl assignment base URL, used to resolve relative links in a text-entry submission.
 */
fun mapSubmissionPreviewPlan(submission: Submission?, baseUrl: String?): NgcSubmissionPreviewPlan {
    submission ?: return NgcSubmissionPreviewPlan.NoPreview
    return when (submission.submissionType) {
        SubmissionType.ONLINE_TEXT_ENTRY.apiString ->
            NgcSubmissionPreviewPlan.TextEntry(submission.body.orEmpty(), baseUrl)

        SubmissionType.ONLINE_URL.apiString ->
            submission.url?.takeIf { it.isNotBlank() }?.let { NgcSubmissionPreviewPlan.Url(it) }
                ?: NgcSubmissionPreviewPlan.NoPreview

        SubmissionType.MEDIA_RECORDING.apiString -> mediaRecordingPlan(submission)

        SubmissionType.ONLINE_UPLOAD.apiString -> uploadPlan(submission)

        SubmissionType.STUDENT_ANNOTATION.apiString ->
            NgcSubmissionPreviewPlan.AnnotatedPdf(submission.id, submission.attempt)

        else -> NgcSubmissionPreviewPlan.NoPreview
    }
}

private fun mediaRecordingPlan(submission: Submission): NgcSubmissionPreviewPlan {
    val mediaComment = submission.mediaComment
    val url = mediaComment?.url
    if (!url.isNullOrBlank()) {
        return NgcSubmissionPreviewPlan.Media(
            url = url,
            thumbnailUrl = "",
            contentType = mediaComment.contentType ?: submission.mediaContentType.orEmpty(),
            displayName = mediaComment.displayName.orEmpty(),
        )
    }
    // Some media recordings arrive as a regular attachment instead of a media comment.
    return uploadPlan(submission)
}

private fun uploadPlan(submission: Submission): NgcSubmissionPreviewPlan {
    val attachments = submission.attachments
    // The first attachment decides between a single-file preview and the file list; the rest are
    // reported as "additional files".
    val additionalFileCount = (attachments.size - 1).coerceAtLeast(0)
    val firstPreview = attachments.firstOrNull()?.let { attachmentPlan(it, additionalFileCount) }
    if (firstPreview != null) return firstPreview
    val fileNames = attachments.mapNotNull { it.displayName ?: it.filename }
    return NgcSubmissionPreviewPlan.NoFilePreview(fileNames)
}

/** Maps a single attachment to a renderable plan, or null when its type can't be previewed inline. */
private fun attachmentPlan(attachment: Attachment, additionalFileCount: Int): NgcSubmissionPreviewPlan? {
    val url = attachment.url?.takeIf { it.isNotBlank() } ?: return null
    val contentType = attachment.contentType.orEmpty()
    val displayName = attachment.displayName ?: attachment.filename.orEmpty()
    return when {
        contentType == "application/pdf" -> NgcSubmissionPreviewPlan.Pdf(url, additionalFileCount)
        contentType.startsWith("image") -> NgcSubmissionPreviewPlan.Image(url, displayName, additionalFileCount)
        contentType.startsWith("video") || contentType.startsWith("audio") ->
            NgcSubmissionPreviewPlan.Media(url, attachment.thumbnailUrl.orEmpty(), contentType, displayName, additionalFileCount)

        contentType.startsWith("text") -> NgcSubmissionPreviewPlan.TextFile(url, contentType, additionalFileCount)
        else -> null
    }
}
