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
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/authentication_interceptor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../utils/platform_config.dart';
import '../utils/test_app.dart';

void main() {
  final login = Login((b) => b
    ..clientSecret = 'client_secret'
    ..clientId = 'client_id');

  final dio = _MockDio();
  final authApi = _MockAuthApi();

  final interceptor = AuthenticationInterceptor(dio);

  setupTestLocator((locator) {
    locator.registerLazySingleton<AuthApi>(() => authApi);
  });

  setUp(() {
    reset(dio);
    reset(authApi);
  });

  test('returns error if response code is not 401', () async {
    await setupPlatformChannels();
    final error = DioError(request: RequestOptions(path: 'accounts/self'), response: Response(statusCode: 403));

    // Test the error response
    expect(await interceptor.onError(error), error);
  });

  test('returns error if path is accounts/self', () async {
    await setupPlatformChannels();
    final error = DioError(request: RequestOptions(path: 'accounts/self'), response: Response(statusCode: 401));

    // Test the error response
    expect(await interceptor.onError(error), error);
  });

  test('returns error if headers have the retry header', () async {
    await setupPlatformChannels();
    final error = DioError(
      request: RequestOptions(headers: {'mobile_refresh': 'mobile_refresh'}),
      response: Response(statusCode: 401),
    );

    // Test the error response
    expect(await interceptor.onError(error), error);
    // TODO: verify log event called
  });

  test('returns error if login is null', () async {
    await setupPlatformChannels();
    final error = DioError(request: RequestOptions(), response: Response(statusCode: 401));

    // Test the error response
    expect(await interceptor.onError(error), error);
    // TODO: verify log event called
  });

  test('returns error if login client id is null', () async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login.rebuild((b) => b..clientId = null)));
    final error = DioError(request: RequestOptions(), response: Response(statusCode: 401));

    // Test the error response
    expect(await interceptor.onError(error), error);
    // TODO: verify log event called
  });

  test('returns error if login client secret is null', () async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login.rebuild((b) => b..clientSecret = null)));
    final error = DioError(request: RequestOptions(), response: Response(statusCode: 401));

    // Test the error response
    expect(await interceptor.onError(error), error);
    // TODO: verify log event called
  });

  test('returns error if the refresh api call failed', () async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));
    final error = DioError(request: RequestOptions(), response: Response(statusCode: 401));

    when(authApi.refreshToken()).thenAnswer((_) => Future.error('Failed to refresh'));

    // Test the error response
    expect(await interceptor.onError(error), error);

    // TODO: verify log event called
    verify(authApi.refreshToken()).called(1);
  });

  test('returns a newly authenticated api call', () async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));

    final tokens = CanvasToken((b) => b..accessToken = 'token');
    final path = 'test/path/stuff';
    final error = DioError(request: RequestOptions(path: path), response: Response(statusCode: 401));
    final expectedOptions = RequestOptions(path: path, headers: {
      'Authorization': 'Bearer ${tokens.accessToken}',
      'mobile_refresh': 'mobile_refresh',
    });
    final expectedAnswer = Response(data: 'data');

    when(authApi.refreshToken()).thenAnswer((_) async => tokens);
    when(dio.request(any, options: anyNamed('options'))).thenAnswer((_) async => expectedAnswer);

    // Do the onError call
    expect(await interceptor.onError(error), expectedAnswer);

    // TODO: verify log event called
    verify(authApi.refreshToken()).called(1);
    final actualOptions = verify(dio.request(path, options: captureAnyNamed('options'))).captured[0] as RequestOptions;
    expect(actualOptions.headers, expectedOptions.headers);
    expect(ApiPrefs.getCurrentLogin().accessToken, tokens.accessToken);
  });
}

class _MockDio extends Mock implements Dio {}

class _MockAuthApi extends Mock implements AuthApi {}
