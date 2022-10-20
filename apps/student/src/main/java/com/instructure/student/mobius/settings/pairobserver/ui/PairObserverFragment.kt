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
package com.instructure.student.mobius.settings.pairobserver.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_PAIR_OBSERVER
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.student.mobius.common.ui.MobiusFragment
import com.instructure.student.mobius.settings.pairobserver.*

@ScreenView(SCREEN_VIEW_PAIR_OBSERVER)
class PairObserverFragment : MobiusFragment<PairObserverModel, PairObserverEvent, PairObserverEffect, PairObserverView, PairObserverViewState>() {

    override fun makeEffectHandler() =
        PairObserverEffectHandler()

    override fun makeUpdate() =
        PairObserverUpdate()

    override fun makeView(inflater: LayoutInflater, parent: ViewGroup) = PairObserverView(inflater, parent)

    override fun makePresenter() =
        PairObserverPresenter

    override fun makeInitModel() =
        PairObserverModel(domain = ApiPrefs.domain)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
    }

    companion object {
        fun newInstance() = PairObserverFragment()
    }
}
