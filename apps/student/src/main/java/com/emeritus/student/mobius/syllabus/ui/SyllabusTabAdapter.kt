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
package com.emeritus.student.mobius.syllabus.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.views.CanvasWebView
import com.emeritus.student.R
import com.emeritus.student.fragment.InternalWebviewFragment
import com.emeritus.student.router.RouteMatcher
import kotlinx.android.synthetic.main.fragment_syllabus_events.view.*
import kotlinx.android.synthetic.main.fragment_syllabus_webview.view.*

class SyllabusTabAdapter(private val canvasContext: CanvasContext, private val titles: List<String>) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any) = view === `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val id =
            if (isSyllabusPosition(position)) R.layout.fragment_syllabus_webview else R.layout.fragment_syllabus_events
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(id, container, false)
        container.addView(view)

        if (!isSyllabusPosition(position)) {
            view.syllabusEventsRecycler.layoutManager = LinearLayoutManager(container.context)
        } else {
            setupWebView(view.syllabusWebViewWrapper.webView)
        }

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_UNCHANGED
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    private fun isSyllabusPosition(position: Int) = position == 0

    private fun setupWebView(webView: CanvasWebView) {
        val activity = (webView.context as? FragmentActivity)
        activity?.let { webView.addVideoClient(it) }
        webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(activity, url)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun onPageFinishedCallback(webView: WebView, url: String) {}

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return RouteMatcher.canRouteInternally(webView.context, url, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(webView.context, url, ApiPrefs.domain, true)
            }
        }
        webView.canvasEmbeddedWebViewCallback =
            object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean {
                    return true
                }

                override fun launchInternalWebViewFragment(url: String) {
                    InternalWebviewFragment.loadInternalWebView(
                        webView.context,
                        InternalWebviewFragment.makeRoute(canvasContext, url, false)
                    )
                }
            }

    }
}