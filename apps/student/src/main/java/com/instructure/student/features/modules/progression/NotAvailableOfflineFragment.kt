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

package com.instructure.student.features.modules.progression

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.databinding.FragmentNotAvailableOfflineBinding
import com.instructure.student.fragment.ParentFragment

private const val MODULE_ITEM_NAME = "module_item_name"
private const val DESCRIPTION = "description"
private const val SHOW_TOOLBAR = "show-toolbar"

class NotAvailableOfflineFragment : ParentFragment() {

    private var moduleItemName: String by StringArg(key = MODULE_ITEM_NAME)
    private var description: String by StringArg(key = DESCRIPTION)
    private var course: Course by ParcelableArg(key = Const.COURSE)
    private var showToolbar: Boolean by BooleanArg(key = SHOW_TOOLBAR, default = true)

    private val binding by viewBinding(FragmentNotAvailableOfflineBinding::bind)

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_not_available_offline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setIconVisibility()
        binding.description.text = description
        if (showToolbar) {
            binding.toolbar.title = moduleItemName
            binding.toolbar.setupAsBackButton(this)
            binding.toolbar.applyTopSystemBarInsets()
            ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, course)
        } else {
            binding.toolbar.setGone()
        }
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun title(): String = moduleItemName

    override fun applyTheme() {}
    //endregion

    companion object {
        fun makeRoute(course: CanvasContext, moduleItemName: String? = null, description: String? = null, showToolbar: Boolean = true): Route {
            val bundle = Bundle().apply {
                putParcelable(Const.COURSE, course)
                putString(MODULE_ITEM_NAME, moduleItemName.orEmpty())
                putString(DESCRIPTION, description.orEmpty())
                putBoolean(SHOW_TOOLBAR, showToolbar)
            }
            return Route(NotAvailableOfflineFragment::class.java, null, bundle)
        }

        fun newInstance(route: Route) = NotAvailableOfflineFragment().apply {
            arguments = route.arguments
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setIconVisibility()
    }

    private fun setIconVisibility() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.notAvailableIcon.setVisible()
        } else {
            binding.notAvailableIcon.setGone()
        }
    }
}
