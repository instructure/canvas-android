/*
 * Copyright (C) 2023 - present Instructure, Inc.
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
 *
 */

package com.instructure.pandautils.di

import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.offline.sync.OfflineSyncHelper
import com.instructure.pandautils.room.offline.facade.SyncSettingsFacade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
class OfflineSyncModule {

    @Provides
    fun provideOfflineSyncHelper(workManager: WorkManager, syncSettingsFacade: SyncSettingsFacade, apiPrefs: ApiPrefs): OfflineSyncHelper {
        return OfflineSyncHelper(workManager, syncSettingsFacade, apiPrefs)
    }
}