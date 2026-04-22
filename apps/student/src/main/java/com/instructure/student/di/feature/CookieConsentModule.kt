/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
 */
package com.instructure.student.di.feature

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.pandautils.analytics.pageview.PageViewUploadWorker
import com.instructure.pandautils.features.cookieconsent.AnalyticsConsentHandler
import com.instructure.pandautils.features.cookieconsent.CookieConsentNamespace
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.student.widget.WidgetLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class CookieConsentModule {

    @Provides
    fun provideCookieConsentNamespace(): CookieConsentNamespace {
        return CookieConsentNamespace.STUDENT
    }

    @Provides
    fun provideAnalyticsConsentHandler(
        @ApplicationContext context: Context,
        userApi: UserAPI.UsersInterface,
        featureFlagProvider: FeatureFlagProvider,
        widgetLogger: WidgetLogger
    ): AnalyticsConsentHandler {
        return object : AnalyticsConsentHandler(userApi, featureFlagProvider) {
            override fun onTrackingEnabled() {
                val workManager = WorkManager.getInstance(context)
                val workRequest = PeriodicWorkRequestBuilder<PageViewUploadWorker>(
                    15, TimeUnit.MINUTES
                ).setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                ).build()
                workManager.enqueueUniquePeriodicWork(
                    "pageView-student",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            }

            override fun onTrackingDisabled() {
                WorkManager.getInstance(context).cancelUniqueWork("pageView-student")
            }

            override suspend fun beforeStartPendoSession() {
                widgetLogger.cancelLogging()
            }
        }
    }
}
