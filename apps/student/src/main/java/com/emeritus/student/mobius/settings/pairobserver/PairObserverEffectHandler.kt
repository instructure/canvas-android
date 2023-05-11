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

import com.instructure.canvasapi2.managers.UserManager
import com.instructure.canvasapi2.utils.AnalyticsEventConstants
import com.instructure.canvasapi2.utils.exhaustive
import com.emeritus.student.mobius.common.ui.EffectHandler
import com.emeritus.student.mobius.settings.pairobserver.ui.PairObserverView
import kotlinx.coroutines.launch

class PairObserverEffectHandler : EffectHandler<PairObserverView, PairObserverEvent, PairObserverEffect>() {
    override fun accept(effect: PairObserverEffect) {
        when (effect) {
            PairObserverEffect.LogRefresh -> logEvent(AnalyticsEventConstants.REFRESH_PAIRING_CODE)
            is PairObserverEffect.LoadData -> loadData()
        }.exhaustive
    }

    private fun loadData() {
        launch {
            val pairingCode = UserManager.generatePairingCodeAsync(true).await()
            val termsOfService = UserManager.getTermsOfServiceAsync(true).await()
            consumer.accept(PairObserverEvent.DataLoaded(pairingCode, termsOfService))
        }
    }
}
