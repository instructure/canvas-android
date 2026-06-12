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

package com.instructure.pandautils.features.grades

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

/**
 * Qualifies the dispatcher used for the Grades screen's CPU-bound transforms (grouping, mapping, grade
 * calculation). Injected so unit tests can substitute a test dispatcher and run those transforms
 * deterministically; production uses [Dispatchers.Default].
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GradesComputationDispatcher

@Module
@InstallIn(ViewModelComponent::class)
object GradesDispatcherModule {

    @Provides
    @GradesComputationDispatcher
    fun provideGradesComputationDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
