/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'dart:convert';

import 'package:flutter/gestures.dart';
import 'package:flutter/services.dart';
import 'package:webview_flutter/webview_flutter.dart';

extension WebViewUtils on WebViewController {
  /**
   * Formats html/rich content into a somewhat suitable form for mobile.
   *
   * See html_wrapper.html for more details
   */
  Future<void> loadHtml(String html,
      {String baseUrl, Map<String, String> headers, double horizontalPadding = 0}) async {
    assert(horizontalPadding != null);

    String fileText = await rootBundle.loadString('assets/html/html_wrapper.html');
    html = _applyWorkAroundForDoubleSlashesAsUrlSource(html);
    html = _addProtocolToLinks(html);
    html = fileText.replaceAll('{CANVAS_CONTENT}', html);
    html = html.replaceAll('{PADDING}', horizontalPadding.toString());
    this.loadData(baseUrl, html, 'text/html', 'utf-8');
  }

  /**
   * Loads html content w/o any change to formatting
   */
  Future<void> loadRawHtml(String html, Map<String, String> headers) async {
    String uri = Uri.dataFromString(html, mimeType: 'text/html', encoding: Encoding.getByName('utf-8')).toString();
    this.loadUrl(uri);
  }
}

String _applyWorkAroundForDoubleSlashesAsUrlSource(String html) {
  if (html.isEmpty) return '';
  // Fix for embedded videos that have // instead of http://
  html = html.replaceAll('href="//', 'href="https://');
  html = html.replaceAll('href=\'//', 'href=\'https://');
  html = html.replaceAll('src="//', 'src="https://');
  html = html.replaceAll('src=\'//', 'src=\'https://');
  return html;
}

String _addProtocolToLinks(String html) {
  if (html.isEmpty) return '';

  html = html.replaceAll('href="www.', 'href="https://www.');
  html = html.replaceAll('href=\'www.', 'href=\'https://www.');
  html = html.replaceAll('src="www.', 'src="https://www.');
  html = html.replaceAll('src=\'www.', 'src=\'https://www.');
  return html;
}

///
/// Due to the Gesture Arena currently favoring basically but the WebView,
/// this will tell the arena to respect vertical swipe gestures on the WebView
/// so it can scroll
/// Code taken from:
///   https://stackoverflow.com/questions/57069716/scrolling-priority-when-combining-horizontal-scrolling-with-webview/57150906#57150906
/// Related issues:
///   https://github.com/flutter/flutter/issues/36304
///   https://github.com/flutter/flutter/issues/35394
class WebViewGestureRecognizer extends VerticalDragGestureRecognizer {
  WebViewGestureRecognizer({PointerDeviceKind kind}) : super(kind: kind);

  Offset _dragDistance = Offset.zero;

  @override
  void addPointer(PointerEvent event) {
    startTrackingPointer(event.pointer);
  }

  @override
  void handleEvent(PointerEvent event) {
    _dragDistance = _dragDistance + event.delta;
    if (event is PointerMoveEvent) {
      final double dy = _dragDistance.dy.abs();
      final double dx = _dragDistance.dx.abs();

      if (dy > dx && dy > kTouchSlop) {
        // Vertical drag - accept
        resolve(GestureDisposition.accepted);
        _dragDistance = Offset.zero;
      } else if (dx > kTouchSlop && dx > dy) {
        // horizontal drag - stop tracking
        stopTrackingPointer(event.pointer);
        _dragDistance = Offset.zero;
      }
    }
  }

  @override
  void didStopTrackingLastPointer(int pointer) {}
}
