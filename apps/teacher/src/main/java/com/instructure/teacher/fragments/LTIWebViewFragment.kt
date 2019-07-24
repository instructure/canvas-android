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
import android.text.Html
import android.text.SpannedString
import android.view.View
import android.widget.Toast
import com.instructure.canvasapi2.managers.SubmissionManager
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.models.LTITool
import com.instructure.canvasapi2.models.Tab
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.canvasapi2.utils.HttpHelper
import com.instructure.canvasapi2.utils.Logger
import com.instructure.canvasapi2.utils.weave.awaitApi
import com.instructure.canvasapi2.utils.weave.catch
import com.instructure.canvasapi2.utils.weave.tryWeave
import com.instructure.canvasapi2.utils.weave.weave
import com.instructure.pandautils.utils.BooleanArg
import com.instructure.pandautils.utils.NullableParcelableArg
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.teacher.R
import com.instructure.teacher.utils.setupMenu
import kotlinx.android.synthetic.main.fragment_internal_webview.*
import kotlinx.coroutines.Job
import org.json.JSONObject

class LTIWebViewFragment : InternalWebViewFragment() {

    var ltiUrl: String by StringArg()
    var ltiTab: Tab? by NullableParcelableArg()
    var sessionLessLaunch: Boolean by BooleanArg()
    private var skipReload: Boolean = false
    var hideToolbar: Boolean by BooleanArg()
    private var externalUrlToLoad: String? = null

    private var ltiUrlLaunchJob: Job? = null
    private var sessionAuthJob: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShouldRouteInternally(false)
        title = if(title.isNotBlank()) title else ltiUrl
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Setup the toolbar here so the LTI fragment uses this menu instead of InternalWebViewFragment's
        toolbar.setupMenu(R.menu.menu_internal_webview) {
            if(ltiTab != null) {
                // Coming from a tab that is an lti tool
                sessionAuthJob = tryWeave {

                    val result = inBackground {
                        // We have to get a new sessionless url
                        getLTIUrlForTab(ltiTab as Tab)
                    }
                    launchIntent(result)
                } catch {
                    Toast.makeText(this@LTIWebViewFragment.requireContext(), R.string.no_apps, Toast.LENGTH_SHORT).show()
                }
            } else {
                sessionAuthJob = tryWeave {
                    if (ApiPrefs.domain in ltiUrl) {
                        // If we have an externalUrlToLoad they've already been authenticated
                        if(externalUrlToLoad != null && sessionLessLaunch && !ltiUrl.contains("api/v1/")) {
                            getSessionlessLtiUrl(ApiPrefs.fullDomain + "/api/v1/accounts/self/external_tools/sessionless_launch?url=" + ltiUrl, true)
                        } else {
                            // Get an authenticated session so the user doesn't have to log in
                            val result = awaitApi<LTITool> { SubmissionManager.getLtiFromAuthenticationUrl(ltiUrl, it, true) }.url

                            launchIntent(result)
                        }
                    }
                } catch  {
                    Toast.makeText(this@LTIWebViewFragment.requireContext(), R.string.no_apps, Toast.LENGTH_SHORT).show()
                }
            }
        }

        toolbar?.visibility = if(hideToolbar) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // After we request permissions to access files (like in Studio) this WebView will reload and call onResume again. In order to not break any other LTI things, this flag should skip
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
                    // Modify the url
                    if (ltiUrl.startsWith("canvas-courses://")) {
                        ltiUrl = ltiUrl.replaceFirst("canvas-courses".toRegex(), ApiPrefs.protocol)
                    }
                    if (ltiUrl.startsWith("canvas-teacher://")) {
                        ltiUrl = ltiUrl.replaceFirst("canvas-teacher".toRegex(), ApiPrefs.protocol)
                    }

