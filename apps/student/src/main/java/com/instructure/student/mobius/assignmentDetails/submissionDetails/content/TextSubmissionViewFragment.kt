/*
 * Copyright (C) 2019 - present Instructure, Inc.
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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.content

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.replaceFirstAfter
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_text_submission.*

class TextSubmissionViewFragment : Fragment() {

    private var submissionText by StringArg()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_text_submission, container, false)
    }

    override fun onStart() {
        super.onStart()
        textSubmissionWebViewWrapper.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (!isAdded) return
                if (newProgress >= 100) {
                    progressBar?.setGone()
                    textSubmissionWebViewWrapper?.setVisible()
                } else {
                    progressBar.announceForAccessibility(getString(R.string.loading))
                }
            }
        }

        textSubmissionWebViewWrapper.webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) = Unit
            override fun onPageStartedCallback(webView: WebView, url: String) = Unit
            override fun onPageFinishedCallback(webView: WebView, url: String) = Unit
            override fun canRouteInternallyDelegate(url: String) =
                RouteMatcher.canRouteInternally(requireContext(), url, ApiPrefs.domain, false)

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(requireContext(), url, ApiPrefs.domain, true)
            }
        }

        textSubmissionWebViewWrapper.webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
            override fun launchInternalWebViewFragment(url: String) = requireActivity().startActivity(
                InternalWebViewActivity.createIntent(requireActivity(), url, "", true)
            )

            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
        }

        textSubmissionWebViewWrapper.loadHtml(submissionText, getString(R.string.a11y_submissionText)) { formatted ->
            /* If the source content begins with a paragraph tag, the WebView automatically applies some vertical padding.
           For other content, we need to apply the padding ourselves. */
            val verticalPadding = if (submissionText.startsWith("<p")) "0px" else "16px"

            // Set padding by updating the relevant CSS property
            formatted.replaceFirstAfter("#content", "padding: 0px 10px 10px;", "padding: $verticalPadding 16px;")
        }
    }

    override fun onStop() {
        super.onStop()
        textSubmissionWebViewWrapper.webView.stopLoading()
    }

    companion object {
        fun newInstance(text: String) = TextSubmissionViewFragment().apply {
            submissionText = text
        }
    }

}

