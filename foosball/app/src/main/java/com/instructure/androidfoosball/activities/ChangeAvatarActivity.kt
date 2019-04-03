/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.instructure.androidfoosball.BuildConfig
import com.instructure.androidfoosball.R
import com.instructure.androidfoosball.utils.setVisible
import kotlinx.android.synthetic.main.activity_change_avatar.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * I was going to add comments and documentation to this file, but then I didn't.
 */

enum class Mode(val displayName : String) {
    CAMERA("Camera"),
    Gallery("Photo Library")
}

class ChangeAvatarActivity : AppCompatActivity() {

    companion object {
        val EXTRA_ACTION_MODE = "actionMode"
        val EXTRA_USER_ID = "userId"
        val EXTRA_AVATAR_URL = "avatarUrl"
        fun createIntent(context: Context, userId: String?, mode: Mode): Intent {
            return Intent(context, ChangeAvatarActivity::class.java).apply {
                putExtra(EXTRA_USER_ID, userId)
                putExtra(EXTRA_ACTION_MODE, mode)
            }
        }
    }

    val USER_AVATARS_DIR = "user_avatars"
    val AVATAR_TMP_FILE_NAME = "avatar_tmp"

    val REQUEST_CODE_TAKE_PICTURE = 1337
    val REQUEST_CODE_GET_GALLERY_IMAGE = 7331

    private val userId: String? by lazy { intent.getStringExtra(EXTRA_USER_ID) }
    private val mode: Mode by lazy { intent.getSerializableExtra(EXTRA_ACTION_MODE) as Mode }
    private val tmpFile by lazy { File(externalCacheDir, AVATAR_TMP_FILE_NAME) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_avatar)
        setup()
        when (mode) {
            Mode.CAMERA -> takeImage()
            Mode.Gallery -> pickImage()
        }
    }

    fun setup() {
        btnBack.setOnClickListener { finish() }
        btnDone.setOnClickListener { cropAndPost() }
        imageView.setOnImageEventListener(object : SubsamplingScaleImageView.OnImageEventListener {
            override fun onReady() { }
            override fun onTileLoadError(p0: Exception?) { }
            override fun onImageLoadError(p0: Exception?) { }
            override fun onPreviewLoadError(p0: Exception?) { }
            override fun onImageLoaded() {
                btnDone.setVisible()
                progressBar.setVisible(false)
            }
        })
    }

    fun takeImage() {
        try {
            val intent = Intent()
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val contentUri = FileProvider.getUriForFile(this, "com.instructure.androidfoosball.fileProvider", tmpFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile))
            }
            startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE)
        } catch (anfe: ActivityNotFoundException) {
            Toast.makeText(this, "No activity found to open this attachment.", Toast.LENGTH_LONG).show()
        }
    }

    fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE_GET_GALLERY_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) { finish(); return }
        when (requestCode) {
            REQUEST_CODE_TAKE_PICTURE -> setImage()
            REQUEST_CODE_GET_GALLERY_IMAGE -> {
                contentResolver.openInputStream(data!!.data).copyTo(FileOutputStream(tmpFile))
                setImage()
            }
        }
    }

    fun setImage() {
        root.visibility = View.VISIBLE
        imageView.orientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF
        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)
        imageView.setImage(ImageSource.uri(tmpFile.absolutePath))
    }

    private fun cropAndPost() {

        val dialog = ProgressDialog(this).apply {
            setMessage(context.getString(R.string.uploading))
            isIndeterminate = true
        }

        dialog.show()

        doAsync {
            val outFile = cropFile(tmpFile, getCropInfo())
            uiThread {
                val uuid = UUID.randomUUID().toString()
                val ref = FirebaseStorage.getInstance().getReferenceFromUrl(BuildConfig.FIREBASE_STORAGE_BASE_URL).child(USER_AVATARS_DIR).child("$uuid.jpg")
                ref.putFile(Uri.fromFile(outFile))
                        .addOnSuccessListener {
                            val avatarUrl = it.downloadUrl.toString()
                            if (!userId.isNullOrBlank()) {
                                FirebaseDatabase.getInstance().reference.child("users").child(userId).child("avatar").setValue(avatarUrl)
                            }
                            dialog.dismiss()
                            if (callingActivity != null) {
                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra(EXTRA_AVATAR_URL, avatarUrl)
                                })
                            }
                            finish()
                        }
                        .addOnFailureListener {
                            dialog.dismiss()
                            Toast.makeText(this@ChangeAvatarActivity, R.string.error_uploading_photo, Toast.LENGTH_SHORT).show()
                        }
            }
        }
    }

    fun getCropInfo(): RectF {
        val origin = imageView.viewToSourceCoord(0f, 0f)
        val dimen = imageView.viewToSourceCoord(imageView.width.toFloat(), imageView.height.toFloat())

        val (appliedWidth, appliedHeight) = when (imageView.appliedOrientation) {
            90, 270 -> Pair(imageView.sHeight, imageView.sWidth)
            else -> Pair(imageView.sWidth, imageView.sHeight)
        }

        return RectF(
                origin.x / appliedWidth,
                origin.y / appliedHeight,
                dimen.x / appliedWidth,
                dimen.y / appliedHeight)
    }

}

