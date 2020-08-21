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
package com.instructure.canvasapi2.utils

import android.content.Context
import android.webkit.MimeTypeMap
import java.io.*

object FileUtils {
    private const val FILE_SUFFIX = ".serializable"
    const val FILE_DIRECTORY = "cache"

    /**
     * Deletes the cache file with the given name
     */
    fun deleteFile(context: Context?, cacheFileName: String?): Boolean {
        return if (context == null || cacheFileName == null) {
            false
        } else try {
            // Use buffering
            val f = File(context.filesDir, FILE_DIRECTORY)
            f.mkdirs()
            val file = File(f, cacheFileName + FILE_SUFFIX)
            file.delete()
        } catch (E: Exception) {
            false
        }
    }

    /**
     * Converts a specified file to a serializable object.
     */
    fun fileToSerializable(context: Context, cacheFileName: String?): Serializable? {
        try {
            // Use buffering
            val f = File(context.filesDir, FILE_DIRECTORY)
            f.mkdirs()
            val file = File(f, cacheFileName + FILE_SUFFIX)
            val fileInputStream: InputStream = FileInputStream(file)
            val buffer: InputStream = BufferedInputStream(fileInputStream)
            ObjectInputStream(buffer).use { input ->
                // Deserialize
                return input.readObject() as Serializable
            }
        } catch (E: Exception) {
            return null
        }
    }

    /**
     * deleteAllFilesInDirectory will RECURSIVELY delete all files/folders in a directory
     */
    fun deleteAllFilesInDirectory(startFile: File?): Boolean = startFile?.deleteRecursively() ?: false

    /**
     * getFileExtensionFromMimeType returns what's after the /.
     * For example : image/png returns png.
     */
    fun getFileExtensionFromMimeType(mimeType: String?): String = mimeType?.substringAfterLast('/').orEmpty()

    fun getMimeType(url: String?): String {
        return MimeTypeMap.getFileExtensionFromUrl(url)?.let {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
        }.orEmpty()
    }

    fun notoriousCodeFromMimeType(mimeType: String?): String = when (mimeType?.substringBefore('/')) {
        "video" -> "1"
        "audio" -> "5"
        else -> "0"
    }

    fun mediaTypeFromNotoriousCode(notoriousCode: Long): String = when (notoriousCode) {
        1L -> "video"
        5L -> "audio"
        else -> ""
    }

    /**
     * GetAssetsFile allows you to open a file that exists in the Assets directory.
     */
    fun getAssetsFile(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
