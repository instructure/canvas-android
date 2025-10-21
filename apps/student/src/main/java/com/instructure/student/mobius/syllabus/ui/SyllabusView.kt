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
package com.instructure.student.mobius.syllabus.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.google.android.material.tabs.TabLayout
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.pandautils.features.assignments.details.AssignmentDetailsFragment
import com.instructure.pandautils.features.calendarevent.details.EventFragment
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.getDrawableCompat
import com.instructure.pandautils.utils.onClick
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.EmptyView
import com.instructure.student.R
import com.instructure.student.databinding.FragmentSyllabusBinding
import com.instructure.student.databinding.FragmentSyllabusEventsBinding
import com.instructure.student.databinding.FragmentSyllabusWebviewBinding
import com.instructure.student.mobius.common.ui.MobiusView
import com.instructure.student.mobius.syllabus.SyllabusEvent
import com.instructure.student.router.RouteMatcher
import com.spotify.mobius.functions.Consumer

class SyllabusView(
    val canvasContext: CanvasContext,
    val webViewRouter: WebViewRouter,
    inflater: LayoutInflater,
    parent: ViewGroup
) : MobiusView<SyllabusViewState, SyllabusEvent, FragmentSyllabusBinding>(
    inflater,
    FragmentSyllabusBinding::inflate,
    parent
) {

    private val adapter: SyllabusTabAdapter

    private var eventsBinding: FragmentSyllabusEventsBinding? = null
    private var webviewBinding: FragmentSyllabusWebviewBinding? = null

    private val tabListener = object : TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
        override fun onTabReselected(tab: TabLayout.Tab?) {}

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            setupSwipeableChildren(tab?.position)
        }
    }

    init {
        binding.toolbar.setupAsBackButton { activity.onBackPressed() }
        binding.toolbar.title = context.getString(com.instructure.pandares.R.string.syllabus)
        binding.toolbar.subtitle = canvasContext.name

        adapter = SyllabusTabAdapter(activity, canvasContext, getTabTitles())

        binding.syllabusPager.adapter = adapter
        binding.syllabusTabLayout.setupWithViewPager(binding.syllabusPager, true)
    }

    override fun applyTheme() {
        ViewStyler.themeToolbarColored(context as Activity, binding.toolbar, canvasContext)
        binding.syllabusTabLayout.setBackgroundColor(canvasContext.color)
    }

    override fun onConnect(output: Consumer<SyllabusEvent>) {
        binding.swipeRefreshLayout.setOnRefreshListener { output.accept(SyllabusEvent.PullToRefresh) }
        binding.syllabusTabLayout.addOnTabSelectedListener(tabListener)
    }

    override fun onDispose() {
        binding.syllabusTabLayout.removeOnTabSelectedListener(tabListener)
    }

    override fun render(state: SyllabusViewState) {
        webviewBinding = adapter.webviewBinding
        eventsBinding = adapter.eventsBinding
        when (state) {
            SyllabusViewState.Loading -> {
                binding.swipeRefreshLayout.isRefreshing = true
            }
            is SyllabusViewState.Loaded -> {
                binding.swipeRefreshLayout.isRefreshing = false

                val pager = binding.syllabusPager

                val hasBoth = state.eventsState != null && state.syllabus != null
                binding.syllabusTabLayout.setVisible(hasBoth)
                pager.canSwipe = hasBoth

                pager.setCurrentItem(if (state.syllabus == null) 1 else 0, false)

                if (state.syllabus != null) renderWebView(state.syllabus)

                if (state.eventsState != null) renderEvents(state.eventsState)

                setupSwipeableChildren(pager.currentItem)
            }
        }
    }

    private fun renderWebView(syllabus: String) {
        webviewBinding?.syllabusWebViewWrapper?.apply {
            webView.canvasWebViewClientCallback?.let {
                webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback by it {
                    override fun openMediaFromWebView(mime: String, url: String, filename: String) = webViewRouter.openMedia(url)

                    override fun canRouteInternallyDelegate(url: String) = webViewRouter.canRouteInternally(url)

                    override fun routeInternallyCallback(url: String) = webViewRouter.routeInternally(url)
                }
            }

            loadHtml(
                syllabus,
                context.getString(com.instructure.pandares.R.string.syllabus)
            )
        }
    }

    private fun setupSwipeableChildren(position: Int?) {
        if (position == 0) {
            binding.swipeRefreshLayout.setSwipeableChildren(R.id.syllabusScrollView)
        } else {
            binding.swipeRefreshLayout.setSwipeableChildren(R.id.syllabusEventsRecycler, R.id.syllabusEmptyView)
        }
    }

    private fun renderEvents(eventsState: EventsViewState) {
        with (eventsState) {
            eventsBinding?.syllabusEmptyView?.setVisible(visibility.empty)
            eventsBinding?.syllabusEventsError?.setVisible(visibility.error)
            eventsBinding?.syllabusEventsRecycler?.setVisible(visibility.list)
        }

        when (eventsState) {
            EventsViewState.Error -> {
                eventsBinding?.syllabusRetry?.onClick { consumer?.accept(SyllabusEvent.PullToRefresh) }
            }
            EventsViewState.Empty -> {
                setEmptyView(eventsBinding?.syllabusEmptyView, R.drawable.ic_panda_space, R.string.noSyllabus, R.string.noSyllabusSubtext)
            }
            is EventsViewState.Loaded -> {
                if (eventsBinding?.syllabusEventsRecycler?.adapter == null) eventsBinding?.syllabusEventsRecycler?.adapter = SyllabusEventsAdapter(consumer)
                (eventsBinding?.syllabusEventsRecycler?.adapter as? SyllabusEventsAdapter)?.updateEvents(eventsState.events)
            }
        }
    }

    fun showAssignmentView(assignmentId: Long, canvasContext: CanvasContext) {
        RouteMatcher.route(activity as FragmentActivity, AssignmentDetailsFragment.makeRoute(canvasContext, assignmentId))
    }

    fun showScheduleItemView(scheduleItem: ScheduleItem, canvasContext: CanvasContext) {
        RouteMatcher.route(activity as FragmentActivity, EventFragment.makeRoute(canvasContext, scheduleItem))
    }

    private fun getTabTitles(): List<String> = listOf(
        context.getString(com.instructure.pandares.R.string.syllabus),
        context.getString(com.instructure.pandares.R.string.summary)
    )

    private fun setEmptyView(emptyView: EmptyView?, drawableId: Int, titleId: Int, messageId: Int) {
        emptyView?.setEmptyViewImage(context.getDrawableCompat(drawableId))
        emptyView?.setTitleText(titleId)
        emptyView?.setMessageText(messageId)
        emptyView?.setListEmpty()
    }

}
