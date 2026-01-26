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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.widget.Toolbar
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.validOrNull
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.MasterDetailInteractions
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.fragments.BaseFragment
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.color
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.databinding.FragmentInternalWebviewBinding
import com.instructure.teacher.router.RouteMatcher
import com.instructure.teacher.utils.setupBackButtonWithExpandCollapseAndBack
import com.instructure.teacher.utils.setupCloseButton
import com.instructure.teacher.utils.setupMenu
import com.instructure.teacher.utils.updateToolbarExpandCollapseIcon
import kotlinx.coroutines.Job

open class InternalWebViewFragment : BaseFragment() {

    protected val binding by viewBinding(FragmentInternalWebviewBinding::bind)

    var toolbar: Toolbar? = null

    var url: String by StringArg()
    var html: String by StringArg()
    var title: String by StringArg()
    var darkToolbar: Boolean by BooleanArg()
    private var shouldAuthenticate: Boolean by BooleanArg(key = AUTHENTICATE)
    var shouldRouteInternally: Boolean by BooleanArg(key = SHOULD_ROUTE_INTERNALLY, default = true)
    private var isInModulesPager: Boolean by BooleanArg(key = IS_IN_MODULES_PAGER, default = false)
    private var allowRoutingTheSameUrlInternally: Boolean by BooleanArg(key = ALLOW_ROUTING_THE_SAME_URL_INTERNALLY, default = true)
    var enableAlgorithmicDarkening: Boolean by BooleanArg(key = ENABLE_ALGORITHMIC_DARKENING, default = false)

    private var shouldLoadUrl = true
    private var mSessionAuthJob: Job? = null
    private var shouldCloseFragment = false

    override fun layoutResId() = R.layout.fragment_internal_webview

