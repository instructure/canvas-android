/*
 * Copyright (C) 2018 - present Instructure, Inc.
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

package com.instructure.pandautils.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.managers.FileUploadConfig
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.models.*
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.copyTo
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.weave.WeaveCoroutine
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.R
import java.io.File
import java.util.*

object MediaUploadUtils {

    fun takeNewPhotoBecausePermissionsAlreadyGranted(fragment: Fragment?, activity: Activity): Uri? {
        // Get the location of the saved picture
        val fileName = "rce_${System.currentTimeMillis()}.jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, fileName)

        val imageUri = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (imageUri != null) {
            // Save the intent information in case we get booted from memory.
            FilePrefs.tempCaptureUri = imageUri.toString()
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraIntent.putExtra(Const.IS_OVERRIDDEN, true)
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1) // Requests front camera on some apps
        fragment?.startActivityForResult(cameraIntent, RequestCodes.CAMERA_PIC_REQUEST)
            ?: activity.startActivityForResult(cameraIntent, RequestCodes.CAMERA_PIC_REQUEST)

        return imageUri
    }

    fun chooseFromGalleryBecausePermissionsAlreadyGranted(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val file = File(activity.filesDir, "/image/*")
        intent.setDataAndType(FileProvider.getUriForFile(activity, activity.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, file), "image/*")
        activity.startActivityForResult(intent, RequestCodes.PICK_IMAGE_GALLERY)
    }

    fun showPickImageDialog(fragment: Fragment) {
        showPickImageDialog(fragment, fragment.requireActivity())
    }

    // Allows fragments to request permissions and receive results for start activity, but defaults to activity if fragment is null
    fun showPickImageDialog(fragment: Fragment?, activity: Activity) {
        val root = LayoutInflater.from(activity).inflate(R.layout.dialog_profile_source, null)
        val dialog = AlertDialog.Builder(activity)
                .setView(root)
                .create()

        root.findViewById<View>(R.id.takePhotoItem).onClick {
            newPhoto(fragment, activity)
            dialog.dismiss()
        }

        root.findViewById<View>(R.id.chooseFromGalleryItem).onClick {
            chooseFromGallery(fragment, activity)
            dialog.dismiss()
        }

        dialog.show()
    }

    const val REQUEST_CODE_PERMISSIONS_TAKE_PHOTO = 223
    private fun newPhoto(fragment: Fragment?, activity: Activity) {
        if (!Utils.hasCameraAvailable(activity)) {
            Toast.makeText(activity, R.string.noCameraOnDevice, Toast.LENGTH_SHORT).show()
            return
        }

        if (PermissionUtils.hasPermissions(activity, PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA)) {
            takeNewPhotoBecausePermissionsAlreadyGranted(fragment, activity)
        } else {
            val permissions = PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA)
            fragment?.requestPermissions(permissions, REQUEST_CODE_PERMISSIONS_TAKE_PHOTO)
                ?: activity.requestPermissions(permissions.toSet()) { results ->
                    if (results.isNotEmpty() && results.all { it.value }) {
                        takeNewPhotoBecausePermissionsAlreadyGranted(fragment, activity)
                    } else {
                        Toast.makeText(activity, R.string.permissionDenied, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    const val REQUEST_CODE_PERMISSIONS_GALLERY = 332
    private fun chooseFromGallery(fragment: Fragment?, activity: Activity) {
        if (!PermissionUtils.hasPermissions(activity, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            fragment?.requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS_GALLERY)
                ?: activity.requestPermissions(setOf(PermissionUtils.WRITE_EXTERNAL_STORAGE)) { results ->
                    if (results.isNotEmpty() && results.all { it.value }) {
                        chooseFromGallery(fragment, activity)
                    } else {
                        Toast.makeText(activity, R.string.permissionDenied, Toast.LENGTH_LONG).show()
                    }
                }
            return
        }

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val file = File(activity.filesDir, "/image/*")
        intent.setDataAndType(FileProvider.getUriForFile(activity, activity.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY, file), "image/*")
        fragment?.startActivityForResult(intent, RequestCodes.PICK_IMAGE_GALLERY)
            ?: activity.startActivityForResult(intent, RequestCodes.PICK_IMAGE_GALLERY)
    }

    fun uploadRceImageJob(uri: Uri, canvasContext: CanvasContext, activity: Activity, insertImageCallback: (text: String, altText: String) -> Unit): WeaveCoroutine {
        val isTeacher = (canvasContext as? Course)?.isTeacher == true
        val tempFile = File(activity.externalCacheDir, "tmp-rce-image")
        var progressDialog: AlertDialog? = null

        return tryWeave(false) {
            // Show progress dialog
            progressDialog = AlertDialog.Builder(activity)
                    .setTitle(R.string.image_uploading)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.cancel) { dialog, _ ->
                        cancel()
                        dialog.dismiss()
                    }.create()
            progressDialog?.show()

            val uploadedFile = inBackground {
                // Copy image to a temp location
                activity.contentResolver.openInputStream(uri)?.copyTo(tempFile)

                val fso = FileSubmitObject(
                    "rce-${UUID.randomUUID()}.jpeg",
                    tempFile.length(),
                    "image/jpeg",
                    tempFile.absolutePath
                )

                val uploadConfig = if (isTeacher) {
                    FileUploadConfig.forCourse(fso, canvasContext.id)
                } else {
                    FileUploadConfig.forUser(fso)
                }

                // Perform file upload
                FileUploadManager.uploadFile(uploadConfig).dataOrThrow
            }

            // Grab the file data so we can get the URL we need to insert into the img tag
            var file = awaitApi<FileFolder> {
                if (isTeacher) FileFolderManager.getCourseFile(canvasContext.id, uploadedFile.id , true, it)
                else FileFolderManager.getUserFile(uploadedFile.id, true, it)
            }

            if (isTeacher) {
                // This file is getting uploaded as a course file; we need to make it published so others can see the image
                val usageRights: Boolean

                file.isLocked = false
                file.isHidden = false
                file.lockDate = null
                file.unlockDate = null

                val updateFileFolder = UpdateFileFolder(file.name, file.lockDate.toApiString(),
                    file.unlockDate.toApiString(), file.isLocked, file.isHidden)

                // Determine if this course has the usage rights feature enabled
                val features = awaitApi<List<String>> { FeaturesManager.getEnabledFeaturesForCourse(canvasContext.id, true, it) }
                usageRights = features.contains("usage_rights_required")
                if (usageRights) {
                    val usageRightsParams: MutableMap<String, Any> = mutableMapOf(Pair("file_ids[]", file.id),
                            Pair("usage_rights[use_justification]", FileUsageRightsJustification.PUBLIC_DOMAIN.apiString))

                    // Update usage rights
                    file.usageRights = awaitApi<UsageRights> { FileFolderManager.updateUsageRights(canvasContext.id, usageRightsParams, it) }
                }

                // Update the file
                file = awaitApi { FileFolderManager.updateFile(file.id, updateFileFolder, it) }
            }

            insertImageCallback(file.url ?: "", "")

            // Delete temporary image
            tempFile.delete()

            // Done uploading file - dismiss progress dialog
            progressDialog?.dismiss()
        } catch {
            progressDialog?.dismiss()
            activity.runOnUiThread {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.image_upload_error)
                        .setPositiveButton(R.string.retry) { _, _ -> uploadRceImageJob(uri, canvasContext, activity, insertImageCallback) }
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show()
            }
        }
    }

    fun handleCameraPicResult(activity: Activity, capturedImageUri: Uri?, crop: Boolean = false): Uri? {
        var imageUri = capturedImageUri
        if (imageUri == null) {
            // Recover Uri from prefs in case we were booted from memory
            imageUri = Uri.parse(FilePrefs.tempCaptureUri)
        }

        // If it's still null, tell the user there is an error and return
        if (imageUri == null) {
            Toast.makeText(activity, R.string.errorGettingPhoto, Toast.LENGTH_SHORT).show()
            return imageUri
        }

        if (crop) {
            // Open image for cropping
            val config = AvatarCropConfig(imageUri)
            val cropIntent = AvatarCropActivity.createIntent(activity, config)
            activity.startActivityForResult(cropIntent, RequestCodes.CROP_IMAGE)
        }

        return imageUri
    }
}
