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
        textSelectionEnd: Int
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
        textSelectionEnd: Int
    ) {
        onHighlightedTextClick(noteId, noteType, selectedText, userComment, startContainer, startOffset, endContainer, endOffset, textSelectionStart, textSelectionEnd)
    }

    companion object {
        private const val JS_INTERFACE_NAME = "TextSelectionInterface"
		private const val JS_CODE_FROM_WEB = """
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
function highlightSelection(noteId, selectedText, userComment, startOffset, startContainer, endOffset, endContainer, noteReactionString, textSelectionStart, textSelectionEnd) {
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
		let cssStyle;
		if (noteReactionString === 'Confusing') {
			cssStyle = `
                    background-color: rgba(199, 31, 35, 0.2);
                    text-decoration: underline;
                    text-decoration-color: rgba(199, 31, 35, 1);
                      `;
		} else if (noteReactionString === 'Important') {
			cssStyle = `
                        background-color: rgba(14, 104, 179, 0.2);
                        text-decoration: underline;
                        text-decoration-color: rgba(14, 104, 179, 1);
                      `;
		} else {
			cssStyle = `
                        background-color: rgba(14, 104, 179, 0.2);
                        text-decoration: underline;
                        text-decoration-color: rgba(14, 104, 179, 1);
                      `;
		}

		const highlightElement = document.createElement("span");
		highlightElement.classList.add(highlightClassName);
		highlightElement.onclick = function () { ${ JS_INTERFACE_NAME }.onHighlightedTextClicked(noteId, noteReactionString, selectedText, userComment, startOffset, startContainer, endOffset, endContainer, textSelectionStart, textSelectionEnd); };
		highlightElement.style.cssText = cssStyle;
		highlightElement.textContent = textNode.textContent;

		if (!highlightElement) return;

		parent.replaceChild(highlightElement, textNode);
	}

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
                textSelectionEnd: Int
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

        fun WebView.highlightNotes(notes: List<Note>) {
            notes.forEach { note ->
                val script = "javascript:highlightSelection('${note.id}', '${note.highlightedText.selectedText.replace("\n", "\\n")}', '${note.userText.replace("\n", "\\n")}', ${note.highlightedText.range.startOffset}, '${note.highlightedText.range.startContainer}', ${note.highlightedText.range.endOffset}, '${note.highlightedText.range.endContainer}', '${note.type.name}', ${note.highlightedText.textPosition.start}, ${note.highlightedText.textPosition.end})"
                this.evaluateJavascript(script, null)
            }
        }
    }
}