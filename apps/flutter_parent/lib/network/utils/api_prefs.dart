// Copyright (C) 2019 - present Instructure, Inc.
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

import 'dart:convert';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:intl/intl.dart';
import 'package:package_info/package_info.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ApiPrefs {
  static const String KEY_USER = 'user';
  static const String KEY_DOMAIN = 'domain';

  static const String KEY_ACCESS_TOKEN = 'access_token';
  static const String KEY_REFRESH_TOKEN = 'refresh_token';

  static const String KEY_CLIENT_ID = 'client_id';
  static const String KEY_CLIENT_SECRET = 'client_secret';

  static SharedPreferences _prefs;
  static PackageInfo _packageInfo;

  static Future<void> init() async {
    if (_prefs == null) _prefs = await SharedPreferences.getInstance();
    _packageInfo = await PackageInfo.fromPlatform();
  }

  static void clean() {
    _prefs = null;
    _packageInfo = null;
  }

  static void _checkInit() {
    if (_prefs == null || _packageInfo == null) throw StateError("ApiPrefs has not been initialized");
  }

  // Login

  static bool isLoggedIn() {
    _checkInit();
    return getAuthToken() != null && getDomain() != null;
  }

  static Future<void> updateLoginInfo(CanvasToken tokens, MobileVerifyResult verifyResult) async {
    await init();
    _prefs.setString(KEY_ACCESS_TOKEN, tokens.accessToken);
    _prefs.setString(KEY_REFRESH_TOKEN, tokens.refreshToken);
    _prefs.setString(KEY_DOMAIN, verifyResult.baseUrl);
    _prefs.setString(KEY_CLIENT_ID, verifyResult.clientId);
    _prefs.setString(KEY_CLIENT_SECRET, verifyResult.clientSecret);

    if (tokens.user != null) {
      setUser(tokens.user);
    }
  }

  static Future<void> performLogout() async {
    await init();
    await DioConfig().clearCache();
    return await new Future<void>.sync(() {
      _prefs.remove(KEY_ACCESS_TOKEN);
      _prefs.remove(KEY_REFRESH_TOKEN);
      _prefs.remove(KEY_DOMAIN);
      _prefs.remove(KEY_USER);
    });
  }

  /// Set the user. If passing in the root app, it will be rebuilt on locale change.
  static Future<void> setUser(User user, {app}) async {
    _checkInit();
    Locale oldLocale = effectiveLocale();
    return await new Future<void>.sync(() {
      _prefs.setString(KEY_USER, json.encode(serialize(user)));
      var newLocale = effectiveLocale();
      if (oldLocale != effectiveLocale()) {
        Intl.defaultLocale = effectiveLocale().toLanguageTag();
        app?.rebuild(newLocale);
      }
    });
  }

  static Locale effectiveLocale() {
    _checkInit();
    User user = _prefs.containsKey(KEY_USER) ? deserialize<User>(json.decode(_prefs.getString(KEY_USER))) : null;
    List<String> userLocale = (user?.effectiveLocale ?? user?.locale ?? ui.window.locale.toLanguageTag()).split("-x-");

    if (userLocale[0].isEmpty) {
      return null;
    }

    List<String> localeParts = userLocale[0].split("-");
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

  static User getUser() {
    _checkInit();
    return _prefs.containsKey(KEY_USER) ? deserialize<User>(jsonDecode(_prefs.getString(KEY_USER))) : null;
  }

  static String getUserAgent() => 'androidParent/${_packageInfo.version} (${_packageInfo.buildNumber})';

  static String getApiUrl({String path = ''}) => "${getDomain()}/api/v1/$path";

  static String getDomain() => _getPrefString(KEY_DOMAIN);

  static String getAuthToken() => _getPrefString(KEY_ACCESS_TOKEN);

  static String getRefreshToken() => _getPrefString(KEY_REFRESH_TOKEN);

  static String getClientId() => _getPrefString(KEY_CLIENT_ID);

  static String getClientSecret() => _getPrefString(KEY_CLIENT_SECRET);

  static String _getPrefString(String key) {
    _checkInit();
    return _prefs.getString(key);
  }

  static Map<String, String> getHeaderMap({
    bool forceDeviceLanguage = false,
    String token = null,
    Map<String, String> extraHeaders = null,
  }) {
    if (token == null) {
      token = getAuthToken();
    }

    var headers = {
      'Authorization': 'Bearer $token',
      'accept-language': (forceDeviceLanguage ? ui.window.locale.toLanguageTag() : effectiveLocale()?.toLanguageTag())
          .replaceAll("-", ","),
      'User-Agent': getUserAgent(),
    };

    if (extraHeaders != null) {
      headers.addAll(extraHeaders);
    }

    return headers;
  }
}
