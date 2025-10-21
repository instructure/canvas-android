/*
 * Copyright (C) 2024 - present Instructure, Inc.
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
package com.instructure.pandautils.di

import android.content.Context
import android.webkit.CookieManager
import com.instructure.pandautils.utils.DownloadNotificationHelper
import com.instructure.pandautils.utils.FileDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FileDownloaderModule {

    @Provides
    @Singleton
    fun provideDownloadNotificationHelper(@ApplicationContext context: Context): DownloadNotificationHelper {
        return DownloadNotificationHelper(context)
    }

    @Provides
    fun provideFileDownloader(
        @ApplicationContext context: Context,
        cookieManager: CookieManager,
        downloadNotificationHelper: DownloadNotificationHelper
    ): FileDownloader {
        return FileDownloader(context, cookieManager, downloadNotificationHelper)
    }
}
