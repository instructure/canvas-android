/*
 * Copyright (C) 2021 - present Instructure, Inc.
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
package com.instructure.student.mobius.elementary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.makeBundle
import com.instructure.student.R
import com.instructure.student.fragment.DashboardFragment
import com.instructure.student.fragment.ParentFragment
import kotlinx.android.synthetic.main.fragment_course_grid.*

class MyCanvasFragment : ParentFragment() {

    override fun title(): String = if (isAdded) getString(R.string.toolbarTitleMyCanvas) else ""

    override fun applyTheme() {
        toolbar.title = title()
        navigation?.attachNavigationDrawer(this, toolbar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        layoutInflater.inflate(R.layout.fragment_my_canvas, container, false)

    companion object {
        fun newInstance(route: Route) =
            MyCanvasFragment().apply {
                arguments = route.canvasContext?.makeBundle(route.arguments) ?: route.arguments
            }

        fun makeRoute(canvasContext: CanvasContext?) = Route(MyCanvasFragment::class.java, canvasContext)
    }
}