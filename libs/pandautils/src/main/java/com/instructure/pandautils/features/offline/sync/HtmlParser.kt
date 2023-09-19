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
package com.instructure.pandautils.features.offline.sync

import android.util.Log
import com.instructure.pandautils.room.offline.daos.LocalFileDao

class HtmlParser(private var localFileDao: LocalFileDao) {

    private val imageRegex = Regex("<img.*src=\"([^\"]*)\".*>")

    suspend fun createHtmlStringWithLocalFiles(html: String?): String? {
        if (html == null) return null

        var result: String = html
        val matches = imageRegex.findAll(result)
        matches.forEach { match ->
            Log.d("asdasd", "match: ${match.groupValues[0]}   src match: ${match.groupValues[1]}")
            val imageUrl = match.groupValues[1]
            val fileId = Regex("files/(\\d+)").find(imageUrl)?.groupValues?.get(1)?.toLongOrNull()
            if (fileId != null) {
                val file = localFileDao.findById(fileId)
                file?.path?.let {
                    result = result.replace(imageUrl, "file://$it")
                }
            }
        }

        return result
    }
}