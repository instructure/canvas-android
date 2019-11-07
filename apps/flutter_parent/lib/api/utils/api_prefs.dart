/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:convert';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter_parent/api/auth_api.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:shared_preferences/shared_preferences.dart';

class ApiPrefs {
  static const String KEY_TOKEN = 'token';
  static const String KEY_DOMAIN = 'domain';
  static const String KEY_USER = 'user';

  static SharedPreferences _prefs;

  static Future<void> init() async {
    if (_prefs == null) _prefs = await SharedPreferences.getInstance();
  }

  static void clean() {
    _prefs = null;
  }

  static void _checkInit() {
    if (_prefs == null) throw StateError("ApiPrefs has not been initialized");
  }

  // Login

  static bool isLoggedIn() {
    _checkInit();
    return getAuthToken() != null && getDomain() != null;
  }

  static Future<void> performLogin(MobileVerifyResult verifyResult, String code) async {
    await init();
    String token = await locator<AuthApi>().getToken(verifyResult, code);
    _prefs.setString(KEY_TOKEN, token);
    _prefs.setString(KEY_DOMAIN, verifyResult.baseUrl);
  }

  static Future<void> performLogout() async {
    await init();
    return await new Future<void>.sync(() {
      _prefs.remove(KEY_TOKEN);
      _prefs.remove(KEY_DOMAIN);
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

  static String getUserAgent() => "androidParent/2.0.4 (21)";

  static String getApiUrl() => "${getDomain()}/api/v1/";

  static String getDomain() {
    _checkInit();
    return _prefs.getString(KEY_DOMAIN);
  }

  static String getAuthToken() {
    _checkInit();
    return _prefs.getString(KEY_TOKEN);
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
      'accept-language': forceDeviceLanguage ? ui.window.locale.toLanguageTag() : effectiveLocale()?.toLanguageTag(),
      'User-Agent': getUserAgent(),
    };

    if (extraHeaders != null) {
      headers.addAll(extraHeaders);
    }

    return headers;
  }
}
