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
package com.instructure.horizon.offline

import com.instructure.pandautils.domain.usecase.BaseUseCase
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider

abstract class OfflineSyncUseCase<in Params, out Result>(
    val syncEnabled: Boolean,
    private val networkStateProvider: NetworkStateProvider,
    private val featureFlagProvider: FeatureFlagProvider,
) : BaseUseCase<Params, Result>() {

    fun isOnline() = networkStateProvider.isOnline()
    suspend fun offlineEnabled() = featureFlagProvider.offlineEnabled()
    suspend fun shouldFetchFromNetwork() = isOnline() || !offlineEnabled()
    suspend fun shouldSync() = syncEnabled && isOnline() && offlineEnabled()
}
