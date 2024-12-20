/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.parentapp.di

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.util.LoginPrefs
import com.instructure.loginapi.login.util.PreviousUsersUtils
import com.instructure.loginapi.login.util.QRLogin
import com.instructure.pandautils.dialogs.RatingDialog
import com.instructure.pandautils.features.reminder.ReminderRepository
import com.instructure.pandautils.room.calendar.daos.CalendarFilterDao
import com.instructure.pandautils.utils.LogoutHelper
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.parentapp.util.FlutterAppMigration
import com.instructure.parentapp.util.ParentLogoutHelper
import com.instructure.parentapp.util.ParentPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

    @Provides
    @Singleton
    fun provideLogoutHelper(): LogoutHelper {
        return ParentLogoutHelper()
    }

    @Provides
    @Singleton
    fun provideQRLogin(): QRLogin {
        return QRLogin
    }

    @Provides
    @Singleton
    fun providePreviousUsersUtils(): PreviousUsersUtils {
        return PreviousUsersUtils
    }

    @Provides
    @Singleton
    fun provideParentPrefs(): ParentPrefs {
        return ParentPrefs
    }

    @Provides
    @Singleton
    fun provideClock(): Clock {
        return Clock.systemDefaultZone()
    }

    @Provides
    @Singleton
    fun provideFlutterAppMigration(
        @ApplicationContext context: Context,
        themePrefs: ThemePrefs,
        parentPrefs: ParentPrefs,
        loginPrefs: LoginPrefs,
        previousUsersUtils: PreviousUsersUtils,
        apiPrefs: ApiPrefs,
        ratingDialogPrefs: RatingDialog.Prefs,
        reminderRepository: ReminderRepository,
        calendarFilterDao: CalendarFilterDao,
        clock: Clock
    ): FlutterAppMigration {
        return FlutterAppMigration(
            context,
            themePrefs,
            parentPrefs,
            loginPrefs,
            previousUsersUtils,
            apiPrefs,
            ratingDialogPrefs,
            reminderRepository,
            calendarFilterDao,
            clock
        )
    }
}