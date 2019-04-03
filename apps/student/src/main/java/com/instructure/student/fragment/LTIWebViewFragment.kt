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
import android.text.Html
import android.text.SpannedString
import android.view.MenuItem
import android.widget.Toast
import com.instructure.canvasapi2.managers.OAuthManager
import com.instructure.canvasapi2.models.AuthenticatedSession
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.HttpHelper
import com.instructure.canvasapi2.utils.isValid
import com.instructure.canvasapi2.utils.pageview.BeforePageView
import com.instructure.canvasapi2.utils.pageview.PageView
import com.instructure.canvasapi2.utils.pageview.PageViewUrl
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.interactions.router.Route
import com.instructure.pandautils.utils.*
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.coroutines.Job
import org.json.JSONObject

@PageView
class LTIWebViewFragment : InternalWebviewFragment() {

    private var ltiUrl: String by StringArg(key = LTI_URL)
    private var ltiTab: Tab? by NullableParcelableArg(key = Const.TAB)
    private var sessionLessLaunch: Boolean by BooleanArg(key = Const.SESSIONLESS_LAUNCH)
    private var hideToolbar by BooleanArg(key = HIDE_TOOLBAR)

    private var externalUrlToLoad: String? = null
    private var skipReload: Boolean = false

    private var ltiUrlLaunchJob: Job? = null
    private var sessionAuthJob: Job? = null

