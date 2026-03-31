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
import androidx.work.WorkManager
import com.instructure.pandautils.features.cookieconsent.AnalyticsConsentHandler
import com.instructure.pandautils.features.cookieconsent.CookieConsentNamespace
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sdk.pendo.io.Pendo

@Module
@InstallIn(SingletonComponent::class)
class CookieConsentModule {

    @Provides
    fun provideCookieConsentNamespace(): CookieConsentNamespace {
        return CookieConsentNamespace.STUDENT
    }

    @Provides
    fun provideAnalyticsConsentHandler(@ApplicationContext context: Context): AnalyticsConsentHandler {
        return object : AnalyticsConsentHandler {
            override fun onConsentGranted() {
                // Pendo session will be started on next app launch via CallbackActivity
                // Re-schedule pandata upload
                val workManager = WorkManager.getInstance(context)
                val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.instructure.pandautils.analytics.pageview.PageViewUploadWorker>(
                    15, java.util.concurrent.TimeUnit.MINUTES
                ).setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                        .build()
                ).build()
                workManager.enqueueUniquePeriodicWork(
                    "pageView-student",
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            }

            override fun onConsentRevoked() {
                Pendo.endSession()
                WorkManager.getInstance(context).cancelUniqueWork("pageView-student")
            }
        }
    }
}
