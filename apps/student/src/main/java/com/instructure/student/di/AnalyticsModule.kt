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
package com.instructure.student.di

import com.instructure.canvasapi2.utils.pageview.PandataInfo
import com.instructure.student.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AnalyticsModule {

    @Provides
    fun provideAppInfo(): PandataInfo.AppKey {
        return PandataInfo.AppKey(
            "CANVAS_STUDENT_ANDROID",
            "Canvas Student for Android - ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        )
    }
}