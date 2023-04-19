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

package com.instructure.pandautils.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File

class StorageUtils(
    @ApplicationContext private val context: Context
) {

    fun getTotalSpace(): Long {
        val externalStorageDirectory = Environment.getExternalStorageDirectory()
        return externalStorageDirectory.totalSpace
    }

    fun getFreeSpace(): Long {
        val externalStorageDirectory = Environment.getExternalStorageDirectory()
        return externalStorageDirectory.freeSpace
    }

    fun getAppSize(): Long {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        val appInfo = packageInfo.applicationInfo

        val appSize = File(appInfo.publicSourceDir).length()
        val dataDirSize = getDirSize(File(appInfo.dataDir))
        val cacheDirSize = getDirSize(context.cacheDir)
        val externalCacheDirSize = getDirSize(context.externalCacheDir)
        val filesDirSize = getDirSize(context.filesDir)
        val externalFilesDirSize = getDirSize(context.getExternalFilesDir(null))

        return appSize + dataDirSize + cacheDirSize + externalCacheDirSize + filesDirSize + externalFilesDirSize
    }

    private fun getDirSize(directory: File?): Long {
        if (directory == null || !directory.isDirectory) return 0
        var size: Long = 0
        val files = directory.listFiles() ?: return 0
        for (file in files) size += if (file.isDirectory) getDirSize(file) else file.length()
        return size
    }
}