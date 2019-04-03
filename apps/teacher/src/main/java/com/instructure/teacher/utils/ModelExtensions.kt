/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
@file:JvmName("ModelExtensions")
package com.instructure.teacher.utils

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.webkit.MimeTypeMap
import com.instructure.canvasapi2.apis.EnrollmentAPI
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.type.EnrollmentType
import com.instructure.canvasapi2.utils.ContextKeeper
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.interactions.router.Route
import com.instructure.interactions.router.RouteContext
import com.instructure.pandautils.activities.BaseViewMediaActivity
import com.instructure.pandautils.models.EditableFile
import com.instructure.pandautils.utils.nonNullArgs
import com.instructure.teacher.R
import com.instructure.teacher.fragments.ViewHtmlFragment
import com.instructure.teacher.fragments.ViewImageFragment
import com.instructure.teacher.fragments.ViewPdfFragment
import com.instructure.teacher.fragments.ViewUnsupportedFileFragment
import com.instructure.teacher.router.RouteMatcher
import com.pspdfkit.ui.PdfFragment

fun Parcel.writeBoolean(bool: Boolean) = writeByte((if (bool) 1 else 0).toByte())
fun Parcel.readBoolean() = readByte() != 0.toByte()

// region Attachment extensions

private const val MEDIA_PLACEHOLDER_ID = -123L

val Attachment.iconRes: Int
    get() = when {
        contentType == null -> R.drawable.vd_utils_attachment
        contentType!!.startsWith("image") -> R.drawable.vd_image
        contentType!!.startsWith("video") -> R.drawable.vd_media
        contentType!!.startsWith("audio") -> R.drawable.vd_audio
        else -> when (filename!!.substringAfterLast('.', "").toLowerCase()) {
            "doc", "docx" -> R.drawable.vd_document
            "txt" -> R.drawable.vd_document
            "rtf" -> R.drawable.vd_document
            "pdf" -> R.drawable.vd_pdf
            "xls" -> R.drawable.vd_document
            "zip","tar", "7z", "apk", "jar", "rar" -> R.drawable.vd_utils_attachment
            else -> R.drawable.vd_utils_attachment
        }
    }


fun Attachment.asMediaSubmissionPlaceholder(submission: Submission?): Attachment {
    submission?.mediaComment?.let {
        id = MEDIA_PLACEHOLDER_ID
        filename = it._fileName
        contentType = it.contentType ?: "video/*"
    }
    return this
}

val Attachment.isMediaSubmissionPlaceholder: Boolean get() = id == MEDIA_PLACEHOLDER_ID

@JvmName("viewAttachment")
fun Attachment.view(context: Context) {
    viewMedia(context, filename!!, contentType!!, url, thumbnailUrl, displayName, iconRes)
}

/**
 * With the exception of the full screen media views, the fragments from this viewMedia method will
 * be launched as a detail view if toolbarColor is set and the device is a tablet. (i.e. from the
 * file list)
 */

fun viewMedia(context: Context, filename: String, contentType: String, url: String?, thumbnailUrl: String?, displayName: String?, iconRes: Int, toolbarColor: Int = 0, editableFile: EditableFile? = null) {
    val extension = filename.substringAfterLast('.')
    when {
    // PDF
        contentType == "application/pdf" -> {
            PdfFragment()
            val bundle = ViewPdfFragment.newInstance(url ?: "", toolbarColor, editableFile).nonNullArgs
            RouteMatcher.route(context, Route(null, ViewPdfFragment::class.java, null, bundle))
        }
    // Audio/Video
        contentType.startsWith("video") || contentType.startsWith("audio") -> {
            val bundle = BaseViewMediaActivity.makeBundle(url.orEmpty(), thumbnailUrl, contentType, displayName, true, editableFile)
            RouteMatcher.route(context, Route(bundle, RouteContext.MEDIA))
        }
    // Image
        contentType.startsWith("image") -> {
            val title = displayName ?: filename
            val bundle = ViewImageFragment.newInstance(title, Uri.parse(url), contentType, true, toolbarColor, editableFile).nonNullArgs
            RouteMatcher.route(context, Route(null, ViewImageFragment::class.java, null, bundle))
        }
    // HTML
        contentType == "text/html" || extension == "htm" || extension == "html" -> {
            val bundle = ViewHtmlFragment.newInstance(url ?: "", filename, toolbarColor, editableFile).nonNullArgs
            RouteMatcher.route(context, Route(null, ViewHtmlFragment::class.java, null, bundle))
        }
    // Multipart (Unknown)
        contentType == "multipart/form-data" -> {
            //Discover the actual type of file
            val type: String? = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if(type != null && type != "multipart/form-data") {
                viewMedia(context, filename, type, url, thumbnailUrl, displayName, iconRes, toolbarColor, editableFile)
            } else {
                // Other
                val bundle = ViewUnsupportedFileFragment.newInstance(Uri.parse(url), filename, contentType, tryOrNull { Uri.parse(thumbnailUrl) }, iconRes, toolbarColor, editableFile).nonNullArgs
                RouteMatcher.route(context, Route(null, ViewUnsupportedFileFragment::class.java, null, bundle))
            }
        }
    // Other
        else -> {
            val bundle = ViewUnsupportedFileFragment.newInstance(Uri.parse(url), filename, contentType, tryOrNull { Uri.parse(thumbnailUrl) }, iconRes, toolbarColor, editableFile).nonNullArgs
            RouteMatcher.route(context, Route(null, ViewUnsupportedFileFragment::class.java, null, bundle))
        }
    }
}

// endregion

@Suppress("unused")
val GroupAssignee.iconRes: Int get() = R.drawable.vd_group

val Enrollment.displayType: CharSequence get() = ContextKeeper.appContext.getText(when {
    isStudent -> R.string.enrollmentTypeStudent
    isTeacher -> R.string.enrollmentTypeTeacher
    isObserver -> R.string.enrollmentTypeObserver
    isTA -> R.string.enrollmentTypeTeachingAssistant
    isDesigner -> R.string.enrollmentTypeDesigner
    else -> R.string.enrollmentTypeUnknown
})

val EnrollmentType?.displayText: CharSequence
    get() = ContextKeeper.appContext.getText(
        when (this) {
            EnrollmentType.STUDENTENROLLMENT -> R.string.enrollmentTypeStudent
            EnrollmentType.TEACHERENROLLMENT -> R.string.enrollmentTypeTeacher
            EnrollmentType.OBSERVERENROLLMENT -> R.string.enrollmentTypeObserver
            EnrollmentType.TAENROLLMENT -> R.string.enrollmentTypeTeachingAssistant
            EnrollmentType.DESIGNERENROLLMENT -> R.string.enrollmentTypeDesigner
            else -> R.string.enrollmentTypeUnknown
        }
    )

fun Course.hasActiveEnrollment(): Boolean = enrollments?.any { it.enrollmentState == EnrollmentAPI.STATE_ACTIVE } ?: false
