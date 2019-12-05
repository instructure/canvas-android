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

import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:mockito/mockito.dart';
import 'package:package_info/package_info.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:test/test.dart';

import '../utils/platform_config.dart';
import '../utils/test_app.dart';

void main() {
  tearDown(() {
    ApiPrefs.clean();
  });

  test('is logged in throws error if not initiailzed', () {
    expect(() => ApiPrefs.isLoggedIn(), throwsStateError);
  });

  test('is logged in returns false', () async {
    await setupPlatformChannels();
    expect(ApiPrefs.isLoggedIn(), false);
  });

  test('is logged in returns false with no domain', () async {
    await setupPlatformChannels(config: PlatformConfig(mockPrefs: {ApiPrefs.KEY_ACCESS_TOKEN: 'token'}));
    expect(ApiPrefs.isLoggedIn(), false);
  });

  test('is logged in returns true with a token and domain', () async {
    await setupPlatformChannels(
      config: PlatformConfig(mockPrefs: {
        ApiPrefs.KEY_ACCESS_TOKEN: 'token',
        ApiPrefs.KEY_DOMAIN: 'domain',
      }),
    );

    expect(ApiPrefs.isLoggedIn(), true);
  });

  test('getApiUrl returns the domain with the api path added', () async {
    await setupPlatformChannels(config: PlatformConfig(mockPrefs: {ApiPrefs.KEY_DOMAIN: 'domain'}));
    expect(ApiPrefs.getApiUrl(), 'domain/api/v1/');
  });

  test('perform login updates information', () async {
    await setupPlatformChannels();

    final verifyResult = _mockVerifyResult('domain');
    final tokens = CanvasToken((b) => b
      ..user = _mockUser().toBuilder()
      ..accessToken = 'token'
      ..refreshToken = 'refresh');

    await ApiPrefs.updateLoginInfo(tokens, verifyResult);

    expect(ApiPrefs.getAuthToken(), tokens.accessToken);
    expect(ApiPrefs.getRefreshToken(), tokens.refreshToken);
    expect(ApiPrefs.getDomain(), verifyResult.baseUrl);
    expect(ApiPrefs.getClientId(), verifyResult.clientId);
    expect(ApiPrefs.getClientSecret(), verifyResult.clientSecret);
    expect(ApiPrefs.getUser(), tokens.user);
  });

  test('perform logout clears out token and domain', () async {
    await setupPlatformChannels(
      config: PlatformConfig(mockPrefs: {
        ApiPrefs.KEY_DOMAIN: 'domain',
        ApiPrefs.KEY_ACCESS_TOKEN: 'token',
        ApiPrefs.KEY_REFRESH_TOKEN: 'refresh',
      }),
    );

    expect(ApiPrefs.getDomain(), 'domain');
    expect(ApiPrefs.getAuthToken(), 'token');
    expect(ApiPrefs.getRefreshToken(), 'refresh');

    await ApiPrefs.performLogout();

    expect(ApiPrefs.getDomain(), null);
    expect(ApiPrefs.getAuthToken(), null);
    expect(ApiPrefs.getRefreshToken(), null);
  });

  test('setting user updates stored user', () async {
    await setupPlatformChannels();

    final user = _mockUser();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getUser(), user);
  });

  test('setting user updates with new locale rebuilds the app', () async {
    await setupPlatformChannels();

    expect(ApiPrefs.getUser(), null);

    final user = _mockUser();
    final app = _MockApp();
    await ApiPrefs.setUser(user, app: app);

    verify(app.rebuild(any)).called(1);
  });

  test('effectiveLocale returns the devices locale', () async {
    await setupPlatformChannels();

    final deviceLocale = window.locale.toLanguageTag();

    final localeParts = deviceLocale.split("-");
    expect(ApiPrefs.effectiveLocale(), Locale(localeParts.first, localeParts.last));
  });

  test('effectiveLocale returns the users effective locale', () async {
    await setupPlatformChannels();

    final user = _mockUser();
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.effectiveLocale(), Locale(user.effectiveLocale, user.effectiveLocale));
  });

  test('effectiveLocale returns the users locale if effective locale is null', () async {
    await setupPlatformChannels();

    final user = _mockUser().rebuild((b) => b
      ..effectiveLocale = null
      ..locale = 'jp');

    await ApiPrefs.setUser(user);

    expect(ApiPrefs.effectiveLocale(), Locale(user.locale, user.locale));
  });

  test('effectiveLocale returns the users locale if effective locale is null', () async {
    await setupPlatformChannels();

    final user = _mockUser().rebuild((b) => b..effectiveLocale = 'en-AU-x-unimelb');

    await ApiPrefs.setUser(user);

    expect(
        ApiPrefs.effectiveLocale(), Locale.fromSubtags(languageCode: 'en', countryCode: 'AU', scriptCode: 'unimelb'));
  });

  test('getUser throws error if not initialized', () {
    expect(() => ApiPrefs.getUser(), throwsStateError);
  });

  test('getUser returns null', () async {
    await setupPlatformChannels();
    expect(ApiPrefs.getUser(), null);
  });

  test('getHeaderMap throws state error', () {
    expect(() => ApiPrefs.getHeaderMap(), throwsStateError);
  });

  test('getHeaderMap returns a map with the accept-language from prefs', () async {
    await setupPlatformChannels();

    final user = _mockUser().rebuild((b) => b..effectiveLocale = 'en-US');
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getHeaderMap()['accept-language'], 'en,US');
  });

  test('getHeaderMap returns a map with the accept-language from device', () async {
    await setupPlatformChannels();

    final deviceLocale = window.locale;
    final user = _mockUser().rebuild((b) => b..effectiveLocale = 'ar');
    await ApiPrefs.setUser(user);

    expect(ApiPrefs.getHeaderMap(forceDeviceLanguage: true)['accept-language'], deviceLocale.toLanguageTag().replaceAll("-", ","));
  });

  test('getHeaderMap returns a map with the token from prefs', () async {
    await setupPlatformChannels(config: PlatformConfig(mockPrefs: {ApiPrefs.KEY_ACCESS_TOKEN: 'token'}));
    expect(ApiPrefs.getHeaderMap()['Authorization'], 'Bearer token');
  });

  test('getHeaderMap returns a map with the token passed in', () async {
    await setupPlatformChannels(config: PlatformConfig(mockPrefs: {ApiPrefs.KEY_ACCESS_TOKEN: 'token'}));
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
}

MobileVerifyResult _mockVerifyResult(String domain) => MobileVerifyResult((b) {
      return b
        ..baseUrl = domain
        ..authorized = true
        ..result = VerifyResultEnum.success
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

abstract class _Rebuildable {
  void rebuild(Locale locale);
}

class _MockApp extends Mock implements _Rebuildable {}
