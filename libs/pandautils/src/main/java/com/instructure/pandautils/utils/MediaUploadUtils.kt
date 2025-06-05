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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.managers.FeaturesManager
import com.instructure.canvasapi2.managers.FileFolderManager
import com.instructure.canvasapi2.managers.FileUploadConfig
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.FileFolder
import com.instructure.canvasapi2.models.FileUsageRightsJustification
import com.instructure.canvasapi2.models.UpdateFileFolder
import com.instructure.canvasapi2.models.UsageRights
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.copyTo
import com.instructure.canvasapi2.utils.toApiString
import com.instructure.canvasapi2.utils.weave.WeaveCoroutine
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.pandautils.R
import instructure.rceditor.databinding.RceDialogAltTextBinding
import java.io.File
import java.util.UUID

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

    fun chooseFromGalleryBecausePermissionsAlreadyGranted(fragment: Fragment?, activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val file = File(activity.filesDir, "/image/*")
        intent.setDataAndType(
            FileProvider.getUriForFile(
                activity,
                activity.applicationContext.packageName + Const.FILE_PROVIDER_AUTHORITY,
                file
            ), "image/*"
        )
        fragment?.startActivityForResult(intent, RequestCodes.PICK_IMAGE_GALLERY)
            ?: activity.startActivityForResult(intent, RequestCodes.PICK_IMAGE_GALLERY)
    }

    fun showPickImageDialog(fragment: Fragment) {
        showPickImageDialog(fragment, fragment.requireActivity())
    }

    // Allows fragments to request permissions and receive results for start activity, but defaults to activity if fragment is null
    fun showPickImageDialog(fragment: Fragment?, activity: Activity) {
        showPickImageDialog(
            activity = activity,
            onNewPhotoClick = {
                newPhoto(fragment, activity)
            },
            onChooseFromGalleryClick = {
                chooseFromGallery(fragment, activity)
            }
        )
    }

    fun showPickImageDialog(activity: Activity, onNewPhotoClick: () -> Unit, onChooseFromGalleryClick: () -> Unit) {
        val root = LayoutInflater.from(activity).inflate(R.layout.dialog_profile_source, null)
        val dialog = AlertDialog.Builder(activity)
            .setView(root)
            .create()

        root.findViewById<View>(R.id.takePhotoItem).onClick {
            checkCameraPermissions(activity) {
                onNewPhotoClick()
                dialog.dismiss()
            }
        }

        root.findViewById<View>(R.id.chooseFromGalleryItem).onClick {
            checkGalleryPermissions(activity) {
                onChooseFromGalleryClick()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    const val REQUEST_CODE_PERMISSIONS_TAKE_PHOTO = 223
    private fun checkCameraPermissions(activity: Activity, onPermissionGranted: () -> Unit) {
        if (!Utils.hasCameraAvailable(activity)) {
            Toast.makeText(activity, R.string.noCameraOnDevice, Toast.LENGTH_SHORT).show()
            return
        }

        if (PermissionUtils.hasPermissions(activity, PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA)) {
            onPermissionGranted()
        } else {
            val permissions = PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA)
            activity.requestPermissions(permissions.toSet()) { results ->
                if (results.isNotEmpty() && results.all { it.value }) {
                    onPermissionGranted()
                } else {
                    Toast.makeText(activity, R.string.permissionDenied, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkGalleryPermissions(activity: Activity, onPermissionGranted: () -> Unit) {
        if (PermissionUtils.hasPermissions(activity, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
            onPermissionGranted()
        } else {
            activity.requestPermissions(setOf(PermissionUtils.WRITE_EXTERNAL_STORAGE)) { results ->
                if (results.isNotEmpty() && results.all { it.value }) {
                    onPermissionGranted()
                } else {
                    Toast.makeText(activity, R.string.permissionDenied, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun newPhoto(fragment: Fragment?, activity: Activity) {
        if (!Utils.hasCameraAvailable(activity)) {
            Toast.makeText(activity, R.string.noCameraOnDevice, Toast.LENGTH_SHORT).show()
            return
        }
        checkCameraPermissions(activity) {
            takeNewPhotoBecausePermissionsAlreadyGranted(fragment, activity)
        }
    }

    const val REQUEST_CODE_PERMISSIONS_GALLERY = 332
    private fun chooseFromGallery(fragment: Fragment?, activity: Activity) {
        checkGalleryPermissions(activity) {
            chooseFromGalleryBecausePermissionsAlreadyGranted(fragment, activity)
        }
    }

    fun uploadRceImageJob(
        uri: Uri,
        canvasContext: CanvasContext,
        activity: Activity,
        @ColorInt buttonColor: Int = ThemePrefs.textButtonColor,
        insertImageCallback: (imageUrl: String) -> Unit = {}
    ): WeaveCoroutine {
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
            progressDialog?.setOnShowListener {
                progressDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(buttonColor)
            }
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
                if (isTeacher) FileFolderManager.getCourseFile(canvasContext.id, uploadedFile.id, true, it)
                else FileFolderManager.getUserFile(uploadedFile.id, true, it)
            }

            if (isTeacher) {
                // This file is getting uploaded as a course file; we need to make it published so others can see the image
                val usageRights: Boolean

                file.isLocked = false
                file.isHidden = false
                file.lockDate = null
                file.unlockDate = null

                val updateFileFolder = UpdateFileFolder(
                    file.name, file.lockDate.toApiString(),
                    file.unlockDate.toApiString(), file.isLocked, file.isHidden
                )

                // Determine if this course has the usage rights feature enabled
                val features = awaitApi<List<String>> { FeaturesManager.getEnabledFeaturesForCourse(canvasContext.id, true, it) }
                usageRights = features.contains("usage_rights_required")
                if (usageRights) {
                    val usageRightsParams: MutableMap<String, Any> = mutableMapOf(
                        Pair("file_ids[]", file.id),
                        Pair("usage_rights[use_justification]", FileUsageRightsJustification.PUBLIC_DOMAIN.apiString)
                    )

                    // Update usage rights
                    file.usageRights =
                        awaitApi<UsageRights> { FileFolderManager.updateUsageRights(canvasContext.id, usageRightsParams, it) }
                }

                // Update the file
                file = awaitApi { FileFolderManager.updateFile(file.id, updateFileFolder, it) }
            }

            insertImageCallback(file.url ?: "")

            // Delete temporary image
            tempFile.delete()

            // Done uploading file - dismiss progress dialog
            progressDialog?.dismiss()
        } catch {
            progressDialog?.dismiss()
            activity.runOnUiThread {
                AlertDialog.Builder(activity)
                    .setTitle(R.string.image_upload_error)
                    .setPositiveButton(R.string.retry) { _, _ -> uploadRceImageJob(uri, canvasContext, activity, insertImageCallback = insertImageCallback) }
                    .setNegativeButton(android.R.string.cancel, null)
                    .showThemed(buttonColor)
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

    fun showAltTextDialog(
        activity: Activity,
        @ColorInt buttonColor: Int = ThemePrefs.textButtonColor,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit
    ) {
        val dialogBinding = RceDialogAltTextBinding.inflate(LayoutInflater.from(activity), null, false)
        val altTextInput = dialogBinding.altText

        var buttonClicked = false

        val altTextDialog = AlertDialog.Builder(activity)
            .setTitle(activity.getString(instructure.rceditor.R.string.rce_dialogAltText))
            .setView(dialogBinding.root)
            .setPositiveButton(activity.getString(android.R.string.ok)) { _, _ ->
                buttonClicked = true
                onPositiveClick(altTextInput.text.toString())
            }
            .setNegativeButton(activity.getString(android.R.string.cancel)) { _, _ ->
                buttonClicked = true
                onNegativeClick()
            }
            .setOnDismissListener {
                if (!buttonClicked) {
                    onNegativeClick()
                }
            }
            .create().apply {
                setOnShowListener {
                    val dialog = it as? AlertDialog
                    dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(buttonColor)
                    dialog?.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(buttonColor)
                    dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                }
            }

        altTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                altTextDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !s.isNullOrEmpty()
            }
        })

        altTextDialog.show()
    }
}