    override fun onPause() {
        super.onPause()
        binding.canvasWebView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.canvasWebView.onResume()
    }

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         retainInstance = true
     }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (view != null) binding.canvasWebView.saveState(outState)
    }

    override fun onCreateView(view: View) = Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.toolbar)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) = with(binding) {
        super.onActivityCreated(savedInstanceState)

        val courseId: String? = RouteMatcher.getCourseIdFromUrl(url)
        var courseBackgroundColor = ThemePrefs.primaryColor
        courseId?.let {
            try {
                courseBackgroundColor = CanvasContext.emptyCourseContext(courseId.toLong()).color
            } catch (e: NumberFormatException) {
                Logger.e(e.message)
            }
        }

        toolbar?.title = title.validOrNull() ?: url

        // If we are displaying html we don't want them to view the url in a different browser
        if(!html.isValid()) {
            toolbar.setupMenu(R.menu.menu_internal_webview) {
                val browserIntent = Intent("android.intent.action.VIEW").apply {
                    data = Uri.parse(url)
                }
                if (browserIntent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(browserIntent)
                } else {
                    toast(R.string.no_apps)
                }
            }
        }

        setupToolbar(courseBackgroundColor)

        canvasWebView.settings.loadWithOverviewMode = true
        canvasWebView.settings.displayZoomControls = false
        canvasWebView.settings.setSupportZoom(true)
        canvasWebView.addVideoClient(requireActivity())
        canvasWebView.setInitialScale(100)
        if (enableAlgorithmicDarkening) canvasWebView.enableAlgorithmicDarkening()

        canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                RouteMatcher.openMedia(activity, url, filename)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                loading.setGone()
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {
                loading.setVisible()
            }

            override fun canRouteInternallyDelegate(url: String): Boolean = shouldRouteInternally
                    && shouldRouteIfUrlIsTheSame(url)
                    && RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, false)

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(activity, url, ApiPrefs.domain, true)
            }

            private fun shouldRouteIfUrlIsTheSame(url: String): Boolean {
                val sameUrl = this@InternalWebViewFragment.url.contains(url)
                return !sameUrl || allowRoutingTheSameUrlInternally
            }
        }

        if (savedInstanceState != null) {
            canvasWebView.restoreState(savedInstanceState)
        }
        if (shouldLoadUrl) {
            loadUrl(url)
        }
    }

    open fun setupToolbar(courseColor: Int) {
        if (isInModulesPager) {
            toolbar.setupBackButtonWithExpandCollapseAndBack(this@InternalWebViewFragment) {
                toolbar.updateToolbarExpandCollapseIcon(this@InternalWebViewFragment)
                ViewStyler.themeToolbarColored(requireActivity(), toolbar!!, courseColor, requireContext().getColor(R.color.textLightest))
                (activity as MasterDetailInteractions).toggleExpandCollapse()
            }
        } else {
            toolbar.setupCloseButton {
                shouldCloseFragment = true
                activity?.onBackPressed()
            }
        }

        if (darkToolbar) {
            if (courseColor != -1) {
                // Use course colors for toolbar
                ViewStyler.themeToolbarColored(requireActivity(), toolbar!!, courseColor, requireContext().getColor(R.color.textLightest))
            } else {
                // Use institution colors for toolbar
                ViewStyler.themeToolbarColored(requireActivity(), toolbar!!, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
            }
        } else {
            ViewStyler.themeToolbarLight(requireActivity(), toolbar!!)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Logic
    ///////////////////////////////////////////////////////////////////////////

    fun setShouldLoadUrl(shouldLoadUrl: Boolean) {
        this.shouldLoadUrl = shouldLoadUrl
    }

    fun loadUrl(targetUrl: String) = with(binding) {
        if (html.isNotEmpty()) {
            canvasWebView.loadHtml(html, title)
            return
        }

        url = targetUrl
        if (url.isNotEmpty() && isAdded) {
            mSessionAuthJob = weave {
                if (ApiPrefs.domain in url && shouldAuthenticate) {
                    try {
                        // Get an authenticated session so the user doesn't have to log in
                        url = awaitApi<AuthenticatedSession> { OAuthManager.getAuthenticatedSession(url, it) }.sessionUrl
                    } catch (e: StatusCallbackError) {
                    }
                }

                canvasWebView.loadUrl(url, getReferer())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSessionAuthJob?.cancel()
    }

    fun loadHtml(data: String, mimeType: String, encoding: String, historyUrl: String) {
        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        binding.canvasWebView.loadDataWithBaseURL(CanvasWebView.getReferrer(), data, mimeType, encoding, historyUrl)
    }

    // BaseURL is set as Referer. Referer needed for some vimeo videos to play
    fun loadHtml(html: String) = binding.canvasWebView.loadDataWithBaseURL(ApiPrefs.fullDomain,
            FileUtils.getAssetsFile(requireContext(), "html_wrapper.html").replace("{\$CONTENT$}", html, ignoreCase = false),
            "text/html", "UTF-8", null)

    private fun getReferer(): Map<String, String> = mutableMapOf(Pair("Referer", ApiPrefs.domain))

    fun canGoBack() = if (!shouldCloseFragment) binding.canvasWebView.canGoBack() else false
    fun goBack() = binding.canvasWebView.goBack()

    fun setShouldAuthenticateUponLoad(shouldAuthenticate: Boolean) {
        this.shouldAuthenticate = shouldAuthenticate
    }

    companion object {
        const val URL = "url"
        const val TITLE = "title"
        const val HTML = "html"
        const val DARK_TOOLBAR = "darkToolbar"
        const val AUTHENTICATE = "authenticate"
        const val ENABLE_ALGORITHMIC_DARKENING = "enableAlgorithmicDarkening"
        private const val SHOULD_ROUTE_INTERNALLY = "shouldRouteInternally"
        private const val IS_IN_MODULES_PAGER = "isInModulesPager"
        private const val ALLOW_ROUTING_THE_SAME_URL_INTERNALLY = "allowRoutingTheSameUrlInternally"

        fun newInstance(url: String) = InternalWebViewFragment().apply {
            this.url = url
        }

        fun newInstance(url: String, html: String) = InternalWebViewFragment().apply {
            this.url = url
            this.html = html
        }

        fun newInstance(url: String, html: String, title: String) = InternalWebViewFragment().apply {
            this.url = url
            this.html = html
            this.title = title
        }

        fun newInstance(args: Bundle) = InternalWebViewFragment().apply {
            url = args.getString(URL)!!
            title = args.getString(TITLE)!!
            html = args.getString(HTML) ?: ""
            darkToolbar = args.getBoolean(DARK_TOOLBAR)
            shouldAuthenticate = args.getBoolean(AUTHENTICATE)
            shouldRouteInternally = args.getBoolean(SHOULD_ROUTE_INTERNALLY)
            isInModulesPager = args.getBoolean(IS_IN_MODULES_PAGER)
            allowRoutingTheSameUrlInternally = args.getBoolean(ALLOW_ROUTING_THE_SAME_URL_INTERNALLY)
            enableAlgorithmicDarkening = args.getBoolean(ENABLE_ALGORITHMIC_DARKENING)
        }

        @JvmOverloads
        fun makeBundle(url: String, title: String, darkToolbar: Boolean = false, html: String = ""): Bundle {
            val args = Bundle()
            args.putString(URL, url)
            args.putString(TITLE, title)
            args.putString(HTML, html)
            args.putBoolean(DARK_TOOLBAR, darkToolbar)
            return args
        }

        fun makeBundle(
            url: String,
            title: String,
            darkToolbar: Boolean = false,
            html: String = "",
            shouldRouteInternally: Boolean = true,
            isInModulesPager: Boolean = false,
            allowRoutingTheSameUrlInternally: Boolean = true,
            shouldAuthenticate: Boolean
        ): Bundle {
            val args = Bundle()
            args.putString(URL, url)
            args.putString(TITLE, title)
            args.putString(HTML, html)
            args.putBoolean(DARK_TOOLBAR, darkToolbar)
            args.putBoolean(AUTHENTICATE, shouldAuthenticate)
            args.putBoolean(SHOULD_ROUTE_INTERNALLY, shouldRouteInternally)
            args.putBoolean(IS_IN_MODULES_PAGER, isInModulesPager)
            args.putBoolean(ALLOW_ROUTING_THE_SAME_URL_INTERNALLY, allowRoutingTheSameUrlInternally)
            return args
        }
    }
}
