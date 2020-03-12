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

import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart' as urlLauncher;

class FlutterLaunchVeneer {
  Future<bool> launch(
    String urlString, {
    bool forceSafariVC,
    bool forceWebView,
    bool enableJavaScript,
    bool enableDomStorage,
    bool universalLinksOnly,
    Map<String, String> headers,
    Brightness statusBarBrightness,
  }) async {
    return await urlLauncher.launch(urlString,
        forceSafariVC: forceSafariVC,
        forceWebView: forceWebView,
        enableJavaScript: enableJavaScript,
        enableDomStorage: enableDomStorage,
        universalLinksOnly: universalLinksOnly,
        headers: headers,
        statusBarBrightness: statusBarBrightness);
  }

  Future<bool> canLaunch(String urlString) async {
    return await urlLauncher.canLaunch(urlString);
  }

  Future<void> closeWebView() async {
    return await urlLauncher.closeWebView();
  }
}
