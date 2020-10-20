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
package com.instructure.teacher.features.syllabus.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.features.syllabus.SyllabusEvent
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.instructure.teacher.view.EmptyPandaView
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_syllabus.*
import kotlinx.android.synthetic.main.fragment_syllabus_events.*
import kotlinx.android.synthetic.main.fragment_syllabus_webview.*

private const val SYLLABUS_TAB_POSITION = 0

class SyllabusView(val canvasContext: CanvasContext, inflater: LayoutInflater, parent: ViewGroup
) : MobiusView<SyllabusViewState, SyllabusEvent>(R.layout.fragment_syllabus, inflater, parent) {

    private val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {}

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (tab?.position == SYLLABUS_TAB_POSITION) {
                swipeRefreshLayout.setSwipeableChildren(R.id.syllabusWebView)
            } else {
                swipeRefreshLayout.setSwipeableChildren(R.id.syllabusEventsRecyclerView, R.id.syllabusEmptyView)
            }
        }
    }

    init {
        ViewStyler.themeToolbar(context as Activity, toolbar, canvasContext)
        syllabusTabLayout.setBackgroundColor(ColorKeeper.getOrGenerateColor(canvasContext))

        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        toolbar.title = context.getString(com.instructure.pandares.R.string.syllabus)
        toolbar.subtitle = canvasContext.name

        syllabusPager.adapter = SyllabusTabAdapter(canvasContext, getTabTitles())
        syllabusTabLayout.setupWithViewPager(syllabusPager, true)
    }

    private fun getTabTitles(): List<String> {
        return listOf(
                context.getString(com.instructure.pandares.R.string.syllabus),
                context.getString(com.instructure.pandares.R.string.summary)
        )
    }

    override fun onConnect(output: Consumer<SyllabusEvent>) {
        swipeRefreshLayout.setOnRefreshListener { output.accept(SyllabusEvent.PullToRefresh) }
        syllabusTabLayout.addOnTabSelectedListener(tabListener)
    }

    override fun onDispose() {
        syllabusTabLayout.removeOnTabSelectedListener(tabListener)
    }

    override fun render(state: SyllabusViewState) {
        when (state) {
            SyllabusViewState.Loading -> swipeRefreshLayout.isRefreshing = true
            is SyllabusViewState.Loaded -> renderLoadedState(state)
        }
    }

    private fun renderLoadedState(state: SyllabusViewState.Loaded) {
        swipeRefreshLayout.isRefreshing = false

        val hasBoth = state.eventsState != null && state.syllabus != null
        syllabusTabLayout.setVisible(hasBoth)
        syllabusPager.canSwipe = hasBoth

        syllabusPager.setCurrentItem(if (state.syllabus == null) 1 else 0, false)

        if (state.syllabus != null) syllabusWebView?.loadHtml(state.syllabus, context.getString(com.instructure.pandares.R.string.syllabus))
        if (state.eventsState != null) renderEvents(state.eventsState)
    }

    private fun renderEvents(eventsState: EventsViewState) {
        with(eventsState) {
            syllabusEmptyView?.setVisible(visibility.empty)
            syllabusEventsError?.setVisible(visibility.error)
            syllabusEventsRecyclerView?.setVisible(visibility.list)
        }

        if (syllabusPager.currentItem == SYLLABUS_TAB_POSITION) {
            swipeRefreshLayout.setSwipeableChildren(R.id.syllabusWebView)
        } else {
            swipeRefreshLayout.setSwipeableChildren(R.id.syllabusEventsRecyclerView, R.id.syllabusEmptyView)
        }

        when (eventsState) {
            EventsViewState.Error -> {
//                syllabusRetry?.onClick { consumer?.accept(SyllabusEvent.PullToRefresh) } TODO Add consumer
            }
            EventsViewState.Empty -> {
                setEmptyView(syllabusEmptyView, R.drawable.ic_panda_space, R.string.noSyllabus, R.string.noSyllabusSubtext)
            }
            is EventsViewState.Loaded -> {
//                if (syllabusEventsRecyclerView?.adapter == null) syllabusEventsRecyclerView?.adapter = SyllabusEventsAdapter(consumer)
//                (syllabusEventsRecyclerView?.adapter as? SyllabusEventsAdapter)?.updateEvents(eventsState.events) TODO Handle loaded events, create adapter
            }
        }
    }

//    fun showAssignmentView(assignment: Assignment, canvasContext: CanvasContext) {
//        RouteMatcher.route(context, AssignmentDetailsFragment.makeRoute(canvasContext, assignment.id))
//    }
//
//    fun showScheduleItemView(scheduleItem: ScheduleItem, canvasContext: CanvasContext) {
//        RouteMatcher.route(context, CalendarEventFragment.makeRoute(canvasContext, scheduleItem))
//    }

    private fun setEmptyView(emptyView: EmptyPandaView?, drawableId: Int, titleId: Int, messageId: Int) {
        emptyView?.setEmptyViewImage(context.getDrawableCompat(drawableId))
        emptyView?.setTitleText(titleId)
        emptyView?.setMessageText(messageId)
        emptyView?.setListEmpty()
    }
}