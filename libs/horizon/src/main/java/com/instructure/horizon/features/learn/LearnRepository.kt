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
package com.instructure.horizon.features.learn

import com.instructure.canvasapi2.managers.graphql.horizon.journey.GetLearningLibraryManager
import com.instructure.canvasapi2.models.journey.learninglibrary.EnrolledLearningLibraryCollection
import javax.inject.Inject

class LearnRepository @Inject constructor(
    private val getLearningLibraryManager: GetLearningLibraryManager,
) {
    suspend fun getEnrolledLearningLibraries(forceNetwork: Boolean): List<EnrolledLearningLibraryCollection> {
        return getLearningLibraryManager.getEnrolledLearningLibraryCollections(4, forceNetwork).collections
    }
}