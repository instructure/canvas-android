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

import com.instructure.canvasapi2.apis.UserAPI
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.ConsentPrefs
import com.instructure.pandautils.features.cookieconsent.AnalyticsConsentHandler
import com.instructure.pandautils.features.cookieconsent.CookieConsentNamespace
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.PendoTokenConfig
import com.instructure.student.BuildConfig
import com.instructure.student.widget.WidgetLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CookieConsentModule {

    @Provides
    fun provideCookieConsentNamespace(): CookieConsentNamespace {
        return CookieConsentNamespace.STUDENT
    }

    @Provides
    @Singleton
    fun providePendoTokenConfig(): PendoTokenConfig {
        return PendoTokenConfig(
            fallbackToken = BuildConfig.PENDO_TOKEN,
            apiTokenSelector = { it.pendoMobileStudentApiKey }
        )
    }

    @Provides
    fun provideAnalyticsConsentHandler(
        userApi: UserAPI.UsersInterface,
        featureFlagProvider: FeatureFlagProvider,
        consentPrefs: ConsentPrefs,
        apiPrefs: ApiPrefs,
        widgetLogger: WidgetLogger
    ): AnalyticsConsentHandler {
        return object : AnalyticsConsentHandler(userApi, featureFlagProvider, consentPrefs, apiPrefs) {
            override suspend fun beforeStartPendoSession() {
                widgetLogger.cancelLogging()
            }
        }
    }
}
