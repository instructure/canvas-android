/*
 * Copyright (C) 2020 - present Instructure, Inc.
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
package com.instructure.teacher.features.syllabus.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.CanvasWebViewWrapper
import com.instructure.teacher.R
import com.instructure.teacher.activities.InternalWebViewActivity
import com.instructure.teacher.router.RouteMatcher

private const val TAB_COUNT = 2
private const val SYLLABUS_TAB_POSITION = 0

class SyllabusTabAdapter(private val titles: List<String>) : PagerAdapter() {

    override fun isViewFromObject(view: View, any: Any) = view === any

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val id = if (isSyllabusPosition(position)) R.layout.fragment_syllabus_webview else R.layout.fragment_syllabus_events
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(id, container, false)
        container.addView(view)

        if (isSyllabusPosition(position)) {
            val syllabusWebViewWrapper = view.findViewById<CanvasWebViewWrapper>(R.id.syllabusWebViewWrapper)
            setupWebView(syllabusWebViewWrapper.webView)
        } else {
            setupLayoutManager(view, container.context)
        }

        return view
    }

    private fun isSyllabusPosition(position: Int) = position == SYLLABUS_TAB_POSITION

    private fun setupWebView(webView: CanvasWebView) {
        val activity = webView.context.getFragmentActivity()
        webView.addVideoClient(activity)
        webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(activity, url)
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {}
            override fun onPageFinishedCallback(webView: WebView, url: String) {}

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }
        }
        webView.canvasEmbeddedWebViewCallback =
            object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                override fun shouldLaunchInternalWebViewFragment(url: String): Boolean {
                    return true
                }

                override fun launchInternalWebViewFragment(url: String) {
                    activity.startActivity(InternalWebViewActivity.createIntent(webView.context, url, "", true))
                }
            }

    }

    private fun setupLayoutManager(view: View, context: Context) {
        val syllabusEventsRecyclerView = view.findViewById<RecyclerView>(R.id.syllabusEventsRecyclerView)
        syllabusEventsRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        container.removeView(any as View)
    }

    override fun getItemPosition(any: Any): Int {
        return POSITION_UNCHANGED
    }

    override fun getCount(): Int {
        return TAB_COUNT
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}