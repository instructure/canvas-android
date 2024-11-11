/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.student.di

import androidx.fragment.app.FragmentActivity
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.loginapi.login.features.acceptableusepolicy.AcceptableUsePolicyRouter
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.pandautils.features.assignments.details.reminder.AlarmScheduler
import com.instructure.student.features.login.StudentAcceptableUsePolicyRouter
import com.instructure.student.features.login.StudentLoginNavigation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class LoginModule {

    @Provides
    fun provideAcceptabelUsePolicyRouter(
        activity: FragmentActivity,
        databaseProvider: DatabaseProvider,
        alarmScheduler: AlarmScheduler
    ): AcceptableUsePolicyRouter {
        return StudentAcceptableUsePolicyRouter(activity, databaseProvider, alarmScheduler)
    }

    @Provides
    fun provideLoginNavigation(activity: FragmentActivity, databaseProvider: DatabaseProvider, alarmScheduler: AlarmScheduler): LoginNavigation {
        return StudentLoginNavigation(activity, databaseProvider, alarmScheduler)
    }
}