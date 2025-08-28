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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.instructure.horizon.R
import com.instructure.horizon.horizonui.organisms.scaffolds.HorizonScaffold
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks

@Composable
fun ReportABugWebView(
    navController: NavController,
) {
    HorizonScaffold(
        title = stringResource(R.string.accountAdvancedTitle),
        onBackPressed = { navController.popBackStack() },
    ) { modifier ->
        ComposeCanvasWebViewWrapper(
            applyOnWebView = {
                settings.javaScriptEnabled = true
            },
            content = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                </head>
                <body>
                </body>
                </html>
            """.trimIndent(),
            webViewCallbacks = ComposeWebViewCallbacks(onPageFinished = { webView, url ->
                webView.evaluateJavascript("""
    const SCRIPT_ID = "jira-issue-collector"
    const script = document.createElement("script")
    script.id = SCRIPT_ID
    script.src =
      "https://instructure.atlassian.net/s/d41d8cd98f00b204e9800998ecf8427e-T/vf1kch/b/0/c95134bc67d3a521bb3f4331beb9b804/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-US&collectorId=e6b73300"
    script.async = true
    document.body.appendChild(script)
    
    window.ATL_JQ_PAGE_PROPS = {
        triggerFunction: function (showCollectorDialog) {
                showCollectorDialog();
        }
    };
                """.trimIndent(), null)
            }),
            modifier = Modifier.fillMaxSize()
        )
    }
}