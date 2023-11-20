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
import 'dart:math';
import 'dart:ui' as ui;

import 'package:collection/collection.dart';
import 'package:encrypted_shared_preferences/encrypted_shared_preferences.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/screens/web_login/web_login_screen.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:intl/intl.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:tuple/tuple.dart';

import 'dio_config.dart';

class ApiPrefs {
  static const String KEY_CAMERA_COUNT = 'camera_count';
  static const String KEY_CURRENT_LOGIN_UUID = 'current_login_uuid';
  static const String KEY_CURRENT_STUDENT = 'current_student';
  static const String KEY_HAS_MIGRATED = 'has_migrated_from_old_app';
  static const String KEY_HAS_CHECKED_OLD_REMINDERS = 'has_checked_old_reminders';
  static const String KEY_HAS_MIGRATED_TO_ENCRYPTED_PREFS = 'has_migrated_to_encrypted_prefs';
  static const String KEY_LOGINS = 'logins';
  static const String KEY_RATING_DONT_SHOW_AGAIN = 'dont_show_again';
  static const String KEY_RATING_NEXT_SHOW_DATE = 'next_show_date';
  static const String KEY_LAST_ACCOUNT = 'last_account';
  static const String KEY_LAST_ACCOUNT_LOGIN_FLOW = 'last_account_login_flow';

  static EncryptedSharedPreferences? _prefs;
  static PackageInfo? _packageInfo;
  static Login? _currentLogin;

  static Future<void> init() async {
    if (_prefs == null) _prefs = await EncryptedSharedPreferences.getInstance();
    _packageInfo = await PackageInfo.fromPlatform();
    await _migrateToEncryptedPrefs();
  }

  static Future<void> _migrateToEncryptedPrefs() async {
    if (_prefs?.getBool(KEY_HAS_MIGRATED_TO_ENCRYPTED_PREFS) ?? false) {
      return;
    }

    // Set the bool flag so we don't migrate multiple times
    await _prefs?.setBool(KEY_HAS_MIGRATED_TO_ENCRYPTED_PREFS, true);

    final oldPrefs = await SharedPreferences.getInstance();

    await _prefs?.setStringList(KEY_LOGINS, oldPrefs.getStringList(KEY_LOGINS));
    await oldPrefs.remove(KEY_LOGINS);

    await _prefs?.setBool(KEY_HAS_MIGRATED, oldPrefs.getBool(KEY_HAS_MIGRATED));
    await oldPrefs.remove(KEY_HAS_MIGRATED);

    await _prefs?.setBool(KEY_HAS_CHECKED_OLD_REMINDERS, oldPrefs.getBool(KEY_HAS_CHECKED_OLD_REMINDERS));
    await oldPrefs.remove(KEY_HAS_CHECKED_OLD_REMINDERS);

    await _prefs?.setString(KEY_CURRENT_LOGIN_UUID, oldPrefs.getString(KEY_CURRENT_LOGIN_UUID));
    await oldPrefs.remove(KEY_CURRENT_LOGIN_UUID);

    await _prefs?.setString(KEY_CURRENT_STUDENT, oldPrefs.getString(KEY_CURRENT_STUDENT));
    await oldPrefs.remove(KEY_CURRENT_STUDENT);
  }

  static Future<void> clean() async {
    _prefs?.clear();
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
    final token = getAuthToken() ?? '';
    final domain = getDomain() ?? '';
    return token.isNotEmpty && domain.isNotEmpty;
  }

  static Login? getCurrentLogin() {
    _checkInit();
    if (_currentLogin == null) {
      final currentLoginUuid = getCurrentLoginUuid();
      _currentLogin = getLogins().firstWhereOrNull((it) => it.uuid == currentLoginUuid);
    }
    return _currentLogin;
  }

  static Future<void> switchLogins(Login login) async {
    _checkInit();
    _currentLogin = login;
    await _prefs?.setString(KEY_CURRENT_LOGIN_UUID, login.uuid);
  }

  static bool isMasquerading() {
    _checkInit();
    return getCurrentLogin()?.isMasquerading == true;
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
      final reminders = await reminderDb.getAllForUser(getDomain(), getUser()?.id);
      final reminderIds = reminders?.map((it) => it.id).toList().nonNulls.toList() ?? [];
      await locator<NotificationUtil>().deleteNotifications(reminderIds);
      await reminderDb.deleteAllForUser(getDomain(), getUser()?.id);

      // Remove calendar filters
      locator<CalendarFilterDb>().deleteAllForUser(getDomain(), getUser()?.id);

      // Remove saved Login data
      await removeLoginByUuid(getCurrentLoginUuid());
    }

