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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.APIHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.ColorKeeper
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.TelemetryUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.setVisible
import com.instructure.student.R
import com.instructure.student.adapter.CalendarListRecyclerAdapter
import com.instructure.student.dialog.CalendarChooserDialogStyled
import com.instructure.student.events.CalendarEventCreated
import com.instructure.student.events.CalendarEventDestroyed
import com.instructure.student.interfaces.AdapterToFragmentCallback
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.Analytics
import com.instructure.student.util.CanvasCalendarUtils
import com.instructure.student.util.StudentPrefs
import com.instructure.student.view.ViewUtils
import com.roomorama.caldroid.CaldroidListener
import hirondelle.date4j.DateTime
import kotlinx.android.extensions.CacheImplementation
import kotlinx.android.extensions.ContainerOptions
import kotlinx.android.synthetic.main.empty_view.view.*
import kotlinx.android.synthetic.main.fragment_calendar_listview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.DateFormatSymbols
import java.util.*

@PageView(url = "calendar")
@ContainerOptions(cache = CacheImplementation.NO_CACHE)
class CalendarListViewFragment : ParentFragment() {

    private var currentCalendarView = CalendarView.DAY_VIEW
    private lateinit var calendarFragment: CanvasCalendarFragment
    private var monthText: TextView? = null
    private var monthContainer: View? = null
    private var dropDownIndicator: ImageView? = null
    private var mRootView: FrameLayout? = null
    private var toolbar: Toolbar? = null
    private var toolbarContainer: FrameLayout? = null
    private var emptyTextView: TextView? = null

    private var recyclerAdapter: CalendarListRecyclerAdapter? = null
    private var dialogCallback: CalendarChooserDialogStyled.CalendarChooserCallback? = null
    private var adapterToCalendarCallback: CalendarListRecyclerAdapter.AdapterToCalendarCallback? = null
    private var adapterToFragmentCallback: AdapterToFragmentCallback<ScheduleItem>? = null

    private var isFirstTimeCreation = false

    private var configuration: Int = 0

    //region CalendarView Enum
    enum class CalendarView {
        DAY_VIEW, WEEK_VIEW, MONTH_VIEW;

        companion object {
            fun fromInteger(x: Int): CalendarView {
                return when (x) {
                    0 -> DAY_VIEW
                    1 -> WEEK_VIEW
                    2 -> MONTH_VIEW
                    else -> DAY_VIEW
                }
            }

            fun toInteger(calendarView: CalendarView): Int {
                return when (calendarView) {
                    DAY_VIEW -> 0
                    WEEK_VIEW -> 1
                    MONTH_VIEW -> 2
                }
            }
        }
    }

    override fun title() = getString(R.string.calendar)

    override fun onCreate(savedInstanceState: Bundle?) {
        TelemetryUtils.setInteractionName(this::class.java.simpleName)
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // Most of this will never get called since we are using
        // onConfig change, however, it will be used to save state
        // in the case that the activity is destroyed.
        calendarFragment = CanvasCalendarFragment()
        if (recyclerAdapter == null) {
            setUpCallbacks()
            recyclerAdapter = CalendarListRecyclerAdapter(requireContext(), adapterToFragmentCallback, adapterToCalendarCallback)
        }
        configuration = resources.configuration.orientation

        // Restore saved state
        val year = StudentPrefs.calendarYearPref
        val month = StudentPrefs.calendarMonthPref
        val day = StudentPrefs.calendarDayPref
        val flag = StudentPrefs.calendarPrefFlag

        if (!flag && recyclerAdapter?.selectedDay == null && month != -1 && year != -1 && day != -1) {
            recyclerAdapter?.selectedDay = DateTime.forDateOnly(year, month, day)
        }

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            calendarFragment.restoreStatesFromKey(savedInstanceState,
                    Const.CALENDAR_STATE)
        } else {
            if (!flag) {
                // We are returning to a saved state
                currentCalendarView = CalendarView.fromInteger(StudentPrefs.calendarViewType)
                calendarFragment.arguments = CanvasCalendarFragment.createBundle(Calendar.getInstance(Locale.getDefault()), month, year)
            } else {
                // This will create default behavior
                calendarFragment.arguments = CanvasCalendarFragment.createBundle(Calendar.getInstance(Locale.getDefault()), -1, -1)
            }

            recyclerAdapter?.isStartDayMonday = StudentPrefs.weekStartsOnMonday
        }// If activity is created from fresh

