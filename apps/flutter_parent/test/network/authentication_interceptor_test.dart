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
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/authentication_interceptor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../utils/platform_config.dart';
import '../utils/test_app.dart';
import '../utils/test_helpers/mock_helpers.dart';
import '../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final login = Login((b) => b
    ..domain = 'domain'
    ..user = User((b) => b..id = '123').toBuilder()
    ..clientSecret = 'client_secret'
    ..clientId = 'client_id');

  final dio = MockDio();
  final authApi = MockAuthApi();
  final analytics = MockAnalytics();
  final errorHandler = MockErrorInterceptorHandler();

  final interceptor = AuthenticationInterceptor(dio);

  setupTestLocator((locator) {
    locator.registerLazySingleton<AuthApi>(() => authApi);
    locator.registerLazySingleton<Analytics>(() => analytics);
  });

  setUp(() {
    reset(dio);
    reset(authApi);
    reset(analytics);
    reset(errorHandler);
  });

  test('returns error if response code is not 401', () async {
    await setupPlatformChannels();
    var path = 'accounts/self';
    final error = DioError(requestOptions: RequestOptions(path: path), response: Response(statusCode: 403, requestOptions: RequestOptions(path: path)));

    // Test the error response
    await interceptor.onError(error, errorHandler);
    verify(errorHandler.next(error));
  });

  test('returns error if path is accounts/self', () async {
    await setupPlatformChannels();
    var path = 'accounts/self';
    final error = DioError(requestOptions: RequestOptions(path: path), response: Response(statusCode: 401, requestOptions: RequestOptions(path: path)));

    // Test the error response
    await interceptor.onError(error, errorHandler);
    verify(errorHandler.next(error));
  });

  test('returns error if headers have the retry header', () async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));
    final error = DioError(
      requestOptions: RequestOptions(path: '', headers: {'mobile_refresh': 'mobile_refresh'}),
      response: Response(statusCode: 401, requestOptions: RequestOptions(path: '')),
    );

    // Test the error response
    await interceptor.onError(error, errorHandler);
    verify(errorHandler.next(error));
    verify(analytics.logEvent(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE, extras: {
      AnalyticsParamConstants.DOMAIN_PARAM: login.domain,
      AnalyticsParamConstants.USER_CONTEXT_ID: 'user_${login.user.id}',
    })).called(1);
  });

  test('returns error if login is null', () async {
    await setupPlatformChannels();
    final error = DioError(requestOptions: RequestOptions(path: ''), response: Response(statusCode: 401, requestOptions: RequestOptions(path: '')));

    // Test the error response
    await interceptor.onError(error, errorHandler);
    verify(errorHandler.next(error));
    verify(analytics.logEvent(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE_NO_SECRET, extras: {
      AnalyticsParamConstants.DOMAIN_PARAM: null,
      AnalyticsParamConstants.USER_CONTEXT_ID: null,
    })).called(1);
  });

  test('returns error if login client id is null', () async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login.rebuild((b) => b..clientId = null)));
    final error = DioError(requestOptions: RequestOptions(path: ''), response: Response(statusCode: 401, requestOptions: RequestOptions(path: '')));

    // Test the error response
    await interceptor.onError(error, errorHandler);
    verify(errorHandler.next(error));
    verify(analytics.logEvent(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE_NO_SECRET, extras: {
      AnalyticsParamConstants.DOMAIN_PARAM: login.domain,
      AnalyticsParamConstants.USER_CONTEXT_ID: 'user_${login.user.id}',
    })).called(1);
  });

  test('returns error if login client secret is null', () async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login.rebuild((b) => b..clientSecret = null)));
    final error = DioError(requestOptions: RequestOptions(path: ''), response: Response(statusCode: 401, requestOptions: RequestOptions(path: '')));

    // Test the error response
    await interceptor.onError(error, errorHandler);
    verify(errorHandler.next(error));
    verify(analytics.logEvent(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE_NO_SECRET, extras: {
      AnalyticsParamConstants.DOMAIN_PARAM: login.domain,
      AnalyticsParamConstants.USER_CONTEXT_ID: 'user_${login.user.id}',
    })).called(1);
  });

  test('returns error if the refresh api call failed', () async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));
    final error = DioError(requestOptions: RequestOptions(path: ''), response: Response(statusCode: 401, requestOptions: RequestOptions(path: '')));

    when(authApi.refreshToken()).thenAnswer((_) => Future.error('Failed to refresh'));

    when(dio.interceptors).thenAnswer((realInvocation) => Interceptors());

    // Test the error response
    await interceptor.onError(error, errorHandler);
    verify(errorHandler.next(error));

    verify(analytics.logEvent(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE_TOKEN_NOT_VALID, extras: {
      AnalyticsParamConstants.DOMAIN_PARAM: login.domain,
      AnalyticsParamConstants.USER_CONTEXT_ID: 'user_${login.user.id}',
    })).called(1);
    verify(authApi.refreshToken()).called(1);
  });

  test('returns a newly authenticated api call', () async {
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));

    final tokens = CanvasToken((b) => b..accessToken = 'token');
    final path = 'test/path/stuff';
    final error = DioError(requestOptions: RequestOptions(path: path), response: Response(statusCode: 401, requestOptions: RequestOptions(path: path)));
    final expectedOptions = RequestOptions(path: path, headers: {
      'Authorization': 'Bearer ${tokens.accessToken}',
      'mobile_refresh': 'mobile_refresh',
    });
    final expectedAnswer = Response(requestOptions: expectedOptions, data: 'data', statusCode: 200);

    when(authApi.refreshToken()).thenAnswer((_) async => tokens);
    when(dio.fetch(any)).thenAnswer((_) async => expectedAnswer);
    when(dio.interceptors).thenAnswer((realInvocation) => Interceptors());

    // Do the onError call
    await interceptor.onError(error, errorHandler);
    verify(errorHandler.resolve(expectedAnswer));

    verify(authApi.refreshToken()).called(1);
    final actualOptions = verify(dio.fetch(captureAny)).captured[0] as RequestOptions;
    expect(actualOptions.headers, expectedOptions.headers);
    expect(ApiPrefs.getCurrentLogin()?.accessToken, tokens.accessToken);
    verifyNever(analytics.logEvent(any, extras: anyNamed('extras')));
  });
}
