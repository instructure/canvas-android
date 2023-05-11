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

import android.content.Context
import com.instructure.canvasapi2.utils.isValid
import com.emeritus.student.mobius.common.ui.Presenter
import com.emeritus.student.mobius.settings.pairobserver.ui.PairObserverViewState

object PairObserverPresenter : Presenter<PairObserverModel, PairObserverViewState> {
    override fun present(model: PairObserverModel, context: Context): PairObserverViewState {
        if (model.isLoading) return PairObserverViewState.Loading
        if (!model.pairingCode.isValid() || model.accountId == null) return PairObserverViewState.Failed

        return PairObserverViewState.Loaded(
                pairingCode = model.pairingCode,
                domain = model.domain,
                accountId = model.accountId
        )
    }
}
