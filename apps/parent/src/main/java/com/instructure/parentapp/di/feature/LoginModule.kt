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

package com.instructure.parentapp.di.feature

import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.utils.Analytics
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.loginapi.login.features.acceptableusepolicy.AcceptableUsePolicyRouter
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.parentapp.features.login.ParentAcceptableUsePolicyRouter
import com.instructure.parentapp.features.login.ParentLoginNavigation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class LoginModule {

    @Provides
    fun provideAcceptableUsePolicyRouter(activity: FragmentActivity, alarmScheduler: AlarmScheduler, analytics: Analytics): AcceptableUsePolicyRouter {
        return ParentAcceptableUsePolicyRouter(activity, alarmScheduler, analytics)
    }

    @Provides
    fun provideLoginNavigation(activity: FragmentActivity, alarmScheduler: AlarmScheduler): LoginNavigation {
        return ParentLoginNavigation(activity, alarmScheduler)
    }
}