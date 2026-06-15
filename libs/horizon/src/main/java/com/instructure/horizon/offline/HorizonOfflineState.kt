/*
 * Copyright (C) 2026 - present Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.instructure.horizon.offline

import androidx.lifecycle.asFlow
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HorizonOfflineState @Inject constructor(
    networkStateProvider: NetworkStateProvider,
    private val featureFlagProvider: FeatureFlagProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val isOffline: StateFlow<Boolean> = networkStateProvider.isOnlineLiveData.asFlow()
        .distinctUntilChanged()
        .map { online -> offlineEnabledSafe() && !online }
        .stateIn(scope, SharingStarted.Eagerly, initialValue = false)

    private suspend fun offlineEnabledSafe(): Boolean {
        return try {
            featureFlagProvider.offlineEnabled()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }
}
