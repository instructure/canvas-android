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

import com.emeritus.student.mobius.common.ui.UpdateInit
import com.spotify.mobius.First
import com.spotify.mobius.Next

class PairObserverUpdate : UpdateInit<PairObserverModel, PairObserverEvent, PairObserverEffect>() {
    override fun performInit(model: PairObserverModel): First<PairObserverModel, PairObserverEffect> {
        return First.first(model.copy(isLoading = true), setOf<PairObserverEffect>(
            PairObserverEffect.LoadData(true)))
    }

    override fun update(model: PairObserverModel, event: PairObserverEvent): Next<PairObserverModel, PairObserverEffect> {
        return when (event) {
            PairObserverEvent.RefreshCode -> Next.next(
                model.copy(isLoading = true),
                setOf(PairObserverEffect.LoadData(true), PairObserverEffect.LogRefresh)
            )
            is PairObserverEvent.DataLoaded -> {
                Next.next(model.copy(
                    isLoading = false,
                    pairingCode = event.pairingCode.dataOrNull?.code,
                    accountId = event.termsOfService.dataOrNull?.accountId
                ))
            }
        }
    }
}
