/*
 * Copyright (C) 2022 - present Instructure, Inc.
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
package com.instructure.pandautils.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.instructure.pandautils.blueprint.BaseCanvasFragment
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.pandautils.databinding.FragmentHtmlContentBinding
import com.instructure.pandautils.navigation.WebViewRouter
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.setupAsCloseButton
import com.instructure.pandautils.views.CanvasWebView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HtmlContentFragment : BaseCanvasFragment() {

    var html: String by StringArg()
    var title: String by StringArg()

    @Inject
    lateinit var webViewRouter: WebViewRouter

    private lateinit var binding: FragmentHtmlContentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentHtmlContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupWebView(binding.canvasWebViewWrapper.webView)

        binding.canvasWebViewWrapper.loadHtml(html, title)
    }

    private fun setupToolbar() {
        binding.toolbar.setTitle(title)
        binding.toolbar.setupAsCloseButton(this)
        ViewStyler.themeToolbarColored(requireActivity(), binding.toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
    }

    private fun setupWebView(canvasWebView: CanvasWebView) {
        canvasWebView.settings.loadWithOverviewMode = true
        canvasWebView.settings.displayZoomControls = false
        canvasWebView.settings.setSupportZoom(true)
        canvasWebView.addVideoClient(requireActivity())
        canvasWebView.setInitialScale(100)

        canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                webViewRouter.openMedia(url)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) = Unit

            override fun onPageStartedCallback(webView: WebView, url: String) = Unit

            override fun canRouteInternallyDelegate(url: String): Boolean = webViewRouter.canRouteInternally(url)

            override fun routeInternallyCallback(url: String) {
                webViewRouter.routeInternally(url)
            }
        }
    }

    companion object {
        const val TITLE = "title"
        const val HTML = "html"

        fun newInstance(args: Bundle) = HtmlContentFragment().apply {
            title = args.getString(TITLE) ?: ""
            html = args.getString(HTML) ?: ""
        }

        fun makeBundle(title: String, html: String): Bundle {
            val args = Bundle()
            args.putString(TITLE, title)
            args.putString(HTML, html)
            return args
        }
    }
}