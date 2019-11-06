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

import 'dart:ui';

import 'package:flutter_parent/api/auth_api.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:mockito/mockito.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:test/test.dart';

void main() {
  tearDown(() {
    ApiPrefs.clean();
  });

  test('is logged in throws error if not initiailzed', () {
    expect(() => ApiPrefs.isLoggedIn(), throwsStateError);
  });

  test('is logged in returns false', () async {
    SharedPreferences.setMockInitialValues({});

    await ApiPrefs.init();
    expect(ApiPrefs.isLoggedIn(), false);
  });

  test('is logged in returns false with no domain', () async {
    SharedPreferences.setMockInitialValues({_debugKey(ApiPrefs.KEY_TOKEN): 'token'});

    await ApiPrefs.init();
    expect(ApiPrefs.isLoggedIn(), false);
  });

  test('is logged in returns true with a token and domain', () async {
    SharedPreferences.setMockInitialValues({
      _debugKey(ApiPrefs.KEY_TOKEN): 'token',
      _debugKey(ApiPrefs.KEY_DOMAIN): 'domain',
    });

    await ApiPrefs.init();
    expect(ApiPrefs.isLoggedIn(), true);
  });

  test('getApiUrl returns the domain with the api path added', () async {
    SharedPreferences.setMockInitialValues({_debugKey(ApiPrefs.KEY_DOMAIN): 'domain'});

    await ApiPrefs.init();
    expect(ApiPrefs.getApiUrl(), 'domain/api/v1/');
  });

  test('perform login updates token and domain', () async {
    SharedPreferences.setMockInitialValues({});

    final result = _mockVerifyResult('domain');

    locator.registerLazySingleton<AuthApi>(() => _MockAuthApi());
    await ApiPrefs.performLogin(result, 'code');

    expect(ApiPrefs.getAuthToken(), 'token');
    expect(ApiPrefs.getDomain(), 'domain');
  });

  test('perform logout clears out token and domain', () async {
    SharedPreferences.setMockInitialValues({
      _debugKey(ApiPrefs.KEY_DOMAIN): 'domain',
      _debugKey(ApiPrefs.KEY_TOKEN): 'token',
    });

    await ApiPrefs.init();

    expect(ApiPrefs.getDomain(), 'domain');
    expect(ApiPrefs.getAuthToken(), 'token');

    await ApiPrefs.performLogout();

    expect(ApiPrefs.getDomain(), null);
    expect(ApiPrefs.getAuthToken(), null);
  });

  test('setting user updates stored user', () async {
    SharedPreferences.setMockInitialValues({});

    final user = _mockUser();
    await ApiPrefs.init();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getUser(), user);
  });

  test('setting user updates with new locale rebuilds the app', () async {
    SharedPreferences.setMockInitialValues({});

    await ApiPrefs.init();
    expect(ApiPrefs.getUser(), null);

    final user = _mockUser();
    final app = _MockApp();
    await ApiPrefs.setUser(user, app: app);

    verify(app.rebuild(any)).called(1);
  });

  test('effectiveLocale returns the devices locale', () async {
    SharedPreferences.setMockInitialValues({});

    final deviceLocale = window.locale.toLanguageTag();
    await ApiPrefs.init();

    final localeParts = deviceLocale.split("-");
    expect(ApiPrefs.effectiveLocale(), Locale(localeParts.first, localeParts.last));
  });

  test('effectiveLocale returns the users effective locale', () async {
    SharedPreferences.setMockInitialValues({});

    final user = _mockUser();
    await ApiPrefs.init();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.effectiveLocale(), Locale(user.effectiveLocale, user.effectiveLocale));
  });

  test('effectiveLocale returns the users locale if effective locale is null', () async {
    SharedPreferences.setMockInitialValues({});

    final user = _mockUser().rebuild((b) => b
      ..effectiveLocale = null
      ..locale = 'jp');

    await ApiPrefs.init();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.effectiveLocale(), Locale(user.locale, user.locale));
  });

  test('effectiveLocale returns the users locale if effective locale is null', () async {
    SharedPreferences.setMockInitialValues({});

    final user = _mockUser().rebuild((b) => b..effectiveLocale = 'en-AU-x-unimelb');

    await ApiPrefs.init();
    await ApiPrefs.setUser(user);

    expect(
        ApiPrefs.effectiveLocale(), Locale.fromSubtags(languageCode: 'en', countryCode: 'AU', scriptCode: 'unimelb'));
  });

  test('getUser throws error if not initialized', () {
    expect(() => ApiPrefs.getUser(), throwsStateError);
  });

  test('getUser returns null', () async {
    SharedPreferences.setMockInitialValues({});
    await ApiPrefs.init();

    expect(ApiPrefs.getUser(), null);
  });

  test('getHeaderMap throws state error', () {
    expect(() => ApiPrefs.getHeaderMap(), throwsStateError);
  });

  test('getHeaderMap returns a map with the accept-language from prefs', () async {
    SharedPreferences.setMockInitialValues({});

    final user = _mockUser().rebuild((b) => b..effectiveLocale = 'en-US');
    await ApiPrefs.init();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getHeaderMap()['accept-language'], 'en-US');
  });

  test('getHeaderMap returns a map with the accept-language from device', () async {
    SharedPreferences.setMockInitialValues({});

    final deviceLocale = window.locale;
    final user = _mockUser().rebuild((b) => b..effectiveLocale = 'ar');
    await ApiPrefs.init();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getHeaderMap(forceDeviceLanguage: true)['accept-language'], deviceLocale.toLanguageTag());
  });

  test('getHeaderMap returns a map with the token from prefs', () async {
    SharedPreferences.setMockInitialValues({
      _debugKey(ApiPrefs.KEY_TOKEN): 'token',
    });

    await ApiPrefs.init();
    expect(ApiPrefs.getHeaderMap()['Authorization'], 'Bearer token');
  });

  test('getHeaderMap returns a map with the token passed in', () async {
    SharedPreferences.setMockInitialValues({
      _debugKey(ApiPrefs.KEY_TOKEN): 'token',
    });

    await ApiPrefs.init();
    expect(ApiPrefs.getHeaderMap(token: 'other token')['Authorization'], 'Bearer other token');
  });

  test('getHeaderMap returns a map with the token passed in', () async {
    SharedPreferences.setMockInitialValues({
      _debugKey(ApiPrefs.KEY_TOKEN): 'token',
    });

    await ApiPrefs.init();
    expect(ApiPrefs.getHeaderMap()['User-Agent'], ApiPrefs.getUserAgent());
  });

  test('getHeaderMap returns a map with the extra headers passed in', () async {
    SharedPreferences.setMockInitialValues({});

    final map = {'key': 'value'};

    await ApiPrefs.init();
    expect(ApiPrefs.getHeaderMap(extraHeaders: map)['key'], 'value');
  });
}

String _debugKey(String key) {
  return "flutter.$key";
}

MobileVerifyResult _mockVerifyResult(String domain) => MobileVerifyResult((b) {
      return b
        ..baseUrl = domain
        ..authorized = true
        ..result = 200
        ..clientId = 'clientId'
        ..clientSecret = 'clientSecret'
        ..apiKey = 'key'
        ..build();
    });

User _mockUser() => User((b) {
      return b
        ..id = 0
        ..name = 'name'
        ..sortableName = 'sortable name'
        ..avatarUrl = 'url'
        ..primaryEmail = 'email'
        ..locale = 'en'
        ..effectiveLocale = 'jp'
        ..build();
    });

class _MockAuthApi extends Mock implements AuthApi {
  @override
  Future<String> getToken(MobileVerifyResult verifyResult, String requestCode) async => 'token';
}

abstract class _Rebuildable {
  void rebuild(Locale locale);
}

class _MockApp extends Mock implements _Rebuildable {}
