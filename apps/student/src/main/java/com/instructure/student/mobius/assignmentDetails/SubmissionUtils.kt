/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.requestPermissions
import com.instructure.student.mobius.assignmentDetails.submissionDetails.ui.SubmissionTypesVisibilities
import com.instructure.student.mobius.common.ConsumerQueueWrapper
import com.instructure.student.mobius.common.ui.SubmissionHelper
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Uri?.getVideoIntent(): Intent? = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    putExtra(MediaStore.EXTRA_OUTPUT, this@getVideoIntent)
}

fun Context.getVideoUri(): Uri {
    // Get the uri that we're saving the file to
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(FileUploadUtils.getExternalCacheDir(this), "video_$timeStamp.mp4")

    return FileProvider.getUriForFile(
        this,
        packageName + Const.FILE_PROVIDER_AUTHORITY,
        file
    )
}

fun <EVENT>Context.launchVideo(createEvent: (uri: Uri) -> EVENT, permissionFail: () -> Unit, consumer: ConsumerQueueWrapper<EVENT>, requestCode: Int) {
    if (this.needsPermissions({this.launchVideo(createEvent, permissionFail, consumer, requestCode)}, { permissionFail() }, PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO))
        return

    val uri = this.getVideoUri()
    val event = createEvent(uri)

    // Store our reference to the URI that's being passed to the video view
    consumer.accept(event)

    // Create new Intent and launch
    val intent = uri.getVideoIntent()

    if (intent != null && isIntentAvailable(intent.action)) {
        (this as Activity).startActivityForResult(intent, requestCode)
    }
}

fun Context.launchAudio(permissionFail: () -> Unit, showRecordingView: () -> Unit) {
    if (this.needsPermissions({this.launchAudio(permissionFail, showRecordingView)}, { permissionFail() }, PermissionUtils.RECORD_AUDIO))
        return

    showRecordingView()
}

fun Context.isIntentAvailable(action: String?): Boolean {
    return this.packageManager.queryIntentActivities(
        Intent(action),
        PackageManager.MATCH_DEFAULT_ONLY
    ).size > 0
}

fun Context.needsPermissions(successCallback: () -> Unit, failureCallback: () -> Unit, vararg permissions: String): Boolean {
    if (PermissionUtils.hasPermissions(this as Activity, *permissions)) {
        return false
    }

    this.requestPermissions(setOf(*permissions)) { results ->
        if (results.isNotEmpty() && results.all { it.value }) {
            successCallback()
        } else {
            failureCallback()
        }
    }
    return true
}

fun uploadAudioRecording(submissionHelper: SubmissionHelper, file: File, assignment: Assignment, course: Course) {
    submissionHelper.startMediaSubmission(
        canvasContext = course,
        assignmentId = assignment.id,
        assignmentGroupCategoryId = assignment.groupCategoryId,
        assignmentName = assignment.name,
        mediaFilePath = file.path,
        mediaSource = "audio_recorder"
    )
}

fun getSubmissionTypesVisibilities(assignment: Assignment, isStudioEnabled: Boolean): SubmissionTypesVisibilities {
    val visibilities = SubmissionTypesVisibilities()

    val submissionTypes = assignment.getSubmissionTypes()

    for (submissionType in submissionTypes) {
        when (submissionType) {
            Assignment.SubmissionType.ONLINE_UPLOAD -> {
                visibilities.fileUpload = true
                visibilities.studioUpload = isStudioEnabled
            }
            Assignment.SubmissionType.ONLINE_TEXT_ENTRY -> visibilities.textEntry = true
            Assignment.SubmissionType.ONLINE_URL -> visibilities.urlEntry = true
            Assignment.SubmissionType.MEDIA_RECORDING -> visibilities.mediaRecording = true
            Assignment.SubmissionType.STUDENT_ANNOTATION -> visibilities.studentAnnotation = true
            else -> {}
        }
    }

    return visibilities
}

val chooseMediaIntent: Intent by lazy {
    Intent(Intent.ACTION_GET_CONTENT).apply {
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        type = "video/*, audio/*"
        addCategory(Intent.CATEGORY_OPENABLE)
    }
}

fun getFormattedAttemptDate(date: Date): String = DateFormat.getDateTimeInstance(
    DateFormat.MEDIUM,
    DateFormat.SHORT,
    Locale.getDefault()
).format(date)
