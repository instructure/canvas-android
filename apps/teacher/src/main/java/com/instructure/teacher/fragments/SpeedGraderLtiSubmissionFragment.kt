/*
 * Copyright (C) 2017 - present Instructure, Inc.
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
package com.instructure.teacher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.lifecycle.lifecycleScope
import com.instructure.canvasapi2.apis.OAuthAPI
import com.instructure.canvasapi2.builders.RestParams
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER_LTI_SUBMISSION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.speedgrader.content.ExternalToolContent
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentSpeedGraderLtiSubmissionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_SPEED_GRADER_LTI_SUBMISSION)
@AndroidEntryPoint
class SpeedGraderLtiSubmissionFragment : BaseCanvasFragment() {

    @Inject
    lateinit var oAuthInterface: OAuthAPI.OAuthInterface

    private val binding by viewBinding(FragmentSpeedGraderLtiSubmissionBinding::bind)

    private var url by StringArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_speed_grader_lti_submission, container, false)
    }

    override fun onStart() {
        super.onStart()
        setupViews()
    }

    private fun setupViews() {
        binding.webView.enableAlgorithmicDarkening()
        binding.webView.setZoomSettings(false)
        binding.webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) = Unit

            override fun onPageStartedCallback(webView: WebView, url: String) {
                if (isAdded) binding.webViewProgress.setVisible()
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                if (isAdded) binding.webViewProgress.setGone()
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = false

            override fun routeInternallyCallback(url: String) = Unit
        }
        lifecycleScope.launch {
            val authenticatedUrl =
                oAuthInterface.getAuthenticatedSession(url, RestParams()).dataOrNull?.sessionUrl
                    ?: url
            binding.webView.loadUrl(authenticatedUrl)
        }
    }

    companion object {
        fun newInstance(content: ExternalToolContent) = SpeedGraderLtiSubmissionFragment().apply {
            url = content.url
        }
    }
}

