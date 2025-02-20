/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
import androidx.room.Room
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.OfflineAnalyticsManager
import com.instructure.pandautils.analytics.pageview.PageViewUtils
import com.instructure.pandautils.analytics.pageview.db.PageViewDao
import com.instructure.pandautils.analytics.pageview.db.PageViewDatabase
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.date.DateTimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AnalyticsModule {

    @Provides
    @Singleton
    fun providePageViewDatabase(@ApplicationContext context: Context): PageViewDatabase {
        return Room.databaseBuilder(context, PageViewDatabase::class.java, "db-page-view").build()
    }

    @Provides
    @Singleton
    fun providePageViewDao(pageViewDatabase: PageViewDatabase): PageViewDao {
        return pageViewDatabase.pageViewDao()
    }

    @Provides
    fun provideAnalytics(): Analytics {
        return Analytics
    }

    @Provides
    fun provideOfflineAnalyticsManager(
        @ApplicationContext context: Context,
        analytics: Analytics,
        pageViewUtils: PageViewUtils,
        apiPrefs: ApiPrefs,
        dateTimeProvider: DateTimeProvider,
        featureFlagProvider: FeatureFlagProvider
    ): OfflineAnalyticsManager {
        return OfflineAnalyticsManager(context, analytics, pageViewUtils, apiPrefs, dateTimeProvider, featureFlagProvider)
    }

    @Provides
    fun providePageViewUtils(pageViewDao: PageViewDao): PageViewUtils {
        return PageViewUtils(pageViewDao)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PageViewEntryPoint {
    fun pageViewUtils(): PageViewUtils
}