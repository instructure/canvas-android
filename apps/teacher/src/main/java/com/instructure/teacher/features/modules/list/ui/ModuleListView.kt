/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.teacher.features.modules.list.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import com.instructure.canvasapi2.models.ModuleItem
import com.instructure.pandautils.utils.toast
import com.instructure.teacher.R
import com.instructure.teacher.features.modules.list.ModulesListEvent
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.spotify.mobius.functions.Consumer

class ModuleListView(
    inflater: LayoutInflater,
    parent: ViewGroup
) : MobiusView<ModuleListViewState, ModulesListEvent>(R.layout.fragment_module_list, inflater, parent) {
    override fun onConnect(output: Consumer<ModulesListEvent>) {
        // TODO
    }

    override fun render(state: ModuleListViewState) {
        // TODO
    }

    override fun onDispose() {
        // TODO
    }

    fun routeToModuleItem(item: ModuleItem) {
        context.toast("Route to Module Item")
        // TODO
    }

    fun scrollToItem(itemId: Long) {
        context.toast("Scroll to item $itemId")
        // TODO
    }
}
