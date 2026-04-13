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
package com.instructure.parentapp.di.feature

import com.instructure.pandautils.features.cookieconsent.AnalyticsConsentHandler
import com.instructure.pandautils.features.cookieconsent.CookieConsentNamespace
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sdk.pendo.io.Pendo

@Module
@InstallIn(SingletonComponent::class)
class CookieConsentModule {

    @Provides
    fun provideCookieConsentNamespace(): CookieConsentNamespace {
        return CookieConsentNamespace.PARENT
    }

    @Provides
    fun provideAnalyticsConsentHandler(): AnalyticsConsentHandler {
        return object : AnalyticsConsentHandler {
            override fun onConsentGranted() {
                // Parent app does not schedule pandata upload
                // Pendo session will be started on next app launch via SplashViewModel
            }

            override fun onConsentRevoked() {
                Pendo.endSession()
            }
        }
    }
}
