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

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.StatusCallback
import com.instructure.canvasapi2.managers.CalendarEventManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.*
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR_EVENT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.flutterChannels.FlutterComm
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.calendar_event_layout.*
import kotlinx.android.synthetic.main.fragment_calendar_event.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Response
import java.net.URLDecoder
import java.util.*

@ScreenView(SCREEN_VIEW_CALENDAR_EVENT)
class CalendarEventFragment : ParentFragment() {

    // Bundle args
    var canvasContext: CanvasContext by ParcelableArg(key = Const.CANVAS_CONTEXT)
    private var scheduleItem: ScheduleItem? by NullableParcelableArg(key = SCHEDULE_ITEM)
    private var scheduleItemId: Long by LongArg(default = -1, key = SCHEDULE_ITEM_ID)

    private lateinit var scheduleItemCallback: StatusCallback<ScheduleItem>
    private lateinit var deleteItemCallback: StatusCallback<ScheduleItem>

    private var loadHtmlJob: Job? = null

    //region Fragment Lifecycle Overrides
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_calendar_event, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpCallback()
        if (scheduleItem == null) {
            CalendarEventManager.getCalendarEvent(scheduleItemId, scheduleItemCallback, true)
        } else {
            initViews()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        if (isTablet) return // No styling for tablet
        dialog?.let {
            it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onResume() {
        super.onResume()
        calendarEventWebView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        calendarEventWebView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadHtmlJob?.cancel()
    }

    //endregion

    //region Fragment Interaction Overrides
    override fun applyTheme() {
        if (scheduleItem?.contextId ?: canvasContext.id == ApiPrefs.user?.id) {
            setupToolbarMenu(toolbar, R.menu.calendar_event_menu)
        }

        toolbar.setupAsBackButtonAsBackPressedOnly(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    override fun title(): String = scheduleItem?.title ?: getString(R.string.Event)
    //endregion

    //region Parent Fragment Overrides
    override fun handleBackPressed(): Boolean = calendarEventWebView?.handleGoBack()
            ?: super.handleBackPressed()

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.menu_delete -> {
                    if (!APIHelper.hasNetworkConnection()) {
                        toast(R.string.notAvailableOffline)
                    } else {
                        deleteEvent()
                    }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
    //endregion

    //region Bus Events
    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackStackChangedEvent(event: OnBackStackChangedEvent) {
        event.get { clazz ->
            if (clazz != null && clazz.isAssignableFrom(CalendarEventFragment::class.java)) {
                calendarEventWebView?.onResume()
            } else {
                calendarEventWebView?.onPause()
            }
        }
    }
    //endregion

    //region Setup
    private fun initViews() {
        with (calendarEventWebView) {
            addVideoClient(requireActivity())
            canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun launchInternalWebViewFragment(url: String) = RouteMatcher.route(requireActivity(), InternalWebviewFragment.makeRoute(canvasContext, url, false))
                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
            }

            canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String, url: String, filename: String) = openMedia(mime, url, filename, canvasContext)
                override fun onPageStartedCallback(webView: WebView, url: String) = Unit
                override fun onPageFinishedCallback(webView: WebView, url: String) = Unit

                override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false)

                override fun routeInternallyCallback(url: String) {
                    RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true)
                }
            }
        }

        populateViews()
    }

