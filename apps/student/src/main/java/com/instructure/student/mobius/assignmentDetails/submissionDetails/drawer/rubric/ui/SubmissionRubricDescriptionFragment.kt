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
package com.instructure.student.mobius.assignmentDetails.submissionDetails.drawer.rubric.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.instructure.pandautils.base.BaseCanvasDialogFragment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.databinding.FragmentSubmissionRubricDescriptionBinding
import com.instructure.student.router.RouteMatcher

class SubmissionRubricDescriptionFragment : BaseCanvasDialogFragment() {

    private val binding by viewBinding(FragmentSubmissionRubricDescriptionBinding::bind)

    var title by StringArg(key = Const.TITLE)
    var description by StringArg(key = Const.BODY)

    init {
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_submission_rubric_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with (binding) {
            toolbar.title = title
            toolbar.setupAsBackButton(this@SubmissionRubricDescriptionFragment)
            toolbar.applyTopSystemBarInsets()
            ViewStyler.themeToolbarLight(requireActivity(), toolbar)

            // Show progress bar while loading description
            progressBar.setVisible()
            progressBar.announceForAccessibility(getString(R.string.loading))
            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress >= 100) {
                        progressBar?.setGone()
                        webView?.setVisible()
                    }
                }
            }

            webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String, url: String, filename: String) {}
                override fun onPageStartedCallback(webView: WebView, url: String) {}
                override fun onPageFinishedCallback(webView: WebView, url: String) {}
                override fun canRouteInternallyDelegate(url: String): Boolean {
                    return RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false)
                }

                override fun routeInternallyCallback(url: String) {
                    RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true)
                }
            }

            webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun launchInternalWebViewFragment(url: String) = requireActivity().startActivity(
                    InternalWebViewActivity.createIntent(requireActivity(), url, "", true)
                )

                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean = true
            }

            // make the WebView background transparent
            webView.setBackgroundResource(android.R.color.transparent)

            webView.applyBottomSystemBarInsets()

            // Load description
            webView.loadHtml(description, title)
        }
    }

    companion object {

        fun makeRoute(title: String, description: String): Route {
            val args = Bundle().apply {
                putString(Const.TITLE, title)
                putString(Const.BODY, description)
            }
            return Route(SubmissionRubricDescriptionFragment::class.java, null, args)
        }

        fun newInstance(route: Route) = SubmissionRubricDescriptionFragment().withArgs(route.arguments)
    }

}
