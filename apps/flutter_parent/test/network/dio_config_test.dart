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

import 'package:dio/dio.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_test/flutter_test.dart';

import '../utils/canvas_model_utils.dart';
import '../utils/test_app.dart';

void main() {
  setUpAll(() async => await setupPlatformChannels());

  test('returns a dio object', () async {
    final domain = 'https://test_domain.com';
    await ApiPrefs.switchLogins(Login((b) => b..domain = domain));
    var dio = await canvasDio();
    expect(dio, isA<Dio>());
  });

  test('DioConfig.canvas returns a config object', () async {
    expect(DioConfig.canvas(), isA<DioConfig>());
  });

  test('DioConfig.core returns a config object', () async {
    expect(DioConfig.core(), isA<DioConfig>());
  });

  group('canvas options', () {
    test('initializes with a base url', () async {

      final domain = 'https://test_domain.com';
      await ApiPrefs.switchLogins(Login((b) => b..domain = domain));

      var dio = await canvasDio();
      final options = dio.options;
      expect(options.baseUrl, '$domain/api/v1/');
    });

    test('sets up headers', () async {
      var dio = await canvasDio();
      final options = dio.options;
      final expectedHeaders = ApiPrefs.getHeaderMap()
        ..putIfAbsent('accept', () => 'application/json+canvas-string-ids')
        ..putIfAbsent('content-type', () => 'application/json; charset=utf-8');
      expect(options.headers, expectedHeaders);
    });

    test('sets up headers with overrides', () async {
      final overrideToken = 'overrideToken';
      final extras = {'other': 'value'};

      var dio = await canvasDio(forceDeviceLanguage: true, overrideToken: overrideToken, extraHeaders: extras);

      final options = dio.options;
      final expected = ApiPrefs.getHeaderMap(
          forceDeviceLanguage: true, token: overrideToken, extraHeaders: extras)
        ..putIfAbsent('accept', () => 'application/json+canvas-string-ids')
        ..putIfAbsent('content-type', () => 'application/json; charset=utf-8');

      expect(options.headers, expected);
    });

    test('sets per page param', () async {
      final perPageSize = 1;
      var dio = await canvasDio(pageSize: PageSize(perPageSize));
      final options = dio.options;

      expect(options.queryParameters['per_page'], 1);
    });

    test('sets no verifiers param by default', () async {
      var dio = await canvasDio();
      final options = dio.options;

      expect(options.queryParameters['no_verifiers'], 1);
    });

    test('sets as_user_id param when masquerading', () async {
      String userId = "masquerade_user_id";
      final login = Login((b) => b
        ..masqueradeDomain = 'https://masqueradeDomain.com'
        ..masqueradeUser = CanvasModelTestUtils.mockUser(id: userId).toBuilder());
      await ApiPrefs.switchLogins(login);

      var dio = await canvasDio();

      final options = dio.options;

      expect(options.queryParameters['as_user_id'], userId);
    });

    test('Does not set as_user_id param when not masquerading', () async {
      final domain = 'https://test_domain.com';
      await ApiPrefs.switchLogins(Login((b) => b..domain = domain));
      var dio = await canvasDio();
      final options = dio.options;

      expect(options.queryParameters.containsKey('as_user_id'), isFalse);
    });

    test('sets cache extras', () async {
      var dio = await canvasDio(forceRefresh: true);
      expect(dio.options.extra, isNotEmpty);
    });

    test('sets cache extras with force refrersh', () async {
      var dio = await canvasDio(forceRefresh: true);
      expect(dio.options.extra['dio_cache_force_refresh'], isTrue);
    });
  });

  group('core options', () {
    test('initializes with a base url', () async {
      var dio = await DioConfig.core().dio;
      final options = dio.options;
      expect(options.baseUrl, 'https://canvas.instructure.com/api/v1/');
    });

    test('initializes with a base url without api path', () async {
      var dio = await DioConfig.core(includeApiPath: false).dio;
      final options = dio.options;
      expect(options.baseUrl, 'https://canvas.instructure.com/');
    });

    test('initializes with a beta base url', () async {
      var dio = await DioConfig.core(useBetaDomain: true).dio;
      final options = dio.options;
      expect(options.baseUrl, 'https://canvas.beta.instructure.com/api/v1/');
    });

    test('sets up headers', () async {
      final headers = {
        '123': '123',
        'content-type': 'application/json; charset=utf-8'
      };
      var dio = await DioConfig.core(headers: headers).dio;
      final options = dio.options;
      expect(options.headers, headers);
    });

    test('sets per page param', () async {
      final perPageSize = 13;
      var dio = await DioConfig.core(pageSize: PageSize(perPageSize)).dio;
      final options = dio.options;
      expect(options.queryParameters, {'per_page': perPageSize});
    });

    test('sets up cache maxAge', () async {
      final age = Duration(minutes: 123);
      var dio = await DioConfig.core(cacheMaxAge: age).dio;
      final options = dio.options;
      expect(options.extra['dio_cache_max_age'], age);
    });

    test('Does not set cache extras if max age is zero', () async {
      var dio = await DioConfig.core(cacheMaxAge: Duration.zero).dio;
      final options = dio.options;
      expect(options.extra['dio_cache_max_age'], isNull);
      expect(options.extra['dio_cache_force_refresh'], isNull);
    });

    test('sets cache extras with force refrersh', () async {
      var dio = await DioConfig.core(cacheMaxAge: Duration(minutes: 1), forceRefresh: true).dio;
      final options = dio.options;
      expect(options.extra['dio_cache_force_refresh'], isTrue);
    });
  });

  group('interceptors', () {
    test('adds cache manager', () async {
      var dio = await canvasDio();
      // The cache manager is an object that hooks in via an interceptor wrapper, so we can't check for the explicit type
      expect(dio.interceptors, contains(isA<InterceptorsWrapper>()));
    });

    test('adds log interceptor', () async {
      var dio = await canvasDio();
      expect(dio.interceptors, contains(isA<LogInterceptor>()));
    });
  });

  group('copy', () {
    test('Empty copy produces identical config', () {
      final original = DioConfig(
        baseUrl: 'fakeUrl',
        baseHeaders: {'fakeHeader': 'fakeValue'},
        cacheMaxAge: Duration(minutes: 13),
        forceRefresh: true,
        pageSize: PageSize(13),
        extraQueryParams: {'param1': '123'},
      );

      final copy = original.copyWith();

      expect(copy.baseUrl, original.baseUrl);
      expect(copy.baseHeaders, original.baseHeaders);
      expect(copy.cacheMaxAge, original.cacheMaxAge);
      expect(copy.forceRefresh, original.forceRefresh);
      expect(copy.pageSize, original.pageSize);
      expect(copy.extraQueryParams, original.extraQueryParams);
    });

    test('Copy with single value produces correct config', () {
      final original = DioConfig(
        baseUrl: 'fakeUrl',
        baseHeaders: {'fakeHeader': 'fakeValue'},
        cacheMaxAge: Duration(minutes: 13),
        forceRefresh: true,
        pageSize: PageSize(13),
        extraQueryParams: {'param1': '123'},
      );

      final copy = original.copyWith(baseUrl: '');

      expect(copy.baseUrl, '');
      expect(copy.baseHeaders, original.baseHeaders);
      expect(copy.cacheMaxAge, original.cacheMaxAge);
      expect(copy.forceRefresh, original.forceRefresh);
      expect(copy.pageSize, original.pageSize);
      expect(copy.extraQueryParams, original.extraQueryParams);
    });

    test('Copy with all values produces correct config', () {
      final original = DioConfig(
        baseUrl: 'fakeUrl',
        baseHeaders: {'fakeHeader': 'fakeValue'},
        cacheMaxAge: Duration(minutes: 13),
        forceRefresh: true,
        pageSize: PageSize(13),
        extraQueryParams: {'param1': '123'},
        retries: 1,
      );

      final copy = original.copyWith(
        baseUrl: '123',
        baseHeaders: {'123': '123'},
        cacheMaxAge: Duration(minutes: 123),
        forceRefresh: false,
        pageSize: PageSize(123),
        extraQueryParams: {'param2': '321'},
        retries: 2,
      );

      expect(copy.baseUrl, '123');
      expect(copy.baseHeaders, {'123': '123'});
      expect(copy.cacheMaxAge, Duration(minutes: 123));
      expect(copy.forceRefresh, false);
      expect(copy.pageSize, PageSize(123));
      expect(copy.extraQueryParams, {'param2': '321'});
      expect(copy.retries, 2);
    });
  });
}
