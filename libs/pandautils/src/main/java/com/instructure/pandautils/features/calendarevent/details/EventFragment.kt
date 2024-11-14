/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.features.calendarevent.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.ScheduleItem
import com.instructure.interactions.FragmentInteractions
import com.instructure.interactions.Navigation
import com.instructure.interactions.router.Route
import com.instructure.pandautils.R
import com.instructure.pandautils.analytics.SCREEN_VIEW_CALENDAR_EVENT
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.features.calendar.CalendarSharedEvents
import com.instructure.pandautils.features.calendar.SharedCalendarAction
import com.instructure.pandautils.features.calendarevent.details.composables.EventScreen
import com.instructure.pandautils.interfaces.NavigationCallbacks
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.collectOneOffEvents
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@ScreenView(SCREEN_VIEW_CALENDAR_EVENT)
class EventFragment : BaseCanvasFragment(), NavigationCallbacks, FragmentInteractions {

    @Inject
    lateinit var eventRouter: EventRouter

    @Inject
    lateinit var webViewRouter: WebViewRouter

    private val viewModel: EventViewModel by viewModels()

    @Inject
    lateinit var sharedEvents: CalendarSharedEvents

    private val embeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
        override fun launchInternalWebViewFragment(url: String) = webViewRouter.launchInternalWebViewFragment(url, viewModel.getCanvasContext())

        override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
    }

    private val webViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
        override fun openMediaFromWebView(mime: String, url: String, filename: String) = webViewRouter.openMedia(url)

        override fun onPageStartedCallback(webView: WebView, url: String) = Unit

        override fun onPageFinishedCallback(webView: WebView, url: String) = Unit

        override fun canRouteInternallyDelegate(url: String) = webViewRouter.canRouteInternally(url)

        override fun routeInternallyCallback(url: String) = webViewRouter.routeInternally(url)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        applyTheme()
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(viewModel.events, ::handleAction)
        viewLifecycleOwner.lifecycleScope.collectOneOffEvents(sharedEvents.events, ::handleSharedViewModelAction)

        return ComposeView(requireActivity()).apply {
            id = R.id.eventFragment
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                EventScreen(
                    title = title(),
                    eventUiState = uiState,
                    actionHandler = viewModel::handleAction,
                    applyOnWebView = {
                        addVideoClient(requireActivity())
                        canvasEmbeddedWebViewCallback = embeddedWebViewCallback
                        canvasWebViewClientCallback = webViewClientCallback
                    },
                    navigationAction = ::navigateBack
                )
            }
        }
    }

    override val navigation: Navigation?
        get() = activity as? Navigation

    override fun title(): String = getString(R.string.Event)

    override fun applyTheme() {
        ViewStyler.setStatusBarDark(requireActivity(), viewModel.getCanvasContext().color)
    }

    override fun getFragment(): Fragment {
        return this
    }

    override fun onHandleBackPressed(): Boolean {
        return false
    }

    private fun navigateBack() {
        activity?.onBackPressed()
    }

    private fun handleAction(action: EventViewModelAction) {
        when (action) {
            is EventViewModelAction.OpenLtiScreen -> webViewRouter.openLtiScreen(viewModel.getCanvasContext(), action.url)
            is EventViewModelAction.OpenEditEvent -> eventRouter.openEditEvent(action.scheduleItem)
            is EventViewModelAction.RefreshCalendarDays -> {
                navigateBack()
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.RefreshDays(action.days))
            }
            is EventViewModelAction.RefreshCalendar -> {
                navigateBack()
                sharedEvents.sendEvent(lifecycleScope, SharedCalendarAction.RefreshCalendar)
            }
        }
    }

    private fun handleSharedViewModelAction(action: SharedCalendarAction) {
        when (action) {
            is SharedCalendarAction.CloseEventScreen -> activity?.onBackPressed()
            else -> {}
        }
    }

    companion object {
        const val SCHEDULE_ITEM = "schedule_item"
        const val SCHEDULE_ITEM_ID = "schedule_item_id"
        const val CONTEXT_TYPE = "context_type"
        const val CONTEXT_ID = "context_id"

        fun newInstance(route: Route) = EventFragment().withArgs(route.arguments)

        fun makeRoute(canvasContext: CanvasContext, scheduleItem: ScheduleItem) = Route(
            EventFragment::class.java, canvasContext, canvasContext.makeBundle {
                putParcelable(SCHEDULE_ITEM, scheduleItem)
            }
        )

        fun makeRoute(canvasContext: CanvasContext, scheduleItemId: Long) = Route(
            EventFragment::class.java, canvasContext, canvasContext.makeBundle {
                putLong(SCHEDULE_ITEM_ID, scheduleItemId)
            }
        )
    }
}
