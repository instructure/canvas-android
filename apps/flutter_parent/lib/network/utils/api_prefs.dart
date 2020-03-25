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
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';
import 'package:package_info/package_info.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'dio_config.dart';

class ApiPrefs {
  static const String KEY_HAS_MIGRATED = 'has_migrated_from_old_app';
  static const String KEY_LOGINS = 'logins';
  static const String KEY_CURRENT_LOGIN_UUID = 'current_login_uuid';
  static const String KEY_CURRENT_STUDENT = 'current_student';

  static SharedPreferences _prefs;
  static PackageInfo _packageInfo;
  static Login _currentLogin;

  static Future<void> init() async {
    if (_prefs == null) _prefs = await SharedPreferences.getInstance();
    _packageInfo = await PackageInfo.fromPlatform();
  }

  static void clean() {
    _prefs = null;
    _packageInfo = null;
    _currentLogin = null;
  }

  static void _checkInit() {
    if (_prefs == null || _packageInfo == null) throw StateError('ApiPrefs has not been initialized');
  }

  // Login

  static bool isLoggedIn() {
    _checkInit();
    return getAuthToken() != null && getDomain() != null;
  }

  static Login getCurrentLogin() {
    _checkInit();
    if (_currentLogin == null) {
      final currentLoginUuid = getCurrentLoginUuid();
      _currentLogin = getLogins().firstWhere((it) => it.uuid == currentLoginUuid, orElse: () => null);
    }
    return _currentLogin;
  }

  static Future<void> switchLogins(Login login) async {
    _checkInit();
    _currentLogin = login;
    await _prefs.setString(KEY_CURRENT_LOGIN_UUID, login.uuid);
  }

  /// Optionally provide ParentApp (ParentApp.of(context)) as app to rebuild the application for any language changes
  static Future<void> performLogout({bool switchingLogins = false, app}) async {
    _checkInit();

    // Clear network cache
    await DioConfig().clearCache();

    // Perform full-logout tasks if we're not just switching users
    if (!switchingLogins) {
      // Remove reminders
      ReminderDb reminderDb = locator<ReminderDb>();
      final reminders = await reminderDb.getAllForUser(getDomain(), getUser().id);
      final reminderIds = reminders.map((it) => it.id).toList();
      await locator<NotificationUtil>().deleteNotifications(reminderIds);
      await reminderDb.deleteAllForUser(getDomain(), getUser().id);

      // Remove calendar filters
      locator<CalendarFilterDb>().deleteAllForUser(getDomain(), getUser().id);

      // Remove saved Login data
      await removeLoginByUuid(getCurrentLoginUuid());
    }

    // Clear current Login
    await _prefs.remove(KEY_CURRENT_LOGIN_UUID);
    _currentLogin = null;
    app?.rebuild(effectiveLocale());
  }

  static Future<void> saveLogins(List<Login> logins) async {
    _checkInit();
    List<String> jsonList = logins.map((it) => json.encode(serialize(it))).toList();
    await _prefs.setStringList(KEY_LOGINS, jsonList);
  }

  static Future<void> addLogin(Login login) async {
    _checkInit();
    var logins = getLogins();
    logins.removeWhere((it) => it.domain == login.domain && it.user.id == login.user.id); // Remove duplicates
    logins.insert(0, login);
    await saveLogins(logins);
  }

  static List<Login> getLogins() {
    _checkInit();
    return _prefs.getStringList(KEY_LOGINS)?.map((it) => deserialize<Login>(json.decode(it)))?.toList() ?? [];
  }

  static Future<void> removeLogin(Login login) => removeLoginByUuid(login.uuid);

  static Future<void> removeLoginByUuid(String uuid) async {
    _checkInit();
    var logins = getLogins();
    logins.retainWhere((it) => it.uuid != uuid);
    await saveLogins(logins);
  }

  /// Updates the current login. If passing in the root app, it will be rebuilt on locale change.
  static Future<void> updateCurrentLogin(dynamic Function(LoginBuilder) updates, {app}) async {
    _checkInit();
    final login = getCurrentLogin();
    Locale oldLocale = effectiveLocale();
    final updatedLogin = login.rebuild(updates);

    // Save in-memory login
    _currentLogin = updatedLogin;

    // Save persisted login
    List<Login> allLogins = getLogins();
    int currentLoginIndex = allLogins.indexWhere((it) => it.uuid == updatedLogin.uuid);
    if (currentLoginIndex != -1) {
      allLogins[currentLoginIndex] = updatedLogin;
      saveLogins(allLogins);
    }

    // Update locale
    return await new Future<void>.sync(() {
      var newLocale = effectiveLocale();
      if (Intl.defaultLocale != newLocale) {
        app?.rebuild(newLocale);
      }
    });
  }

  static Future<void> setUser(User user, {app}) async {
    await updateCurrentLogin((b) => b..user = user.toBuilder(), app: app);
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
        scriptCode: userLocale[1].length < 5 ? 'inst${userLocale[1]}' : userLocale[1].substring(0, 8),
        countryCode: localeParts.last,
      );
    }
  }

  static String getCurrentLoginUuid() => _getPrefString(KEY_CURRENT_LOGIN_UUID);

  static User getUser() => getCurrentLogin()?.user;

  static String getUserAgent() => 'androidParent/${_packageInfo.version} (${_packageInfo.buildNumber})';

  static String getApiUrl({String path = ''}) => '${getDomain()}/api/v1/$path';

  static String getDomain() => getCurrentLogin()?.domain;

  static String getAuthToken() => getCurrentLogin()?.accessToken;

  static String getRefreshToken() => getCurrentLogin()?.refreshToken;

  static String getClientId() => getCurrentLogin()?.clientId;

  static String getClientSecret() => getCurrentLogin()?.clientSecret;

  static bool getHasMigrated() => _getPrefBool(KEY_HAS_MIGRATED);

  static Future<void> setHasMigrated(bool hasMigrated) => _setPrefBool(KEY_HAS_MIGRATED, hasMigrated);

  static Future<void> _setPrefBool(String key, bool value) async {
    _checkInit();
    await _prefs.setBool(key, value);
  }

  static bool _getPrefBool(String key) {
    _checkInit();
    return _prefs.getBool(key);
  }

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
          .replaceAll('-', ',')
          .replaceAll('_', '-'),
      'User-Agent': getUserAgent(),
    };

    if (extraHeaders != null) {
      headers.addAll(extraHeaders);
    }

    return headers;
  }

  static setCurrentStudent(User currentStudent) {
    _checkInit();
    _prefs.setString(KEY_CURRENT_STUDENT, json.encode(serialize(currentStudent)));
  }

  static User getCurrentStudent() {
    _checkInit();
    final studentJson = _prefs.getString(KEY_CURRENT_STUDENT);
    if (studentJson == null || studentJson.isEmpty) return null;
    return deserialize<User>(json.decode(studentJson));
  }
}