                    if (sessionLessLaunch) {
                        if (ltiUrl.contains("api/v1/")) {
                            getSessionlessLtiUrl(ltiUrl, false)
                        } else {
                            getSessionlessLtiUrl(ApiPrefs.fullDomain + "/api/v1/accounts/self/external_tools/sessionless_launch?url=" + ltiUrl, false)
                        }
                    } else {
                        externalUrlToLoad = ltiUrl

                        loadUrl(Uri.parse(ltiUrl).buildUpon()
                                .appendQueryParameter("display", "borderless")
                                .appendQueryParameter("platform", "android")
                                .build()
                                .toString())
                    }
                } else if (ltiUrl.isNotBlank()) {
                    getSessionlessLtiUrl(ltiUrl, false)
                } else {
                    loadDisplayError()
                }
            }
        } catch (e: Exception) {
            // If it gets here we're in trouble and won't know what the tab is, so just display an error message
            loadDisplayError()
        }

        canvasWebView?.setCanvasWebChromeClientShowFilePickerCallback(object : CanvasWebView.VideoPickerCallback {
            override fun requestStartActivityForResult(intent: Intent, requestCode: Int) {
                startActivityForResult(intent, requestCode)
            }

            override fun permissionsGranted(): Boolean {
                return if (PermissionUtils.hasPermissions(requireActivity(), *PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE))) {
                    true
                } else {
                    requestFilePermissions()
                    false
                }
            }
        })
    }

    private fun getLtiUrl(ltiTab: Tab?) {
        if (ltiTab == null) {
            loadDisplayError()
            return
        }

        ltiUrlLaunchJob = weave {
            var result: String? = null
            inBackground {
                result = getLTIUrlForTab(ltiTab)
            }

            if (result != null) {
                val uri = Uri.parse(result).buildUpon()
                        .appendQueryParameter("display", "borderless")
                        .appendQueryParameter("platform", "android")
                        .build()
                externalUrlToLoad = uri.toString()
                loadUrl(uri.toString())
            } else {
                // Error
                loadDisplayError()
            }
        }
    }

    private fun getSessionlessLtiUrl(ltiUrl: String, loadExternally: Boolean) {
        ltiUrlLaunchJob = weave {
            var result: String? = null
            inBackground {
                result = getLTIUrl(ltiUrl)
            }

            if (result != null) {
                val uri = Uri.parse(result).buildUpon()
                        .appendQueryParameter("display", "borderless")
                        .appendQueryParameter("platform", "android")
                        .build()
                // Set the sessionless url here in case the user wants to use an external browser
                externalUrlToLoad = uri.toString()

                if (loadExternally) {
                    launchIntent(uri.toString())
                } else {
                    loadUrl(uri.toString())
                }
            } else {
                // Error
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

    override fun onHandleBackPressed(): Boolean {
        if (canGoBack()) {
            // This prevents a silly bug where the Studio WebView cannot go back far enough to pop it's fragment, but we also want to
            // be able to navigate within the Studio WebView.
            val webBackForwardList = canvasWebView?.copyBackForwardList()
            val historyUrl = webBackForwardList?.getItemAtIndex(webBackForwardList.currentIndex - 1)?.url
            if (historyUrl != null && (historyUrl.contains("external_tools/")
                    && historyUrl.contains("resource_selection")
                    || (historyUrl.contains("media-picker")))) {
                canvasWebView?.handleGoBack()
                return true
            }
        }
        return super.onHandleBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((canvasWebView?.handleOnActivityResult(requestCode, resultCode, data)) != true) {
            super.onActivityResult(requestCode, resultCode, data)
        }
        // We don't want to reload the LTI now, it may cancel the upload
        skipReload = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
            canvasWebView?.clearPickerCallback()
            Toast.makeText(requireContext(), R.string.pleaseTryAgain, Toast.LENGTH_SHORT).show()
            skipReload = true
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sessionAuthJob?.cancel()
        ltiUrlLaunchJob?.cancel()
    }

    private fun launchIntent(result: String?) {
        Logger.d("result url: $result")
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result))
        // Make sure we can handle the intent
        if (intent.resolveActivity(this@LTIWebViewFragment.requireContext().packageManager) != null) {
            this@LTIWebViewFragment.startActivity(intent)
        } else {
            Toast.makeText(this@LTIWebViewFragment.requireContext(), R.string.no_apps, Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestFilePermissions() {
        requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA), PermissionUtils.PERMISSION_REQUEST_CODE)
    }

    private fun getLTIUrlForTab(tab: Tab): String? {
        return getLTIUrl(tab.ltiUrl)
    }

    private fun getLTIUrl(url: String): String? {
        return try {
            val result = HttpHelper.externalHttpGet(requireContext(), url, true).responseBody
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

    companion object {
        private const val LTI_URL = "lti_url"
        private const val TAB = "tab"
        private const val SESSION_LESS = "session_less"
        private const val HIDE_TOOLBAR = "hideToolbar"

        @JvmStatic
        fun makeLTIBundle(ltiTab: Tab): Bundle {
            val args = Bundle()
            args.putParcelable(TAB, ltiTab)
            return args
        }

        @JvmStatic
        fun makeLTIBundle(ltiUrl: String, title: String, sessionLessLaunch: Boolean): Bundle {
            val args = Bundle()
            args.putString(LTI_URL, ltiUrl)
            args.putBoolean(SESSION_LESS, sessionLessLaunch)
            args.putString(TITLE, title)
            return args
        }

        @JvmStatic
        fun newInstance(args: Bundle) = LTIWebViewFragment().apply {
            ltiUrl = args.getString(LTI_URL, "")
            title = args.getString(TITLE, "")
            sessionLessLaunch = args.getBoolean(SESSION_LESS, false)
            if (args.containsKey(TAB)) {
                ltiTab = args.getParcelable(TAB)
            }
            hideToolbar = args.getBoolean(HIDE_TOOLBAR, false)
            setShouldAuthenticateUponLoad(args.getBoolean(AUTHENTICATE, false))
            setShouldLoadUrl(false)
        }

        @JvmStatic
        fun makeBundle(canvasContext: CanvasContext, url: String, title: String, sessionLessLaunch: Boolean, hideToolbar: Boolean): Bundle {
            val extras = createBundle(canvasContext)
            extras.putBoolean(AUTHENTICATE, false)
            extras.putString(LTI_URL, url)
            extras.putBoolean(HIDE_TOOLBAR, hideToolbar)
            extras.putBoolean(SESSION_LESS, sessionLessLaunch)
            extras.putString(TITLE, title)
            return extras
        }
    }
}
