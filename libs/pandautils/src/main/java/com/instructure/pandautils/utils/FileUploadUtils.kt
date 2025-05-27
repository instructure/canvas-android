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
package com.instructure.pandautils.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.canvasapi2.utils.tryOrNull
import com.instructure.pandautils.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

object FileUploadUtils {
    private const val FILE_SCHEME = "file"
    private const val CONTENT_SCHEME = "content"

    private val APPLE_EXTENSIONS_MIME_TYPES = mapOf<String, String> (
        "pages" to "application/vnd.apple.pages",
        "numbers" to "application/vnd.apple.numbers",
        "key" to "application/vnd.apple.keynote",
    )

    @SuppressLint("Recycle")
    private fun getFileNameFromUri(resolver: ContentResolver, uri: Uri): String? {
        val cursor = resolver.query(uri, null, null, null, null) ?: return null
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val name = cursor.getString(nameIndex)
        cursor.close()
        return name
    }

    fun getFile(context: Context, uri: Uri): FileSubmitObject? {
        val contentResolver = context.contentResolver
        val mimeType = getFileMimeType(contentResolver, uri)
        val fileName = getFileNameWithDefault(contentResolver, uri)
        return getFileSubmitObjectFromInputStream(context, uri, fileName, mimeType)
    }

    fun getFileSubmitObjectFromInputStream(context: Context, uri: Uri?, filename: String, mimeType: String?): FileSubmitObject? {
        uri ?: return null
        var fileName = filename
        var file: File?
        var errorMessage: String? = ""
        // copy file from uri into new temporary file and pass back that new file's path
        var input: InputStream? = null
        var output: FileOutputStream? = null
        try {
            val cr = context.contentResolver
            input = cr.openInputStream(uri)
            // add extension to filename if needed
            val lastDot = fileName.lastIndexOf(".")
            val extension = getFileExtensionFromMimeType(cr.getType(uri))
            if (lastDot == -1 && extension.isNotEmpty()) {
                fileName = "$fileName.$extension"
            }

            // add file version if needed
            var version = 1
            var fileNameFile = File(getTempFilePath(context, fileName))
            val ext = fileName.substring(fileName.lastIndexOf("."))
            while (fileNameFile.exists()) {
                fileName = "${filename.dropLast(ext.length)}(${version++})$ext"
                fileNameFile = File(getTempFilePath(context, fileName))
            }

            // create a temp file to copy the uri contents into
            val tempFilePath = getTempFilePath(context, fileName)
            output = FileOutputStream(tempFilePath)
            var read: Int
            val bytes = ByteArray(4096)
            if (input != null) {
                while (input.read(bytes).also { read = it } != -1) {
                    output.write(bytes, 0, read)
                }
            }
            // return the filepath of our copied file.
            file = File(tempFilePath)
        } catch (e: FileNotFoundException) {
            file = null
            errorMessage = context.getString(R.string.errorOccurred)
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, e.toString())
        } catch (exception: Exception) {
            // if querying the data column and the FileDescriptor both fail We can't handle the shared file.
            file = null
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, exception.toString())
            errorMessage = context.getString(R.string.errorLoadingFiles)
        } finally {
            tryOrNull { input?.close() }
            tryOrNull { output?.close() }
        }
        return if (file != null) {
            FileSubmitObject(fileName, file.length(), mimeType!!, file.absolutePath, errorMessage, FileSubmitObject.STATE.NORMAL)
        } else FileSubmitObject(fileName, 0, mimeType!!, "", errorMessage, FileSubmitObject.STATE.NORMAL)
    }

    fun getFileSubmitObjectByFileUri(uri: Uri?, filename: String, mimeType: String?): FileSubmitObject? {
        return uri?.path?.let {
            val file = File(it)
            FileSubmitObject(
                name = filename,
                size = file.length(),
                contentType = mimeType.orEmpty(),
                fullPath = file.absolutePath
            )
        }
    }

    fun getFileNameWithDefault(resolver: ContentResolver, uri: Uri): String {
        var fileName: String? = ""
        val scheme = uri.scheme
        if (FILE_SCHEME.equals(scheme, ignoreCase = true)) {
            fileName = uri.lastPathSegment
        } else if (CONTENT_SCHEME.equals(scheme, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

            // get file name
            var metaCursor: Cursor? = null
            // Don't have try with resources, so we get a finally block that can close the cursor
            try {
                metaCursor = resolver.query(uri, projection, null, null, null)
                if (metaCursor != null) {
                    if (metaCursor.moveToFirst()) {
                        fileName = metaCursor.getString(0)
                    }
                }
            } catch (ignore: Exception) {
                fileName = uri.lastPathSegment
            } finally {
                metaCursor?.close()
            }
        }
        return getTempFilename(fileName)
    }

    fun getFileMimeType(resolver: ContentResolver, uri: Uri): String {
        val scheme = uri.scheme
        var mimeType: String? = null
        if (FILE_SCHEME.equals(scheme, ignoreCase = true)) {
            if (uri.lastPathSegment != null) {
                val extension = uri.lastPathSegment!!
                mimeType = getMimeTypeFromFileNameWithExtension(extension)
            }
        } else if (CONTENT_SCHEME.equals(scheme, ignoreCase = true)) {
            mimeType = resolver.getType(uri)
            val fileName = getFileNameFromUri(resolver, uri)
            val extension = fileName?.substringAfterLast('.')?.substringBefore(' ')
            if (APPLE_EXTENSIONS_MIME_TYPES.keys.contains(extension)) {
                mimeType = APPLE_EXTENSIONS_MIME_TYPES[extension]
            }
        }
        return mimeType ?: "*/*"
    }

    private fun getMimeTypeFromFileNameWithExtension(fileNameWithExtension: String): String {
        val mime = MimeTypeMap.getSingleton()
        val index = fileNameWithExtension.indexOf(".")
        var ext = ""
        if (index != -1) {
            ext = fileNameWithExtension.substring(index + 1).lowercase(Locale.getDefault()) // Add one so the dot isn't included
        }
        var mimeType = mime.getMimeTypeFromExtension(ext).orEmpty()
        val extension = fileNameWithExtension.substringAfterLast('.')
        if (APPLE_EXTENSIONS_MIME_TYPES.keys.contains(extension)) {
            mimeType = APPLE_EXTENSIONS_MIME_TYPES[extension].orEmpty()
        }
        return mimeType
    }

    private fun getTempFilename(fileName: String?): String {
        return when (fileName) {
            null, "" -> "File_Upload"
            "image.jpg" -> "Image_Upload"
            "video.mpg", "video.mpeg" ->  "Video_Upload"
            else -> fileName
        }
    }

    fun getFileExtensionFromMimeType(mimeType: String?): String {
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(mimeType) ?: return ""
    }

    private fun getTempFilePath(context: Context, fileName: String): String {
        val outputDir = getCacheDir(context)
        val outputFile = File(outputDir, fileName.replace("/", "_"))
        return outputFile.absolutePath
    }

    private fun getCacheDir(context: Context): File {
        val canvasFolder = File(context.cacheDir, "file_upload")
        if (!canvasFolder.exists()) canvasFolder.mkdirs()
        return canvasFolder
    }

    fun deleteTempFile(filename: String?): Boolean {
        val file = File(filename)
        return file.delete()
    }

    fun getExternalCacheDir(context: Context): File {
        val cacheDir = File(context.externalCacheDir, "file_upload")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        return cacheDir
    }

    fun createTaskLoaderBundle(canvasContext: CanvasContext?, url: String?, title: String?, authenticate: Boolean): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext)
        bundle.putString(Const.INTERNAL_URL, url)
        bundle.putBoolean(Const.AUTHENTICATE, authenticate)
        bundle.putString(Const.ACTION_BAR_TITLE, title)
        return bundle
    }
}
