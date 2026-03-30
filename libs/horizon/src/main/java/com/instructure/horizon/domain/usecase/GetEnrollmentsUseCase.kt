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
package com.instructure.horizon.domain.usecase

import com.instructure.canvasapi2.GetCoursesQuery
import com.instructure.horizon.data.repository.CourseEnrollmentOfflineRepository
import com.instructure.horizon.data.repository.CourseEnrollmentOnlineRepository
import com.instructure.horizon.offline.OfflineSyncUseCase
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

class GetEnrollmentsUseCase @Inject constructor(
    private val onlineRepository: CourseEnrollmentOnlineRepository,
    private val offlineRepository: CourseEnrollmentOfflineRepository,
    networkStateProvider: NetworkStateProvider,
    featureFlagProvider: FeatureFlagProvider,
) : OfflineSyncUseCase<Unit, List<GetCoursesQuery.Enrollment>>(
    syncEnabled = true,
    networkStateProvider = networkStateProvider,
    featureFlagProvider = featureFlagProvider,
) {

    suspend operator fun invoke() = invoke(Unit)

    override suspend fun execute(params: Unit): List<GetCoursesQuery.Enrollment> {
        return if (shouldFetchFromNetwork()) {
            onlineRepository.getEnrollments().also {
                if (shouldSync()) offlineRepository.saveEnrollments(it)
            }
        } else {
            offlineRepository.getEnrollments()
        }
    }
}
