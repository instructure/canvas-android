/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.instructure.student.features.elementary.course

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_ELEMENTARY_COURSE
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.elementary.grades.GradesFragment
import com.instructure.pandautils.utils.*
import com.instructure.student.databinding.FragmentElementaryCourseBinding
import com.instructure.student.fragment.CourseBrowserFragment
import com.instructure.student.fragment.GradesListFragment
import com.instructure.student.fragment.ModuleListFragment
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_elementary_course.*

@ScreenView(SCREEN_VIEW_ELEMENTARY_COURSE)
@AndroidEntryPoint
class ElementaryCourseFragment : Fragment() {

    private var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var tabId: String by StringArg(key = TAB_ID)

    private val viewModel: ElementaryCourseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentElementaryCourseBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        if (canvasContext.isCourse) {
            binding.course = canvasContext as Course
        }

        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyTheme()
        viewModel.getData(canvasContext, tabId)

        courseTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    courseTabPager.setCurrentItem(it.position, !isTablet)
                }
            }
        })

        viewModel.data.observe(viewLifecycleOwner, { data ->
            data?.let {
                courseTabPager.offscreenPageLimit = it.tabs.size
                courseTabPager.adapter = ElementaryCoursePagerAdapter(it.tabs)

                val selectedTab = it.tabs.find { it.tabId == tabId }
                val selectedTabPosition = it.tabs.indexOf(selectedTab)

                if (selectedTabPosition != -1) {
                    Handler().postDelayed({
                        courseTabLayout.selectTab(courseTabLayout.getTabAt(selectedTabPosition))
                    }, 100)
                }

            }
        })

        viewModel.events.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })
    }

    private fun applyTheme() {
        toolbar.title = canvasContext.name
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    private fun handleAction(action: ElementaryCourseAction) {
        when (action) {
            is ElementaryCourseAction.RedirectToCourseBrowserPage -> redirect(CourseBrowserFragment.makeRoute(canvasContext))
            is ElementaryCourseAction.RedirectToGrades -> redirect(GradesListFragment.makeRoute(canvasContext))
            is ElementaryCourseAction.RedirectToModules -> redirect(ModuleListFragment.makeRoute(canvasContext))
        }
    }

    private fun redirect(route: Route) {
        RouteMatcher.route(requireContext(), route.copy(removePreviousScreen = true))
    }

    companion object {
        const val TAB_ID = "tabId"
        fun newInstance(route: Route) =
            if (validateRoute(route)) ElementaryCourseFragment().apply {
                val fullUrl = route.uri.toString()
                val tabId = if (fullUrl.contains("#")) {
                    fullUrl.split("#")[1]
                } else {
                    null
                }
                arguments = route.argsWithContext
                arguments?.apply {
                    putString(TAB_ID, route.tabId ?: tabId)
                }
            } else null

        private fun validateRoute(route: Route) = route.canvasContext != null

        fun makeRoute(canvasContext: CanvasContext?) = Route(ElementaryCourseFragment::class.java, canvasContext)

        fun makeRoute(canvasContext: CanvasContext?, tabId: String) =
            Route(primaryClass = ElementaryCourseFragment::class.java, canvasContext = canvasContext, tabId = tabId)
    }
}