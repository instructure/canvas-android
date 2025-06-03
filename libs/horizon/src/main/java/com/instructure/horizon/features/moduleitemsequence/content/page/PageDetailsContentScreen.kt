/*
 * Copyright (C) 2025 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package com.instructure.horizon.features.moduleitemsequence.content.page

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.instructure.horizon.horizonui.foundation.HorizonColors
import com.instructure.horizon.horizonui.foundation.HorizonCornerRadius
import com.instructure.horizon.horizonui.foundation.HorizonSpace
import com.instructure.horizon.horizonui.foundation.SpaceSize
import com.instructure.pandautils.compose.composables.ComposeCanvasWebViewWrapper
import com.instructure.pandautils.compose.composables.ComposeEmbeddedWebViewCallbacks
import com.instructure.pandautils.compose.composables.ComposeWebViewCallbacks
import com.instructure.pandautils.utils.Const
import com.instructure.pandautils.utils.ThemePrefs
import com.instructure.pandautils.utils.getActivityOrNull
import com.instructure.pandautils.utils.launchCustomTab
import com.instructure.pandautils.views.JSInterface
import com.instructure.pandautils.views.JSTextSelectionInterface

@Composable
fun PageDetailsContentScreen(
    uiState: PageDetailsUiState,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.getActivityOrNull()
    LaunchedEffect(uiState.urlToOpen) {
        uiState.urlToOpen?.let { url ->
            activity?.launchCustomTab(url, ThemePrefs.brandColor)
            uiState.onUrlOpened()
        }
    }
    uiState.pageHtmlContent?.let {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .clip(HorizonCornerRadius.level5)
                .background(HorizonColors.Surface.cardPrimary())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                ComposeCanvasWebViewWrapper(
                    content = it,
                    applyOnWebView = {
                        activity?.let { addVideoClient(it) }
                        overrideHtmlFormatColors = HorizonColors.htmlFormatColors
                        if (uiState.ltiButtonPressed != null) {
                            addJavascriptInterface(JSInterface(uiState.ltiButtonPressed), Const.LTI_TOOL)
                        }

                        addJavascriptInterface(JSTextSelectionInterface(
                            { Log.d("PageDetailsContentScreen", "Text selected: $it") },
                            { Log.d("PageDetailsContentScreen", "Note clicked: $it") },
                        ), JSTextSelectionInterface.JS_INTERFACE_NAME)

                        evaluateJavascript("""
                            javascript:(function() { document.addEventListener("selectionchange", () => {
                              ${JSTextSelectionInterface.JS_INTERFACE_NAME}.onTextSelected(document.getSelection().toString());
                            })})();
                        """.trimIndent(), null)
                    },
                    embeddedWebViewCallbacks = ComposeEmbeddedWebViewCallbacks(
                        shouldLaunchInternalWebViewFragment = { _ -> true },
                        launchInternalWebViewFragment = { url -> activity?.launchCustomTab(url, ThemePrefs.brandColor) }
                    ),
                    webViewCallbacks = ComposeWebViewCallbacks(
                        onPageFinished = { webView, _ ->
                            webView.addJavascriptInterface(JSTextSelectionInterface(
                                { Log.d("PageDetailsContentScreen", "Text selected: $it") },
                                { Log.d("PageDetailsContentScreen", "Note clicked: $it") },
                            ), JSTextSelectionInterface.JS_INTERFACE_NAME)

                            webView.evaluateJavascript("""
                                // Add CSS
                                const style = document.createElement('style');
                                style.textContent = `
                                  .important-highlight {
                                    background-color: rgba(0, 0, 255, 0.2);
                                    text-decoration: underline;
                                    text-decoration-color: rgba(0, 0, 255, 1);
                                  }
                                  .confusing-highlight {
                                    background-color: rgba(255, 0, 0, 0.2);
                                    text-decoration: underline;
                                    text-decoration-color: rgba(255, 0, 0, 1);
                                  }
                                `;
                                document.head.appendChild(style);
                                
                                function getFirstTextNode(node) {
                                    if (node.nodeType === Node.TEXT_NODE) return node;
                                    for (const child of node.childNodes) {
                                        const result = getFirstTextNode(child);
                                        if (result) return result;
                                    }
                                    return null;
                                }
                                function getTextNodeByXPath(path) {
                                    const xpathResult = document.evaluate(
                                        '/' + path,
                                        document.body, // relative to <body>
                                        null,
                                        XPathResult.FIRST_ORDERED_NODE_TYPE,
                                        null
                                    );

                                    let node = xpathResult.singleNodeValue;

                                    // Ensure it's a Text node
                                    if (!node) return null;

                                    if (node.nodeType === Node.TEXT_NODE) {
                                        return node;
                                    }

                                    // Find the first text descendant
                                    return getFirstTextNode(node);
                                }
                                
                                function highlightSelection(noteId, startOffset, startContainer, endOffset, endContainer, noteReactionString) {
                                  const startNode = getTextNodeByXPath(startContainer);
                                  const endNode = getTextNodeByXPath(endContainer);
                                  const range = document.createRange();
                                    
                                  console.log(startNode.textContent.length);
                                  console.log(endNode.textContent.length);
                                  range.setStart(startNode, startOffset);
                                  try {
                                    range.setEnd(endNode, endOffset);
                                  } catch (e) {
                                    range.setEnd(startNode, startNode.textContent.length);
                                  }
                                  const span = document.createElement('span');
                                  if (noteReactionString === 'Confusing') {
                                  span.className = 'confusing-highlight';
                                  } else if (noteReactionString === 'Important') {
                                    span.className = 'important-highlight';
                                  } else {
                                    span.className = 'important-highlight';
                                  }
                                  span.onclick = function() { ${JSTextSelectionInterface.JS_INTERFACE_NAME}.onHighlightedTextClicked(noteId); };
                                  
                                  range.surroundContents(span);
    
                                }
                                javascript:(function() { document.addEventListener("selectionchange", () => {
                                  ${JSTextSelectionInterface.JS_INTERFACE_NAME}.onTextSelected(document.getSelection().toString());
                                  //highlightSelection(document.getSelection().getRangeAt(0), "Important");
                                  //document.getSelection().removeAllRanges();
                                })})();
                            """.trimIndent(), null)

                            uiState.notes.forEach { note ->
                                webView.evaluateJavascript("javascript:highlightSelection('${note.id}', ${note.highlightedText.range.startOffset}, '${note.highlightedText.range.startContainer}', ${note.highlightedText.range.endOffset}, '${note.highlightedText.range.endContainer}', '${note.type.name}')", null)
                            }
                        }
                    )
                )
                HorizonSpace(SpaceSize.SPACE_48)
            }
        }
    }
}
