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
package com.instructure.teacher.features.syllabus.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Course
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.exhaustive
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.teacher.R
import com.instructure.teacher.activities.MasterDetailActivity
import com.instructure.teacher.events.SyllabusUpdatedEvent
import com.instructure.teacher.features.calendar.event.CalendarEventFragment
import com.instructure.teacher.features.syllabus.SyllabusEvent
import com.instructure.teacher.features.syllabus.edit.EditSyllabusFragment
import com.instructure.teacher.fragments.AssignmentDetailsFragment
import com.instructure.teacher.mobius.common.ui.MobiusView
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupMenu
import com.spotify.mobius.functions.Consumer
import kotlinx.android.synthetic.main.fragment_syllabus.*
import kotlinx.android.synthetic.main.fragment_syllabus_events.*
import kotlinx.android.synthetic.main.fragment_syllabus_webview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private const val SYLLABUS_TAB_POSITION = 0
private const val SUMMARY_TAB_POSITION = 1

class SyllabusView(val canvasContext: CanvasContext, inflater: LayoutInflater, parent: ViewGroup
) : MobiusView<SyllabusViewState, SyllabusEvent>(R.layout.fragment_syllabus, inflater, parent) {

    private var consumer: Consumer<SyllabusEvent>? = null

    private val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {}

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            if (tab?.position == SYLLABUS_TAB_POSITION) {
                swipeRefreshLayout.setSwipeableChildren(R.id.syllabusScrollView)
            } else {
                swipeRefreshLayout.setSwipeableChildren(R.id.syllabusEventsRecyclerView, R.id.syllabusEmptyView)
            }
        }
    }

    init {
        toolbar.setupMenu(R.menu.menu_edit_generic) { consumer?.accept(SyllabusEvent.EditClicked) }
        setEditVisibility(false)
        ViewStyler.themeToolbarColored(context as Activity, toolbar, canvasContext)

        syllabusTabLayout.setBackgroundColor(canvasContext.backgroundColor)

        if (context !is MasterDetailActivity) {
            toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
        }

        toolbar.title = context.getString(com.instructure.pandares.R.string.syllabus)
        toolbar.subtitle = canvasContext.name

        syllabusPager.adapter = SyllabusTabAdapter(canvasContext, getTabTitles())
        syllabusTabLayout.setupWithViewPager(syllabusPager, true)
    }

    private fun setEditVisibility(isVisible: Boolean) {
        val editItem = toolbar.menu?.findItem(R.id.menu_edit)
        editItem?.isVisible = isVisible
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
        consumer = output
    }

    override fun onDispose() {
        syllabusTabLayout.removeOnTabSelectedListener(tabListener)
        consumer = null
    }

    override fun render(state: SyllabusViewState) {
        when (state) {
            SyllabusViewState.Loading -> swipeRefreshLayout.isRefreshing = true
            is SyllabusViewState.Loaded -> renderLoadedState(state)
        }.exhaustive
    }

    private fun renderLoadedState(state: SyllabusViewState.Loaded) {
        swipeRefreshLayout.isRefreshing = false

        if (syllabusPager.currentItem == SYLLABUS_TAB_POSITION) {
            swipeRefreshLayout.setSwipeableChildren(R.id.syllabusScrollView)
        } else {
            swipeRefreshLayout.setSwipeableChildren(R.id.syllabusEventsRecyclerView, R.id.syllabusEmptyView)
        }

        setEditVisibility(state.canEdit)
        // We need to do this again after changing the edit button to visible to make it the correct color.
        ViewStyler.themeToolbarColored(context as Activity, toolbar, canvasContext)

        val showSummary = state.showSummary && state.eventsState != null
        val hasBoth = showSummary && state.syllabus != null
        syllabusTabLayout.setVisible(hasBoth)
        syllabusPager.canSwipe = hasBoth

        syllabusPager.setCurrentItem(if (state.syllabus == null) SUMMARY_TAB_POSITION else SYLLABUS_TAB_POSITION, false)

        if (state.syllabus != null) syllabusWebViewWrapper?.loadHtml(state.syllabus, context.getString(com.instructure.pandares.R.string.syllabus))
        if (state.eventsState != null) renderEvents(state.eventsState)
    }

    private fun renderEvents(eventsState: EventsViewState) {
        with(eventsState) {
            syllabusEmptyView?.setVisible(visibility.empty)
            syllabusEventsError?.setVisible(visibility.error)
            syllabusEventsRecyclerView?.setVisible(visibility.list)
        }

        when (eventsState) {
            EventsViewState.Error -> setupErrorView()
            EventsViewState.Empty -> setupEmptyView()
            is EventsViewState.Loaded -> setupLoadedEventsView(eventsState)
        }.exhaustive
    }

    private fun setupErrorView() {
        syllabusRetry?.onClick { consumer?.accept(SyllabusEvent.PullToRefresh) }
    }

    private fun setupEmptyView() {
        syllabusEmptyView?.setEmptyViewImage(context.getDrawableCompat(R.drawable.ic_panda_space))
        syllabusEmptyView?.setTitleText(R.string.noSyllabus)
        syllabusEmptyView?.setMessageText(R.string.noSyllabusSubtext)
        syllabusEmptyView?.setListEmpty()
    }

    private fun setupLoadedEventsView(eventsState: EventsViewState.Loaded) {
        if (syllabusEventsRecyclerView?.adapter == null) syllabusEventsRecyclerView?.adapter = SyllabusEventsAdapter(consumer)
        (syllabusEventsRecyclerView?.adapter as? SyllabusEventsAdapter)?.updateEvents(eventsState.events)
    }

    fun showAssignmentView(assignment: Assignment, canvasContext: CanvasContext) {
        RouteMatcher.route(context, AssignmentDetailsFragment.makeRoute(canvasContext, assignment.id))
    }

    fun showScheduleItemView(scheduleItem: ScheduleItem, canvasContext: CanvasContext) {
        val route = Route(null, CalendarEventFragment::class.java, canvasContext, CalendarEventFragment.createArgs(canvasContext, scheduleItem))
        RouteMatcher.route(context, route)
    }

    fun openEditSyllabus(course: Course, summaryAllowed: Boolean) {
        val fragmentArgs = EditSyllabusFragment.createArgs(course, summaryAllowed)
        RouteMatcher.route(context, Route(EditSyllabusFragment::class.java, course, fragmentArgs))
    }

    fun registerEventBus() {
        EventBus.getDefault().register(this)
    }

    fun unregisterEventBus() {
        EventBus.getDefault().unregister(this)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onSyllabusUpdated(event: SyllabusUpdatedEvent) {
        event.once(javaClass.simpleName) {
            consumer?.accept(SyllabusEvent.SyllabusUpdatedEvent(event.content, event.summaryAllowed))
        }
    }
}