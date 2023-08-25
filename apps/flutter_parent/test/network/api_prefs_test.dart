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
import 'dart:ui';

import 'package:encrypted_shared_preferences/encrypted_shared_preferences.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../utils/canvas_model_utils.dart';
import '../utils/platform_config.dart';
import '../utils/test_app.dart';
import '../utils/test_helpers/mock_helpers.dart';
import '../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  tearDown(() {
    ApiPrefs.clean();
  });

  test('is logged in returns false', () async {
    await setupPlatformChannels();
    expect(ApiPrefs.isLoggedIn(), false);
  });

  test('is logged in returns false with no login uuid', () async {
    var login = Login((b) => b
      ..domain = 'domain'
      ..accessToken = 'token'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig());
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.isLoggedIn(), false);
  });

  test('is logged in returns false with no valid uuid but no matching login', () async {
    var login = Login((b) => b
      ..uuid = 'uuid'
      ..domain = 'domain'
      ..accessToken = 'token'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: 'other_uuid'}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.isLoggedIn(), false);
  });

  test('is logged in returns true with a valid uuid and matching login', () async {
    var login = Login((b) => b
      ..domain = 'domain'
      ..accessToken = 'token'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.isLoggedIn(), true);
  });

  test('is logged in returns false with an empty token', () async {
    var login = Login((b) => b
      ..domain = 'domain'
      ..accessToken = ''
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.isLoggedIn(), false);
  });

  test('is logged in returns false with an empty domain', () async {
    var login = Login((b) => b
      ..domain = ''
      ..accessToken = 'token'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.isLoggedIn(), false);
  });

  test('getApiUrl returns the domain with the api path added', () async {
    var login = Login((b) => b
      ..domain = 'domain'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.getApiUrl(), 'domain/api/v1/');
  });

  test('switchLogins updates current login data', () async {
    await setupPlatformChannels();

    Login user = Login((b) => b
      ..accessToken = 'token'
      ..refreshToken = 'refresh'
      ..domain = 'domain'
      ..clientId = 'clientId'
      ..clientSecret = 'clientSecret'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());

    ApiPrefs.switchLogins(user);

    expect(ApiPrefs.getAuthToken(), user.accessToken);
    expect(ApiPrefs.getRefreshToken(), user.refreshToken);
    expect(ApiPrefs.getDomain(), user.domain);
    expect(ApiPrefs.getClientId(), user.clientId);
    expect(ApiPrefs.getClientSecret(), user.clientSecret);
    expect(ApiPrefs.getUser(), user.user);
  });

  test('perform logout clears out data', () async {
    var login = Login((b) => b
      ..domain = 'domain'
      ..accessToken = 'accessToken'
      ..refreshToken = 'refreshToken'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());

    await setupPlatformChannels(
        config: PlatformConfig(
      mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid},
    ));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.getDomain(), login.domain);
    expect(ApiPrefs.getAuthToken(), login.accessToken);
    expect(ApiPrefs.getRefreshToken(), login.refreshToken);
    expect(ApiPrefs.getCurrentLoginUuid(), login.uuid);

    await ApiPrefs.performLogout(switchingLogins: true);

    expect(ApiPrefs.getDomain(), null);
    expect(ApiPrefs.getAuthToken(), null);
    expect(ApiPrefs.getRefreshToken(), null);
    expect(ApiPrefs.getCurrentLoginUuid(), null);
  });

  test('perform logout clears out reminders and calendar filters, and deletes auth token', () async {
    final reminderDb = MockReminderDb();
    final calendarFilterDb = MockCalendarFilterDb();
    final notificationUtil = MockNotificationUtil();
    final authApi = MockAuthApi();
    setupTestLocator((locator) {
      locator.registerLazySingleton<ReminderDb>(() => reminderDb);
      locator.registerLazySingleton<AuthApi>(() => authApi);
      locator.registerLazySingleton<CalendarFilterDb>(() => calendarFilterDb);
      locator.registerLazySingleton<NotificationUtil>(() => notificationUtil);
    });

    final reminder = Reminder((b) => b..id = 1234);
    when(reminderDb.getAllForUser(any, any)).thenAnswer((_) async => [reminder]);

    var login = Login((b) => b
      ..domain = 'domain'
      ..accessToken = 'accessToken'
      ..refreshToken = 'refreshToken'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());

    await setupPlatformChannels(
        config: PlatformConfig(
      mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid},
    ));
    await ApiPrefs.addLogin(login);
    await ApiPrefs.performLogout(switchingLogins: false);

    verify(reminderDb.getAllForUser(login.domain, login.user.id));
    verify(notificationUtil.deleteNotifications([reminder.id!]));
    verify(reminderDb.deleteAllForUser(login.domain, login.user.id));
    verify(calendarFilterDb.deleteAllForUser(login.domain, login.user.id));
    verify(authApi.deleteToken(login.domain, login.accessToken));
  });

  test('isMasquerading returns false if not masquerading', () async {
    final login = Login();
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.isMasquerading(), isFalse);
  });

  test('isMasquerading returns true if masquerading', () async {
    final login = Login((b) => b
      ..masqueradeDomain = 'masqueradeDomain'
      ..masqueradeUser = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.isMasquerading(), isTrue);
  });

  test('setting user updates stored user', () async {
    final login = Login();
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    final user = CanvasModelTestUtils.mockUser();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getUser(), user);
  });

  test('setting user updates stored masqueradeUser if masquerading', () async {
    final login = Login((b) => b
      ..masqueradeDomain = 'masqueradeDomain'
      ..masqueradeUser = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    final user = CanvasModelTestUtils.mockUser();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getCurrentLogin()?.masqueradeUser, user);
  });

  test('setting user updates with new locale rebuilds the app', () async {
    final login = Login();
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));

    expect(ApiPrefs.getUser(), null);

    await ApiPrefs.addLogin(login);

    final user = CanvasModelTestUtils.mockUser();
    final app = _MockApp();
    await ApiPrefs.setUser(user, app: app);

    verify(app.rebuild(any)).called(1);
  });

  test('effectiveLocale returns the devices locale', () async {
    await setupPlatformChannels();

    final deviceLocale = window.locale.toLanguageTag();

    final localeParts = deviceLocale.split('-');
    expect(ApiPrefs.effectiveLocale(), Locale(localeParts.first, localeParts.last));
  });

  test('effectiveLocale returns the users effective locale', () async {
    final login = Login();
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    final user = CanvasModelTestUtils.mockUser();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.effectiveLocale(), Locale(user.effectiveLocale!));
  });

  test('effectiveLocale returns the users locale if effective locale is null', () async {
    final login = Login();
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    final user = CanvasModelTestUtils.mockUser().rebuild((b) => b
      ..effectiveLocale = null
      ..locale = 'jp');

    await ApiPrefs.setUser(user);

    expect(ApiPrefs.effectiveLocale(), Locale(user.locale!));
  });

  test('effectiveLocale returns the users effective locale without inst if script is longer than 5', () async {
    final login = Login();
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    final user = CanvasModelTestUtils.mockUser().rebuild((b) => b..effectiveLocale = 'en-AU-x-unimelb');

    await ApiPrefs.setUser(user);

    expect(
        ApiPrefs.effectiveLocale(), Locale.fromSubtags(languageCode: 'en', countryCode: 'AU', scriptCode: 'unimelb'));
  });

  test('effectiveLocale returns the users effective locale with inst if script is less than 5', () async {
    final login = Login();
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    final user = CanvasModelTestUtils.mockUser().rebuild((b) => b..effectiveLocale = 'en-GB-x-ukhe');

    await ApiPrefs.setUser(user);

    expect(
        ApiPrefs.effectiveLocale(), Locale.fromSubtags(languageCode: 'en', countryCode: 'GB', scriptCode: 'instukhe'));
  });

  test('getUser returns null', () async {
    await setupPlatformChannels();
    expect(ApiPrefs.getUser(), null);
  });

  test('getUser returns masquerade user if masquerading', () async {
    final masqueradeUser = CanvasModelTestUtils.mockUser();
    final login = Login((b) => b
      ..masqueradeDomain = 'masqueradeDomain'
      ..masqueradeUser = masqueradeUser.toBuilder());
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));

    expect(ApiPrefs.getUser(), masqueradeUser);
  });

  test('getDomain returns masquerade domain if masquerading', () async {
    final masqueradeDomain = 'masqueradeDomain';
    final login = Login((b) => b
      ..domain = 'domain'
      ..masqueradeDomain = masqueradeDomain
      ..masqueradeUser = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.getDomain(), masqueradeDomain);
  });

  test('getHeaderMap returns a map with the accept-language from prefs', () async {
    final login = Login();
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    final user = CanvasModelTestUtils.mockUser().rebuild((b) => b..effectiveLocale = 'en-US');
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getHeaderMap()['accept-language'], 'en,US');
  });

  test('getHeaderMap returns a map with the accept-language from device', () async {
    final login = Login();
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    final deviceLocale = window.locale;
    final user = CanvasModelTestUtils.mockUser().rebuild((b) => b..effectiveLocale = 'ar');
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getHeaderMap(forceDeviceLanguage: true)['accept-language'],
        deviceLocale.toLanguageTag().replaceAll('-', ','));
  });

  test('getHeaderMap returns a map with the token from prefs', () async {
    var login = Login((b) => b
      ..accessToken = 'token'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.getHeaderMap()['Authorization'], 'Bearer token');
  });

  test('getHeaderMap returns a map with the token passed in', () async {
    var login = Login((b) => b
      ..accessToken = 'token'
      ..user = CanvasModelTestUtils.mockUser().toBuilder());
    await setupPlatformChannels(config: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    expect(ApiPrefs.getHeaderMap(token: 'other token')['Authorization'], 'Bearer other token');
  });

  test('getHeaderMap returns a map with the correct user-agent from prefs', () async {
    await setupPlatformChannels();
    var info = await PackageInfo.fromPlatform();
    var userAgent = 'androidParent/${info.version} (${info.buildNumber})';

    expect(ApiPrefs.getUserAgent(), userAgent);
    expect(ApiPrefs.getHeaderMap()['User-Agent'], ApiPrefs.getUserAgent());
  });

  test('getHeaderMap returns a map with the extra headers passed in', () async {
    await setupPlatformChannels();

    final map = {'key': 'value'};

    expect(ApiPrefs.getHeaderMap(extraHeaders: map)['key'], 'value');
  });

  test('gets and sets hasMigrated', () async {
    await setupPlatformChannels();
    await ApiPrefs.setHasMigrated(true);

    expect(ApiPrefs.getHasMigrated(), isTrue);
  });

  test('gets and sets selected student', () async {
    await setupPlatformChannels();
    User user = CanvasModelTestUtils.mockUser();
    await ApiPrefs.setCurrentStudent(user);

    expect(ApiPrefs.getCurrentStudent(), user);
  });

  test('gets and sets camera count', () async {
    await setupPlatformChannels();
    final count = 2;
    await ApiPrefs.setCameraCount(count);

    expect(ApiPrefs.getCameraCount(), count);
  });

  test('migrates old prefs to encrypted prefs', () async {
    // Setup platform channels with minimal configuration
    setupPlatformChannels(config: PlatformConfig(mockApiPrefs: null));

    // Values
    final logins = List.generate(3, (i) {
      return Login((b) => b
        ..user = CanvasModelTestUtils.mockUser(id: i.toString(), name: 'Test Login $i').toBuilder()
        ..accessToken = 'access_$i'
        ..refreshToken = 'refresh_$i'
        ..clientSecret = 'client_secret'
        ..clientId = 'client_id'
        ..domain = 'domain'
        ..uuid = i.toString());
    });
    final hasMigrated = true;
    final hasCheckedOldReminders = true;
    final currentLoginId = '123';
    final currentStudent = CanvasModelTestUtils.mockUser();

    // Setup
    final config = PlatformConfig(mockPrefs: {
      ApiPrefs.KEY_LOGINS: logins.map((it) => json.encode(serialize<Login>(it))).toList(),
      ApiPrefs.KEY_HAS_MIGRATED: hasMigrated,
      ApiPrefs.KEY_HAS_CHECKED_OLD_REMINDERS: hasCheckedOldReminders,
      ApiPrefs.KEY_CURRENT_LOGIN_UUID: currentLoginId,
      ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize<User>(currentStudent)),
    });

    SharedPreferences.setMockInitialValues(config.mockPrefs!);
    EncryptedSharedPreferences.setMockInitialValues({});

    final oldPrefs = await SharedPreferences.getInstance();
    final encryptedPrefs = await EncryptedSharedPreferences.getInstance();

    // Old prefs should not be null
    expect(
        oldPrefs.getStringList(ApiPrefs.KEY_LOGINS)!.map((it) => deserialize<Login>(json.decode(it))).toList(), logins);
    expect(oldPrefs.getBool(ApiPrefs.KEY_HAS_MIGRATED), hasMigrated);
    expect(oldPrefs.getBool(ApiPrefs.KEY_HAS_CHECKED_OLD_REMINDERS), hasCheckedOldReminders);
    expect(oldPrefs.getString(ApiPrefs.KEY_CURRENT_LOGIN_UUID), currentLoginId);
    expect(deserialize<User>(json.decode(oldPrefs.get(ApiPrefs.KEY_CURRENT_STUDENT) as String)), currentStudent);

    // Actually do the test, init api prefs so we get the migration
    await ApiPrefs.init();

    // encryptedPrefs should be not null
    expect(encryptedPrefs.getStringList(ApiPrefs.KEY_LOGINS).map((it) => deserialize<Login>(json.decode(it))).toList(), logins);
    expect(encryptedPrefs.getBool(ApiPrefs.KEY_HAS_MIGRATED), hasMigrated);
    expect(encryptedPrefs.getBool(ApiPrefs.KEY_HAS_CHECKED_OLD_REMINDERS), hasCheckedOldReminders);
    expect(encryptedPrefs.getString(ApiPrefs.KEY_CURRENT_LOGIN_UUID), currentLoginId);
    expect(deserialize<User>(json.decode(encryptedPrefs.get(ApiPrefs.KEY_CURRENT_STUDENT))), currentStudent);

    // Old prefs should be cleared out
    expect(oldPrefs.get(ApiPrefs.KEY_LOGINS), isNull);
    expect(oldPrefs.get(ApiPrefs.KEY_HAS_MIGRATED), isNull);
    expect(oldPrefs.get(ApiPrefs.KEY_HAS_CHECKED_OLD_REMINDERS), isNull);
    expect(oldPrefs.get(ApiPrefs.KEY_CURRENT_LOGIN_UUID), isNull);
    expect(oldPrefs.get(ApiPrefs.KEY_CURRENT_STUDENT), isNull);
  });
}

abstract class _Rebuildable {
  void rebuild(Locale? locale);
}

class _MockApp extends Mock implements _Rebuildable {}
