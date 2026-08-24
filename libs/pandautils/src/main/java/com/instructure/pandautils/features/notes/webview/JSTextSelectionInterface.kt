/*
 * Copyright (C) 2026 - present Instructure, Inc.
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
package com.instructure.pandautils.features.notes.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import com.instructure.canvasapi2.models.NoteType
import com.instructure.canvasapi2.models.StudyNote
import com.instructure.pandautils.R
import com.instructure.pandautils.features.notes.composable.toNoteLocalisedDateFormat
import org.json.JSONObject

data class WebViewHighlight(
    val id: String,
    val selectedText: String,
    val userText: String,
    val reactionLabel: String,
    val range: HighlightRange,
    val textPosition: HighlightTextPosition,
    val updatedAt: String
)


fun StudyNote.toWebViewHighlight(): WebViewHighlight? {
    val highlightData = highlightData ?: return null
    val range = highlightData.range ?: return null
    val textPosition = highlightData.textPosition ?: return null
    return WebViewHighlight(
        id = id,
        selectedText = highlightData.selectedText,
        userText = userText,
        // Intentionally NOT normalized via NoteType.apiValue: reactionLabel is also interpolated into
        // the a11y string a11y_webViewHighlightMarkedAs ("... marked as %2$s ..."); mapping an unknown
        // reaction to NoteType.OTHER.apiValue ("") would silently degrade that label. The CSS-class
        // branch in JS_CODE already treats any unrecognized value as "confusing" either way.
        reactionLabel = reactions.firstOrNull().orEmpty(),
        range = HighlightRange(range.startOffset, range.endOffset, range.startContainer, range.endContainer),
        textPosition = HighlightTextPosition(textPosition.start, textPosition.end),
        updatedAt = updatedAt?.toNoteLocalisedDateFormat().orEmpty()
    )
}

data class HighlightRange(val startOffset: Int, val endOffset: Int, val startContainer: String, val endContainer: String)

data class HighlightTextPosition(val start: Int, val end: Int)

class JSTextSelectionInterface(
    private val onTextSelect: (
        text: String,
        startContainer: String,
        startOffset: Int,
        endContainer: String,
        endOffset: Int,
        textSelectionStart: Int,
        textSelectionEnd: Int
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
        textSelectionStart: Int,
        textSelectionEnd: Int,
        updatedAt: String,
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
        textSelectionStart: Int,
        textSelectionEnd: Int
    ) {
        onTextSelect(text, startContainer, startOffset, endContainer, endOffset, textSelectionStart, textSelectionEnd)
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
        textSelectionStart: Int,
        textSelectionEnd: Int,
        updatedAt: String,
    ) {
        onHighlightedTextClick(noteId, noteType, selectedText, userComment, startContainer, startOffset, endContainer, endOffset, textSelectionStart, textSelectionEnd, updatedAt)
    }

    data class HighlightParams(
        val noteId: String,
        val selectedText: String,
        val userComment: String,
        val startOffset: Int,
        val startContainer: String,
        val endOffset: Int,
        val endContainer: String,
        val noteReactionString: String,
        val textSelectionStart: Int,
        val textSelectionEnd: Int,
        val updatedAt: String,
        val accessibilityLabel: String
    )

    companion object {
        private val gson = Gson()
        private const val JS_INTERFACE_NAME = "TextSelectionInterface"
        private const val JS_CODE_FROM_WEB = """
let highlightCss = `
    .highlighted-important {
      background-color: rgba(14, 104, 179, 0.2);
      text-decoration: underline;
      text-decoration-color: rgba(14, 104, 179, 1);
      position: relative;
    }

    .highlighted-confusing {
      background-color: rgba(199, 31, 35, 0.2);
      text-decoration: underline;
      text-decoration-color: rgba(199, 31, 35, 1);
      text-decoration-style: dashed;
      position: relative;
    }
`
const styleSheet = document.createElement("style");
styleSheet.innerText = highlightCss;
document.head.appendChild(styleSheet);

const getNodeName = (node) => {
	const nodeName = node.nodeName.toLowerCase();
	return nodeName === "#text" ? "text()" : nodeName;
};

function getNodePosition(node) {
	let pos = 0;
	let tmp = node;
	while (tmp) {
		if (tmp.nodeName === node.nodeName) {
			pos += 1;
		}
		tmp = tmp.previousSibling;
	}
	return pos;
}

const getPathSegment = (node) => {
	const name = getNodeName(node);
	const pos = getNodePosition(node);
	return `${'$'}{name}[${'$'}{pos}]`;
};

const xpathFromNode = (node, root) => {
	let xpath = "";

	let elem = node;
	while (elem !== root) {
		if (!elem) {
			console.error("Node is not a descendant of root");
			return;
		}
		xpath = `${'$'}{getPathSegment(elem)}/${'$'}{xpath}`;
		elem = elem.parentNode;
	}
	xpath = `/${'$'}{xpath}`;
	xpath = xpath.replace(/\/${'$'}/, ""); // Remove trailing slash

	return xpath;
};

const nthChildOfType = (element, nodeName, index) => {
	const name = nodeName.toUpperCase();

	let matchIndex = -1;
	for (let i = 0; i < element.children.length; i++) {
		const child = element.children[i];
		if (child.nodeName.toUpperCase() === name) {
			++matchIndex;
			if (matchIndex === index) {
				return child;
			}
		}
	}

	return null;
};

const evaluateSimpleXPath = (xpath, root) => {
	const isSimpleXPath = xpath.match(/^(\/[A-Za-z0-9-]+(\[[0-9]+])?)+${'$'}/) !== null;
	if (!isSimpleXPath) {
		console.error("Expression is not a simple XPath");
		return null;
	}

	const segments = xpath.split("/");
	let element = root;

	// Remove leading empty segment. The regex above validates that the XPath
	// has at least two segments, with the first being empty and the others non-empty.
	segments.shift();

	for (const segment of segments) {
		let elementName;
		let elementIndex;

		const separatorPos = segment.indexOf("[");
		if (separatorPos !== -1) {
			elementName = segment.slice(0, separatorPos);

			const indexStr = segment.slice(separatorPos + 1, segment.indexOf("]"));
			elementIndex = Number.parseInt(indexStr) - 1;
			if (elementIndex < 0) {
				return null;
			}
		} else {
			elementName = segment;
			elementIndex = 0;
		}

		const child = nthChildOfType(element, elementName, elementIndex);
		if (!child) {
			return null;
		}

		element = child;
	}

	return element;
};

const nodeFromXPath = (xpath, root = document.body) => {
	try {
		return evaluateSimpleXPath(xpath, root);
	} catch {
		return document.evaluate(
			`.${'$'}{xpath}`,
			root,
			null,
			XPathResult.FIRST_ORDERED_NODE_TYPE,
			null,
		).singleNodeValue;
	}
};

const nodeTextLength = (node) => {
	switch (node.nodeType) {
		case Node.ELEMENT_NODE:
		case Node.TEXT_NODE:
			return node.textContent?.length ?? 0;
		default:
			return 0;
	}
};

const previousSiblingsTextLength = (node) => {
	let sibling = node.previousSibling;
	let length = 0;
	while (sibling) {
		length += nodeTextLength(sibling);
		sibling = sibling.previousSibling;
	}
	return length;
};

const resolveOffsets = (element, ...offsets) => {
	let nextOffset = offsets.shift();
	const nodeIter = element.ownerDocument.createNodeIterator(
		element,
		NodeFilter.SHOW_TEXT
	);
	const results = [];

	let currentNode = nodeIter.nextNode();
	let textNode = null;
	let length = 0;

	while (nextOffset !== undefined && currentNode) {
		textNode = currentNode;
		if (length + textNode.data.length > nextOffset) {
			results.push({ node: textNode, offset: nextOffset - length });
			nextOffset = offsets.shift();
		} else {
			currentNode = nodeIter.nextNode();
			length += textNode.data.length;
		}
	}

	while (nextOffset !== undefined && textNode && length === nextOffset) {
		results.push({ node: textNode, offset: textNode.data.length });
		nextOffset = offsets.shift();
	}

	if (nextOffset !== undefined) {
		console.error("Offset exceeds text length");
	}

	return results;
};

const ResolveDirection = {
	FORWARDS: 1,
	BACKWARDS: 2,
};

class TextPosition {
	constructor(element, offset) {
		if (offset < 0) {
			console.error("Offset is invalid");
		}

		this.element = element;
		this.offset = offset;
	}

	resolve(options = {}) {
		try {
			return resolveOffsets(this.element, this.offset)[0];
		} catch (err) {
			if (this.offset === 0 && options.direction !== undefined) {
				const tw = document.createTreeWalker(
					this.element.getRootNode(),
					NodeFilter.SHOW_TEXT
				);
				tw.currentNode = this.element;
				const forwards = options.direction === ResolveDirection.FORWARDS;
				const text = forwards
					? tw.nextNode()
					: tw.previousNode();
				if (!text) {
					throw err;
				}
				return { node: text, offset: forwards ? 0 : text.data.length };
			}
			throw err;
		}
	}

	static fromCharOffset(node, offset) {
		switch (node.nodeType) {
			case Node.TEXT_NODE:
				return TextPosition.fromPoint(node, offset);
			case Node.ELEMENT_NODE:
				return new TextPosition(node, offset);
			default:
				console.error("Node is not an element or text node");
				return null;
		}
	}

	static fromPoint(node, offset) {
		switch (node.nodeType) {
			case Node.TEXT_NODE: {
				if (offset < 0 || offset > node.data.length) {
					console.error("Text node offset is out of range");
					return null;
				}

				if (!node.parentElement) {
					console.error("Text node has no parent");
					return null;
				}

				const textOffset = previousSiblingsTextLength(node) + offset;

				return new TextPosition(node.parentElement, textOffset);
			}
			case Node.ELEMENT_NODE: {
				if (offset < 0 || offset > node.childNodes.length) {
					console.error("Child node offset is out of range");
					return null;
				}

				let textOffset = 0;
				for (let i = 0; i < offset; i++) {
					textOffset += nodeTextLength(node.childNodes[i]);
				}

				return new TextPosition(node, textOffset);
			}
			default:
				console.error("Point is not in an element or text node");
				return null;
		}
	}

	relativeTo(parent) {
		if (!parent.contains(this.element)) {
			throw new Error("Parent is not an ancestor of current element");
		}

		let el = this.element;
		let offset = this.offset;
		while (el !== parent) {
			offset += previousSiblingsTextLength(el);
			if (el.parentElement) {
				el = el.parentElement;
			}
		}

		return new TextPosition(el, offset);
	}
}

class TextRange {
	constructor(start, end) {
		this.start = start;
		this.end = end;
	}

	toRange() {
		let start;
		let end;

		if (
			this.start.element === this.end.element &&
			this.start.offset <= this.end.offset
		) {
			[start, end] = resolveOffsets(
				this.start.element,
				this.start.offset,
				this.end.offset
			);
		} else {
			start = this.start.resolve({
				direction: ResolveDirection.FORWARDS,
			});
			end = this.end.resolve({ direction: ResolveDirection.BACKWARDS });
		}

		const range = new Range();
		range.setStart(start.node, start.offset);
		range.setEnd(end.node, end.offset);
		return range;
	}

	static fromRange(range) {
		const start = TextPosition.fromPoint(
			range.startContainer,
			range.startOffset
		);
		const end = TextPosition.fromPoint(range.endContainer, range.endOffset);
		if (!start || !end) {
			return null;
		}
		return new TextRange(start, end);
	}

	static fromOffsets(root, start, end) {
		return new TextRange(
			new TextPosition(root, start),
			new TextPosition(root, end)
		);
	}

	relativeTo(element) {
		return new TextRange(
			this.start.relativeTo(element),
			this.end.relativeTo(element)
		);
	}
}

class RangeAnchor {
	constructor(root, range) {
		this.root = root;
		this.range = range;
	}

	static fromRange(root, range) {
		return new RangeAnchor(root, range);
	}

	static fromSelector(root, selector) {
		if (!selector?.startContainer || !selector?.endContainer) {
			console.error("No start or end container in selector");
			return null;
		}
		const startContainer = nodeFromXPath(selector.startContainer, root);
		if (!startContainer) {
			console.error("Failed to resolve startContainer XPath");
			return null;
		}

		const endContainer = nodeFromXPath(selector.endContainer, root);
		if (!endContainer) {
			console.error("Failed to resolve endContainer XPath");
			return null;
		}

		const startPos = TextPosition.fromCharOffset(
			startContainer,
			selector.startOffset
		);
		const endPos = TextPosition.fromCharOffset(
			endContainer,
			selector.endOffset
		);

		if (!startPos || !endPos) {
			return null;
		}

		const range = new TextRange(startPos, endPos).toRange();
		return new RangeAnchor(root, range);
	}

	toRange() {
		return this.range;
	}

	toSelector() {
		const normalizedRange = TextRange.fromRange(this.range)?.toRange();
		const textRange = normalizedRange && TextRange.fromRange(normalizedRange);
		const startContainer =
			textRange && xpathFromNode(textRange.start.element, this.root);
		const endContainer =
			textRange && xpathFromNode(textRange.end.element, this.root);

		if (!startContainer || !endContainer) {
			return null;
		}

		return {
			startContainer: startContainer,
			startOffset: textRange.start.offset,
			endContainer: endContainer,
			endOffset: textRange.end.offset,
		};
	}
}

class TextPositionAnchor {
	constructor(root, start, end) {
		this.root = root;
		this.start = start;
		this.end = end;
	}

	static fromRange(root, range) {
		const textRange = TextRange.fromRange(range)?.relativeTo(root);
		if (!textRange) return null;
		return new TextPositionAnchor(
			root,
			textRange.start.offset,
			textRange.end.offset
		);
	}

	static fromSelector(root, selector) {
		return new TextPositionAnchor(root, selector.start, selector.end);
	}

	toSelector() {
		return {
			start: this.start,
			end: this.end,
		};
	}

	toRange() {
		return TextRange.fromOffsets(this.root, this.start, this.end).toRange();
	}
}

const highlightClassName = "notebook-highlight";

function clearHighlights() {
	const elements = document.getElementsByClassName(highlightClassName);
	while (elements.length) {
		const element = elements[0];
		element.replaceWith(...element.childNodes);
	}
}

// Find all of the text nodes in the range and group them into spans
const getHighlightRange = (range) => {
	const textNodes = wholeTextNodesInRange(range);
	const whitespace = /^\s*${'$'}/;

	// Filter out text nodes that consist only of whitespace
	return textNodes.filter((node) => {
		const parentElement = node.parentElement;
		return (
			(parentElement?.childNodes.length === 1 &&
				parentElement?.tagName === "SPAN") ||
			!whitespace.test(node.data)
		);
	});
};

const wholeTextNodesInRange = (range) => {
	if (range.collapsed) {
		return [];
	}

	let root = range.commonAncestorContainer;
	if (root && root.nodeType !== Node.ELEMENT_NODE) {
		root = root.parentElement;
	}
	if (!root) {
		return [];
	}

	const textNodes = [];
	const nodeIter = root?.ownerDocument?.createNodeIterator(
		root,
		NodeFilter.SHOW_TEXT
	);
	let node = nodeIter?.nextNode() || null;

	while (node) {
		if (!isNodeInRange(range, node)) {
			node = nodeIter?.nextNode() || null;
			continue;
		}

		const text = node;

		if (text === range.startContainer && range.startOffset > 0) {
			text.splitText(range.startOffset);
			node = nodeIter?.nextNode() || null;
			continue;
		}

		if (text === range.endContainer && range.endOffset < text.data.length) {
			text.splitText(range.endOffset);
		}

		textNodes.push(text);
		node = nodeIter?.nextNode() || null;
	}

	return textNodes;
};

const isNodeInRange = (range, node) => {
	try {
		const length = node.nodeValue?.length ?? node.childNodes.length;
		return (
			range.comparePoint(node, 0) <= 0 && range.comparePoint(node, length) >= 0
		);
	} catch {
		return false;
	}
};
		"""
        private val JS_CODE = """
${JS_CODE_FROM_WEB}
function highlightSelection(paramsJson) {
	const params = JSON.parse(paramsJson);
	const { noteId, selectedText, userComment, startOffset, startContainer, endOffset, endContainer, noteReactionString, textSelectionStart, textSelectionEnd, updatedAt, accessibilityLabel } = params;

	let parent = document.getElementById("parent-container");//document.documentElement;
	if (!parent) return;

	let range = RangeAnchor.fromSelector(
		parent,
		{ startContainer: startContainer, startOffset: startOffset, endContainer: endContainer, endOffset: endOffset }
	)?.toRange();
	if (!range) return;

	const textNodeSpans = getHighlightRange(range);
	for (const textNode of textNodeSpans) {
		const parent = textNode.parentNode;
		let cssClass;
		if (noteReactionString === '${NoteType.CONFUSING.apiValue}') {
			cssClass = `highlighted-confusing`;
		} else if (noteReactionString === '${NoteType.IMPORTANT.apiValue}') {
			cssClass = `highlighted-important`;
		} else {
			cssClass = `highlighted-confusing`;
		}

		const highlightElement = document.createElement("span");
		highlightElement.classList.add(highlightClassName);
		highlightElement.classList.add(cssClass);
		highlightElement.setAttribute('data-note-id', noteId);
		highlightElement.setAttribute('role', 'button');
		highlightElement.setAttribute('aria-label', accessibilityLabel);
		highlightElement.onclick = function () { ${ JS_INTERFACE_NAME }.onHighlightedTextClicked(noteId, noteReactionString, selectedText, userComment, startOffset, startContainer, endOffset, endContainer, textSelectionStart, textSelectionEnd, updatedAt); };
		highlightElement.textContent = textNode.textContent;

		if (!highlightElement) return;

		parent.replaceChild(highlightElement, textNode);
	}

}

function getNotePosition(noteId) {
	const highlights = document.getElementsByClassName('notebook-highlight');
	for (const highlight of highlights) {
		if (highlight.getAttribute('data-note-id') === noteId) {
			const rect = highlight.getBoundingClientRect();
			return rect.top + window.scrollY;
		}
	}
	return 0;
}

function scrollToNote(noteId) {
	const highlights = document.getElementsByClassName('notebook-highlight');
	for (const highlight of highlights) {
		if (highlight.getAttribute('data-note-id') === noteId) {
			highlight.scrollIntoView({ behavior: 'smooth', block: 'center' });
			return true;
		}
	}
	return false;
}
javascript: (function () {
	document.addEventListener("selectionchange", () => {
		const selection = window.getSelection();
		const range = selection?.getRangeAt(0);
		const parentRef = document.getElementById("parent-container");//document.documentElement;

		const rangeAnchor =
			parentRef && range ? RangeAnchor.fromRange(parentRef, range) : null;

		const textAnchor =
			parentRef && range ? TextPositionAnchor.fromRange(parentRef, range) : null;

		const startContainer = rangeAnchor?.toSelector()?.startContainer;
		const endContainer = rangeAnchor?.toSelector()?.endContainer;
		const startOffset = rangeAnchor?.toSelector()?.startOffset;
		const endOffset = rangeAnchor?.toSelector()?.endOffset;
		const textSelectionStart = textAnchor?.start;
		const textSelectionEnd = textAnchor?.end;
                ${ JS_INTERFACE_NAME }.onTextSelected(document.getSelection().toString(), startContainer, startOffset, endContainer, endOffset, textSelectionStart, textSelectionEnd);
		const rect = getSelection().getRangeAt(0).getBoundingClientRect();
                ${ JS_INTERFACE_NAME }.onSelectedTextPositionChanged(rect.left, rect.top, rect.right, rect.bottom);
	})
})();
        """.trimIndent()

        fun WebView.addTextSelectionInterface(
            onTextSelect: (
                text: String,
                startContainer: String,
                startOffset: Int,
                endContainer: String,
                endOffset: Int,
                textSelectionStart: Int,
                textSelectionEnd: Int
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
                textSelectionStart: Int,
                textSelectionEnd: Int,
                updatedAt: String,
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
            this.evaluateJavascript(JS_CODE, null)
        }

        fun WebView.highlightRanges(highlights: List<WebViewHighlight>, onHighlighted: () -> Unit) {
            highlights.forEachIndexed { index, highlight ->
                val accessibilityLabel = context.getString(
                    R.string.a11y_webViewHighlightMarkedAs,
                    highlight.selectedText,
                    highlight.reactionLabel
                )
                val params = HighlightParams(
                    noteId = highlight.id,
                    selectedText = highlight.selectedText,
                    userComment = highlight.userText,
                    startOffset = highlight.range.startOffset,
                    startContainer = highlight.range.startContainer,
                    endOffset = highlight.range.endOffset,
                    endContainer = highlight.range.endContainer,
                    noteReactionString = highlight.reactionLabel,
                    textSelectionStart = highlight.textPosition.start,
                    textSelectionEnd = highlight.textPosition.end,
                    updatedAt = highlight.updatedAt,
                    accessibilityLabel = accessibilityLabel
                )
                val paramsJson = gson.toJson(params)
                val quotedJson = JSONObject.quote(paramsJson)
                val script = "javascript:highlightSelection($quotedJson)"
                this.evaluateJavascript(script) {
                    if (index == highlights.lastIndex) { onHighlighted() }
                }
            }
        }

        fun WebView.getNoteYPosition(noteId: String, onResult: (Float?) -> Unit) {
            val quotedNoteId = JSONObject.quote(noteId)
            this.evaluateJavascript("getNotePosition($quotedNoteId)") { result ->
                val position = result?.replace("\"", "")?.toFloatOrNull()
                onResult(if (position != null && position >= 0) position else null)
            }
        }

        /** Scrolls the WebView's own document to the given note's highlight, entirely in-JS via
         * `Element.scrollIntoView` - unlike [getNoteYPosition] (which reports a position for a *caller-
         * owned* Compose [androidx.compose.foundation.ScrollState] to scroll, e.g. Horizon's page screen,
         * which nests its WebView inside a scrollable Column), this is for a WebView that scrolls itself
         * natively, so there's no Kotlin-side position/coordinate handling needed at all. No-ops if the
         * note's highlight span isn't in the DOM (e.g. wrong id, or called before highlightRanges()). */
        fun WebView.scrollToNote(noteId: String) {
            val quotedNoteId = JSONObject.quote(noteId)
            this.evaluateJavascript("scrollToNote($quotedNoteId)", null)
        }
    }
}