    // Clear current Login
    await _prefs!.remove(KEY_CURRENT_LOGIN_UUID);
    _currentLogin = null;
    app?.rebuild(effectiveLocale());
  }

  static Future<void> saveLogins(List<Login> logins) async {
    _checkInit();
    List<String> jsonList = logins.map((it) => json.encode(serialize(it))).toList();
    await _prefs!.setStringList(KEY_LOGINS, jsonList);
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
    var list = _prefs!.getStringList(KEY_LOGINS);
    return list.map((it) => deserialize<Login>(json.decode(it))).nonNulls.toList();
  }

  static setLastAccount(SchoolDomain lastAccount, LoginFlow loginFlow) {
    _checkInit();
    final lastAccountJson = json.encode(serialize(lastAccount));
    _prefs!.setString(KEY_LAST_ACCOUNT, lastAccountJson);
    _prefs!.setInt(KEY_LAST_ACCOUNT_LOGIN_FLOW, loginFlow.index);
  }

  static Tuple2<SchoolDomain, LoginFlow>? getLastAccount() {
    _checkInit();
    if (!_prefs!.containsKey(KEY_LAST_ACCOUNT)) return null;

    final accountJson = _prefs!.getString(KEY_LAST_ACCOUNT);
    if (accountJson == null || accountJson.isEmpty == true) return null;

    final lastAccount = deserialize<SchoolDomain>(json.decode(accountJson));
    int? lastLogin = _prefs!.getInt(KEY_LAST_ACCOUNT_LOGIN_FLOW);
    if (lastLogin == null) return null;
    final loginFlow = _prefs!.containsKey(KEY_LAST_ACCOUNT_LOGIN_FLOW) ? LoginFlow.values[lastLogin] : LoginFlow.normal;

    return Tuple2(lastAccount!, loginFlow);
  }

  static Future<void> removeLogin(Login login) => removeLoginByUuid(login.uuid);

  static Future<void> removeLoginByUuid(String? uuid) async {
    _checkInit();
    var logins = getLogins();
    Login? login = logins.firstWhereOrNull((it) => it.uuid == uuid);
    if (login != null) {
      // Delete token (fire and forget - no need to await)
      locator<AuthApi>().deleteToken(login.domain, login.accessToken);
      logins.retainWhere((it) => it.uuid != uuid);
      await saveLogins(logins);
    }
  }

  /// Updates the current login. If passing in the root app, it will be rebuilt on locale change.
  static Future<void> updateCurrentLogin(dynamic Function(LoginBuilder) updates, {app}) async {
    _checkInit();
    final login = getCurrentLogin();
    if (login == null) return;
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
    if (isMasquerading()) {
      await updateCurrentLogin((b) => b..masqueradeUser = user.toBuilder(), app: app);
    } else {
      await updateCurrentLogin((b) => b..user = user.toBuilder(), app: app);
    }
  }

  static Locale? effectiveLocale() {
    _checkInit();
    User? user = getUser();
    List<String> userLocale = (user?.effectiveLocale ?? user?.locale ?? ui.window.locale.toLanguageTag()).split('-x-');

    if (userLocale[0].isEmpty) {
      return null;
    }

    List<String> localeParts = userLocale[0].split('-');
    final countryCode = localeParts.length > 1 ? localeParts.last : null;

    if (userLocale.length == 1) {
      return Locale(localeParts.first, countryCode);
    } else {
      // Custom language pack
      return Locale.fromSubtags(
        languageCode: localeParts.first,
        scriptCode: userLocale[1].length < 5
            ? 'inst${userLocale[1]}' // da-k12 -> da-instk12 (can't be less than 4 characters)
            : userLocale[1].substring(0, min(8, userLocale[1].length)), // en-unimelb -> en-unimelb (no more than 8)
        countryCode: countryCode,
      );
    }
  }

  /// Prefs

  static String? getCurrentLoginUuid() => _getPrefString(KEY_CURRENT_LOGIN_UUID);

  static User? getUser() => getCurrentLogin()?.currentUser;

  static String getUserAgent() => 'androidParent/${_packageInfo?.version} (${_packageInfo?.buildNumber})';

  static String getApiUrl({String path = ''}) => '${getDomain()}/api/v1/$path';

  static String? getDomain() => getCurrentLogin()?.currentDomain;

  static String? getAuthToken() => getCurrentLogin()?.accessToken;

  static String? getRefreshToken() => getCurrentLogin()?.refreshToken;

  static String? getClientId() => getCurrentLogin()?.clientId;

  static String? getClientSecret() => getCurrentLogin()?.clientSecret;

  static bool getHasMigrated() => _getPrefBool(KEY_HAS_MIGRATED) ?? false;

  static Future<void> setHasMigrated(bool? hasMigrated) => _setPrefBool(KEY_HAS_MIGRATED, hasMigrated);

  static bool getHasCheckedOldReminders() => _getPrefBool(KEY_HAS_CHECKED_OLD_REMINDERS) ?? false;

  static Future<void> setHasCheckedOldReminders(bool checked) => _setPrefBool(KEY_HAS_CHECKED_OLD_REMINDERS, checked);

  static int? getCameraCount() => _getPrefInt(KEY_CAMERA_COUNT);

  static Future<void> setCameraCount(int? count) => _setPrefInt(KEY_CAMERA_COUNT, count);

  static DateTime? getRatingNextShowDate() {
    final nextShow = _getPrefString(KEY_RATING_NEXT_SHOW_DATE);
    if (nextShow == null) return null;
    return DateTime.parse(nextShow);
  }

  static Future<void> setRatingNextShowDate(DateTime? nextShowDate) =>
      _setPrefString(KEY_RATING_NEXT_SHOW_DATE, nextShowDate?.toIso8601String());

  static bool? getRatingDontShowAgain() => _getPrefBool(KEY_RATING_DONT_SHOW_AGAIN);

  static Future<void> setRatingDontShowAgain(bool? dontShowAgain) =>
      _setPrefBool(KEY_RATING_DONT_SHOW_AGAIN, dontShowAgain);

  /// Pref helpers

  static Future<bool> _setPrefBool(String key, bool? value) async {
    _checkInit();
    if (value == null) return _prefs!.remove(key);
    return _prefs!.setBool(key, value);
  }

  static bool? _getPrefBool(String key) {
    _checkInit();
    return _prefs?.getBool(key);
  }

  static Future<bool> _setPrefString(String key, String? value) async {
    _checkInit();
    if (value == null) return _prefs!.remove(key);

    return _prefs!.setString(key, value);
  }

  static String? _getPrefString(String key) {
    _checkInit();
    return _prefs?.getString(key);
  }

  static int? _getPrefInt(String key) {
    _checkInit();
    return _prefs?.getInt(key);
  }

  static Future<bool> _setPrefInt(String key, int? value) async {
    _checkInit();
    if (value == null) return _prefs!.remove(key);
    return _prefs!.setInt(key, value);
  }

  static Future<bool> _removeKey(String key) async {
    _checkInit();
    return _prefs!.remove(key);
  }

  /// Utility functions

  static Map<String, String> getHeaderMap({
    bool forceDeviceLanguage = false,
    String? token = null,
    Map<String, String>? extraHeaders = null,
  }) {
    if (token == null) {
      token = getAuthToken();
    }

    var headers = {
      'Authorization': 'Bearer $token',
      'accept-language': (forceDeviceLanguage ? ui.window.locale.toLanguageTag() : effectiveLocale()?.toLanguageTag())?.replaceAll('-', ',').replaceAll('_', '-') ?? '',
      'User-Agent': getUserAgent(),
    };

    if (extraHeaders != null) {
      headers.addAll(extraHeaders);
    }

    return headers;
  }

  static setCurrentStudent(User? currentStudent) {
    _checkInit();
    if (currentStudent == null) {
      _prefs!.remove(KEY_CURRENT_STUDENT);
    } else {
      _prefs!.setString(KEY_CURRENT_STUDENT, json.encode(serialize(currentStudent)));
    }
  }

  static User? getCurrentStudent() {
    _checkInit();
    final studentJson = _prefs?.getString(KEY_CURRENT_STUDENT);
    if (studentJson == null || studentJson.isEmpty) return null;
    return deserialize<User>(json.decode(studentJson));
  }
}
