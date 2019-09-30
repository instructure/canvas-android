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

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.instructure.student.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ProgressBar
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.FileUtils
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.weave.*
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.activity.InternalWebViewActivity
import com.instructure.student.router.RouteMatcher
import com.instructure.student.util.FileDownloadJobIntentService
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.android.synthetic.main.fragment_webview.view.*
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class InternalWebviewFragment : ParentFragment() {

    var canvasContext: CanvasContext by ParcelableArg(default = CanvasContext.emptyUserContext(), key = Const.CANVAS_CONTEXT)
    var assignmentLtiUrl: String? by NullableStringArg(key = Const.API_URL)    // If we're coming from an lti assignment we need the original assignment url, not the sessionless one
    var html: String? by NullableStringArg(key = Const.HTML)
    var isLTITool: Boolean by BooleanArg(key = Const.IS_EXTERNAL_TOOL)
    var isUnsupportedFeature: Boolean by BooleanArg(key = Const.IS_UNSUPPORTED_FEATURE)
    private var shouldAuthenticate: Boolean by BooleanArg(key = Const.AUTHENTICATE)
    var title: String? by NullableStringArg(key = Const.ACTION_BAR_TITLE)
    var url: String? by NullableStringArg(key = Const.INTERNAL_URL)

    /*
     * Our router has some catch-all routes which open the UnsupportedFeatureFragment for urls that match the user's
     * domain but don't match any other internal routes. In some cases, such as viewing an HTML file preview, we need to
     * disable this behavior to ensure that content loads in the WebView instead of the app.
     */
    val allowUnsupportedRouting by BooleanArg(key = Const.ALLOW_UNSUPPORTED_ROUTING, default = true)

    var downloadUrl: String? = null
    var downloadFilename: String? = null

    private var externalLTIUrl: String? = null

    var shouldRouteInternally: Boolean by BooleanArg(key = SHOULD_ROUTE_INTERNALLY, default = true)
    private var shouldLoadUrl = true
    private var sessionAuthJob: Job? = null
    private var shouldCloseFragment = false

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Notify that we have action bar items
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_webview, container, false) ?: return null

        with(rootView) {
            canvasWebView.settings.loadWithOverviewMode = true
            canvasWebView.setInitialScale(100)
            canvasWebView.settings.userAgentString = ApiPrefs.userAgent

            canvasWebView.canvasWebChromeClientCallback = CanvasWebView.CanvasWebChromeClientCallback { _, newProgress ->
                if (newProgress == 100) {
                    webViewLoading?.setGone()
                }
            }

            // Open a new page to view some types of embedded video content
            canvasWebView.addVideoClient(requireActivity())
            canvasWebView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                    openMedia(canvasContext, url, filename)
                }

                override fun onPageFinishedCallback(webView: WebView, url: String) {
                    webViewLoading?.setGone()
                }

                override fun onPageStartedCallback(webView: WebView, url: String) {
                    webViewLoading?.setVisible()
                }

                override fun canRouteInternallyDelegate(url: String): Boolean {
                    if (activity == null) return false
                    return shouldRouteInternally && !isUnsupportedFeature && RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false, allowUnsupportedRouting)
                }

                override fun routeInternallyCallback(url: String) {
                    if (activity == null) return
                    RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true, allowUnsupportedRouting)
                }
            }
            canvasWebView.setMediaDownloadCallback { _, url, filename ->
                downloadUrl = url
                downloadFilename = filename

                if (PermissionUtils.hasPermissions(activity!!, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                    downloadFile()
                } else {
                    requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE)
                }
            }
            canvasWebView?.restoreState(savedInstanceState)
        }

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (shouldLoadUrl) {
            if (url?.isNotBlank() == true) loadUrl(url)
            else if (html?.isNotBlank() == true) loadUrl(html)
        }

        val hideToolbar = arguments?.getBoolean(InternalWebViewActivity.HIDE_TOOLBAR, false) ?: false
        toolbar.setVisible(!hideToolbar)

        if (isLTITool) {
            setupToolbarMenu(toolbar, R.menu.menu_internal_webview)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                downloadFile()
            }
        }
    }

    private fun downloadFile() {
        if (downloadFilename != null && downloadUrl != null) {
            FileDownloadJobIntentService.scheduleDownloadJob(requireContext(), downloadFilename!!, downloadUrl!!)
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        canvasWebView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        canvasWebView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        canvasWebView?.saveState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sessionAuthJob?.cancel()
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun applyTheme() {
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    override fun title(): String = title ?: canvasContext.name ?: ""

    //endregion

    //region Parent Fragment Overrides
    override fun handleBackPressed() = canvasWebView?.handleGoBack() ?: false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.launchExternalWeb) {
            if (externalLTIUrl.isValid() && !assignmentLtiUrl.isValid()) {
                sessionAuthJob = tryWeave {
                    val result = awaitApi<AuthenticatedSession> { OAuthManager.getAuthenticatedSession(externalLTIUrl!!, it) }.sessionUrl
                    launchIntent(result)
                } catch {
                    toast(R.string.utils_unableToViewInBrowser)
                }
            } else if (assignmentLtiUrl.isValid()) {
                sessionAuthJob = tryWeave {
                    val result = awaitApi<LTITool> { SubmissionManager.getLtiFromAuthenticationUrl(assignmentLtiUrl!!, it, true) }.url
                    launchIntent(result!!)
                } catch {
                    toast(R.string.utils_unableToViewInBrowser)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //endregion

    //region Bus Events
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBackStackChangedEvent(event: OnBackStackChangedEvent) {
        event.get { clazz ->
            // We want to check for Modules as well, since the fragments within the viewpager won't show up as top level
            if (InternalWebviewFragment::class.java.isAssignableFrom(clazz)
                    || CourseModuleProgressionFragment::class.java.isAssignableFrom(clazz)) {
                canvasWebView.onResume()
            } else {
                canvasWebView.onPause()
            }
        }
    }
    //endregion

    //region Functionality
    fun canGoBack(): Boolean {
        return if (!shouldCloseFragment) {
            canvasWebView?.canGoBack() ?: false
        } else false
    }

    fun getCanvasLoading(): ProgressBar? = webViewLoading
    fun getCanvasWebView(): CanvasWebView? = canvasWebView
    fun getIsUnsupportedFeature(): Boolean = isUnsupportedFeature
    private fun getReferer(): Map<String, String> = mutableMapOf(Pair("Referer", ApiPrefs.domain))

    private fun launchIntent(result: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
        // Make sure we can handle the intent
        if (intent.resolveActivity(this@InternalWebviewFragment.requireContext().packageManager) != null) {
            this@InternalWebviewFragment.startActivity(intent)
        } else {
            toast(R.string.utils_unableToViewInBrowser)
        }
    }

    // BaseURL is set as Referer. Referer needed for some vimeo videos to play
    fun loadHtml(html: String) {
        canvasWebView?.loadDataWithBaseURL(ApiPrefs.fullDomain,
                FileUtils.getAssetsFile(requireContext(), "html_wrapper.html").replace("{\$CONTENT$}", html, ignoreCase = false),
                "text/html", "UTF-8", null)
    }

    fun loadHtml(data: String, mimeType: String, encoding: String, historyUrl: String?) {
        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        canvasWebView?.loadDataWithBaseURL(CanvasWebView.getReferrer(), data, mimeType, encoding, historyUrl)
    }

    fun loadUrl(targetUrl: String?) {
        if (!html.isNullOrBlank()) {
            loadHtml(html!!)
            return
        }

        if (isLTITool) {
            externalLTIUrl = targetUrl
        }

        url = targetUrl
        if (url?.isNotBlank() == true && isAdded) {
            sessionAuthJob = weave {
                if (ApiPrefs.domain in url!! && shouldAuthenticate) {
                    try {
                        // Get an authenticated session so the user doesn't have to log in
                        url = awaitApi<AuthenticatedSession> { OAuthManager.getAuthenticatedSession(url!!, it) }.sessionUrl
                    } catch (e: StatusCallbackError) {
                    }
                }

                if (getIsUnsupportedFeature()) {
                    // Add query param
                    url = Uri.parse(url).buildUpon()
                            .appendQueryParameter("embedded", "1")
                            .appendQueryParameter("display", "borderless")
                            .build().toString()
                }

                canvasWebView?.loadUrl(url, getReferer())
            }
        }
    }

    protected fun populateWebView(content: String) = populateWebView(content, null)
    protected fun populateWebView(content: String, title: String?) = canvasWebView?.loadHtml(content, title)

    fun setShouldLoadUrl(shouldLoadUrl: Boolean) {
        this.shouldLoadUrl = shouldLoadUrl
    }

    //endregion

    companion object {
        internal const val SHOULD_ROUTE_INTERNALLY = "shouldRouteInternally"

        fun newInstance(route: Route): InternalWebviewFragment? {
            return InternalWebviewFragment().withArgs(route.argsWithContext)
        }

        /*
     * Do not use this method if the InternalWebViewFragment has the ActionBar DropDownMenu visible,
     * Otherwise the canvasContext won't be saved and will cause issues with the dropdown navigation
     * -dw
     */
        fun makeRoute(url: String, title: String, authenticate: Boolean, html: String): Route =
                Route(InternalWebviewFragment::class.java, CanvasContext.emptyUserContext(),
                        CanvasContext.emptyUserContext().makeBundle().apply {
                            putString(Const.INTERNAL_URL, url)
                            putString(Const.ACTION_BAR_TITLE, title)
                            putBoolean(Const.AUTHENTICATE, authenticate)
                            putString(Const.HTML, html)
                        })

        fun makeRoute(canvasContext: CanvasContext, url: String?, title: String?, authenticate: Boolean, html: String): Route =
                Route(InternalWebviewFragment::class.java, canvasContext,
                        canvasContext.makeBundle().apply {
                            putString(Const.INTERNAL_URL, url)
                            putString(Const.ACTION_BAR_TITLE, title)
                            putBoolean(Const.AUTHENTICATE, authenticate)
                            putString(Const.HTML, html)
                        })

        fun makeRoute(canvasContext: CanvasContext, url: String, title: String, authenticate: Boolean, isUnsupportedFeature: Boolean, isLTITool: Boolean): Route =
                Route(InternalWebviewFragment::class.java, canvasContext,
                        canvasContext.makeBundle().apply {
                            putString(Const.INTERNAL_URL, url)
                            putString(Const.ACTION_BAR_TITLE, title)
                            putBoolean(Const.AUTHENTICATE, authenticate)
                            putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
                            putBoolean(Const.IS_EXTERNAL_TOOL, isLTITool)
                        })

        fun makeRoute(url: String, title: String, authenticate: Boolean, isUnsupportedFeature: Boolean, isLTITool: Boolean): Bundle {
            val extras = Bundle()
            extras.putString(Const.INTERNAL_URL, url)
            extras.putString(Const.ACTION_BAR_TITLE, title)
            extras.putBoolean(Const.AUTHENTICATE, authenticate)
            extras.putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
            extras.putBoolean(Const.IS_EXTERNAL_TOOL, isLTITool)
            return extras
        }

        fun makeBundle(canvasContext: CanvasContext, url: String, title: String, authenticate: Boolean, isUnsupportedFeature: Boolean, isLTITool: Boolean): Bundle {
            return Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putString(Const.INTERNAL_URL, url)
                putString(Const.ACTION_BAR_TITLE, title)
                putBoolean(Const.AUTHENTICATE, authenticate)
                putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
                putBoolean(Const.IS_EXTERNAL_TOOL, isLTITool)
            }
        }

        fun makeRoute(canvasContext: CanvasContext, url: String, title: String, authenticate: Boolean, isUnsupportedFeature: Boolean, isLTITool: Boolean, ltiUrl: String): Route =
                Route(InternalWebviewFragment::class.java, canvasContext,
                        canvasContext.makeBundle().apply {
                            putString(Const.INTERNAL_URL, url)
                            putString(Const.ACTION_BAR_TITLE, title)
                            putBoolean(Const.AUTHENTICATE, authenticate)
                            putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
                            putBoolean(Const.IS_EXTERNAL_TOOL, isLTITool)
                            putString(Const.API_URL, ltiUrl)
                        })

        fun makeRoute(canvasContext: CanvasContext, url: String, title: String, authenticate: Boolean): Route =
                Route(InternalWebviewFragment::class.java, canvasContext,
                        canvasContext.makeBundle().apply {
                            putString(Const.INTERNAL_URL, url)
                            putBoolean(Const.AUTHENTICATE, authenticate)
                            putString(Const.ACTION_BAR_TITLE, title)
                        })

        fun makeRoute(canvasContext: CanvasContext, url: String, authenticate: Boolean): Route =
                Route(InternalWebviewFragment::class.java, canvasContext,
                        canvasContext.makeBundle().apply {
                            putString(Const.INTERNAL_URL, url)
                            putBoolean(Const.AUTHENTICATE, authenticate)
                        })

        fun makeRoute(
            canvasContext: CanvasContext,
            url: String,
            authenticate: Boolean,
            isUnsupportedFeature: Boolean,
            shouldRouteInternally: Boolean = true,
            allowUnsupportedRouting: Boolean = true
        ): Route =
                Route(InternalWebviewFragment::class.java, canvasContext,
                        canvasContext.makeBundle().apply {
                            putString(Const.INTERNAL_URL, url)
                            putBoolean(Const.AUTHENTICATE, authenticate)
                            putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
                            putBoolean(SHOULD_ROUTE_INTERNALLY, shouldRouteInternally)
                            putBoolean(Const.ALLOW_UNSUPPORTED_ROUTING, allowUnsupportedRouting)
                        })

        fun makeRoute(bundle: Bundle) = Route(InternalWebviewFragment::class.java, null, bundle)

        fun makeRouteHTML(canvasContext: CanvasContext, html: String): Route = makeRoute(canvasContext, null, null, false, html)

        fun loadInternalWebView(context: Context?, route: Route) {
            if (context == null) {
                Logger.e("loadInternalWebView could not complete, context is null")
                return
            }
            RouteMatcher.route(context, route)
        }
    }
}


