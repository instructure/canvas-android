/*
 * Copyright (C) 2023 - present Instructure, Inc.
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

package com.instructure.teacher.features.files.details

import com.instructure.pandautils.models.EditableFile

data class FileDetailsViewData(
    val fileData: FileViewData
)

sealed class FileViewData {
    data class Pdf(
        val url: String,
        val editableFile: EditableFile
    ) : FileViewData()

    data class Media(
        val url: String,
        val thumbnailUrl: String,
        val contentType: String,
        val displayName: String,
        val editableFile: EditableFile
    ) : FileViewData()

    data class Image(
        val title: String,
        val url: String,
        val contentType: String,
        val editableFile: EditableFile
    ) : FileViewData()

    data class Html(
        val url: String,
        val fileName: String,
        val editableFile: EditableFile
    ) : FileViewData()

    data class Other(
        val url: String,
        val fileName: String,
        val contentType: String,
        val thumbnailUrl: String,
        val editableFile: EditableFile
    ) : FileViewData()
}