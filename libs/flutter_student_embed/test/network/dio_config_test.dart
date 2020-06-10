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

import 'package:dio/dio.dart';
import 'package:flutter_student_embed/models/login.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/network/utils/dio_config.dart';
import 'package:flutter_test/flutter_test.dart';

import '../testutils/test_app.dart';

void main() {
  setUpAll(() async => await setupPlatformChannels());

  test('returns a dio object', () async {
    expect(canvasDio(), isA<Dio>());
  });

  group('base constructor asserts', () {
    test('throws an error if baseUrl is null', () async {
      expect(() => DioConfig(baseUrl: null), throwsAssertionError);
    });

    test('throws an error if cacheMaxAge is null', () async {
      expect(() => DioConfig(cacheMaxAge: null), throwsAssertionError);
    });

    test('throws an error if forceRefresh is null', () async {
      expect(() => DioConfig(forceRefresh: null), throwsAssertionError);
    });

    test('throws an error if pageSize is null', () async {
      expect(() => DioConfig(pageSize: null), throwsAssertionError);
    });
  });

  test('DioConfig.canvas returns a config object', () async {
    expect(DioConfig.canvas(), isA<DioConfig>());
  });

  test('DioConfig.core returns a config object', () async {
    expect(DioConfig.core(), isA<DioConfig>());
  });

  group('canvas options', () {
    test('initializes with a base url', () async {
      final domain = 'test_domain';
      ApiPrefs.setLogin(Login((b) => b..domain = domain));

      final options = canvasDio().options;
      expect(options.baseUrl, '$domain/api/v1/');
    });

    test('sets up headers', () async {
      final options = canvasDio().options;
      final expectedHeaders = ApiPrefs.getHeaderMap()
        ..putIfAbsent('accept', () => 'application/json+canvas-string-ids')
        ..putIfAbsent('content-type', () => null);
      expect(options.headers, expectedHeaders);
    });

    test('sets up headers with overrides', () async {
      final overrideToken = 'overrideToken';
      final extras = {'other': 'value'};

      final options = canvasDio(forceDeviceLanguage: true, overrideToken: overrideToken, extraHeaders: extras).options;
      final expected = ApiPrefs.getHeaderMap(forceDeviceLanguage: true, token: overrideToken, extraHeaders: extras)
        ..putIfAbsent('accept', () => 'application/json+canvas-string-ids')
        ..putIfAbsent('content-type', () => null);

      expect(options.headers, expected);
    });

    test('sets per page param', () async {
      final perPageSize = 1;
      final options = canvasDio(pageSize: PageSize(perPageSize)).options;

      expect(options.queryParameters, {'per_page': perPageSize});
    });

    test('sets as_user_id param when masquerading', () async {
      String userId = "masquerade_user_id";
      final login = Login((b) => b..masqueradeId = userId);

      ApiPrefs.setLogin(login);

      final options = canvasDio().options;

      expect(options.queryParameters['as_user_id'], userId);
    });

    test('Does not set as_user_id param when not masquerading', () async {
      ApiPrefs.setLogin(Login());
      final options = canvasDio().options;

      expect(options.queryParameters.containsKey('as_user_id'), isFalse);
    });

    test('sets cache extras', () async {
      expect(canvasDio(forceRefresh: true).options.extra, isNotEmpty);
    });

    test('sets cache extras with force refrersh', () async {
      expect(canvasDio(forceRefresh: true).options.extra['dio_cache_force_refresh'], isTrue);
    });
  });

  group('core options', () {
    test('initializes with a base url', () async {
      final options = DioConfig.core().dio.options;
      expect(options.baseUrl, 'https://canvas.instructure.com/api/v1/');
    });

    test('initializes with a base url without api path', () async {
      final options = DioConfig.core(includeApiPath: false).dio.options;
      expect(options.baseUrl, 'https://canvas.instructure.com/');
    });

    test('sets up headers', () async {
      final headers = {'123': '123'};
      final options = DioConfig.core(headers: headers).dio.options;
      expect(options.headers, headers);
    });

    test('sets per page param', () async {
      final perPageSize = 13;
      final options = DioConfig.core(pageSize: PageSize(perPageSize)).dio.options;
      expect(options.queryParameters, {'per_page': perPageSize});
    });

    test('sets up cache maxAge', () async {
      final age = Duration(minutes: 123);
      final options = DioConfig.core(cacheMaxAge: age).dio.options;
      expect(options.extra['dio_cache_max_age'], age);
    });

    test('Does not set cache extras if max age is zero', () async {
      final options = DioConfig.core(cacheMaxAge: Duration.zero).dio.options;
      expect(options.extra['dio_cache_max_age'], isNull);
      expect(options.extra['dio_cache_force_refresh'], isNull);
    });

    test('sets cache extras with force refrersh', () async {
      final options = DioConfig.core(cacheMaxAge: Duration(minutes: 1), forceRefresh: true).dio.options;
      expect(options.extra['dio_cache_force_refresh'], isTrue);
    });
  });

  group('interceptors', () {
    test('adds cache manager', () async {
      // The cache manager is an object that hooks in via an interceptor wrapper, so we can't check for the explicit type
      expect(canvasDio().interceptors, contains(isA<InterceptorsWrapper>()));
    });

    test('adds log interceptor', () async {
      expect(canvasDio().interceptors, contains(isA<LogInterceptor>()));
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
      );

      final copy = original.copyWith(
        baseUrl: '123',
        baseHeaders: {'123': '123'},
        cacheMaxAge: Duration(minutes: 123),
        forceRefresh: false,
        pageSize: PageSize(123),
        extraQueryParams: {'param2': '321'},
      );

      expect(copy.baseUrl, '123');
      expect(copy.baseHeaders, {'123': '123'});
      expect(copy.cacheMaxAge, Duration(minutes: 123));
      expect(copy.forceRefresh, false);
      expect(copy.pageSize, PageSize(123));
      expect(copy.extraQueryParams, {'param2': '321'});
    });
  });
}