    private fun populateViews() {
        scheduleItem?.let {
            toolbar.title = title()
            val content: String? = it.description

            calendarView.setVisible()
            calendarEventWebView.setGone()

            if (it.isAllDay) {
                date1.text = getString(R.string.allDayEvent)
                date2.text = getFullDateString(it.endDate)
            } else {
                // Setup the calendar event start/end times
                if (it.startDate != null && it.endDate != null) {
                    // Our date times are different so we display two strings
                    date1.text = getFullDateString(it.endDate)
                    val startTime = DateHelper.getFormattedTime(context, it.startDate)
                    val endTime = DateHelper.getFormattedTime(context, it.endDate)

                    val isTimeIntervalEvent = (it.startDate?.time != it.endDate?.time)
                    date2.text = if (isTimeIntervalEvent) "$startTime - $endTime" else startTime
                } else {
                    date1.text = getFullDateString(it.startDate)
                    date2.setInvisible()
                }
            }

            val noLocationTitle = it.locationName.isNullOrBlank()
            val noLocation = it.locationAddress.isNullOrBlank()

            if (noLocation && noLocationTitle) {
                address1.text = getString(R.string.noLocation)
                address2.setInvisible()
            } else {
                if (noLocationTitle) {
                    address1.text = it.locationAddress
                } else {
                    address1.text = it.locationName
                    address2.text = it.locationAddress
                }
            }

            if (content?.isNotEmpty() == true) {
                loadHtmlJob = calendarEventWebView.loadHtmlWithIframes(requireContext(), isTablet, content,
                        ::loadCalendarHtml, { url ->
                    val args = LtiLaunchFragment.makeLTIBundle(
                            URLDecoder.decode(url, "utf-8"), getString(R.string.utils_externalToolTitle), true)
                    RouteMatcher.route(requireContext(), Route(LtiLaunchFragment::class.java, canvasContext, args))
                }, it.title)
            }
        }
    }

    private fun loadCalendarHtml(html: String, contentDescription: String?) {
        calendarEventWebView.setVisible()
        calendarEventWebView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.canvasBackgroundLight))
        calendarEventWebView.loadHtml(html, contentDescription)
    }

    private fun setUpCallback() {
        scheduleItemCallback = object : StatusCallback<ScheduleItem>() {
            override fun onResponse(response: Response<ScheduleItem>, linkHeaders: LinkHeaders, type: ApiType) {
                if (!isAdded) return
                if (response.body() != null) {
                    scheduleItem = response.body() as ScheduleItem
                    initViews()
                }
            }
        }

        deleteItemCallback = object : StatusCallback<ScheduleItem>() {
            override fun onResponse(response: Response<ScheduleItem>, linkHeaders: LinkHeaders, type: ApiType) {
                if (!isAdded) {
                    return
                }

                toast(R.string.eventSuccessfulDeletion)
                response.body()?.let {
                    FlutterComm.updateCalendarDates(listOf(it.allDayDate, it.startDate, it.endDate))
                }
                requireActivity().onBackPressed()
            }
        }
    }
    //endregion

    //region Functionality
    private fun getFullDateString(date: Date?): String {
        if (scheduleItem == null || date == null) {
            return ""
        }

        val dayOfWeek = DateHelper.fullDayFormat.format(date)
        val dateString = DateHelper.getFormattedDate(requireContext(), date)

        return "$dayOfWeek $dateString"
    }

    private fun deleteEvent() {
        val dialog = AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.confirmDeleteEvent))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                CalendarEventManager.deleteCalendarEvent(scheduleItem!!.id, "", deleteItemCallback)
            }
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AppCompatDialog.BUTTON_POSITIVE).setTextColor(ThemePrefs.buttonColor)
            dialog.getButton(AppCompatDialog.BUTTON_NEGATIVE).setTextColor(ThemePrefs.buttonColor)
        }
        dialog.show()
    }
    //endregion

    companion object {

        const val SCHEDULE_ITEM = "schedule_item"
        const val SCHEDULE_ITEM_ID = "schedule_item_id"

        fun makeRoute(canvasContext: CanvasContext, scheduleItem: ScheduleItem): Route =
                Route(null, CalendarEventFragment::class.java, canvasContext, canvasContext.makeBundle { putParcelable(SCHEDULE_ITEM, scheduleItem) })

        fun makeRoute(canvasContext: CanvasContext, scheduleItemId: Long): Route =
                Route(null, CalendarEventFragment::class.java, canvasContext, canvasContext.makeBundle { putLong(SCHEDULE_ITEM_ID, scheduleItemId) })

        fun newInstance(route: Route): CalendarEventFragment? =
                if (validRoute(route)) CalendarEventFragment().apply {
                    arguments = route.arguments
                } else null

        private fun validRoute(route: Route): Boolean = route.canvasContext != null &&
                (route.arguments.containsKey(SCHEDULE_ITEM) || route.arguments.containsKey(SCHEDULE_ITEM_ID))
    }
}
