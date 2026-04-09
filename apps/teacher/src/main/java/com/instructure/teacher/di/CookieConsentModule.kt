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
package com.instructure.teacher.di

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.instructure.pandautils.analytics.pageview.PageViewUploadWorker
import com.instructure.pandautils.features.cookieconsent.AnalyticsConsentHandler
import com.instructure.pandautils.features.cookieconsent.CookieConsentNamespace
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sdk.pendo.io.Pendo
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class CookieConsentModule {

    @Provides
    fun provideCookieConsentNamespace(): CookieConsentNamespace {
        return CookieConsentNamespace.TEACHER
    }

    @Provides
    fun provideAnalyticsConsentHandler(@ApplicationContext context: Context): AnalyticsConsentHandler {
        return object : AnalyticsConsentHandler {
            override fun onConsentGranted() {
                val workManager = WorkManager.getInstance(context)
                val workRequest = PeriodicWorkRequestBuilder<PageViewUploadWorker>(
                    15, TimeUnit.MINUTES
                ).build()
                workManager.enqueueUniquePeriodicWork(
                    "pageView-teacher",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            }

            override fun onConsentRevoked() {
                Pendo.endSession()
                WorkManager.getInstance(context).cancelUniqueWork("pageView-teacher")
            }
        }
    }
}
