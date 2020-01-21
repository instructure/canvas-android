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

import 'package:flutter/services.dart';
import 'package:webview_flutter/webview_flutter.dart';


extension WebViewUtils on WebViewController {
  /**
   * Formats html/rich content into a somewhat suitable form for mobile.
   *
   * See html_wrapper.html for more details
   */
  Future<void> loadHtml(String html, {Map<String, String> headers}) async {
    String fileText = await rootBundle.loadString('assets/html_wrapper.html');
    html = _applyWorkAroundForDoubleSlashesAsUrlSource(html);
    html = _addProtocolToLinks(html);
    html = html.replaceAll('users/170000004596934/files/170000000042155/preview?verifier=npXK1CmONpIoSjDDyJxQ4gLfcrod5HYeZMW0r7ca', 'https://mobiledev.instructure.com/users/170000004596934/files/170000000042155/preview?verifier=npXK1CmONpIoSjDDyJxQ4gLfcrod5HYeZMW0r7ca');
    html = fileText.replaceAll('{CANVAS_CONTENT}', html);
    String uri = Uri.dataFromString(html,
        mimeType: 'text/html',
        encoding: Encoding.getByName('utf-8'))
        .toString();
    this.loadUrl(uri);
  }

  /**
   * Loads html content w/o any change to formatting
   */
  Future<void> loadRawHtml(String html, Map<String, String> headers) async {
    String uri = Uri.dataFromString(html,
        mimeType: 'text/html',
        encoding: Encoding.getByName('utf-8'))
        .toString();
    this.loadUrl(uri);
  }

}

String _applyWorkAroundForDoubleSlashesAsUrlSource(String html) {
  if(html.isEmpty) return "";
  // Fix for embedded videos that have // instead of http://
  html = html.replaceAll("href=\"//", "href=\"https://");
  html = html.replaceAll("href='//", "href='https://");
  html = html.replaceAll("src=\"//", "src=\"https://");
  html = html.replaceAll("src='//", "src='https://");
  return html;
}

String _addProtocolToLinks(String html) {
  if(html.isEmpty) return "";

  html = html.replaceAll("href=\"www.", "href=\"https://www.");
  html = html.replaceAll("href='www.", "href='https://www.");
  html = html.replaceAll("src=\"www.", "src=\"https://www.");
  html = html.replaceAll("src='www.", "src='https://www.");
  return html;
}