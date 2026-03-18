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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity
import androidx.work.WorkManager
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.PageViewUrlParam
import com.instructure.canvasapi2.utils.weave.StatusCallbackError
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.binding.viewBinding
import com.instructure.pandautils.features.file.download.FileDownloadWorker
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.NullableStringArg
import com.instructure.pandautils.utils.OnBackStackChangedEvent
import com.instructure.pandautils.utils.ParcelableArg
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.ViewStyler
import com.instructure.pandautils.utils.applyBottomSystemBarInsets
import com.instructure.pandautils.utils.applyTopSystemBarInsets
import com.instructure.pandautils.utils.argsWithContext
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.utils.setGone
import com.instructure.pandautils.utils.setVisible
import com.instructure.pandautils.utils.setupAsBackButton
import com.instructure.pandautils.utils.toast
import com.instructure.pandautils.utils.withArgs
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.databinding.FragmentWebviewBinding
import com.instructure.student.features.modules.progression.CourseModuleProgressionFragment
import com.instructure.student.router.RouteMatcher
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

open class InternalWebviewFragment : ParentFragment() {

    val binding by viewBinding(FragmentWebviewBinding::bind)

    @get:PageViewUrlParam("canvasContext")
    var canvasContext: CanvasContext by ParcelableArg(
        default = CanvasContext.emptyUserContext(),
        key = Const.CANVAS_CONTEXT
    )
    var assignmentLtiUrl: String? by NullableStringArg(key = Const.API_URL)    // If we're coming from an lti assignment we need the original assignment url, not the sessionless one
    var html: String? by NullableStringArg(key = Const.HTML)
    var isLTITool: Boolean by BooleanArg(key = Const.IS_EXTERNAL_TOOL)
    var isUnsupportedFeature: Boolean by BooleanArg(key = Const.IS_UNSUPPORTED_FEATURE)
    private var shouldAuthenticate: Boolean by BooleanArg(key = Const.AUTHENTICATE)
    var title: String? by NullableStringArg(key = Const.ACTION_BAR_TITLE)
    var url: String? by NullableStringArg(key = Const.INTERNAL_URL)
    var allowRoutingTheSameUrlInternally: Boolean by BooleanArg(
        default = true,
        key = ALLOW_ROUTING_THE_SAME_URL_INTERNALLY
    )
    var allowRoutingToLogin: Boolean by BooleanArg(default = true, key = ALLOW_ROUTING_TO_LOGIN)
    var allowEmbedRouting: Boolean by BooleanArg(default = true, key = ALLOW_EMBED_ROUTING)
    var enableAlgorithmicDarkening: Boolean by BooleanArg(default = false, key = ENABLE_ALGORITHMIC_DARKENING)

    var hideToolbar: Boolean by BooleanArg(key = Const.HIDDEN_TOOLBAR)

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
    private var shouldCloseFragment: Boolean by BooleanArg(key = SHOULD_CLOSE_FRAGMENT, default = false)

