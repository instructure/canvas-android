/*
 * Copyright (C) 2024 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.student.di.feature

import android.content.Context
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.settings.SettingsBehaviour
import com.instructure.pandautils.features.settings.SettingsRouter
import com.instructure.student.features.settings.StudentSettingsBehaviour
import com.instructure.student.features.settings.StudentSettingsRouter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ActivityComponent::class)
class SettingsRouterModule{
    @Provides
    fun provideSettingsRouter(@ActivityContext context: Context): SettingsRouter {
        return StudentSettingsRouter(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class SettingsModule {

    @Provides
    fun provideSettingsBehavior(apiPrefs: ApiPrefs): SettingsBehaviour {
        return StudentSettingsBehaviour(apiPrefs)
    }
}