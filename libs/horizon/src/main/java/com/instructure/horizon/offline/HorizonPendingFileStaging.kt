/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.horizon.offline

import android.content.Context
import android.net.Uri
import com.instructure.canvasapi2.utils.copyTo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HorizonPendingFileStaging @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private fun rootDir(): File = File(context.filesDir, ROOT_DIR_NAME)

    private fun actionDir(actionId: Long): File = File(rootDir(), actionId.toString())

    fun stage(actionId: Long, uri: Uri, fileName: String): File {
        val dir = actionDir(actionId).apply { mkdirs() }
        val destFile = File(dir, fileName.replace("/", "_"))
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open input stream for staged file: $uri")
        input.copyTo(destFile)
        return destFile
    }

    fun filesFor(actionId: Long): List<File> {
        val dir = actionDir(actionId)
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles()?.toList().orEmpty()
    }

    fun delete(actionId: Long) {
        actionDir(actionId).deleteRecursively()
    }

    fun deleteOrphans(activeActionIds: Set<Long>) {
        val root = rootDir()
        if (!root.isDirectory) return
        root.listFiles()?.forEach { dir ->
            val id = dir.name.toLongOrNull()
            if (id == null || id !in activeActionIds) {
                dir.deleteRecursively()
            }
        }
    }

    companion object {
        private const val ROOT_DIR_NAME = "horizon-pending"
    }
}
