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
package com.instructure.teacher.di

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.instructure.pandautils.features.settings.SettingsBehaviour
import com.instructure.pandautils.features.settings.SettingsRouter
import com.instructure.teacher.features.settings.TeacherSettingsBehaviour
import com.instructure.teacher.features.settings.TeacherSettingsRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(FragmentComponent::class)
class SettingsRouterModule {
    @Provides
    fun provideSettingsRouter(activity: FragmentActivity): SettingsRouter {
        return TeacherSettingsRouter(activity)
    }
}
@Module
@InstallIn(ViewModelComponent::class)
class SettingsModule {

    @Provides
    fun provideSettingsBehavior(): SettingsBehaviour {
        return TeacherSettingsBehaviour()
    }
}