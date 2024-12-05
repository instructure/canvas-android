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

import com.instructure.pandautils.features.settings.SettingsBehaviour
import com.instructure.pandautils.features.settings.SettingsRouter
import com.instructure.parentapp.features.dashboard.SelectedStudentHolder
import com.instructure.parentapp.features.settings.ParentSettingsBehaviour
import com.instructure.parentapp.features.settings.ParentSettingsRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class SettingsModule {

    @Provides
    fun provideSettingsBehaviour(selectedStudentHolder: SelectedStudentHolder): SettingsBehaviour {
        return ParentSettingsBehaviour(selectedStudentHolder)
    }

    @Provides
    fun provideSettingsRouter(): SettingsRouter {
        return ParentSettingsRouter()
    }
}