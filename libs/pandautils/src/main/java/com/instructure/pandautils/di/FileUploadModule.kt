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

package com.instructure.pandautils.di

import android.content.ContentResolver
import android.content.Context
import com.instructure.canvasapi2.managers.FileUploadManager
import com.instructure.pandautils.features.file.upload.FileUploadUtilsHelper
import com.instructure.pandautils.utils.FileUploadUtils
import com.instructure.pandautils.utils.NotoriousUploader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class FileUploadModule {

    @Provides
    fun provideFileUploadUtils(): FileUploadUtils {
        return FileUploadUtils
    }

    @Provides
    fun provideFileUploadUtilsHelper(@ApplicationContext context: Context, contentResolver: ContentResolver, fileUploadUtils: FileUploadUtils): FileUploadUtilsHelper {
        return FileUploadUtilsHelper(fileUploadUtils, context, contentResolver)
    }

    @Provides
    fun provideNotoriousUploader(): NotoriousUploader {
        return NotoriousUploader
    }

    @Provides
    fun provideFileUploadManager(): FileUploadManager {
        return FileUploadManager
    }
}