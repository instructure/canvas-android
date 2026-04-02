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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.instructure.canvasapi2.utils.weave.tryLaunch
import com.instructure.pandautils.utils.FeatureFlagProvider
import com.instructure.pandautils.utils.NetworkStateProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop

abstract class HorizonOfflineViewModel(
    private val networkStateProvider: NetworkStateProvider,
    private val featureFlagProvider: FeatureFlagProvider
) : ViewModel() {

    abstract fun onNetworkRestored()

    abstract fun onNetworkLost()

    init {
        viewModelScope.tryLaunch {
            networkStateProvider.isOnlineLiveData.asFlow()
                .distinctUntilChanged()
                .drop(1)
                .collect { isOnline ->
                    if (featureFlagProvider.offlineEnabled()) {
                        if (isOnline) onNetworkRestored() else onNetworkLost()
                    }
                }
        }
    }
}