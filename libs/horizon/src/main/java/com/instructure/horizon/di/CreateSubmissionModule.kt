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
package com.instructure.horizon.di

import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.horizon.features.moduleitemsequence.content.assignment.submission.HorizonSubmissionHelper
import com.instructure.pandautils.room.studentdb.StudentDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class CreateSubmissionModule {

    @Provides
    fun provideSubmissionHelper(
        studentDb: StudentDb,
        apiPrefs: ApiPrefs,
        workManager: WorkManager
    ): HorizonSubmissionHelper {
        return HorizonSubmissionHelper(studentDb, apiPrefs, workManager)
    }
}