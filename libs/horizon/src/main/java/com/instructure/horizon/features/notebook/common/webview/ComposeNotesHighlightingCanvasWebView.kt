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

import android.webkit.WebView
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.instructure.horizon.R
import com.instructure.horizon.features.notebook.common.model.Note
import com.instructure.horizon.features.notebook.common.model.NotebookType
import com.instructure.horizon.features.notebook.common.webview.JSTextSelectionInterface.Companion.addTextSelectionInterface
import com.instructure.horizon.features.notebook.common.webview.JSTextSelectionInterface.Companion.evaluateTextSelectionInterface
import com.instructure.horizon.features.notebook.common.webview.JSTextSelectionInterface.Companion.getNoteYPosition
import com.instructure.horizon.features.notebook.common.webview.JSTextSelectionInterface.Companion.highlightNotes
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.HtmlContentFormatter
import com.instructure.pandautils.utils.JsExternalToolInterface
import com.instructure.pandautils.utils.JsGoogleDocsInterface
import com.instructure.pandautils.utils.toPx
import com.instructure.pandautils.views.CanvasWebView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun ComposeNotesHighlightingCanvasWebView(
    content: String,
    notes: List<Note>,
    notesCallback: NotesCallback,
    modifier: Modifier = Modifier,
    contentType: String = "text/html",
    useInAppFormatting: Boolean = true,
    title: String? = null,
    onLtiButtonPressed: ((ltiUrl: String) -> Unit)? = null,
    applyOnWebView: (CanvasWebView.() -> Unit)? = null,
    webViewCallbacks: ComposeWebViewCallbacks = ComposeWebViewCallbacks(),
    embeddedWebViewCallbacks: ComposeEmbeddedWebViewCallbacks? = null,
    scrollState: ScrollState? = null,
    scrollToNoteId: String? = null
) {
    var pageHeight by remember { mutableIntStateOf(0) }
    var scrollValue by rememberSaveable { mutableIntStateOf(0) }
    var previousScrollMaxValue by rememberSaveable { mutableIntStateOf(0) }
    var scrollMaxValue by rememberSaveable { mutableIntStateOf(0) }
    val webViewState = rememberSaveable { bundleOf() }
    val selectionLocation: MutableStateFlow<SelectionLocation> by remember { mutableStateOf(MutableStateFlow(SelectionLocation(0f, 0f, 0f, 0f))) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val composeScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val notesStateValue = rememberUpdatedState(notes)

    var selectedText by remember { mutableStateOf("") }
    var selectedTextRangeStartContainer by remember { mutableStateOf("") }
    var selectedTextRangeStartOffset by remember { mutableIntStateOf(0) }
    var selectedTextRangeEndContainer by remember { mutableStateOf("") }
    var selectedTextRangeEndOffset by remember { mutableIntStateOf(0) }
    var selectedTextStart by remember { mutableIntStateOf(0) }
    var selectedTextEnd by remember { mutableIntStateOf(0) }

    var isScrolled by rememberSaveable { mutableStateOf(false) }
    var isPageLoaded by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<NotesHighlightingCanvasWebViewWrapper?>(null) }

    LaunchedEffect(scrollState?.maxValue) {
        val maxValue = scrollState?.maxValue ?: 0

        if (maxValue > 0 && maxValue < Int.MAX_VALUE) {
            previousScrollMaxValue = scrollMaxValue
            scrollMaxValue = maxValue
        }
    }

    LaunchedEffect(scrollToNoteId, isPageLoaded) {
        delay(500)
        val noteId = scrollToNoteId
        if (noteId != null && isPageLoaded && scrollState != null && webViewInstance != null && !isScrolled) {
            isScrolled = true
            webViewInstance?.webView?.getNoteYPosition(noteId) { yPosition ->
                if (yPosition != null) {
                    composeScope.launch {
                        val targetScroll = (yPosition.toInt().toPx).coerceIn(0, scrollState.maxValue)
                        scrollState.animateScrollTo(targetScroll)
                    }
                }
            }
        }
    }

    var previousHeight by remember { mutableIntStateOf(0) }
    if (LocalInspectionMode.current) {
        Text(text = content)
    } else {
        AndroidView(
            factory = {
                scrollValue = scrollState?.value ?: 0
                val wrapper = NotesHighlightingCanvasWebViewWrapper(
                    it,
                    callback = AddNoteActionModeCallback(
                        lifecycleOwner,
                        selectionLocation,
                        menuItems = {
                            buildList {
                                add(
                                    ActionMenuItem(1, context.getString(R.string.notesActionMenuCopy)) {
                                        clipboardManager.setText(AnnotatedString(selectedText))
                                    }
                                )
                                if (notes.none { intersects(it.highlightedText.textPosition.start to it.highlightedText.textPosition.end, selectedTextStart to selectedTextEnd) }){
                                    add(
                                        ActionMenuItem(2, context.getString(R.string.notesActionMenuMarkImportantNote)) {
                                            notesCallback.onNoteAdded(
                                                selectedText,
                                                NotebookType.Important.name,
                                                selectedTextRangeStartContainer,
                                                selectedTextRangeStartOffset,
                                                selectedTextRangeEndContainer,
                                                selectedTextRangeEndOffset,
                                                selectedTextStart,
                                                selectedTextEnd
                                            )
                                        }
                                    )
                                    add(
                                        ActionMenuItem(3, context.getString(R.string.notesActionMenuMarkConfusingNote)) {
                                            notesCallback.onNoteAdded(
                                                selectedText,
                                                NotebookType.Confusing.name,
                                                selectedTextRangeStartContainer,
                                                selectedTextRangeStartOffset,
                                                selectedTextRangeEndContainer,
                                                selectedTextRangeEndOffset,
                                                selectedTextStart,
                                                selectedTextEnd
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    )).apply {
                    webView.canvasWebViewClientCallback = object : CanvasWebView.CanvasWebViewClientCallback {
                        override fun openMediaFromWebView(mime: String, url: String, filename: String) =
                            webViewCallbacks.openMedia(mime, url, filename)

                        override fun onPageFinishedCallback(webView: WebView, url: String) {
                            webViewCallbacks.onPageFinished(webView, url)

                            webView.evaluateTextSelectionInterface()
                            webView.highlightNotes(notesStateValue.value) {
                                isPageLoaded = true
                            }
                        }

                        override fun onPageStartedCallback(webView: WebView, url: String) = webViewCallbacks.onPageStarted(webView, url)

                        override fun canRouteInternallyDelegate(url: String): Boolean = webViewCallbacks.canRouteInternally(url)

                        override fun routeInternallyCallback(url: String) = webViewCallbacks.routeInternally(url)

                        override fun onReceivedErrorCallback(webView: WebView, errorCode: Int, description: String, failingUrl: String) =
                            webViewCallbacks.onReceivedError(webView, errorCode, description, failingUrl)
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
                webViewInstance = wrapper
                wrapper
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
                    it.loadHtml(content, title)
                }

                it.webView.addTextSelectionInterface(
                    onTextSelect = { text, startContainer, startOffset, endContainer, endOffset, selectedTextStartParam, selectedTextEndParam ->
                        selectedText = text
                        selectedTextRangeStartContainer = startContainer
                        selectedTextRangeStartOffset = startOffset
                        selectedTextRangeEndContainer = endContainer
                        selectedTextRangeEndOffset = endOffset
                        selectedTextStart = selectedTextStartParam
                        selectedTextEnd = selectedTextEndParam
                    },
                    onHighlightedTextClick = { noteId, noteType, selectedText, userComment, startContainer, startOffset, endContainer, endOffset, selectedTextStartParam, selectedTextEndParam, updatedAt ->
                        lifecycleOwner.lifecycleScope.launch {
                            notesCallback.onNoteSelected(
                                noteId,
                                noteType,
                                selectedText,
                                userComment,
                                startContainer,
                                startOffset,
                                endContainer,
                                endOffset,
                                selectedTextStartParam,
                                selectedTextEndParam,
                                updatedAt
                            )
                        }
                    },
                    onSelectionPositionChange = { left, top, right, bottom ->
                        selectionLocation.tryEmit(SelectionLocation(left, top, right, bottom))
                    }
                )
            },
            onRelease = {
                it.webView.saveState(webViewState)
            },
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    pageHeight = coordinates.size.height
                    if (coordinates.size.height > 0 && coordinates.size.height != previousHeight) {
                        lifecycleOwner.lifecycleScope.launch {
                            val scrollRatio = scrollValue.toFloat() / previousScrollMaxValue.toFloat()
                            scrollState?.scrollTo((scrollRatio * (scrollState.maxValue)).toInt())
                        }
                    }
                    previousHeight = coordinates.size.height

                }
        )
    }
}

private fun intersects(a: Pair<Int, Int>, b: Pair<Int, Int>): Boolean {
    return a.first <= b.second && b.first <= a.second
}