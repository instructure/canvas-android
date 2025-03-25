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

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.instructure.canvasapi2.LoginRouter
import com.instructure.canvasapi2.TokenRefresher
import com.instructure.canvasapi2.models.AccountDomain
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.loginapi.login.LoginNavigation
import com.instructure.loginapi.login.features.acceptableusepolicy.AcceptableUsePolicyRouter
import com.instructure.loginapi.login.util.LoginPrefs
import com.instructure.pandautils.features.reminder.AlarmScheduler
import com.instructure.pandautils.room.offline.DatabaseProvider
import com.instructure.student.activity.SignInActivity
import com.instructure.student.features.login.StudentAcceptableUsePolicyRouter
import com.instructure.student.features.login.StudentLoginNavigation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
    fun provideLoginNavigation(
        activity: FragmentActivity,
        databaseProvider: DatabaseProvider,
        alarmScheduler: AlarmScheduler
    ): LoginNavigation {
        return StudentLoginNavigation(activity, databaseProvider, alarmScheduler)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class  LoginRouterModule {

    @Provides
    @Singleton
    fun provideLoginRouter(
        @ApplicationContext context: Context,
        loginPrefs: LoginPrefs,
        apiPrefs: ApiPrefs
    ): LoginRouter {
        return object : LoginRouter {
            override fun loginIntent(): Intent {
                return SignInActivity.createIntent(
                    context,
                    loginPrefs.lastSavedLogin?.accountDomain ?: AccountDomain(apiPrefs.domain)
                )
            }
        }
    }

    @Provides
    @Singleton
    fun provideTokenRefresher(loginRouter: LoginRouter): TokenRefresher {
        return TokenRefresher(loginRouter)
    }
}