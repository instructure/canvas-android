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

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.webkit.WebView
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.analytics.SCREEN_VIEW_QUIZ_PREVIEW
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButton
import kotlinx.coroutines.Job

@ScreenView(SCREEN_VIEW_QUIZ_PREVIEW)
class QuizPreviewWebviewFragment : InternalWebViewFragment() {

    var mClickedPreview = false
    private var apiCall: Job? = null

    private fun clickPreviewButton() {

        // Try to automatically tap the Preview button so we take them directly into the quiz preview
        Handler().postDelayed({
            activity?.runOnUiThread {
                mClickedPreview = true
                val js = "javascript: { " +
                        "document.getElementById('preview_quiz_button').click();" +
                        "};"

                binding.canvasWebView.evaluateJavascript(js) { }
            }
        }, 0)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) = with(binding) {
        setShouldLoadUrl(false)
        super.onActivityCreated(savedInstanceState)
        canvasWebView.enableAlgorithmicDarkening()

        canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) =
                RouteMatcher.openMedia(requireActivity(), url)

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                // if the teacher submits a quiz preview, it will load the details again and automatically click the preview button again
                if(!mClickedPreview) {
                    clickPreviewButton()
                }
                loading.setGone()
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {
                loading?.setVisible()
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = false

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }
        }
        loadUrl(url)
    }

    override fun setupToolbar(courseColor: Int) {
        toolbar?.let {
            it.setupBackButton {
                if (!canGoBack()) {
                    (requireContext() as Activity).onBackPressed()
                } else {
                    goBack()
                }
            }
            ViewStyler.setToolbarElevationSmall(requireContext(), it)
            ViewStyler.themeToolbarLight(requireActivity(), it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        apiCall?.cancel()
    }

    companion object {
        @JvmStatic val URL = "url"
        @JvmStatic val TITLE = "title"

        fun newInstance(args: Bundle) = QuizPreviewWebviewFragment().apply {
            arguments = args
            url = args.getString(URL)!!
            title = args.getString(TITLE)!!
        }

        fun makeBundle(url: String, title: String): Bundle {
            val args = Bundle()
            args.putString(URL, url)
            args.putString(TITLE, title)
            args.putBoolean(DARK_TOOLBAR, false)
            args.putBoolean(AUTHENTICATE, true)
            return args
        }
    }
}
