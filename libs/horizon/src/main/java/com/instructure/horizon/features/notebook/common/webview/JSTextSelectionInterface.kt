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

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.instructure.horizon.features.notebook.common.model.Note

class JSTextSelectionInterface(
    private val onTextSelect: (
        text: String,
        startContainer: String,
        startOffset: Int,
        endContainer: String,
        endOffset: Int
    ) -> Unit,
    private val onHighlightedTextClick: (
        noteId: String,
        noteType: String,
        selectedText: String,
        userComment: String,
        startContainer: String,
        startOffset: Int,
        endContainer: String,
        endOffset: Int,
    ) -> Unit,
    private val onSelectionPositionChange: (
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) -> Unit
) {
    @JavascriptInterface
    fun onTextSelected(
        text: String,
        startContainer: String,
        startOffset: Int,
        endContainer: String,
        endOffset: Int,
    ) {
        onTextSelect(text, startContainer, startOffset, endContainer, endOffset)
    }

    @JavascriptInterface
    fun onSelectedTextPositionChanged(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        onSelectionPositionChange(left, top, right, bottom)
    }

    @JavascriptInterface
    fun onHighlightedTextClicked(
        noteId: String,
        noteType: String,
        selectedText: String,
        userComment: String,
        startOffset: Int,
        startContainer: String,
        endOffset: Int,
        endContainer: String,
    ) {
        onHighlightedTextClick(noteId, noteType, selectedText, userComment, startContainer, startOffset, endContainer, endOffset)
    }

    companion object {
        private const val JS_INTERFACE_NAME = "TextSelectionInterface"
        private val jsCode = """
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
            
            function getXPathForNode(node, root = document.body) {
                if (node === root) return '';
                if (!node || !node.parentNode) return null;

                const parts = [];
                while (node && node !== root) {
                    const parent = node.parentNode;
                    if (!parent) break;
                    if (node.nodeType !== Node.TEXT_NODE) {
                        const siblings = Array.from(parent.childNodes).filter(n => n.nodeName === node.nodeName);
                        const index = siblings.indexOf(node) + 1; // XPath is 1-based
                        const nodeName = node.nodeType === Node.TEXT_NODE ? 'text()' : node.nodeName.toLowerCase();
                        parts.unshift(`${'$'}{nodeName}[${'$'}{index}]`);
                    }
                    node = parent;
                }

                return '/' + parts.join('/');
            }
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
            
                if (!node) return null;
            
                if (node.nodeType === Node.TEXT_NODE) {
                    return node;
                }
            
                return getFirstTextNode(node);
            }
            
            function highlightSelection(noteId, selectedText, userComment, startOffset, startContainer, endOffset, endContainer, noteReactionString) {
                const startNode = getTextNodeByXPath(startContainer);
                const endNode = getTextNodeByXPath(endContainer);
                const range = document.createRange();
                
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
                span.onclick = function() { ${JS_INTERFACE_NAME}.onHighlightedTextClicked(noteId, noteReactionString, selectedText, userComment, startOffset, startContainer, endOffset, endContainer); };
              
                range.surroundContents(span);
            }
            javascript:(function() { document.addEventListener("selectionchange", () => {
                const range = document.getSelection().getRangeAt(0);
                const startContainer = getXPathForNode(range.startContainer);
                const endContainer = getXPathForNode(range.endContainer);
                const startOffset = range.startOffset;
                const endOffset = range.endOffset;
                ${JS_INTERFACE_NAME}.onTextSelected(document.getSelection().toString(), startContainer, startOffset, endContainer, endOffset);
                const rect = getSelection().getRangeAt(0).getBoundingClientRect();
                ${JS_INTERFACE_NAME}.onSelectedTextPositionChanged(rect.left, rect.top, rect.right, rect.bottom);
            })})();
        """.trimIndent()

        fun WebView.addTextSelectionInterface(
            onTextSelect: (
                text: String,
                startContainer: String,
                startOffset: Int,
                endContainer: String,
                endOffset: Int
            ) -> Unit,
            onHighlightedTextClick: (
                noteId: String,
                noteType: String,
                selectedText: String,
                userComment: String,
                startContainer: String,
                startOffset: Int,
                endContainer: String,
                endOffset: Int,
            ) -> Unit,
            onSelectionPositionChange: (
                left: Float,
                top: Float,
                right: Float,
                bottom: Float
            ) -> Unit
        ) {
            val jsInterface = JSTextSelectionInterface(onTextSelect, onHighlightedTextClick, onSelectionPositionChange)
            this.addJavascriptInterface(jsInterface, JS_INTERFACE_NAME)
        }

        fun WebView.evaluateTextSelectionInterface() {
            this.evaluateJavascript(jsCode, null)
        }

        fun WebView.highlightNotes(notes: List<Note>) {
            notes.forEach { note ->
                this.evaluateJavascript("javascript:highlightSelection('${note.id}', '${note.highlightedText.selectedText}', '${note.userText}', ${note.highlightedText.range.startOffset}, '${note.highlightedText.range.startContainer}', ${note.highlightedText.range.endOffset}, '${note.highlightedText.range.endContainer}', '${note.type.name}')", null)
            }
        }
    }
}