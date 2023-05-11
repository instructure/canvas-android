/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.emeritus.student.mobius.settings.pairobserver

import com.instructure.canvasapi2.models.PairingCode
import com.instructure.canvasapi2.models.TermsOfService
import com.instructure.canvasapi2.utils.DataResult

sealed class PairObserverEvent {
    object RefreshCode : PairObserverEvent()
    data class DataLoaded(
            val pairingCode: DataResult<PairingCode>,
            val termsOfService: DataResult<TermsOfService>
    ) : PairObserverEvent()
}

sealed class PairObserverEffect {
    object LogRefresh : PairObserverEffect()
    data class LoadData(val forceNetwork: Boolean) : PairObserverEffect()
}

data class PairObserverModel(
    val isLoading: Boolean = false,
    val pairingCode: String? = null,
    val accountId: Long? = null,
    val domain: String
)
