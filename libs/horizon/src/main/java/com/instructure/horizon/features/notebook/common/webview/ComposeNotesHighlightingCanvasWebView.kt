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
package com.instructure.horizon.features.notebook.common.webview

import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.webview.JSTextSelectionInterface.Companion.addTextSelectionInterface
import com.instructure.horizon.features.notebook.common.webview.JSTextSelectionInterface.Companion.highlightNotes
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.JsExternalToolInterface
import com.instructure.pandautils.utils.JsGoogleDocsInterface
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ComposeNotesHighlightingCanvasWebView(
    content: String,
    notes: List<Note>,
    modifier: Modifier = Modifier,
    contentType: String = "text/html",
    useInAppFormatting: Boolean = true,
    title: String? = null,
    onLtiButtonPressed: ((ltiUrl: String) -> Unit)? = null,
    applyOnWebView: (CanvasWebView.() -> Unit)? = null,
    webViewCallbacks: ComposeWebViewCallbacks? = ComposeWebViewCallbacks(),
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks? = null,
) {
    val webViewState = rememberSaveable { bundleOf() }
    val selectionLocation: MutableStateFlow<SelectionLocation> by remember { mutableStateOf(MutableStateFlow(SelectionLocation(0f, 0f, 0f, 0f))) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val menuItems by remember {
        mutableStateOf(
            listOf(
                ActionMenuItem(1, "Copy", {}),
                ActionMenuItem(2, "Add Note", {})
            )
        )
    }

    if (LocalInspectionMode.current) {
        Text(text = content)
    } else {
        AndroidView(
            factory = {
                NotesHighlightingCanvasWebViewWrapper(it, callback = AddNoteActionModeCallback(lifecycleOwner, selectionLocation, menuItems)).apply {
                    if (webViewCallbacks != null) {
                        webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                            override fun openMediaFromWebView(mime: String, url: String, filename: String) =
                                webViewCallbacks.openMedia(mime, url, filename)

                            override fun onPageFinishedCallback(webView: WebView, url: String) {
                                webViewCallbacks.onPageFinished(webView, url)

                                webView.addTextSelectionInterface(
                                    onTextSelect = {text, startContainer, startOffset, endContainer, endOffset -> Log.d("PageDetailsContentScreen", "Text selected: $text, {$startContainer}:$startOffset, {$endContainer}:$endOffset") },
                                    onHighlightedTextClick = { Log.d("PageDetailsContentScreen", "Note clicked: $it") },
                                    onSelectionPositionChange = { left, top, right, bottom ->
                                        Log.d("PageDetailsContentScreen", "Selection position changed: ($left, $top, $right, $bottom)")
                                        selectionLocation.tryEmit(SelectionLocation(left, top, right, bottom))
                                    }
                                )
                                webView.highlightNotes(notes)
                            }

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

                    webView.addTextSelectionInterface(
                        onTextSelect = { text, startContainer, startOffset, endContainer, endOffset ->
                            Log.d("PageDetailsContentScreen", "Text selected: $text, {$startContainer}:$startOffset, {$endContainer}:$endOffset")
                        },
                        onHighlightedTextClick = { Log.d("PageDetailsContentScreen", "Note clicked: $it") },
                        onSelectionPositionChange = { left, top, right, bottom ->
                            Log.d("PageDetailsContentScreen", "Selection position changed: ($left, $top, $right, $bottom)")
                            selectionLocation.tryEmit(SelectionLocation(left, top, right, bottom))
                        }
                    )
                    webView.highlightNotes(notes)
                }
            },
            update = {
                if (webViewState.isEmpty) {
                    if (useInAppFormatting) {
                        it.loadHtml(content, title)
                    } else {
                        it.loadDataWithBaseUrl(CanvasWebView.getReferrer(true), content, contentType, "UTF-8", null)
                    }

                    if (onLtiButtonPressed != null) {
                        it.webView.addJavascriptInterface(JsExternalToolInterface(onLtiButtonPressed), Const.LTI_TOOL)
                    }

                    if (HtmlContentFormatter.hasGoogleDocsUrl(content)) {
                        it.webView.addJavascriptInterface(JsGoogleDocsInterface(it.context), Const.GOOGLE_DOCS)
                    }
                } else {
                    it.webView.restoreState(webViewState)
                }
            },
            onRelease = {
                it.webView.saveState(webViewState)
            },
            modifier = modifier.fillMaxSize()
        )
    }
}