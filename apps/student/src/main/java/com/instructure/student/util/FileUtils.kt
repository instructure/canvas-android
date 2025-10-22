/*
 * Copyright (C) 2016 - present Instructure, Inc.
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
 */

package com.instructure.student.util

import android.content.Context
import android.net.Uri
import androidx.annotation.IntegerRes
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader
import com.instructure.student.R
import com.instructure.student.activity.CandroidPSPDFActivity
import com.instructure.pandautils.features.shareextension.ShareFileSubmissionTarget
import com.pspdfkit.PSPDFKit
import com.pspdfkit.annotations.AnnotationType
import com.pspdfkit.configuration.activity.PdfActivityConfiguration
import com.pspdfkit.configuration.activity.ThumbnailBarMode
import com.pspdfkit.configuration.page.PageFitMode
import com.pspdfkit.configuration.page.PageScrollDirection
import com.pspdfkit.preferences.PSPDFKitPreferences
import com.pspdfkit.ui.PdfActivityIntentBuilder
import com.pspdfkit.ui.special_mode.controller.AnnotationTool

object FileUtils {

    fun showPdfDocument(
        uri: Uri,
        loadedMedia: OpenMediaAsyncTaskLoader.LoadedMedia,
        context: Context,
        submissionTarget: ShareFileSubmissionTarget? = null
    ) {
        val annotationCreationList = listOf(
                AnnotationTool.INK,
                AnnotationTool.HIGHLIGHT,
                AnnotationTool.STRIKEOUT,
                AnnotationTool.SQUARE,
                AnnotationTool.NOTE,
                AnnotationTool.FREETEXT,
                AnnotationTool.ERASER
        )

        val annotationEditList = listOf(
            AnnotationType.INK,
            AnnotationType.HIGHLIGHT,
            AnnotationType.STRIKEOUT,
            AnnotationType.SQUARE,
            AnnotationType.NOTE,
            AnnotationType.FREETEXT,
            AnnotationType.NONE // Wee need this to enable the eraser
        )
        if (!PSPDFKitPreferences.get(context).isAnnotationCreatorSet) {
            PSPDFKitPreferences.get(context).setAnnotationCreator(ApiPrefs.user?.shortName.orEmpty())
        }

        val pspdfActivityConfiguration: PdfActivityConfiguration

        if (loadedMedia.isSubmission) {
            // We don't want to allow users to edit for submission viewing
            pspdfActivityConfiguration = PdfActivityConfiguration.Builder(context)
                .scrollDirection(PageScrollDirection.HORIZONTAL)
                .setThumbnailBarMode(ThumbnailBarMode.THUMBNAIL_BAR_MODE_PINNED)
                .fitMode(PageFitMode.FIT_TO_WIDTH)
                .build()
        } else {
            // Standard behavior
            pspdfActivityConfiguration = PdfActivityConfiguration.Builder(context)
                .scrollDirection(PageScrollDirection.HORIZONTAL)
                .setDocumentInfoViewSeparated(false)
                .enabledAnnotationTools(annotationCreationList)
                .editableAnnotationTypes(annotationEditList)
                .fitMode(PageFitMode.FIT_TO_WIDTH)
                .build()
        }

        if (PSPDFKit.isOpenableUri(context, uri)) {
            val intent = PdfActivityIntentBuilder
                .fromUri(context, uri)
                .configuration(pspdfActivityConfiguration)
                .activityClass(CandroidPSPDFActivity::class.java)
                .build()
            intent.putExtra(com.instructure.pandautils.utils.Const.SUBMISSION_TARGET, submissionTarget)
            context.startActivity(intent)
        } else {
            //If we still can't open this PDF, we will then attempt to pass it off to the user's pdfviewer
            context.startActivity(loadedMedia.intent)
        }

    }

    @IntegerRes
    fun getFileIcon(filename: String, contentType: String): Int {
        return when {
            contentType.startsWith("image") -> R.drawable.ic_image
            contentType.startsWith("video") -> R.drawable.ic_media
            contentType.startsWith("audio") -> R.drawable.ic_audio
            else -> when (filename.substringAfterLast(".")) {
                "doc", "docx", "txt", "rtf", "pdf", "xls" -> R.drawable.ic_document
                "zip", "tar", "7z", "apk", "jar", "rar" -> R.drawable.ic_attachment
                else -> R.drawable.ic_attachment
            }
        }
    }
}
