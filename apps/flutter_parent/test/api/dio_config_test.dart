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
import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/api/utils/dio_config.dart';
import 'package:flutter_test/flutter_test.dart';

import '../utils/platform_config.dart';
import '../utils/test_app.dart';

void main() {

  setUpAll(() async => await setupPlatformChannels());

  test('returns a dio object', () async {
    expect(canvasDio(), isA<Dio>());
  });

  group('options', () {
    test('initializes with a base url', () async {
      final domain = 'test_domain';
      await setupPlatformChannels(config: PlatformConfig(mockPrefs: {ApiPrefs.KEY_DOMAIN: domain}));

      final options = canvasDio().options;
      expect(options.baseUrl, '$domain/api/v1/');
    });

    test('sets up headers', () async {
      final options = canvasDio().options;
      expect(options.headers, ApiPrefs.getHeaderMap()..putIfAbsent('content-type', () => null));
    });

    test('sets up headers with overrides', () async {
      final overrideToken = 'overrideToken';
      final extras = {'other': 'value'};

      final options = canvasDio(forceDeviceLanguage: true, overrideToken: overrideToken, extraHeaders: extras).options;
      final expected = ApiPrefs.getHeaderMap(forceDeviceLanguage: true, token: overrideToken, extraHeaders: extras)
        ..putIfAbsent('content-type', () => null);

      expect(options.headers, expected);
    });

    test('sets per page param', () async {
      final perPageSize = 1;
      final options = canvasDio(usePerPageParam: true, perPageSize: perPageSize).options;

      expect(options.queryParameters, {'per_page': perPageSize});
    });

    test('sets cache extras', () async {
      expect(canvasDio(forceRefresh: true).options.extra, isNotEmpty);
    });

    test('sets cache extras with force refrersh', () async {
      expect(canvasDio(forceRefresh: true).options.extra['dio_cache_force_refresh'], isTrue);
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
}
