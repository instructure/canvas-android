/*
 * Copyright (C) 2024 - present Instructure, Inc.
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

package com.instructure.pandautils.compose.composables

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.JsExternalToolInterface
import com.instructure.pandautils.utils.JsGoogleDocsInterface
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.CanvasWebViewWrapper

@Composable
fun ComposeCanvasWebViewWrapper(
    content: String,
    modifier: Modifier = Modifier,
    contentType: String = "text/html",
    useInAppFormatting: Boolean = true,
    title: String? = null,
    onLtiButtonPressed: ((ltiUrl: String) -> Unit)? = null,
    applyOnWebView: (CanvasWebView.() -> Unit)? = null,
    applyOnUpdate: (CanvasWebViewWrapper.() -> Unit)? = null,
    webViewCallbacks: ComposeWebViewCallbacks? = null,
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks? = null,
) {
    val webViewState = rememberSaveable(content) { bundleOf() }
    val savedHtml = rememberSaveable(content) { content }
    val savedThemeSwitched = rememberSaveable { bundleOf("themeSwitched" to false) }
    val configuration = LocalConfiguration.current
    val configKey = "${configuration.orientation}-${configuration.uiMode}"

    if (LocalInspectionMode.current) {
        Text(text = content)
    } else {
        AndroidView(
            factory = {
                CanvasWebViewWrapper(it).apply {
                    if (webViewCallbacks != null) {
                        webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                            override fun openMediaFromWebView(mime: String, url: String, filename: String) =
                                webViewCallbacks.openMedia(mime, url, filename)

                            override fun onPageFinishedCallback(webView: WebView, url: String) = webViewCallbacks.onPageFinished(webView, url)

                            override fun onPageStartedCallback(webView: WebView, url: String) = webViewCallbacks.onPageStarted(webView, url)

                            override fun canRouteInternallyDelegate(url: String): Boolean = webViewCallbacks.canRouteInternally(url)

                            override fun routeInternallyCallback(url: String) = webViewCallbacks.routeInternally(url)

                            override fun onReceivedErrorCallback(webView: WebView, errorCode: Int, description: String, failingUrl: String) =
                                webViewCallbacks.onReceivedError(webView, errorCode, description, failingUrl)
                        }
                    }
                    if (embeddedWebViewCallbacks != null) {
                        webView.canvasEmbeddedWebViewCallback = object : CanvasWebView.CanvasEmbeddedWebViewCallback {
                            override fun launchInternalWebViewFragment(url: String) =
                                embeddedWebViewCallbacks.launchInternalWebViewFragment(url)

                            override fun shouldLaunchInternalWebViewFragment(url: String): Boolean =
                                embeddedWebViewCallbacks.shouldLaunchInternalWebViewFragment(url)
                        }
                    }

                    applyOnWebView?.let { applyOnWebView -> webView.applyOnWebView() }
                }
            },
            update = { view ->
                configKey // Read configuration to trigger update on change
                if (webViewState.isEmpty) {
                    if (useInAppFormatting) {
                        view.loadHtml(savedHtml, title)
                    } else {
                        view.loadDataWithBaseUrl(CanvasWebView.getReferrer(true), savedHtml, contentType, "UTF-8", null)
                    }

                    if (onLtiButtonPressed != null) {
                        view.webView.addJavascriptInterface(JsExternalToolInterface(onLtiButtonPressed), Const.LTI_TOOL)
                    }

                    if (HtmlContentFormatter.hasGoogleDocsUrl(savedHtml)) {
                        view.webView.addJavascriptInterface(JsGoogleDocsInterface(view.context), Const.GOOGLE_DOCS)
                    }
                    view.handleConfigurationChange()
                } else {
                    view.webView.restoreState(webViewState)
                    view.setHtmlContent(savedHtml)
                    view.setThemeSwitched(savedThemeSwitched.getBoolean("themeSwitched", false))
                    view.handleConfigurationChange(reloadContent = false)
                }
                applyOnUpdate?.let { applyOnUpdate -> view.applyOnUpdate() }
            },
            onRelease = {
                savedThemeSwitched.putBoolean("themeSwitched", it.themeSwitched)
                it.webView.saveState(webViewState)
            },
            modifier = modifier.fillMaxSize()
        )
    }
}
