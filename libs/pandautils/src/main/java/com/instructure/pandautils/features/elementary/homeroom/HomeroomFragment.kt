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
package com.instructure.pandautils.features.elementary.homeroom

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.instructure.canvasapi2.models.Course
import com.instructure.pandautils.BuildConfig
import com.instructure.pandautils.R
import com.instructure.pandautils.databinding.FragmentHomeroomBinding
import com.instructure.pandautils.discussions.DiscussionUtils
import com.instructure.pandautils.features.dashboard.notifications.DashboardNotificationsFragment
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.children
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.SpacesItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_homeroom.*
import kotlinx.android.synthetic.main.fragment_homeroom.view.*
import kotlinx.android.synthetic.main.item_announcement.view.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeroomFragment : Fragment() {

    @Inject
    lateinit var homeroomRouter: HomeroomRouter

    @Inject
    lateinit var webViewRouter: WebViewRouter

    private val viewModel: HomeroomViewModel by viewModels()

    private var updateAssignments = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHomeroomBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.events.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spacing = resources.getDimension(R.dimen.homeroomCardSpacing)
        val decoration = SpacesItemDecoration(spacing.toInt())
        coursesRecyclerView.addItemDecoration(decoration)
        setUpRecyclerViewSpan()

        homeroomSwipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
            (childFragmentManager.findFragmentByTag("notifications_fragment") as DashboardNotificationsFragment).refresh()
        }
    }

    private fun setUpRecyclerViewSpan() {

        view?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                val spacing = resources.getDimension(R.dimen.homeroomCardSpacing)
                val cardRequiredSpace = resources.getDimension(R.dimen.homeroomCardMinRequiredSpace)
                val width = view?.width ?: 0

                val calculatedSpan = (width - spacing * 4) / cardRequiredSpace

                val span = if (calculatedSpan < 2) 1 else 2

                (coursesRecyclerView.layoutManager as GridLayoutManager).spanCount = span
            }

        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setUpRecyclerViewSpan()
    }

    private fun handleAction(action: HomeroomAction) {
        when (action) {
            is HomeroomAction.OpenAnnouncements -> homeroomRouter.openAnnouncements(action.canvasContext)
            is HomeroomAction.LtiButtonPressed -> DiscussionUtils.launchIntent(requireContext(), action.url)
            HomeroomAction.ShowRefreshError -> toast(R.string.homeroomRefreshFail)
            HomeroomAction.AnnouncementViewsReady -> setupWebViews()
            is HomeroomAction.OpenCourse -> homeroomRouter.openCourse(action.course)
            is HomeroomAction.OpenAssignments -> openAssignments(action.course)
            is HomeroomAction.OpenAnnouncementDetails -> homeroomRouter.openAnnouncementDetails(action.course, action.announcement)
            HomeroomAction.UpdateColors -> homeroomRouter.updateColors()
        }
    }

    private fun setupWebViews() {
        announcementsContainer.children.forEach {
            val webView = it.announcementWebView
            if (webView != null) {
                setupWebView(webView)
            }
        }
    }

    private fun setupWebView(announcementWebView: CanvasWebView) {
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        announcementWebView.setBackgroundColor(Color.WHITE)
        announcementWebView.settings.allowFileAccess = true
        announcementWebView.settings.loadWithOverviewMode = true
        announcementWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun routeInternallyCallback(url: String) {
                webViewRouter.routeInternally(url)
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = webViewRouter.canRouteInternally(url)

            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                webViewRouter.openMedia(url)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) = Unit
            override fun onPageFinishedCallback(webView: WebView, url: String) = Unit
        }

        announcementWebView.addVideoClient(requireActivity())
    }

    private fun openAssignments(course: Course) {
        updateAssignments = true
        homeroomRouter.openAssignments(course)
    }

    fun refreshAssignmentStatus() {
        if (updateAssignments) {
            viewModel.refreshAssignmentsStatus()
            updateAssignments = false
        }
    }

    companion object {
        fun newInstance(): HomeroomFragment {
            return HomeroomFragment()
        }
    }
}