    @Suppress("unused")
    @PageViewUrl
    private fun makePageViewUrl() =
        ltiTab?.externalUrl ?: ApiPrefs.fullDomain+canvasContext.toAPIString()+"/external_tools"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shouldRouteInternally = false
    }

    override fun title(): String {
        if (title.isNullOrBlank() && ltiUrl.isBlank()) {
            return ltiTab?.label ?: ""
        }
        return if (title.isNullOrBlank()) ltiUrl else title!!
    }

    override fun applyTheme() {
        toolbar.title = title()
        toolbar.setupAsBackButton(this)
        ViewStyler.themeToolbar(requireActivity(), toolbar, canvasContext)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbar.setVisible(!hideToolbar)
        setupToolbarMenu(toolbar, R.menu.menu_internal_webview)
    }

    override fun handleBackPressed(): Boolean {
        if (canGoBack()) {
            //This prevents users from going back to the launch url for LITs, that url shows an error message and is
            //only used to forward the user to the actual LTI.
            val webBackForwardList = getCanvasWebView()?.copyBackForwardList()
            val historyUrl = webBackForwardList?.getItemAtIndex(webBackForwardList.currentIndex - 1)?.url
            if (historyUrl != null && historyUrl.contains("external_tools/sessionless_launch")) {
                navigation?.popCurrentFragment()
                return true
            }
        }
        return super.handleBackPressed()
    }

    override fun onResume() {
        super.onResume()
        // After we request permissions to access files (like in Arc) this webview will reload and call onResume again. In order to not break any other LTI things, this flag should skip
        // reloading the url and keep the user where they are
        if (skipReload) {
            skipReload = false
            return
        }

        try {
            if (ltiTab != null) {
                getLtiUrl(ltiTab)
            } else {
                if (ltiUrl.isNotBlank()) {
                    //modify the url
                    if (ltiUrl.startsWith("canvas-courses://")) {
                        ltiUrl = ltiUrl.replaceFirst("canvas-courses".toRegex(), ApiPrefs.protocol)
                    }
                    if (ltiUrl.startsWith("canvas-student://")) {
                        ltiUrl = ltiUrl.replaceFirst("canvas-student".toRegex(), ApiPrefs.protocol)
                    }

                    if (sessionLessLaunch) {
                        getSessionlessLtiUrl(ApiPrefs.fullDomain + "/api/v1/accounts/self/external_tools/sessionless_launch?url=" + ltiUrl)
                    } else {
                        externalUrlToLoad = ltiUrl

                        loadUrl(
                            Uri.parse(ltiUrl).buildUpon()
                                .appendQueryParameter("display", "borderless")
                                .appendQueryParameter("platform", "android")
                                .build()
                                .toString()
                        )
                    }
                } else if (ltiUrl.isNotBlank()) {
                    getSessionlessLtiUrl(ltiUrl)
                } else {
                    loadDisplayError()
                }
            }
        } catch (e: Exception) {
            //if it gets here we're in trouble and won't know what the tab is, so just display an error message
            loadDisplayError()
        }

        getCanvasWebView()?.setCanvasWebChromeClientShowFilePickerCallback(object : CanvasWebView.VideoPickerCallback {
            override fun requestStartActivityForResult(intent: Intent, requestCode: Int) {
                startActivityForResult(intent, requestCode)
            }

            override fun permissionsGranted(): Boolean {
                return if (PermissionUtils.hasPermissions(requireActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                    true
                } else {
                    requestFilePermissions()
                    false
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.launchExternalWeb) {

            if (ltiTab != null) {
                //coming from a tab that is an lti tool
                sessionAuthJob = tryWeave {

                    val result = inBackground {
                        // we have to get a new sessionless url
                        getLTIUrlForTab(requireContext(), ltiTab as Tab)
                    }
                    launchIntent(result)
                } catch {
                    Toast.makeText(
                        this@LTIWebViewFragment.context,
                        R.string.utils_unableToViewInBrowser,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // coming from anywhere else
                var url = ltiUrl
                if (externalUrlToLoad.isValid()) {
                    url = externalUrlToLoad!!
                }

                sessionAuthJob = tryWeave {
                    if (ApiPrefs.domain in url) {
                        // Get an authenticated session so the user doesn't have to log in
                        url = awaitApi<AuthenticatedSession> {
                            OAuthManager.getAuthenticatedSession(
                                url,
                                it
                            )
                        }.sessionUrl

                        launchIntent(url)
                    }
                } catch {
                    Toast.makeText(
                        this@LTIWebViewFragment.context,
                        R.string.utils_unableToViewInBrowser,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun launchIntent(result: String?) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
        // Make sure we can handle the intent
        if (intent.resolveActivity(this@LTIWebViewFragment.requireContext().packageManager) != null) {
            this@LTIWebViewFragment.startActivity(intent)
        } else {
            Toast.makeText(this@LTIWebViewFragment.context, R.string.utils_unableToViewInBrowser, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getLtiUrl(ltiTab: Tab?) {
        if (ltiTab == null) {
            loadDisplayError()
            return
        }

        ltiUrlLaunchJob = weave {
            var result: String? = null
            inBackground {
                result = getLTIUrlForTab(requireContext(), ltiTab)
            }

            if (result != null) {
                val uri = Uri.parse(result).buildUpon()
                    .appendQueryParameter("display", "borderless")
                    .appendQueryParameter("platform", "android")
                    .build()
                externalUrlToLoad = uri.toString()
                loadUrl(uri.toString())
            } else {
                //error
                loadDisplayError()
            }
        }
    }

    private fun getSessionlessLtiUrl(ltiUrl: String) {
        ltiUrlLaunchJob = weave {
            var result: String? = null
            inBackground {
                result = getLTIUrl(requireContext(), ltiUrl)
            }

            if (result != null) {
                val uri = Uri.parse(result).buildUpon()
                    .appendQueryParameter("display", "borderless")
                    .appendQueryParameter("platform", "android")
                    .build()
                // Set the sessionless url here in case the user wants to use an external browser
                externalUrlToLoad = uri.toString()

                loadUrl(uri.toString())
            } else {
                //error
                loadDisplayError()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun loadDisplayError() {
        val spannedString = SpannedString(getString(R.string.errorOccurred))
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            loadHtml(Html.toHtml(spannedString, Html.FROM_HTML_MODE_LEGACY))
        } else {
            loadHtml(Html.toHtml(spannedString))
        }
    }

    @BeforePageView
    private fun getLTIUrlForTab(context: Context, tab: Tab): String? {
        return getLTIUrl(context, tab.ltiUrl)
    }

    private fun getLTIUrl(context: Context, url: String): String? {
        return try {
            val result = HttpHelper.externalHttpGet(context, url, true).responseBody
            var ltiUrl: String? = null
            if (result != null) {
                val ltiJSON = JSONObject(result)
                ltiUrl = ltiJSON.getString("url")
            }
            ltiUrl
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ltiUrlLaunchJob?.cancel()
        sessionAuthJob?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((getCanvasWebView()?.handleOnActivityResult(requestCode, resultCode, data)) != true) {
            super.onActivityResult(requestCode, resultCode, data)
        }
        // We don't want to reload the LTI now, it may cancel the upload
        skipReload = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
            getCanvasWebView()?.clearPickerCallback()
            Toast.makeText(requireContext(), R.string.pleaseTryAgain, Toast.LENGTH_SHORT).show()
            skipReload = true
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestFilePermissions() {
        requestPermissions(
            PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA),
            PermissionUtils.PERMISSION_REQUEST_CODE
        )
    }

    companion object {
        const val LTI_URL = "ltiUrl"
        const val HIDE_TOOLBAR = "hideToolbar"


        @JvmStatic
        fun makeLTIBundle(ltiUrl: String, title: String, sessionLessLaunch: Boolean): Bundle {
            val args = Bundle()
            args.putString(LTI_URL, ltiUrl)
            args.putBoolean(Const.SESSIONLESS_LAUNCH, sessionLessLaunch)
            args.putString(Const.ACTION_BAR_TITLE, title)
            return args
        }

        fun makeRoute(canvasContext: CanvasContext, ltiTab: Tab): Route {
            val bundle = Bundle().apply { putParcelable(Const.TAB, ltiTab) }
            return Route(LTIWebViewFragment::class.java, canvasContext, bundle)
        }

        fun makeRoute(
            canvasContext: CanvasContext,
            url: String,
            title: String? = null,
            sessionLessLaunch: Boolean = false,
            hideToolbar: Boolean = false
        ): Route {
            val bundle = Bundle().apply {
                putString(LTI_URL, url)
                putBoolean(HIDE_TOOLBAR, hideToolbar)
                putBoolean(Const.SESSIONLESS_LAUNCH, sessionLessLaunch)
                putString(Const.ACTION_BAR_TITLE, title) // For 'title' property in InternalWebViewFragment
            }
            return Route(LTIWebViewFragment::class.java, canvasContext, bundle)
        }

        fun validateRoute(route: Route): Boolean {
            route.canvasContext ?: return false
            return route.arguments.getParcelable<Tab>(Const.TAB) != null || route.arguments.getString(LTI_URL).isValid()
        }

        fun newInstance(route: Route): LTIWebViewFragment? {
            if (!validateRoute(route)) return null
            return LTIWebViewFragment().withArgs(route.argsWithContext)
        }
    }
}
