/*
 * Copyright (C) 2022 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.pandautils.features.file.upload

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.instructure.canvasapi2.models.postmodels.FileSubmitObject
import com.instructure.pandautils.utils.FileUploadUtils

class FileUploadUtilsHelper(private val fileUploadUtils: FileUploadUtils, private val context: Context, private val contentResolver: ContentResolver) {
    fun getFileMimeType(fileUri: Uri): String {
        return fileUploadUtils.getFileMimeType(contentResolver, fileUri)
    }

    fun getFileNameWithDefault(fileUri: Uri): String {
        return fileUploadUtils.getFileNameWithDefault(contentResolver, fileUri)
    }

    fun getFileSubmitObjectFromInputStream(fileUri: Uri, fileName: String, mimeType: String): FileSubmitObject? {
        return fileUploadUtils.getFileSubmitObjectFromInputStream(context, fileUri, fileName, mimeType)
    }
}