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

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.instructure.pandautils.BuildConfig
import com.instructure.pandautils.databinding.FragmentHomeroomBinding
import com.instructure.pandautils.mvvm.Event
import com.instructure.pandautils.mvvm.ViewState
import com.instructure.pandautils.utils.children
import com.instructure.pandautils.views.CanvasWebView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_homeroom.*
import kotlinx.android.synthetic.main.item_announcement.view.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeroomFragment : Fragment() {

    @Inject
    lateinit var homeroomRouter: HomeroomRouter

    private val viewModel: HomeroomViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHomeroomBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.state.observe(viewLifecycleOwner, Observer { state ->
            state?.let {
                handleState(it)
            }
        })

        viewModel.events.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                handleAction(it)
            }
        })

        return binding.root
    }

    private fun handleState(viewState: ViewState) {
        if (viewState == ViewState.Success) {
            Handler().postDelayed({
                setupWebViews()
            }, 400)
        }
    }

    private fun handleAction(action: HomeroomAction) {
        if (action is HomeroomAction.OpenAnnouncements) {
            homeroomRouter.openAnnouncements(action.canvasContext)
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
                homeroomRouter.routeInternally(url)
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = homeroomRouter.canRouteInternally(url)

            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                homeroomRouter.openMedia(url)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) = Unit
            override fun onPageFinishedCallback(webView: WebView, url: String) = Unit
        }

        announcementWebView.addVideoClient(requireActivity())
    }

    companion object {
        fun newInstance(): HomeroomFragment {
            return HomeroomFragment()
        }
    }
}