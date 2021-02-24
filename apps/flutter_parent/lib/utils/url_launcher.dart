// Copyright (C) 2020 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:io';

import 'package:flutter/services.dart';
import 'package:url_launcher/url_launcher.dart' as UrlLauncherExt;

class UrlLauncher {
  static const channelName = 'com.instructure.parentapp/url_launcher';
  static const launchMethod = 'launch';
  static const canLaunchMethod = 'canLaunch';

  MethodChannel channel = MethodChannel(channelName);

  Future<bool> canLaunch(String url, {bool excludeInstructure = true}) async {
    // TODO Currently using a custom url launcher for Android to exclude canvas apps, but using the plugin on iOS
    // Could be better to use the same solution.
    if (Platform.isAndroid) {
      return channel.invokeMethod<bool>(
        canLaunchMethod,
        <String, Object>{
          'url': url,
          'excludeInstructure': excludeInstructure,
        },
      );
    } else {
      return await UrlLauncherExt.canLaunch(url);
    }
  }

  Future<void> launch(String url, {bool excludeInstructure = true}) async {
    if (Platform.isAndroid) {
      return channel.invokeMethod<void>(
        launchMethod,
        <String, Object>{
          'url': url,
          'excludeInstructure': excludeInstructure,
        },
      );  
    } else {
      return await UrlLauncherExt.launch(url);
    }
  }

  Future<void> launchAppStore() => launch('https://play.google.com/store/apps/details?id=com.instructure.parentapp');
}
