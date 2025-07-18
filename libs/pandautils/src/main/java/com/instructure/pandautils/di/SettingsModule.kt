/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.pandautils.di

import com.instructure.canvasapi2.apis.ExperienceAPI
import com.instructure.canvasapi2.apis.FeaturesAPI
import com.instructure.canvasapi2.managers.InboxSettingsManager
import com.instructure.pandautils.features.settings.SettingsBehaviour
import com.instructure.pandautils.features.settings.SettingsRepository
import com.instructure.pandautils.features.settings.SettingsSharedEvents
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
class SettingsModule {

    @Provides
    fun provideSettingsRepository(
        featuresApi: FeaturesAPI.FeaturesInterface,
        inboxSettingsManager: InboxSettingsManager,
        settingsBehaviour: SettingsBehaviour,
        experienceAPI: ExperienceAPI
    ): SettingsRepository {
        return SettingsRepository(featuresApi, inboxSettingsManager, settingsBehaviour, experienceAPI)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class SettingsSingletonModule {

    @Provides
    @Singleton
    fun provideSettingsSharedEvents(): SettingsSharedEvents {
        return SettingsSharedEvents()
    }
}