/*
 * Copyright (C) 2017 - present  Instructure, Inc.
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
package com.instructure.parentapp.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.instructure.canvasapi2.models.User
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.isValid
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.Utils
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.parentapp.R
import com.instructure.parentapp.util.RouteMatcher
import kotlinx.android.synthetic.main.webview_fragment.*

class InternalWebViewFragment : ParentFragment() {

    private var url by NullableStringArg(key = Const.INTERNAL_URL)
    private var html by NullableStringArg(key = Const.HTML)
    private var student by NullableParcelableArg<User>(key = Const.STUDENT)

    override val rootLayout = R.layout.webview_fragment

    override fun onPause() {
        super.onPause()
        internalWebview.onPause()
    }

    override fun onResume() {
        super.onResume()
        internalWebview.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        internalWebview.saveState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(rootLayout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        internalWebview.settings.loadWithOverviewMode = true
        internalWebview.settings.displayZoomControls = false
        internalWebview.settings.setSupportZoom(true)
        internalWebview.addVideoClient(activity)

        if (student != null) {
            internalWebview.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String, url: String, filename: String) {}
                override fun onPageStartedCallback(webView: WebView, url: String) {}
                override fun onPageFinishedCallback(webView: WebView, url: String) {}

                override fun canRouteInternallyDelegate(url: String): Boolean =
                    RouteMatcher.canRouteInternally(null, url, student, ApiPrefs.domain, false)

                override fun routeInternallyCallback(url: String) {
                    RouteMatcher.canRouteInternally(activity, url, student, ApiPrefs.domain, true)
                }
            }
        }
        internalWebview.restoreState(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadUrl(url)
    }

    private fun loadUrl(url: String?) {
        if (html.isValid()) {
            loadHtml(html!!)
            return
        }
        this.url = url
        if (!TextUtils.isEmpty(url)) {
            internalWebview.loadUrl(url, Utils.getReferer(context))
        }
    }


    private fun loadHtml(html: String) {
        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        internalWebview.loadDataWithBaseURL(ApiPrefs.fullDomain,
                FileUtils.getAssetsFile(context, "html_wrapper.html").replace("{\$CONTENT$}", html), "text/html", "UTF-8", null)
    }

    fun canGoBack() = internalWebview.canGoBack()
    fun goBack() = internalWebview.goBack()

    companion object {
        fun newInstance(title: String, url: String?, html: String?) = InternalWebViewFragment().apply {
            arguments = createBundle(url, title, html, null)
        }

        fun createBundle(url: String?, title: String, html: String?, student: User?): Bundle {
            return Bundle().apply {
                if (student != null) putParcelable(Const.STUDENT, student)
                putString(Const.INTERNAL_URL, url)
                putString(Const.ACTION_BAR_TITLE, title)
                putString(Const.HTML, html)
            }
        }
    }
}
