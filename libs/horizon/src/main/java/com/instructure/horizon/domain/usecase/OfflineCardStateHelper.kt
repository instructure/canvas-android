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

import com.instructure.pandautils.utils.NetworkStateProvider
import javax.inject.Inject

data class OfflineCardContext(
    val isOffline: Boolean,
    val syncedCourseIds: Set<Long>,
    val resolvedImageUrls: Map<String, String>,
) {
    fun isSynced(courseId: Long?): Boolean =
        !isOffline || (courseId != null && courseId in syncedCourseIds)

    fun resolveImageUrl(url: String?): String? =
        url?.let { resolvedImageUrls[it] ?: it }
}

class OfflineCardStateHelper @Inject constructor(
    private val networkStateProvider: NetworkStateProvider,
    private val getSyncedCourseIdsUseCase: GetSyncedCourseIdsUseCase,
    private val resolveImageUrlUseCase: ResolveImageUrlUseCase,
) {
    suspend fun buildContext(imageUrls: List<String?> = emptyList()): OfflineCardContext {
        val isOffline = !networkStateProvider.isOnline()
        if (!isOffline) return OfflineCardContext(isOffline = false, syncedCourseIds = emptySet(), resolvedImageUrls = emptyMap())

        val syncedCourseIds = getSyncedCourseIdsUseCase()
        val resolvedImageUrls = resolveImageUrlUseCase.resolveBatch(imageUrls)
        return OfflineCardContext(
            isOffline = true,
            syncedCourseIds = syncedCourseIds,
            resolvedImageUrls = resolvedImageUrls,
        )
    }
}
