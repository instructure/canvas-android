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

import 'package:flutter/material.dart';
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/user.dart';

import 'api_prefs.dart';

/// Mockable wrapper for ApiPrefs to be accessed via service locator
class ApiPrefsWrapper {
  Future<void> init() => ApiPrefs.init();

  void clean() => ApiPrefs.clean();

  bool isLoggedIn() => ApiPrefs.isLoggedIn();

  Future<void> updateLoginInfo(CanvasToken tokens, MobileVerifyResult verifyResult) =>
      ApiPrefs.updateLoginInfo(tokens, verifyResult);

  Future<void> performLogout() => ApiPrefs.performLogout();

  Future<void> setUser(User user, {app}) => ApiPrefs.setUser(user, app: app);

  Locale effectiveLocale() => ApiPrefs.effectiveLocale();

  User getUser() => ApiPrefs.getUser();

  String getUserAgent() => ApiPrefs.getUserAgent();

  String getApiUrl({String path = ''}) => ApiPrefs.getApiUrl(path: path);

  String getDomain() => ApiPrefs.getDomain();

  String getAuthToken() => ApiPrefs.getAuthToken();

  String getRefreshToken() => ApiPrefs.getRefreshToken();

  String getClientId() => ApiPrefs.getClientId();

  String getClientSecret() => ApiPrefs.getClientSecret();

  Map<String, String> getHeaderMap({
    bool forceDeviceLanguage = false,
    String token = null,
    Map<String, String> extraHeaders = null,
  }) =>
      ApiPrefs.getHeaderMap(forceDeviceLanguage: forceDeviceLanguage, token: token, extraHeaders: extraHeaders);
}
