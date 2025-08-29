/*
 * Copyright (C) 2025 - present Instructure, Inc.
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
package com.instructure.horizon.features.account.reportabug

import android.content.Intent
import android.webkit.JavascriptInterface
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.coroutines.launch

@Composable
fun ReportABugWebView(
    navController: NavController,
) {
    HorizonScaffold(
        title = "Report a bug",
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        val scope = LocalLifecycleOwner.current.lifecycleScope
        val activity = LocalContext.current.getActivityOrNull()
        var webViewReference: CanvasWebView? by remember { mutableStateOf(null) }
        var request by remember { mutableIntStateOf(0) }
        val launchPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            webViewReference?.handleOnActivityResult(request, result.resultCode, result.data)
        }

        ComposeCanvasWebViewWrapper(
            applyOnWebView = {
                settings.javaScriptEnabled = true

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )

                activity?.let { addVideoClient(activity) }
                setCanvasWebChromeClientShowFilePickerCallback(object: CanvasWebView.VideoPickerCallback {
                    override fun requestStartActivityForResult(
                        intent: Intent,
                        requestCode: Int
                    ) {
                        request = requestCode
                        launchPicker.launch(intent)
                    }

                    override fun permissionsGranted(): Boolean = true
                })
                webViewReference = this
            },
            content = """
                <!DOCTYPE html>
                <html lang="en">
                    <head>
                        <style>
                            body {
                               height: 100%;
                            }
                        </style>
                        <meta name="viewport" content="width=device-width initial-scale=1">
                    </head>
                    <body>
                    </body>
                </html>
            """.trimIndent(),
            applyOnUpdate = {
                webView.addJavascriptInterface(
                    JsReportABugInterface(
                        { scope.launch { navController.popBackStack() } }
                    ),
                    JsReportABugInterface.INTERFACE_NAME
                )
            },
            webViewCallbacks = ComposeWebViewCallbacks(onPageFinished = { webView, url ->
                webView.evaluateJavascript("""
                    const SCRIPT_ID = "jira-issue-collector"
                    const script = document.createElement("script")
                    script.id = SCRIPT_ID
                    script.src =
                      "https://instructure.atlassian.net/s/d41d8cd98f00b204e9800998ecf8427e-T/vf1kch/b/0/c95134bc67d3a521bb3f4331beb9b804/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-US&collectorId=e6b73300"
                    script.addEventListener("load", function(){
                        window.ATL_JQ_PAGE_PROPS = {
                            triggerFunction: function (showCollectorDialog) {
                                setTimeout(function() {
                                    showCollectorDialog();
                                }, 100);
                            }
                        };
                    });
                    
                    document.body.appendChild(script)
                    
                    window.addEventListener('message', (event) => { 
                       if (event.data === 'cancelFeedbackDialog') {
                           ${JsReportABugInterface.INTERFACE_NAME}.close()
                       }
                    });
                """.trimIndent(), null)
            }),
            modifier = modifier
        )
    }
}

private class JsReportABugInterface(val onNavigateBack: () -> Unit) {
    @JavascriptInterface
    fun close() {
        onNavigateBack()
    }

    companion object {
        const val INTERFACE_NAME = "ReportABugInterface"
    }
}