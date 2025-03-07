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

package com.instructure.parentapp.features.alerts.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.pandautils.analytics.SCREEN_VIEW_ANNOUNCEMENT_DETAILS
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.views.CanvasWebView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@ScreenView(SCREEN_VIEW_ANNOUNCEMENT_DETAILS)
@AndroidEntryPoint
class AnnouncementDetailsFragment : BaseCanvasFragment() {

    private val viewModel: AnnouncementDetailsViewModel by viewModels()

    private val courseId by LongArg(key = COURSE_ID)

    @Inject
    lateinit var webViewRouter: WebViewRouter

    private val embeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
        override fun launchInternalWebViewFragment(url: String) = webViewRouter.launchInternalWebViewFragment(url, CanvasContext.emptyCourseContext(courseId))

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireActivity()).apply {
            setContent {
                val uiState by viewModel.uiState.collectAsState()
                ViewStyler.setStatusBarDark(requireActivity(), uiState.studentColor)
                AnnouncementDetailsScreen(
                    uiState,
                    viewModel::handleAction,
                    applyOnWebView = {
                        addVideoClient(requireActivity())
                        canvasEmbeddedWebViewCallback = embeddedWebViewCallback
                        canvasWebViewClientCallback = webViewClientCallback
                    },
                    navigationActionClick = {
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }

    companion object {
        const val COURSE_ID = "course-id"
        const val ANNOUNCEMENT_ID = "announcement-id"
    }
}
