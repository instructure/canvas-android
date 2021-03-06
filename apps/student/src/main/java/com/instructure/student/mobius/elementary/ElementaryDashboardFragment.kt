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
import com.google.android.material.tabs.TabLayout
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.interactions.router.Route
import com.instructure.pandautils.features.elementary.ElementaryDashboardPagerAdapter
import com.instructure.pandautils.utils.*
import com.instructure.student.R
import com.instructure.student.fragment.ParentFragment
import com.instructure.student.util.FeatureFlagPrefs
import kotlinx.android.synthetic.main.fragment_course_grid.toolbar
import kotlinx.android.synthetic.main.fragment_elementary_dashboard.*

class ElementaryDashboardFragment : ParentFragment() {

    private val canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    override fun title(): String = if (isAdded) getString(R.string.dashboard) else ""

    override fun applyTheme() {
        toolbar.title = title()
        navigation?.attachNavigationDrawer(this, toolbar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        layoutInflater.inflate(R.layout.fragment_elementary_dashboard, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!FeatureFlagPrefs.showInProgressK5Tabs) {
            dashboardTabLayout.removeTabAt(3)
            dashboardTabLayout.removeTabAt(2)
            dashboardTabLayout.removeTabAt(1)
        }

        dashboardPager.adapter = ElementaryDashboardPagerAdapter(canvasContext, childFragmentManager)
        dashboardTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    dashboardPager.setCurrentItem(it.position, !isTablet)
                }
            }

        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (dashboardPager?.adapter as? ElementaryDashboardPagerAdapter)?.refreshHomeroomAssignments()
        }
    }

    companion object {
        fun newInstance(route: Route) =
            ElementaryDashboardFragment().apply {
                arguments = route.canvasContext?.makeBundle(route.arguments) ?: route.arguments
            }

        fun makeRoute(canvasContext: CanvasContext?) = Route(ElementaryDashboardFragment::class.java, canvasContext)
    }
}