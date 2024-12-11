/*
 * Copyright (C) 2016 - present Instructure, Inc.
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

package com.instructure.student.fragment

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.instructure.canvasapi2.models.Assignment
import com.instructure.canvasapi2.models.CanvasContext
import com.instructure.canvasapi2.utils.ApiPrefs
import com.instructure.interactions.router.Route
import com.instructure.pandautils.analytics.SCREEN_VIEW_STUDIO_WEB_VIEW
import com.instructure.pandautils.analytics.ScreenView
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.LongArg
import com.instructure.pandautils.utils.PermissionUtils
import com.instructure.pandautils.utils.StringArg
import com.instructure.pandautils.utils.enableAlgorithmicDarkening
import com.instructure.pandautils.utils.getFragmentActivity
import com.instructure.pandautils.utils.makeBundle
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.student.R
import com.instructure.student.mobius.common.ui.SubmissionHelper
import com.instructure.student.router.RouteMatcher
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.text.StringEscapeUtils
import javax.inject.Inject

@ScreenView(SCREEN_VIEW_STUDIO_WEB_VIEW)
@AndroidEntryPoint
class StudioWebViewFragment : InternalWebviewFragment() {
    val assignmentId: Long by LongArg(key = Const.ASSIGNMENT_ID)
    val assignmentName: String by StringArg(key = Const.ASSIGNMENT_NAME)

    @Inject
    lateinit var submissionHelper: SubmissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shouldRouteInternally = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCanvasWebView()?.enableAlgorithmicDarkening()
        getCanvasWebView()?.addJavascriptInterface(JSInterface(), "HtmlViewer")

        getCanvasWebView()?.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
            override fun openMediaFromWebView(mime: String, url: String, filename: String) {
                openMedia(mime, url, filename, null, canvasContext)
            }

            override fun onPageFinishedCallback(webView: WebView, url: String) {
                getCanvasLoading()?.visibility = View.GONE

                // Check for a successful Studio submission
                if (url.contains("success/external_tool_dialog")) {
                    webView.loadUrl("javascript:HtmlViewer.showHTML" + "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');")
                }
            }

            override fun onPageStartedCallback(webView: WebView, url: String) {
                getCanvasLoading()?.visibility = View.VISIBLE
            }

            override fun canRouteInternallyDelegate(url: String): Boolean {
                return shouldRouteInternally && !getIsUnsupportedFeature() && RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, false)
            }

            override fun routeInternallyCallback(url: String) {
                RouteMatcher.canRouteInternally(requireActivity(), url, ApiPrefs.domain, true)
            }
        }

        getCanvasWebView()?.setCanvasWebChromeClientShowFilePickerCallback(object : CanvasWebView.VideoPickerCallback {
            override fun requestStartActivityForResult(intent: Intent, requestCode: Int) {
                startActivityForResult(intent, requestCode)
            }

            override fun permissionsGranted(): Boolean {
                return if (PermissionUtils.hasPermissions(requireActivity().getFragmentActivity(), *PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA))) {
                    true
                } else {
                    requestFilePermissions()
                    false
                }
            }
        })
    }

    override fun handleBackPressed(): Boolean {
        if (canGoBack()) {
            // This prevents a silly bug where the Studio WebView cannot go back far enough to pop its fragment
            val webBackForwardList = getCanvasWebView()?.copyBackForwardList()
            val historyUrl = webBackForwardList?.getItemAtIndex(webBackForwardList.currentIndex - 1)?.url
            if (historyUrl != null && historyUrl.contains("external_tools/") && historyUrl.contains("resource_selection")) {
                navigation?.popCurrentFragment()
                return true
            }
        }
        return super.handleBackPressed()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun requestFilePermissions() {
        requireContext().getFragmentActivity().requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA), PermissionUtils.PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
            getCanvasWebView()?.clearPickerCallback()
            Toast.makeText(requireContext(), R.string.pleaseTryAgain, Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    internal inner class JSInterface {
        @Suppress("unused")
        @JavascriptInterface
        fun showHTML(html: String) {
            val mark = "@id\":\""
            val index = html.indexOf(mark)
            if (index != -1) {
                val endIndex = html.indexOf(",", index)
                var url = html.substring(index + mark.length, endIndex - 1)
                url = StringEscapeUtils.unescapeJava(url)

                // Upload the url as a submission
                submissionHelper.startStudioSubmission(canvasContext, assignmentId, assignmentName, url)

                // Close this page
                navigation?.popCurrentFragment()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((getCanvasWebView()?.handleOnActivityResult(requestCode, resultCode, data)) != true) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        fun newInstance(route: Route) = if (validRoute(route)) {
            StudioWebViewFragment().apply {
                arguments = route.arguments
            }
        } else null

        fun makeRoute(canvasContext: CanvasContext, url: String, title: String, authenticate: Boolean, assignment: Assignment): Route =
            Route(
                StudioWebViewFragment::class.java, canvasContext,
                        canvasContext.makeBundle().apply {
                            putString(Const.INTERNAL_URL, url)
                            putBoolean(Const.AUTHENTICATE, authenticate)
                            putString(Const.ACTION_BAR_TITLE, title)
                            putString(Const.ASSIGNMENT_NAME, assignment.name)
                            putLong(Const.ASSIGNMENT_ID, assignment.id)
                        })

        fun validRoute(route: Route) : Boolean {
            return route.canvasContext != null &&
                    route.arguments.containsKey(Const.INTERNAL_URL) &&
                    route.arguments.containsKey(Const.ACTION_BAR_TITLE) &&
                    route.arguments.containsKey(Const.ASSIGNMENT_NAME) &&
                    route.arguments.containsKey(Const.ASSIGNMENT_ID)
        }
    }
}
