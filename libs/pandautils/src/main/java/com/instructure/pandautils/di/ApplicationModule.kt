/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.pandautils.di

import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.webkit.CookieManager
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.apis.FileFolderAPI
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.pageview.PageViewUtils
import com.instructure.pandautils.analytics.OfflineAnalyticsManager
import com.instructure.pandautils.features.offline.sync.HtmlParser
import com.instructure.pandautils.room.offline.daos.FileFolderDao
import com.instructure.pandautils.room.offline.daos.FileSyncSettingsDao
import com.instructure.pandautils.room.offline.daos.LocalFileDao
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.StorageUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.date.DateTimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.threeten.bp.Clock
import java.util.Locale
import java.util.TimeZone
import javax.inject.Singleton

/**
 * Module that provides all the application scope dependencies, that are not related to other module.
 */
@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Singleton
    @Provides
    fun providesTypefaceBehavior(@ApplicationContext context: Context): TypefaceBehavior {
        return TypefaceBehavior(context)
    }

    @Provides
    fun provideCrashlytics(@ApplicationContext context: Context): FirebaseCrashlytics {
        // Have to initialize FirebaseApp because we inject DatabaseProvider into AppManager and DatabaseProvider uses FirebaseCrashlytics
        // No-op if already initialized
        FirebaseApp.initializeApp(context)
        return FirebaseCrashlytics.getInstance()
    }

    @Provides
    fun provideResources(@ApplicationContext context: Context): Resources {
        return context.resources
    }

    @Provides
    fun provideHtmlContentFormatter(@ApplicationContext context: Context, oAuthManager: OAuthManager, firebaseCrashlytics: FirebaseCrashlytics): HtmlContentFormatter {
        return HtmlContentFormatter(context, firebaseCrashlytics, oAuthManager)
    }

    @Provides
    @Singleton
    fun provideColorKeeper(): ColorKeeper {
        return ColorKeeper
    }

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideCookieManager(): CookieManager {
        return CookieManager.getInstance()
    }

    @Provides
    @Singleton
    fun provideStorageUtils(@ApplicationContext context: Context): StorageUtils {
        return StorageUtils(context)
    }

    @Provides
    fun provideHtmlParses(
        localFileDao: LocalFileDao,
        apiPrefs: ApiPrefs,
        fileFolderDao: FileFolderDao,
        @ApplicationContext context: Context,
        fileSyncSettingsDao: FileSyncSettingsDao,
        fileFolderApi: FileFolderAPI.FilesFoldersInterface
    ): HtmlParser {
        return HtmlParser(localFileDao, apiPrefs, fileFolderDao, context, fileSyncSettingsDao, fileFolderApi)
    }

    @Provides
    fun provideClock(): Clock {
        return Clock.systemDefaultZone()
    }

    @Provides
    @Singleton
    fun provideThemePrefs(): ThemePrefs {
        return ThemePrefs
    }

    @Provides
    fun provideAnalytics(): Analytics {
        return Analytics
    }

    @Provides
    fun providePageViewUtils(): PageViewUtils {
        return PageViewUtils
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
    @Singleton
    fun provideLocale(): Locale {
        return Locale.getDefault()
    }

    @Provides
    @Singleton
    fun provideTimeZone(): TimeZone {
        return TimeZone.getDefault()
    }
}