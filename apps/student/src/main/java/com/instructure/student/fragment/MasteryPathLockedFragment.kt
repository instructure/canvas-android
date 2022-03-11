/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.instructure.student.R
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_MASTERY_PATH_LOCKED
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.StringArg
import kotlinx.android.synthetic.main.fragment_master_paths_locked.*

@ScreenView(SCREEN_VIEW_MASTERY_PATH_LOCKED)
class MasteryPathLockedFragment : ParentFragment() {

    private var moduleItemName: String by StringArg(key = MODULE_ITEM)

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_master_paths_locked, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        moduleItemNameTextView.text = moduleItemName
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = getString(R.string.locked)

    override fun applyTheme() { }

    //endregion

    companion object {
        const val MODULE_ITEM = "module_item"

        fun makeRoute(moduleItemName: String): Route {
            val bundle = Bundle().apply {
                putString(MODULE_ITEM, moduleItemName)
            }
            return Route(MasteryPathLockedFragment::class.java, null, bundle)
        }

        fun newInstance(route: Route) = if (validRoute(route)) { MasteryPathLockedFragment().apply {
                arguments = route.arguments
            }
        } else null

        private fun validRoute(route: Route) = route.arguments.containsKey(MODULE_ITEM)
    }
}
