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
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.pandautils.base.BaseCanvasFragment
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.databinding.FragmentDiscussionSubmissionViewBinding
import com.instructure.student.router.RouteMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DiscussionSubmissionViewFragment : BaseCanvasFragment() {

    private val binding by viewBinding(FragmentDiscussionSubmissionViewBinding::bind)

    private var discussionUrl: String by StringArg()
    private var authJob: Job? = null

    /**
     * Check if a URL belongs to any of the valid Canvas domains (main domain or override domains)
     */
    private fun isValidCanvasDomain(url: String): Boolean {
        if (url.contains(ApiPrefs.domain)) return true
        return ApiPrefs.overrideDomains.values.any { overrideDomain ->
            overrideDomain != null && url.contains(overrideDomain)
        }
    }

    /**
     * Get the appropriate domain for a given URL (main domain or matching override domain)
     */
    private fun getDomainForUrl(url: String): String {
        if (url.contains(ApiPrefs.domain)) return ApiPrefs.domain
        ApiPrefs.overrideDomains.values.forEach { overrideDomain ->
            if (overrideDomain != null && url.contains(overrideDomain)) {
                return overrideDomain
            }
        }
        return ApiPrefs.domain
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discussion_submission_view, container, false)
    }

    override fun onStart() {
        super.onStart()
        binding.progressBar.announceForAccessibility(getString(R.string.loading))
        binding.discussionSubmissionWebView.enableAlgorithmicDarkening()
        binding.discussionSubmissionWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (!isAdded) return
                if (newProgress >= 100) {
                    binding.progressBar.setGone()
                    binding.discussionSubmissionWebView.setVisible()
                }
            }
        }

        binding.discussionSubmissionWebView.canvasWebViewClientCallback =
            object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String, url: String, filename: String) =
                    RouteMatcher.openMedia(requireActivity(), url)
                override fun onPageStartedCallback(webView: WebView, url: String) = Unit
                override fun onPageFinishedCallback(webView: WebView, url: String) = Unit
                override fun canRouteInternallyDelegate(url: String) =
                    // Let urls with 'root_discussion_topic_id' get redirected so we can capture the correct topic id.
                    // This was an issue when routing a group discussion, with the 'root_discussion_topic_id'
                    // being the course discussion id rather than the group discussion id.
                    (url != discussionUrl && !url.contains("root_discussion_topic_id")) && RouteMatcher.canRouteInternally(
                        requireActivity(),
                        url,
                        getDomainForUrl(url),
                        false
                    )

                override fun routeInternallyCallback(url: String) {
                    RouteMatcher.canRouteInternally(requireActivity(), url, getDomainForUrl(url), true)
                }
            }

        binding.discussionSubmissionWebView.canvasEmbeddedWebViewCallback =
            object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun launchInternalWebViewFragment(url: String) =
                    requireActivity().startActivity(
                        InternalWebViewActivity.createIntent(requireActivity(), url, "", true)
                    )

                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean =
                    !isValidCanvasDomain(url)
            }

        binding.discussionSubmissionWebView.setInitialScale(100)

        authJob = GlobalScope.launch(Dispatchers.Main) {
            val authenticatedUrl = if (isValidCanvasDomain(discussionUrl))
                try {
                    awaitApi {
                        OAuthManager.getAuthenticatedSession(
                            discussionUrl,
                            it,
                            getDomainForUrl(discussionUrl).takeIf { it != ApiPrefs.domain }
                        )
                    }.sessionUrl
                } catch (e: StatusCallbackError) {
                    discussionUrl
                }
            else
                discussionUrl

            binding.discussionSubmissionWebView.loadUrl(authenticatedUrl)
        }
    }

    override fun onStop() {
        super.onStop()
        binding.discussionSubmissionWebView.stopLoading()
    }

    override fun onDestroy() {
        super.onDestroy()
        authJob?.cancel()
    }

    companion object {
        fun newInstance(discussionReplyUrl: String): DiscussionSubmissionViewFragment {
            return DiscussionSubmissionViewFragment().apply {
                discussionUrl = discussionReplyUrl
            }
        }
    }
}