        dialogCallback = CalendarChooserDialogStyled.CalendarChooserCallback { subscribedContexts -> recyclerAdapter?.updateSelectedCalendarContexts(subscribedContexts) }
    }

    override fun onResume() {
        super.onResume()
        // Restore Fragment on resume
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if (!isFirstTimeCreation) { // For resuming from sleep
            fragmentTransaction.replace(R.id.calendar1, calendarFragment).commit()
            isFirstTimeCreation = true
        } else {
            if (recyclerAdapter?.isStartDayChanged == true) { // For resuming from settings change
                navigation?.updateCalendarStartDay()
            } else {
                fragmentTransaction.attach(calendarFragment)
                fragmentTransaction.commit()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        recyclerAdapter?.selectedDay?.let {
            StudentPrefs.calendarYearPref = it.year
            StudentPrefs.calendarMonthPref = it.month
            StudentPrefs.calendarDayPref = it.day
            StudentPrefs.calendarPrefFlag = false
            StudentPrefs.calendarViewType = CalendarView.toInteger(currentCalendarView)
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCalendarEventCreated(event: CalendarEventCreated) {
        event.once(javaClass.simpleName) {
            recyclerAdapter?.refresh()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCalendarEventDestroyed(event: CalendarEventDestroyed) {
        event.once(javaClass.simpleName) {
            recyclerAdapter?.refresh()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        recyclerAdapter?.cancel()
        super.onDestroyView()
    }

    override fun applyTheme() {
        if(toolbar != null && isResumed) {
            setupToolbarMenu(toolbar!!, R.menu.calendar_menu)
            setCalendarViewTypeChecked(toolbar!!.menu)
            setupCalendarSpinner()
            navigation?.attachNavigationDrawer(this, toolbar!!)
            // Styling done in attachNavigationDrawer
        }
        if (recyclerAdapter?.size() == 0) {
            setEmptyView(calendarEmptyView, R.drawable.vd_panda_noevents, R.string.noEvents, R.string.noEventsSubtext)
            calendarEmptyView.changeTextSize(true)
        }
    }

    private fun setupCalendarSpinner() {
        if (recyclerAdapter?.isCalendarViewCreated == true) {
            setupActionbarSpinnerForMonth()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        // Restore Fragment on configuration change
        // The nested fragment needs to be detached then reattached on rotation
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.detach(calendarFragment)
        fragmentTransaction.commitNowAllowingStateLoss()
        super.onConfigurationChanged(newConfig)
        mRootView?.removeAllViews()
        mRootView?.addView(populateView(layoutInflater, mRootView))
        if (recyclerAdapter?.size() == 0) {
            calendarEmptyView.changeTextSize(true)
        }
        hidePanda()
    }

    private fun onRowClick(scheduleItem: ScheduleItem) {
        val canvasContext = findContextForScheduleItem(scheduleItem)
        if (scheduleItem.itemType == ScheduleItem.Type.TYPE_ASSIGNMENT) {
            RouteMatcher.routeUrl(requireActivity(), scheduleItem.htmlUrl!!)
        } else if (scheduleItem.itemType == ScheduleItem.Type.TYPE_CALENDAR || scheduleItem.itemType == ScheduleItem.Type.TYPE_SYLLABUS) {
            RouteMatcher.route(requireContext(), CalendarEventFragment.makeRoute(canvasContext, scheduleItem))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_orientation, container, false) as FrameLayout
        mRootView?.addView(populateView(inflater, mRootView))
        return mRootView
    }

    private fun populateView(inflater: LayoutInflater, container: ViewGroup?): View {
        val rootView = inflater.inflate(R.layout.fragment_calendar_listview, container, false)
        toolbar = rootView.findViewById(R.id.calendarListToolbar)
        toolbarContainer = rootView.findViewById(R.id.toolbarContentWrapper)
        configureRecyclerView(rootView, requireContext(), recyclerAdapter!!, R.id.calendarSwipeRefreshLayout, R.id.calendarEmptyView, R.id.calendarRecyclerView)

        // First time will replace, all others attach
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if (!isFirstTimeCreation) {
            fragmentTransaction.replace(R.id.calendar1, calendarFragment).commit()
            isFirstTimeCreation = true
        } else {
            fragmentTransaction.attach(calendarFragment)
            fragmentTransaction.commitAllowingStateLoss()
        }
        setUpListeners()
        return rootView
    }

    private fun setUpCallbacks() {
        adapterToCalendarCallback = object : CalendarListRecyclerAdapter.AdapterToCalendarCallback {
            override fun showChooserDialog(firstShow: Boolean) = showCalendarChooserDialog(firstShow)
            override fun hidePandaLoading() = hidePanda()
            override fun showPandaLoading() = showPanda()
            override fun getCurrentCalendarView(): Int = CalendarView.toInteger(this@CalendarListViewFragment.currentCalendarView)
            override fun getExtraCalendarData(): HashMap<String, Any>? = calendarFragment.extraData
            override fun refreshCalendarFragment() = calendarFragment.refreshView()
            override fun setSelectedDates(d1: Date, d2: Date) = calendarFragment.setSelectedDates(d1, d2)
        }

        adapterToFragmentCallback = object : AdapterToFragmentCallback<ScheduleItem> {
            override fun onRowClicked(item: ScheduleItem, position: Int, isOpenDetail: Boolean) = onRowClick(item)
            override fun onRefreshFinished() = setRefreshing(false)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        recyclerAdapter?.loadData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.calendarToday -> todayClick()
            R.id.calendarDayView -> dayClick(item)
            R.id.calendarWeekView -> weekClick(item)
            R.id.calendarMonthView -> monthClick(item)
            R.id.selectCalendars -> showCalendarChooserDialog(false)
            R.id.createEvent -> eventCreation()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun eventCreation() {
        if (!APIHelper.hasNetworkConnection()) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show()
            return
        }
        Analytics.trackAppFlow(requireActivity(), CreateCalendarEventFragment::class.java)
        val time: Long? = recyclerAdapter?.selectedDay?.getMilliseconds(TimeZone.getDefault())
        RouteMatcher.route(requireContext(), CreateCalendarEventFragment.makeRoute(time ?: 0L))
    }

    private fun showCalendarChooserDialog(firstShow: Boolean) {
        recyclerAdapter?.let {
            CalendarChooserDialogStyled.show(requireActivity(), CalendarListRecyclerAdapter.getFilterPrefs(), it.contextNames, it.contextCourseCodes, firstShow, dialogCallback)
        }
    }

    private fun setUpListeners() {
        val listener = object : CaldroidListener() {

            override fun onSelectDate(date: Date, view: View) {
                // New date selected, clear out prior
                calendarFragment.clearSelectedDates()
                recyclerAdapter?.selectedDay = DateTime.forInstant(date.time, TimeZone.getDefault())

                if (currentCalendarView == CalendarView.DAY_VIEW) {
                    calendarFragment.setSelectedDates(date, date)
                } else if (currentCalendarView == CalendarView.WEEK_VIEW) {
                    val dateWindow = CanvasCalendarUtils.setSelectedWeekWindow(date,
                            recyclerAdapter?.isStartDayMonday == true)
                    calendarFragment.setSelectedDates(dateWindow.start, dateWindow.end)
                }

                calendarFragment.refreshView()
                recyclerAdapter?.refreshListView()
            }

            override fun onCaldroidViewCreated() {
                super.onCaldroidViewCreated()
                // Removing styling for upper buttons
                val leftButton = calendarFragment.leftArrowButton
                val rightButton = calendarFragment.rightArrowButton
                val textView = calendarFragment.monthTitleTextView
                leftButton?.visibility = View.GONE
                rightButton?.visibility = View.GONE
                textView?.visibility = View.GONE

                // Initialize post view created calendarFragment elements
                val viewPager = calendarFragment.dateViewPager
                viewPager?.pageMargin = ViewUtils.convertDipsToPixels(32f, context).toInt()
                if (recyclerAdapter?.selectedDay == null) {
                    recyclerAdapter?.selectedDay = DateTime.today(TimeZone.getDefault())
                    val today = Date(recyclerAdapter?.selectedDay?.getMilliseconds(TimeZone.getDefault()) ?: 0L)
                    calendarFragment.setSelectedDates(today, today)
                }
                recyclerAdapter?.isCalendarViewCreated = true
                applyTheme()
            }

            override fun onChangeMonth(month: Int, year: Int, fromCreation: Boolean) {

                if (recyclerAdapter?.selectedDay != null && recyclerAdapter?.isTodayPressed == false && month == recyclerAdapter?.selectedDay?.month) {
                    // This will often get called a second time on resume from the view pager page changed
                    // listener. We don't want to trigger the month change logic if the month is not changing.
                    return
                }

                if (monthText != null) {
                    if (fromCreation) {
                        hidePanda()
                        return
                    }

                    // Update Actionbar
                    monthText?.text = DateFormatSymbols().months[month - 1] + " " + year
                }

                // First time loading the calendar will trigger this, but the API calls have already been made
                if (recyclerAdapter?.isTodayPressed == false && !fromCreation) { //Refresh for month, unless this was triggered by "today" button
                    calendarFragment.clearSelectedDates()
                    val today = DateTime.today(TimeZone.getDefault())
                    if (today.month == month && today.year == year) {
                        recyclerAdapter?.selectedDay = today
                    } else {
                        recyclerAdapter?.selectedDay = DateTime(year, month, 1, null, null, null, null)
                    }

                    recyclerAdapter?.refreshCalendar()
                }
            }
        }

        calendarFragment.caldroidListener = listener
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save current calendarState
        calendarFragment.let {
            try {
                it.saveStatesToKey(outState, Const.CALENDAR_STATE)
                val fragmentTransaction = childFragmentManager.beginTransaction()
                fragmentTransaction.detach(it)
                fragmentTransaction.commit()
            } catch (e: IllegalStateException) {
                Logger.e("CalendarListViewFragment crash: $e")
            }
        }

        if (recyclerAdapter?.selectedDay != null) {
            StudentPrefs.calendarPrefFlag = false
        }
    }

    /**
     * Helper method to configure actionbar button for calendar grid drop down
     */
    private fun setupActionbarSpinnerForMonth() {
        monthContainer = LayoutInflater.from(requireContext()).inflate(R.layout.actionbar_calendar_layout, null)
        toolbarContainer?.removeAllViews()
        toolbarContainer?.addView(monthContainer)

        dropDownIndicator = monthContainer?.findViewById(R.id.indicator)
        dropDownIndicator?.setImageDrawable(ColorKeeper.getColoredDrawable(requireContext(), R.drawable.vd_expand, ThemePrefs.primaryTextColor))

        monthText = monthContainer?.findViewById(R.id.monthText)
        if (calendarFragment.currentMonth != -1) {
            // This is month - 1 because DateFormatSymbols uses 0 indexed months, and DateTime uses
            // 1 indexed months.
            monthText?.text = DateFormatSymbols().months[calendarFragment.currentMonth.minus(1)] + " " + calendarFragment.currentYear
        }

        monthContainer?.setOnClickListener { expandOrCollapseCalendar(calendarContainer, calendarContainer?.visibility == View.GONE) }

        monthText?.setTextColor(ThemePrefs.primaryTextColor)
    }

    /**
     * Helper method to animate the calendar grid off or on to the screen.
     * Also animates associated arrow flipper.
     *
     * @param calendarView
     * @param isExpand
     */
    private fun expandOrCollapseCalendar(calendarView: View, isExpand: Boolean) {
        if (!isAdded) return

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        // If we're not expanding, we translate to 0.
        // Otherwise, if landscape, then animate the width of the calendar, otherwise the height.
        val translation = if (isExpand) 0 else if (isLandscape) -calendarView.width else -calendarView.height

        dropDownIndicator?.animate()?.rotationBy(if (isExpand) -180f else 180f)

        val onFinished = object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                calendarView.isEnabled = false
                monthContainer?.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!isExpand) {
                    calendarView.visibility = View.GONE
                    calendarEmptyView.setImageVisible(true)
                    calendarEmptyView.changeTextSize(isExpand)
                }
                calendarView.isEnabled = true
                monthContainer?.isEnabled = true
            }
        }

        if (isExpand) {
            calendarView.visibility = View.VISIBLE
            calendarEmptyView.changeTextSize(isExpand)
        }

        if (isLandscape) calendarView.animate().translationX(translation.toFloat()).setListener(onFinished)
        else calendarView.animate().translationY(translation.toFloat()).setListener(onFinished)
    }

    /**
     * Simple helper to show the loading panda
     */
    private fun showPanda() {
        // This can be called while in transition to an event detail fragment, throws NPE
        if (!isAdded) {
            return
        }

        calendarEmptyView.visibility = View.VISIBLE
        calendarEmptyView.loading.setVisible()
    }

    /**
     * Simple helper to hide the loading panda and update the text for empty/un-selected
     */
    private fun hidePanda() {
        // This can be called while in transition to an event detail fragment, throws NPE
        if (!isAdded) {
            return
        }

        calendarEmptyView.setListEmpty()
        if (recyclerAdapter?.selectedDay == null && currentCalendarView != CalendarView.MONTH_VIEW) {
            emptyTextView?.text = resources.getString(R.string.noDatesSelected)
        } else if (recyclerAdapter?.itemCount == 0) {
            emptyTextView?.visibility = View.VISIBLE
            when (currentCalendarView) {
                CalendarListViewFragment.CalendarView.DAY_VIEW -> if (recyclerAdapter?.selectedDay != null) {
                    val date = Date(recyclerAdapter?.selectedDay?.getMilliseconds(TimeZone.getDefault()) ?: 0)
                    val cleanDate = CanvasCalendarUtils.getSimpleDate(date)
                    emptyTextView?.text = resources.getString(R.string.emptyCalendarDate) + " " + cleanDate
                }
                CalendarListViewFragment.CalendarView.WEEK_VIEW -> emptyTextView?.text = resources.getString(R.string.emptyCalendarWeek)
                CalendarListViewFragment.CalendarView.MONTH_VIEW -> emptyTextView?.text = resources.getString(R.string.emptyCalendarMonth)
            }
        } else {
            emptyTextView?.visibility = View.GONE
        }
    }

    /**
     * Finds the associated canvasContext for a given scheduleItem
     *
     * @param scheduleItem
     * @return
     */
    private fun findContextForScheduleItem(scheduleItem: ScheduleItem): CanvasContext {
        recyclerAdapter?.canvasContextItems?.let {
            for (context in it) {
                if (context.id == scheduleItem.contextId) {
                    return context
                }
            }
        }

        // Todo this would represent an error occurring, need to handle gracefully
        return CanvasContext.emptyCourseContext()
    }

    /**
     * menu item 0 = Today Button
     * menu item 1 = Day View
     * menu item 2 = Week View
     * menu item 3 = Schedule View
     * menu item 4 = Calendar Settings
     *
     * @param menu A menu object
     */
    private fun setCalendarViewTypeChecked(menu: Menu) {
        val item = menu.getItem(StudentPrefs.calendarViewType + 1)
        if (item != null) item.isChecked = true
    }

    ///////////////////////////////////////////////////////
    //           Overflow menu helper methods            //
    ///////////////////////////////////////////////////////

    private fun todayClick() {
        recyclerAdapter?.isTodayPressed = true
        val today = DateTime.today(TimeZone.getDefault())
        recyclerAdapter?.selectedDay = today
        calendarFragment.moveToDateTime(today)
        recyclerAdapter?.refreshForTodayPressed()
        if (calendarContainer.visibility == View.GONE) {
            expandOrCollapseCalendar(calendarContainer, true)
        }
    }

    private fun monthClick(item: MenuItem) {
        currentCalendarView = CalendarView.MONTH_VIEW
        calendarFragment.clearSelectedDates()
        recyclerAdapter?.refreshListView()
        calendarFragment.refreshView()
        if (calendarContainer.visibility == View.GONE) {
            expandOrCollapseCalendar(calendarContainer, true)
        }
        item.isChecked = true
    }

    private fun weekClick(item: MenuItem) {
        currentCalendarView = CalendarView.WEEK_VIEW
        if (recyclerAdapter?.selectedDay != null) {
            val dateWindow = CanvasCalendarUtils.setSelectedWeekWindow(
                    Date(recyclerAdapter?.selectedDay?.getMilliseconds(TimeZone.getDefault()) ?: 0L),
                    recyclerAdapter?.isStartDayMonday == true)
            calendarFragment.setSelectedDates(dateWindow.start, dateWindow.end)
        }
        recyclerAdapter?.refreshListView()
        calendarFragment.refreshView()
        if (calendarContainer.visibility == View.GONE) {
            expandOrCollapseCalendar(calendarContainer, true)
        }
        item.isChecked = true
    }

    private fun dayClick(item: MenuItem) {
        currentCalendarView = CalendarView.DAY_VIEW
        if (recyclerAdapter?.selectedDay != null) {
            val today = Date(recyclerAdapter?.selectedDay?.getMilliseconds(TimeZone.getDefault()) ?: 0L)
            calendarFragment.setSelectedDates(today, today)
        }
        recyclerAdapter?.refreshListView()
        calendarFragment.refreshView()
        if (calendarContainer.visibility == View.GONE) {
            expandOrCollapseCalendar(calendarContainer, true)
        }
        item.isChecked = true
    }


    companion object {
        @JvmStatic
        fun newInstance(route: Route) = if (validRoute(route)) CalendarListViewFragment() else null

        private fun validRoute(route: Route) = route.primaryClass == CalendarListViewFragment::class.java

        fun makeRoute() = Route(CalendarListViewFragment::class.java, null)
    }
}
