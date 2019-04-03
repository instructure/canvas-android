/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.activities

import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.DiscussionEntry
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.copyTo
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.R
import com.instructure.pandautils.services.NotoriousUploadService
import com.instructure.pandautils.utils.*
import kotlinx.android.synthetic.main.notorious_media_upload_picker.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NotoriousMediaUploadPicker : AppCompatActivity() {

    private var capturedImageURI: Uri? = null

    // Use the internal cache dir so other apps can't access the user's media files
    private val mediaStorageDir: File
        get() = File(cacheDir, "media_capture").apply { if (!exists()) mkdirs() }

    /**
     * Create a File for saving an image or video
     */
    private val outputMediaFileUri: Uri?
        get() {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val mediaFile = File(mediaStorageDir, "canvas_media_comment$timeStamp.mp4")
            capturedImageURI = Uri.parse(mediaFile.absolutePath)
            return getUriForFile(this, applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, mediaFile)
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notorious_media_upload_picker)
        rootView.onClick { finish() }
        takeVideo.onClick { newVideo() }
        chooseMedia.onClick { chooseMedia() }
    }

    @TargetApi(23)
    private fun newVideo() {
        if (PermissionUtils.hasPermissions(this, PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO)) {
            takeVideoBecausePermissionsAlreadyGranted()
        } else {
            requestPermissions(
                PermissionUtils.makeArray(PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO),
                PermissionUtils.PERMISSION_REQUEST_CODE
            )
        }
    }

    @TargetApi(23)
    private fun chooseMedia() {
        if (PermissionUtils.hasPermissions(this, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "video/*,audio/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*", "audio/*"))
            }
            startActivityForResult(intent, RequestCodes.SELECT_MEDIA)
        } else {
            requestPermissions(
                PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE),
                PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun takeVideoBecausePermissionsAlreadyGranted() {
        // Check to see if the device has a camera
        if (!Utils.hasCameraAvailable(this)) {
            Toast.makeText(applicationContext, R.string.noCameraOnDevice, Toast.LENGTH_LONG).show()
            return
        }

        // Create new Intent
        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(MediaStore.EXTRA_OUTPUT, outputMediaFileUri)
        }
        startActivityForResult(cameraIntent, RequestCodes.TAKE_VIDEO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                takeVideoBecausePermissionsAlreadyGranted()
            } else {
                Toast.makeText(this, R.string.permissionDenied, Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                chooseMedia()
            } else {
                Toast.makeText(this, R.string.permissionDenied, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        weave {
            var mediaUri: Uri? = null
            if (requestCode == RequestCodes.SELECT_MEDIA && data != null) {
                val tempMediaUri = data.data

                if (tempMediaUri == null || tempMediaUri.path == null || tempMediaUri.path.isEmpty()) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                } else {
                    val filePath = FileUploadUtils.getPath(this@NotoriousMediaUploadPicker, tempMediaUri)
                    if (filePath != null && !filePath.isEmpty() && File(filePath).exists()) {
                        // This is a local file
                        mediaUri = Uri.parse(filePath)
                    } else {
                        /* This is a remote file and we'll need to make a local copy first. Temporary access
                        permissions are only granted to the current Context, so the copy operation must happen here. */
                        val progressDialog = ProgressDialog(this@NotoriousMediaUploadPicker).apply {
                            isIndeterminate = true
                            setCanceledOnTouchOutside(false)
                            setMessage(getString(R.string.loading))
                        }
                        progressDialog.show()
                        mediaUri = inBackground {
                            val fileName = FileUploadUtils.getFileNameFromUri(contentResolver, tempMediaUri)
                            val file = File(mediaStorageDir, fileName)
                            contentResolver.openInputStream(tempMediaUri).copyTo(file)
                            Uri.parse(file.absolutePath)
                        }
                        progressDialog.hide()
                    }
                }

            } else if (requestCode == RequestCodes.TAKE_VIDEO) {
                mediaUri = capturedImageURI
            }

            if (mediaUri != null) {
                val serviceIntent = Intent(this@NotoriousMediaUploadPicker, NotoriousUploadService::class.java).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Const.MEDIA_FILE_PATH, mediaUri.path)
                    val action = intent.getSerializableExtra(Const.ACTION) as NotoriousUploadService.ACTION
                    when (action) {
                        NotoriousUploadService.ACTION.SUBMISSION_COMMENT -> {
                            putExtra(Const.ACTION, NotoriousUploadService.ACTION.SUBMISSION_COMMENT)
                            putExtra(Const.ASSIGNMENT, intent.getParcelableExtra<Parcelable>(Const.ASSIGNMENT))
                            putExtra(Const.STUDENT_ID, intent.getLongExtra(Const.STUDENT_ID, ApiPrefs.user!!.id))
                            putExtra(Const.IS_GROUP, intent.getBooleanExtra(Const.IS_GROUP, false))
                        }
                        NotoriousUploadService.ACTION.ASSIGNMENT_SUBMISSION -> {
                            putExtra(Const.ACTION, NotoriousUploadService.ACTION.ASSIGNMENT_SUBMISSION)
                            putExtra(Const.ASSIGNMENT, intent.getParcelableExtra<Parcelable>(Const.ASSIGNMENT))
                        }
                        NotoriousUploadService.ACTION.DISCUSSION_COMMENT -> {
                            putExtra(Const.ACTION, NotoriousUploadService.ACTION.DISCUSSION_COMMENT)
                            putExtra(
                                Const.DISCUSSION_ENTRY,
                                intent.getParcelableExtra<Parcelable>(Const.DISCUSSION_ENTRY)
                            )
                            putExtra(Const.MESSAGE, intent.getStringExtra(Const.MESSAGE))
                            putExtra(Const.DISCUSSION_ID, intent.getLongExtra(Const.DISCUSSION_ID, 0))
                            putExtra(
                                Const.CANVAS_CONTEXT,
                                intent.getParcelableExtra<Parcelable>(Const.CANVAS_CONTEXT)
                            )
                        }
                    }
                }
                startService(serviceIntent)
                setResult(Activity.RESULT_OK)
                val intent = Intent(Const.UPLOAD_STARTED)
                LocalBroadcastManager.getInstance(this@NotoriousMediaUploadPicker).sendBroadcast(intent)
                finish()
            }
        }
    }

    companion object {

        @JvmStatic
        fun createIntentForAssignmentSubmission(context: Context, assignment: Assignment): Intent {
            return Intent(context, NotoriousMediaUploadPicker::class.java).apply {
                putExtra(Const.ACTION, NotoriousUploadService.ACTION.ASSIGNMENT_SUBMISSION)
                putExtra(Const.ASSIGNMENT, assignment as Parcelable)
            }
        }

        @JvmStatic
        fun createIntentForSubmissionComment(context: Context, assignment: Assignment): Intent {
            return Intent(context, NotoriousMediaUploadPicker::class.java).apply {
                putExtra(Const.ACTION, NotoriousUploadService.ACTION.SUBMISSION_COMMENT)
                putExtra(Const.ASSIGNMENT, assignment as Parcelable)
            }
        }

        @JvmStatic
        fun createIntentForTeacherSubmissionComment(
            context: Context,
            assignment: Assignment,
            studentId: Long,
            isGroupComment: Boolean
        ) = Intent(context, NotoriousMediaUploadPicker::class.java).apply {
            putExtra(Const.ACTION, NotoriousUploadService.ACTION.SUBMISSION_COMMENT)
            putExtra(Const.ASSIGNMENT, assignment as Parcelable)
            putExtra(Const.STUDENT_ID, studentId)
            putExtra(Const.IS_GROUP, isGroupComment)
        }

        @JvmStatic
        fun createIntentForDiscussionReply(
            context: Context,
            discussionEntry: DiscussionEntry,
            message: String,
            discussionId: Long,
            canvasContext: CanvasContext
        ) = Intent(context, NotoriousMediaUploadPicker::class.java).apply {
            putExtra(Const.ACTION, NotoriousUploadService.ACTION.DISCUSSION_COMMENT)
            putExtra(Const.DISCUSSION_ENTRY, discussionEntry as Parcelable)
            putExtra(Const.MESSAGE, message)
            putExtra(Const.DISCUSSION_ID, discussionId)
            putExtra(Const.CANVAS_CONTEXT, canvasContext as Parcelable)
        }
    }
}