    //region Fragment Lifecycle Overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Notify that we have action bar items
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_webview, container, false) ?: return null
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        canvasWebViewWrapper.webView.settings.loadWithOverviewMode = true
        canvasWebViewWrapper.webView.setInitialScale(100)
        webViewLoading.setVisible(true)
        if (enableAlgorithmicDarkening) canvasWebViewWrapper.webView.enableAlgorithmicDarkening()

        canvasWebViewWrapper.webView.canvasWebChromeClientCallback =
            object : CanvasWebView.CanvasWebChromeClientCallback {
                override fun onProgressChangedCallback(view: WebView?, newProgress: Int) {
                    if (newProgress == 100) {
                        webViewLoading.setGone()
                    }
                }

            }

        // Open a new page to view some types of embedded video content
        canvasWebViewWrapper.webView.addVideoClient(requireActivity())
        canvasWebViewWrapper.webView.canvasWebViewClientCallback =
            object : CanvasWebView.CanvasWebViewClientCallback {
                override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                    openMedia(canvasContext, url, filename, null)
                }

                override fun onPageFinishedCallback(webView: WebView, url: String) {
                    webViewLoading.setGone()
                }

                override fun onPageStartedCallback(webView: WebView, url: String) {
                    webViewLoading.setVisible()
                }

                override fun canRouteInternallyDelegate(url: String): Boolean {
                    if (activity == null) return false
                    return shouldRouteInternally
                            && shouldRouteToLogin(url)
                            && shouldRouteEmbedded(url)
                            && shouldRouteIfUrlIsTheSame(url)
                            && !isUnsupportedFeature
                            && RouteMatcher.canRouteInternally(
                        requireActivity(),
                        url,
                        ApiPrefs.domain,
                        false,
                        allowUnsupportedRouting
                    )
                }

                // We currently have a flaw in our routing implementation what causes that the when the WebView loads an URL what can be routed internally
                // will be routed to the same screen again. To prevent this we check if the loaded url is the same as the url what we opened the Fragment with.
                // We need to check if it contains the loaded url, because when authenticating it can contain additional params, so they won't be exactly the same.
                private fun shouldRouteIfUrlIsTheSame(url: String): Boolean {
                    val sameUrl = this@InternalWebviewFragment.url?.contains(url) ?: false
                    return !sameUrl || allowRoutingTheSameUrlInternally
                }

                private fun shouldRouteToLogin(url: String?): Boolean {
                    return if (url?.contains("login") == true) {
                        allowRoutingToLogin
                    } else {
                        true
                    }
                }

                private fun shouldRouteEmbedded(url: String?): Boolean {
                    return if (url?.contains("embed=true") == true) {
                        allowEmbedRouting
                    } else {
                        true
                    }
                }

                override fun routeInternallyCallback(url: String) {
                    if (activity == null) return
                    RouteMatcher.canRouteInternally(
                        requireActivity(),
                        url,
                        ApiPrefs.domain,
                        true,
                        allowUnsupportedRouting
                    )
                }
            }
        canvasWebViewWrapper.webView.setMediaDownloadCallback(object : CanvasWebView.MediaDownloadCallback {
            override fun downloadMedia(mime: String?, url: String?, filename: String?) {
                downloadUrl = url
                downloadFilename = filename

                if (PermissionUtils.hasPermissions(activity!!, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                    downloadFile()
                } else {
                    requestPermissions(
                        PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE),
                        PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE
                    )
                }
            }

            override fun downloadInternalMedia(mime: String?, url: String?, filename: String?) {
                this@InternalWebviewFragment.downloadInternalMedia(mime, url, filename)
            }

        })

        canvasWebViewWrapper.applyBottomSystemBarInsets()

        if (savedInstanceState != null) {
            canvasWebViewWrapper?.webView?.restoreState(savedInstanceState)
        }
    }

    open fun downloadInternalMedia(mime: String?, url: String?, filename: String?) = Unit

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (shouldLoadUrl) {
            if (url?.isNotBlank() == true) loadUrl(url)
            else if (html?.isNotBlank() == true) loadUrl(html)
        }

        binding.toolbar.setVisible(!hideToolbar)

        if (isLTITool) {
            setupToolbarMenu(binding.toolbar, R.menu.menu_internal_webview)
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
        WorkManager.getInstance(requireContext()).enqueue(FileDownloadWorker.createOneTimeWorkRequest(downloadFilename.orEmpty(), downloadUrl.orEmpty()))
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        binding.canvasWebViewWrapper.webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.canvasWebViewWrapper.webView.onPause()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.canvasWebViewWrapper.webView.saveState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sessionAuthJob?.cancel()
    }
    //endregion

    //region Fragment Interaction Overrides
    override fun applyTheme() = with(binding) {
        toolbar.title = title()
        toolbar.setupAsBackButton(this@InternalWebviewFragment)
        toolbar.applyTopSystemBarInsets()
        if (canvasContext.type != CanvasContext.Type.COURSE && canvasContext.type != CanvasContext.Type.GROUP) {
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, ThemePrefs.primaryColor, ThemePrefs.primaryTextColor)
        } else {
            ViewStyler.themeToolbarColored(requireActivity(), toolbar, canvasContext)
        }
    }

    override fun title(): String = title ?: canvasContext.name ?: ""

    //endregion

    //region Parent Fragment Overrides
    override fun handleBackPressed() = if (shouldCloseFragment) false else binding.canvasWebViewWrapper.webView.handleGoBack()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.launchExternalWeb) {
            if (externalLTIUrl.isValid() && !assignmentLtiUrl.isValid()) {
                sessionAuthJob = tryWeave {
                    val result = awaitApi<AuthenticatedSession> {
                        OAuthManager.getAuthenticatedSession(
                            externalLTIUrl!!,
                            it
                        )
                    }.sessionUrl
                    launchIntent(result)
                } catch {
                    toast(R.string.utils_unableToViewInBrowser)
                }
            } else if (assignmentLtiUrl.isValid()) {
                sessionAuthJob = tryWeave {
                    val result = awaitApi<LTITool> {
                        SubmissionManager.getLtiFromAuthenticationUrl(
                            assignmentLtiUrl!!,
                            it,
                            true
                        )
                    }.url
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
            if (clazz != null && (InternalWebviewFragment::class.java.isAssignableFrom(clazz)
                        || CourseModuleProgressionFragment::class.java.isAssignableFrom(clazz))
            ) {
                binding.canvasWebViewWrapper.webView.onResume()
            } else {
                binding.canvasWebViewWrapper.webView.onPause()
            }
        }
    }
    //endregion

    //region Functionality
    fun canGoBack(): Boolean {
        return if (!shouldCloseFragment) {
            binding.canvasWebViewWrapper.webView.canGoBack()
        } else false
    }

    fun getCanvasLoading(): ProgressBar? = if (view != null) binding.webViewLoading else null
    fun getCanvasWebView(): CanvasWebView? = if (view != null) binding.canvasWebViewWrapper.webView else null
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

    fun loadHtml(data: String, mimeType: String, encoding: String, historyUrl: String?) {
        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        binding.canvasWebViewWrapper.webView.loadDataWithBaseURL(
            CanvasWebView.getReferrer(),
            data,
            mimeType,
            encoding,
            historyUrl
        )
    }

    fun loadUrl(targetUrl: String?) {
        if (!html.isNullOrBlank()) {
            binding.canvasWebViewWrapper.loadHtml(html!!, title ?: "")
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
                        url = awaitApi<AuthenticatedSession> {
                            OAuthManager.getAuthenticatedSession(
                                url!!,
                                it
                            )
                        }.sessionUrl
                    } catch (e: StatusCallbackError) {
                        e.printStackTrace()
                    }
                }

                if (getIsUnsupportedFeature()) {
                    // Add query param
                    url = Uri.parse(url).buildUpon()
                        .appendQueryParameter("embedded", "1")
                        .appendQueryParameter("display", "borderless")
                        .build().toString()
                }

                if (view != null) binding.canvasWebViewWrapper.webView.loadUrl(url!!, getReferer())
            }
        }
    }

    protected fun populateWebView(content: String) = populateWebView(content, null)
    protected fun populateWebView(content: String, title: String?) =
        binding.canvasWebViewWrapper.loadHtml(content, title)

    fun setShouldLoadUrl(shouldLoadUrl: Boolean) {
        this.shouldLoadUrl = shouldLoadUrl
    }

    //endregion

    companion object {
        internal const val SHOULD_ROUTE_INTERNALLY = "shouldRouteInternally"
        internal const val SHOULD_CLOSE_FRAGMENT = "shouldCloseFragment"
        internal const val ENABLE_ALGORITHMIC_DARKENING = "enableAlgorithmicDarkening"
        const val ALLOW_ROUTING_THE_SAME_URL_INTERNALLY = "allowRoutingTheSameUrlInternally"
        const val ALLOW_ROUTING_TO_LOGIN = "allowRoutingToLogin"
        const val ALLOW_EMBED_ROUTING = "allowEmbedRouting"

        fun newInstance(route: Route): InternalWebviewFragment {
            return InternalWebviewFragment().withArgs(route.argsWithContext)
        }

        /*
     * Do not use this method if the InternalWebViewFragment has the ActionBar DropDownMenu visible,
     * Otherwise the canvasContext won't be saved and will cause issues with the dropdown navigation
     * -dw
     */
        fun makeRoute(
            url: String,
            title: String,
            authenticate: Boolean,
            html: String,
            allowUnsupportedRouting: Boolean = true
        ): Route =
            Route(InternalWebviewFragment::class.java, CanvasContext.emptyUserContext(),
                CanvasContext.emptyUserContext().makeBundle().apply {
                    putString(Const.INTERNAL_URL, url)
                    putString(Const.ACTION_BAR_TITLE, title)
                    putBoolean(Const.AUTHENTICATE, authenticate)
                    putString(Const.HTML, html)
                    putBoolean(Const.ALLOW_UNSUPPORTED_ROUTING, allowUnsupportedRouting)
                })

        fun makeRoute(
            canvasContext: CanvasContext,
            url: String?,
            title: String?,
            authenticate: Boolean,
            html: String
        ): Route =
            Route(InternalWebviewFragment::class.java, canvasContext,
                canvasContext.makeBundle().apply {
                    putString(Const.INTERNAL_URL, url)
                    putString(Const.ACTION_BAR_TITLE, title)
                    putBoolean(Const.AUTHENTICATE, authenticate)
                    putString(Const.HTML, html)
                })

        fun makeRoute(
            canvasContext: CanvasContext,
            url: String,
            title: String,
            authenticate: Boolean,
            isUnsupportedFeature: Boolean,
            isLTITool: Boolean
        ): Route =
            Route(InternalWebviewFragment::class.java, canvasContext,
                canvasContext.makeBundle().apply {
                    putString(Const.INTERNAL_URL, url)
                    putString(Const.ACTION_BAR_TITLE, title)
                    putBoolean(Const.AUTHENTICATE, authenticate)
                    putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
                    putBoolean(Const.IS_EXTERNAL_TOOL, isLTITool)
                })

        fun makeRoute(
            url: String,
            title: String,
            authenticate: Boolean,
            isUnsupportedFeature: Boolean,
            isLTITool: Boolean
        ): Bundle {
            val extras = Bundle()
            extras.putString(Const.INTERNAL_URL, url)
            extras.putString(Const.ACTION_BAR_TITLE, title)
            extras.putBoolean(Const.AUTHENTICATE, authenticate)
            extras.putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
            extras.putBoolean(Const.IS_EXTERNAL_TOOL, isLTITool)
            return extras
        }

        fun makeBundle(
            canvasContext: CanvasContext,
            url: String,
            title: String,
            authenticate: Boolean,
            isUnsupportedFeature: Boolean,
            isLTITool: Boolean
        ): Bundle {
            return Bundle().apply {
                putParcelable(Const.CANVAS_CONTEXT, canvasContext)
                putString(Const.INTERNAL_URL, url)
                putString(Const.ACTION_BAR_TITLE, title)
                putBoolean(Const.AUTHENTICATE, authenticate)
                putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
                putBoolean(Const.IS_EXTERNAL_TOOL, isLTITool)
            }
        }

        fun makeRoute(
            canvasContext: CanvasContext,
            url: String,
            title: String,
            authenticate: Boolean,
            isUnsupportedFeature: Boolean,
            isLTITool: Boolean,
            ltiUrl: String
        ): Route =
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
            hideToolbar: Boolean,
            allowRoutingTheSameUrlInternally: Boolean,
            shouldRouteToLogin: Boolean,
            allowEmbedRouting: Boolean,
            isUnsupportedFeature: Boolean
        ): Route =
            Route(InternalWebviewFragment::class.java, canvasContext,
                canvasContext.makeBundle().apply {
                    putString(Const.INTERNAL_URL, url)
                    putBoolean(Const.AUTHENTICATE, authenticate)
                    putBoolean(Const.HIDDEN_TOOLBAR, hideToolbar)
                    putBoolean(ALLOW_ROUTING_THE_SAME_URL_INTERNALLY, allowRoutingTheSameUrlInternally)
                    putBoolean(ALLOW_ROUTING_TO_LOGIN, shouldRouteToLogin)
                    putBoolean(ALLOW_EMBED_ROUTING, allowEmbedRouting)
                    putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
                })

        fun makeRoute(
            canvasContext: CanvasContext,
            url: String,
            authenticate: Boolean,
            isUnsupportedFeature: Boolean,
            shouldRouteInternally: Boolean = true,
            allowUnsupportedRouting: Boolean = true,
            allowRoutingTheSameUrlInternally: Boolean = true
        ): Route = Route(InternalWebviewFragment::class.java, canvasContext,
            canvasContext.makeBundle().apply {
                putString(Const.INTERNAL_URL, url)
                putBoolean(Const.AUTHENTICATE, authenticate)
                putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature)
                putBoolean(SHOULD_ROUTE_INTERNALLY, shouldRouteInternally)
                putBoolean(Const.ALLOW_UNSUPPORTED_ROUTING, allowUnsupportedRouting)
                putBoolean(ALLOW_ROUTING_THE_SAME_URL_INTERNALLY, allowRoutingTheSameUrlInternally)
            })

        fun makeRoute(bundle: Bundle) = Route(InternalWebviewFragment::class.java, null, bundle)

        fun makeRouteHTML(canvasContext: CanvasContext, html: String): Route =
            makeRoute(canvasContext, null, null, false, html)

        fun loadInternalWebView(activity: FragmentActivity?, route: Route) {
            if (activity == null) {
                Logger.e("loadInternalWebView could not complete, context is null")
                return
            }
            RouteMatcher.route(activity, route)
        }
    }
}