fun showImageSourcePicker(context: Context, userId: String? = null, onSelected: (intent: Intent) -> Unit) {
    AlertDialog.Builder(context)
            .setItems(Mode.values().map { it.displayName }.toTypedArray()) { dialog, which ->
                onSelected(ChangeAvatarActivity.createIntent(context, userId, Mode.values()[which]))
            }.show()
}

private val CROP_SIZE = 512
private val COMPRESS_QUALITY = 70

private fun cropFile(srcFile: File, cropInfoInput: RectF?): File {

    val bOptions = BitmapFactory.Options()
    bOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(srcFile.absolutePath, bOptions)
    bOptions.inJustDecodeBounds = false

    val orientationTag = ExifInterface(srcFile.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

    val sWidth = bOptions.outWidth
    val sHeight = bOptions.outHeight

    val cropInfo: RectF = if (cropInfoInput != null) cropInfoInput else {

        var aWidth = sWidth
        var aHeight = sHeight

        if (orientationTag == ExifInterface.ORIENTATION_ROTATE_270 || orientationTag == ExifInterface.ORIENTATION_ROTATE_90) {
            aWidth = sHeight
            aHeight = sWidth
        }

        val dimenX: Float = if (aHeight > aWidth) 0.5f else aHeight / 2f / aWidth
        val dimenY: Float = if (aHeight > aWidth) aWidth / 2f / aHeight else 0.5f
        RectF(0.5f - dimenX, 0.5f - dimenY, 0.5f + dimenX, 0.5f + dimenY)
    }

    val adjustedCrop = when (orientationTag) {
        ExifInterface.ORIENTATION_ROTATE_90 -> RectF(cropInfo.top, 1 - cropInfo.right, cropInfo.bottom, 1 - cropInfo.left)
        ExifInterface.ORIENTATION_ROTATE_180 -> RectF(1 - cropInfo.right, 1 - cropInfo.bottom, 1 - cropInfo.left, 1 - cropInfo.top)
        ExifInterface.ORIENTATION_ROTATE_270 -> RectF(1 - cropInfo.bottom, cropInfo.left, 1 - cropInfo.top, cropInfo.right)
        else -> cropInfo
    }

    val croppedWidth = (sWidth * adjustedCrop.width()).toInt()
    val sampleScale = if (croppedWidth < CROP_SIZE) 1 else croppedWidth / CROP_SIZE * 2

    bOptions.inSampleSize = sampleScale

    // Ignored by system in Lollipop and higher
    @Suppress("DEPRECATION")
    bOptions.inPurgeable = true

    val decodeRect = Rect(
            (sWidth * adjustedCrop.left).toInt(),
            (sHeight * adjustedCrop.top).toInt(),
            (sWidth * adjustedCrop.right).toInt(),
            (sHeight * adjustedCrop.bottom).toInt())

    val input = BitmapRegionDecoder.newInstance(srcFile.absolutePath, false).decodeRegion(decodeRect, bOptions)

    val filterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    filterPaint.isFilterBitmap = true

    val output = Bitmap.createBitmap(CROP_SIZE, CROP_SIZE, Bitmap.Config.RGB_565)
    val cacheCanvas = Canvas(output)

    val matrix = Matrix()
    val scale = CROP_SIZE.toFloat() / input.width
    val center = CROP_SIZE / 2f
    matrix.preScale(scale, scale)
    when (orientationTag) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f, center, center)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f, center, center)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f, center, center)
    }

    cacheCanvas.drawBitmap(input, matrix, filterPaint)

    val outFile = File(srcFile.absolutePath + "_cropped")
    output.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, outFile.outputStream())
    return outFile
}
