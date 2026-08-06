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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.JsExternalToolInterface
import com.instructure.pandautils.utils.JsGoogleDocsInterface
import com.instructure.pandautils.views.CanvasWebView
import com.instructure.pandautils.views.CanvasWebViewWrapper

/**
 * @param webViewStateKey When multiple [ComposeCanvasWebViewWrapper]s share a ViewModelStoreOwner,
 * pass a unique key per wrapper so each gets its own state holder. Otherwise they share one
 * [ComposeCanvasWebViewStateViewModel] and overwrite each other's [webViewState] (e.g. the syllabus
 * and front-page tabs would display each other's content). The default `null` is equivalent to
 * `viewModel()` and is safe when only one wrapper exists per scope.
 *
 * @param preserveState When `true` (default) the WebView's state is saved/restored across
 * configuration changes to keep scroll position and DOM. Set to `false` for short, read-only
 * previews that reference local `file://` resources: `WebView.restoreState()` does not re-render
 * `file://` image subresources of a `loadDataWithBaseURL` page, so we reload the HTML fresh on
 * recreation instead, which renders them correctly.
 */
@Composable
fun ComposeCanvasWebViewWrapper(
    content: String,
    modifier: Modifier = Modifier,
    contentType: String = "text/html",
    useInAppFormatting: Boolean = true,
    title: String? = null,
    baseUrl: String? = null,
    webViewStateKey: String? = null,
    preserveState: Boolean = true,
    onLtiButtonPressed: ((ltiUrl: String) -> Unit)? = null,
    applyOnWebView: (CanvasWebView.() -> Unit)? = null,
    applyOnUpdate: (CanvasWebViewWrapper.() -> Unit)? = null,
    webViewCallbacks: ComposeWebViewCallbacks? = null,
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks? = null,
) {
    val stateViewModel: ComposeCanvasWebViewStateViewModel = viewModel(key = webViewStateKey)
    val webViewState = stateViewModel.webViewState
    val savedThemeSwitched = rememberSaveable { bundleOf("themeSwitched" to false) }
    // Both reset exactly when this composable's underlying View is torn down and recreated (e.g. the
    // Fragment view is destroyed/recreated), and persist across recompositions that leave the same View
    // instance in place (e.g. a configuration change the host Activity handles itself without
    // recreating the View) - which is what lets us tell "genuinely new View" apart from "same View,
    // just re-evaluated" below, instead of reloading (and losing scroll position) on every recomposition.
    // Plain (non-snapshot) state: these flags never need to trigger recomposition, and reading them
    // via mutableStateOf inside AndroidView's update lambda while also writing them there would
    // invalidate the update block's own snapshot read-observer, scheduling a redundant second pass.
    val viewState = remember { object { var isFirstUpdate = true; var loadedContent = content } }

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
                        webViewCallbacks.downloadInternalMedia?.let { downloadInternalMedia ->
                            webView.setMediaDownloadCallback(object : CanvasWebView.MediaDownloadCallback {
                                override fun downloadMedia(mime: String?, url: String?, filename: String?) = Unit

                                override fun downloadInternalMedia(mime: String?, url: String?, filename: String?) {
                                    downloadInternalMedia(mime, url.orEmpty(), filename.orEmpty())
                                }
                            })
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
                val contentChanged = !viewState.isFirstUpdate && viewState.loadedContent != content
                val canRestore = viewState.isFirstUpdate && preserveState && !webViewState.isEmpty
                val isFirstUpdate = viewState.isFirstUpdate
                viewState.loadedContent = content
                viewState.isFirstUpdate = false
                if (canRestore) {
                    // This exact View instance was just (re)created (e.g. popping back from an internal
                    // screen, or a configuration change the host Activity doesn't handle itself) and we
                    // have saved state for it - restore rather than reload so scroll position/DOM state
                    // survive.
                    view.webView.restoreState(webViewState)
                    view.setHtmlContent(content)
                    view.setThemeSwitched(savedThemeSwitched.getBoolean("themeSwitched", false))
                    view.handleConfigurationChange(reloadContent = false)
                } else if (isFirstUpdate || contentChanged || !preserveState) {
                    // First ever load for this WebView, a genuinely new/different page, or a consumer
                    // that opted out of state preservation - (re)load it fresh.
                    if (useInAppFormatting) {
                        view.loadHtml(content, title, baseUrl)
                    } else {
                        view.loadDataWithBaseUrl(CanvasWebView.getReferrer(true), content, contentType, "UTF-8", null)
                    }

                    if (onLtiButtonPressed != null) {
                        view.webView.addJavascriptInterface(JsExternalToolInterface(onLtiButtonPressed), Const.LTI_TOOL)
                    }

                    if (HtmlContentFormatter.hasGoogleDocsUrl(content)) {
                        view.webView.addJavascriptInterface(JsGoogleDocsInterface(view.context), Const.GOOGLE_DOCS)
                    }
                    view.handleConfigurationChange()
                }
                // else: the same View instance already showing this exact content was just re-evaluated
                // (e.g. a configuration change the host Activity handles itself without recreating the
                // View) - nothing to do, the WebView keeps its own scroll position and already reacts to
                // configuration changes on its own.
                applyOnUpdate?.let { applyOnUpdate -> view.applyOnUpdate() }
            },
            onRelease = {
                savedThemeSwitched.putBoolean("themeSwitched", it.themeSwitched)
                if (preserveState) {
                    it.webView.saveState(webViewState)
                }
            },
            modifier = modifier.fillMaxSize()
        )
    }
}
