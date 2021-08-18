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
import android.content.Context
import android.content.res.Resources
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.pandautils.typeface.TypefaceBehavior
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.HtmlContentFormatter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
    fun provideCrashlytics(): FirebaseCrashlytics {
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
}