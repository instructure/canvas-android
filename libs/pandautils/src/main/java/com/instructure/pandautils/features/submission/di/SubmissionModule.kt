/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.features.submission.di

import androidx.work.WorkManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.features.submission.SubmissionUploadHelper
import com.instructure.pandautils.room.studentdb.StudentDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module that provides the [SubmissionUploadHelper] — the clean, shared submission
 * entry point for NGC and future consumers. WorkManager and SubmissionAPI.SubmissionInterface
 * are both available in the singleton graph via ApplicationModule and canvas-api-2's ApiModule.
 */
@Module
@InstallIn(SingletonComponent::class)
class SubmissionModule {

    @Provides
    fun provideSubmissionUploadHelper(
        studentDb: StudentDb,
        apiPrefs: ApiPrefs,
        workManager: WorkManager
    ): SubmissionUploadHelper {
        return SubmissionUploadHelper(studentDb, apiPrefs, workManager)
    }
}
