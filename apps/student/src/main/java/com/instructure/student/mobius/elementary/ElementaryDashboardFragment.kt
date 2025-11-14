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
import com.instructure.pandautils.analytics.SCREEN_VIEW_ELEMENTARY_DASHBOARD
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.elementary.ElementaryDashboardPagerAdapter
import com.instructure.pandautils.features.elementary.grades.GradesFragment
import com.instructure.pandautils.features.elementary.homeroom.HomeroomFragment
import com.instructure.pandautils.features.elementary.importantdates.ImportantDatesFragment
import com.instructure.pandautils.features.elementary.resources.ResourcesFragment
import com.instructure.pandautils.features.elementary.schedule.pager.SchedulePagerFragment
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.isTablet
import com.instructure.pandautils.utils.makeBundle
import com.instructure.student.R
import com.instructure.student.databinding.FragmentElementaryDashboardBinding
import com.instructure.student.fragment.ParentFragment

@ScreenView(SCREEN_VIEW_ELEMENTARY_DASHBOARD)
class ElementaryDashboardFragment : ParentFragment() {

    private lateinit var binding: FragmentElementaryDashboardBinding

    private val canvasContext by ParcelableArg<CanvasContext>(key = Const.CANVAS_CONTEXT)

    private val schedulePagerFragment = SchedulePagerFragment.newInstance()
    private val importantDatesFragment = ImportantDatesFragment.newInstance()

    private val fragments = mutableListOf(
        HomeroomFragment.newInstance(),
        schedulePagerFragment,
        GradesFragment.newInstance(),
        ResourcesFragment.newInstance(),
    )

    override fun title(): String = if (isAdded) getString(R.string.dashboard) else ""

    override fun applyTheme() {
        binding.toolbar.title = title()
        binding.toolbar.applyTopSystemBarInsets()
        navigation?.attachNavigationDrawer(this, binding.toolbar)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentElementaryDashboardBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.todayButtonVisibility = schedulePagerFragment.getTodayButtonVisibility()

        binding.todayButton.setOnClickListener {
            schedulePagerFragment.jumpToToday()
        }

        binding.dashboardPager.offscreenPageLimit = fragments.size

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        dashboardPager.adapter = ElementaryDashboardPagerAdapter(fragments, childFragmentManager)
        dashboardPager.applyBottomSystemBarInsets()
        dashboardTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    dashboardPager.setCurrentItem(it.position, !isTablet)
                    if (it.position != fragments.indexOf(schedulePagerFragment)) {
                        todayButton.visibility = View.GONE
                    } else {
                        todayButton.visibility =
                            if (schedulePagerFragment.getTodayButtonVisibility().value == true) View.VISIBLE else View.GONE
                    }
                }
            }
        })

        importantDates?.let {
            childFragmentManager
                    .beginTransaction()
                    .add(R.id.importantDates, importantDatesFragment)
                    .commit()
        } ?: addImportantDatesFragment()

        applyTheme()
    }

    private fun addImportantDatesFragment() {
        fragments.add(importantDatesFragment)
        binding.dashboardPager.adapter?.notifyDataSetChanged()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            (binding.dashboardPager.adapter as? ElementaryDashboardPagerAdapter)?.refreshHomeroomAssignments()
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