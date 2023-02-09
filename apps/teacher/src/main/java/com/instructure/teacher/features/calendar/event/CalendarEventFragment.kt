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
package com.instructure.teacher.features.calendar.event

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.core.content.ContextCompat
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR_EVENT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.fragments.BaseFragment
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.fragments.LtiLaunchFragment
import com.instructure.teacher.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_calendar_event.*
import kotlinx.android.synthetic.main.fragment_syllabus.toolbar
import kotlinx.coroutines.Job

@ScreenView(SCREEN_VIEW_CALENDAR_EVENT)
class CalendarEventFragment : BaseFragment() {

    var canvasContext: CanvasContext? by NullableParcelableArg(key = Const.CANVAS_CONTEXT)

    private val transformer = CalendarEventStateTransformer()

    private var scheduleItem: ScheduleItem? by NullableParcelableArg(key = SCHEDULE_ITEM)

    private var loadHtmlJob: Job? = null

    override fun layoutResId(): Int {
        return R.layout.fragment_calendar_event
    }

    override fun onResume() {
        super.onResume()
        calendarEventWebViewWrapper?.webView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        calendarEventWebViewWrapper?.webView?.onPause()
    }

    override fun onCreateView(view: View) {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewState = transformer.transformScheduleItem(requireContext(), scheduleItem)
        applyTheme(viewState)
        initWebView()
        setupViews(viewState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadHtmlJob?.cancel()
    }

    private fun applyTheme(viewState: CalendarEventViewState) {
        toolbar?.title = viewState.eventTitle
        ViewStyler.themeToolbarColored(context as Activity, toolbar, canvasContext)
        toolbar.setupAsBackButton { (context as? Activity)?.onBackPressed() }
    }

    private fun initWebView() {
        with(calendarEventWebViewWrapper.webView) {
            addVideoClient(requireActivity())
            canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun launchInternalWebViewFragment(url: String) {
                    activity?.startActivity(InternalWebViewActivity.createIntent(calendarEventWebViewWrapper.context, url, "", true))
                }

                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
            }

            canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                    RouteMatcher.openMedia(activity, url)
                }

                override fun onPageStartedCallback(webView: WebView, url: String) = Unit
                override fun onPageFinishedCallback(webView: WebView, url: String) = Unit

                override fun canRouteInternallyDelegate(url: String): Boolean = RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false)

                override fun routeInternallyCallback(url: String) {
                    RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true)
                }
            }
        }
    }

    private fun setupViews(viewState: CalendarEventViewState) {
        dateTitle.text = viewState.dateTitle
        dateSubtitle.text = viewState.dateSubtitle
        locationTitle.text = viewState.locationTitle
        locationSubtitle.text = viewState.locationSubtitle

        if (viewState.htmlContent.isNotEmpty()) {
            loadHtmlJob = calendarEventWebViewWrapper.webView.loadHtmlWithIframes(requireContext(), viewState.htmlContent, {
                loadCalendarHtml(it, viewState.eventTitle)
            }) {
                LtiLaunchFragment.routeLtiLaunchFragment(requireContext(), canvasContext, it)
            }
        }
    }

    private fun loadCalendarHtml(html: String, contentDescription: String) {
        calendarEventWebViewWrapper?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.backgroundLightest))
        calendarEventWebViewWrapper?.loadHtml(html, contentDescription, baseUrl = scheduleItem?.htmlUrl)
    }

    companion object {

        private const val SCHEDULE_ITEM = "schedule_item"

        fun newInstance(bundle: Bundle): CalendarEventFragment {
            return CalendarEventFragment().apply {
                arguments = bundle
            }
        }

        fun createArgs(canvasContext: CanvasContext, scheduleItem: ScheduleItem): Bundle {
            return createBundle(canvasContext).apply {
                putParcelable(SCHEDULE_ITEM, scheduleItem)
            }
        }
    }
}