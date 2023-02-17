/*
 * Copyright (C) 2021 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.emeritus.student.features.elementary.course

import android.content.ContextWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.PagerAdapter
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.setDarkModeSupport
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.views.CanvasWebView
import com.emeritus.student.R
import com.emeritus.student.activity.InternalWebViewActivity
import com.emeritus.student.router.RouteMatcher

class ElementaryCoursePagerAdapter(
    private val tabs: List<ElementaryCourseTab>,
) : PagerAdapter() {

    private fun getReferer(): Map<String, String> = mutableMapOf(Pair("Referer", ApiPrefs.domain))

    override fun getCount(): Int = tabs.size

    override fun isViewFromObject(view: View, any: Any): Boolean = view == any

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.elementary_course_webview, container, false)
        val webView = view.findViewById<CanvasWebView>(R.id.elementaryWebView)
        val progressBar = view.findViewById<ProgressBar>(R.id.webViewProgress)
        container.addView(view)

        progressBar.setVisible()
        setupViews(webView, progressBar)
        webView.loadUrl(tabs[position].url, getReferer())

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        container.removeView(item as View)
    }

    private fun setupViews(webView: CanvasWebView, progressBar: ProgressBar) {
        val baseContext = (webView.context as ContextWrapper).baseContext
        val activity = (baseContext as? FragmentActivity)
        activity?.let { webView.addVideoClient(it) }
        webView.setDarkModeSupport()
        webView.setZoomSettings(false)
        webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(activity, url)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {
                progressBar.setVisible()
            }
            override fun onPageFinishedCallback(webView: WebView, url: String) {
                progressBar.setGone()
            }

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return !isUrlSame(webView, url) && RouteMatcher.canRouteInternally(baseContext, url, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(baseContext, url, ApiPrefs.domain, true)
            }
        }
        webView.canvasEmbeddedWebViewCallback =
            object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean {
                    return false
                }

                override fun launchInternalWebViewFragment(url: String) {
                    activity?.startActivity(InternalWebViewActivity.createIntent(baseContext, url, "", true))
                }
            }
    }

    private fun isUrlSame(webView: CanvasWebView, url: String): Boolean {
        val strippedUrl = webView.url?.replace(Regex("&session_token=[^&|^#\\s]+|session_token=[^&\\s]+&?"), "")
        return strippedUrl?.contains(url) ?: false
    }
}