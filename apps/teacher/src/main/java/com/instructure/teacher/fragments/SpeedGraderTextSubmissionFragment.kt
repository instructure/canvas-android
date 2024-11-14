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
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_SPEED_GRADER_TEXT_SUBMISSION
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.databinding.FragmentSpeedGraderTextSubmissionBinding
import com.instructure.teacher.interfaces.SpeedGraderWebNavigator
import com.instructure.teacher.router.RouteMatcher

@ScreenView(SCREEN_VIEW_SPEED_GRADER_TEXT_SUBMISSION)
class SpeedGraderTextSubmissionFragment : BaseCanvasFragment(), SpeedGraderWebNavigator {

    private val binding by viewBinding(FragmentSpeedGraderTextSubmissionBinding::bind)

    private var mSubmissionText by StringArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_speed_grader_text_submission, container, false)
    }

    override fun canGoBack() = binding.textSubmissionWebViewWrapper.webView.canGoBack()
    override fun goBack() = binding.textSubmissionWebViewWrapper.webView.goBack()

    override fun onStart() = with(binding) {
        super.onStart()
        textSubmissionWebViewWrapper.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (!isAdded) return
                if (newProgress >= 100) {
                    progressBar.setGone()
                    textSubmissionWebViewWrapper.setVisible()
                } else {
                    progressBar.announceForAccessibility(getString(R.string.loading))
                }
            }
        }

        textSubmissionWebViewWrapper.webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) = Unit
            override fun onPageStartedCallback(webView: WebView, url: String) = Unit
            override fun onPageFinishedCallback(webView: WebView, url: String) = Unit
            override fun canRouteInternallyDelegate(url: String) = RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)
            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }
        }

        textSubmissionWebViewWrapper.webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) = requireActivity().startActivity(InternalWebViewActivity.createIntent(requireActivity(), url, "", true))
            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        textSubmissionWebViewWrapper.loadHtml(mSubmissionText, getString(R.string.a11y_submissionText))
    }

    override fun onStop() {
        super.onStop()
        binding.textSubmissionWebViewWrapper.webView.stopLoading()
    }

    companion object {
        fun newInstance(text: String) = SpeedGraderTextSubmissionFragment().apply {
            mSubmissionText = text
        }
    }

}

