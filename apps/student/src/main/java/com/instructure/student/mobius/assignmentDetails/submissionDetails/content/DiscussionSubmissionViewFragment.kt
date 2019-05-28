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
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_discussion_submission_view.*

class DiscussionSubmissionViewFragment : Fragment() {

    private var discussionUrl: String by StringArg()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discussion_submission_view, container, false)
    }

    override fun onStart() {
        super.onStart()
        discussionSubmissionWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (!isAdded) return
                if (newProgress >= 100) {
                    progressBar?.setGone()
                    discussionSubmissionWebView?.setVisible()
                } else {
                    progressBar.announceForAccessibility(getString(R.string.loading))
                }
            }
        }

        discussionSubmissionWebView.canvasWebViewClientCallback =
            object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String?, url: String?, filename: String?) =
                    Unit

                override fun onPageStartedCallback(webView: WebView?, url: String?) = Unit
                override fun onPageFinishedCallback(webView: WebView?, url: String?) = Unit
                override fun canRouteInternallyDelegate(url: String?) =
                    RouteMatcher.canRouteInternally(requireContext(), url!!, ApiPrefs.domain, false)

                override fun routeInternallyCallback(url: String?) {
                    RouteMatcher.canRouteInternally(requireContext(), url!!, ApiPrefs.domain, true)
                }
            }

        discussionSubmissionWebView.canvasEmbeddedWebViewCallback =
            object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun launchInternalWebViewFragment(url: String) =
                    requireActivity().startActivity(
                        InternalWebViewActivity.createIntent(requireActivity(), url, "", true)
                    )

                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
            }

        discussionSubmissionWebView.setInitialScale(100)
        discussionSubmissionWebView.loadUrl(discussionUrl)
    }

    override fun onStop() {
        super.onStop()
        discussionSubmissionWebView.stopLoading()
    }

    companion object {
        fun newInstance(discussionReplyUrl: String): DiscussionSubmissionViewFragment {
            return DiscussionSubmissionViewFragment().apply {
                discussionUrl = discussionReplyUrl
            }
        }
    }
}