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

import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_student_embed/models/login.dart';
import 'package:flutter_student_embed/models/user.dart';
import 'package:package_info/package_info.dart';

class ApiPrefs {
  static Login _currentLogin;
  static PackageInfo _packageInfo;

  static Future<void> init() async {
    _packageInfo = await PackageInfo.fromPlatform();
  }

  static void clean() {
    _packageInfo = null;
    _currentLogin = null;
  }

  static void reset() {
    _currentLogin = null;
  }

  static void _checkInit() {
    if (_packageInfo == null) throw StateError('ApiPrefs has not been initialized');
  }

  static Login getCurrentLogin() {
    _checkInit();
    return _currentLogin;
  }

  /// Updates the current login
  static void setLogin(Login login) {
    _currentLogin = login;
  }

  static Locale effectiveLocale() {
    _checkInit();
    User user = getUser();
    List<String> userLocale = (user?.effectiveLocale ?? user?.locale ?? ui.window.locale.toLanguageTag()).split('-x-');

    if (userLocale[0].isEmpty) {
      return null;
    }

    List<String> localeParts = userLocale[0].split('-');
    if (userLocale.length == 1) {
      return Locale(localeParts.first, localeParts.last);
    } else {
      return Locale.fromSubtags(
        languageCode: localeParts.first,
        scriptCode: userLocale[1],
        countryCode: localeParts.last,
      );
    }
  }

  static User getUser() => getCurrentLogin()?.user;

  static String getUserAgent() => 'androidStudent/${_packageInfo.version} (${_packageInfo.buildNumber})';

  static String getApiUrl({String path = ''}) => '${getDomain()}/api/v1/$path';

  static String getDomain() => getCurrentLogin()?.domain;

  static String getAuthToken() => getCurrentLogin()?.accessToken;

  static Map<String, String> getHeaderMap({
    bool forceDeviceLanguage = false,
    String token,
    Map<String, String> extraHeaders,
  }) {
    if (token == null) {
      token = getAuthToken();
    }

    var headers = {
      'Authorization': 'Bearer $token',
      'accept-language': (forceDeviceLanguage ? ui.window.locale.toLanguageTag() : effectiveLocale()?.toLanguageTag())
          .replaceAll('-', ','),
      'User-Agent': getUserAgent(),
    };

    if (extraHeaders != null) {
      headers.addAll(extraHeaders);
    }

    return headers;
  }
}
