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
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/utils/service_locator.dart';

import 'api_prefs.dart';

class AuthenticationInterceptor extends InterceptorsWrapper {
  final String _RETRY_HEADER = 'mobile_refresh';

  final Dio _dio;

  AuthenticationInterceptor(this._dio);

  @override
  Future<void> onError(DioError error, ErrorInterceptorHandler handler) async {
    // Only proceed if it was an authentication error
    if (error.response?.statusCode != 401) return handler.next(error);

    final currentLogin = ApiPrefs.getCurrentLogin();

    // Check for any errors
    if (error.requestOptions.path.contains('accounts/self') == true) {
      // We are likely just checking if the user can masquerade or not, which happens on login - don't try to re-auth here
      return handler.next(error);
    } else if (error.requestOptions.headers[_RETRY_HEADER] != null) {
      _logAuthAnalytics(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE);
      return handler.next(error);
    } else if (currentLogin == null ||
        (currentLogin.clientId?.isEmpty ?? true) ||
        (currentLogin.clientSecret?.isEmpty ?? true)) {
      _logAuthAnalytics(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE_NO_SECRET);
      return handler.next(error);
    }

    // Lock new requests from being processed while refreshing the token
    _dio.interceptors.requestLock.lock();
    _dio.interceptors.responseLock.lock();

    // Refresh the token and update the login
    CanvasToken? tokens;

    tokens = await locator<AuthApi>().refreshToken().catchError((e) => null);

    if (tokens == null) {
      _logAuthAnalytics(AnalyticsEventConstants.TOKEN_REFRESH_FAILURE_TOKEN_NOT_VALID);

      _dio.interceptors.requestLock.unlock();
      _dio.interceptors.responseLock.unlock();

      return handler.next(error);
    } else {
      Login login = currentLogin.rebuild((b) => b..accessToken = tokens?.accessToken);
      ApiPrefs.addLogin(login);
      ApiPrefs.switchLogins(login);

      // Update the header and make the request again
      RequestOptions requestOptions = error.requestOptions;

      requestOptions.headers['Authorization'] = 'Bearer ${tokens.accessToken}';
      requestOptions.headers[_RETRY_HEADER] = _RETRY_HEADER; // Mark retry to prevent infinite recursion

      _dio.interceptors.requestLock.unlock();
      _dio.interceptors.responseLock.unlock();

      final response = await _dio.fetch(requestOptions);
      if (response.statusCode == 200 || response.statusCode == 201) {
        return handler.resolve(response);
      } else {
        return handler.next(error);
      }
    }
  }

  _logAuthAnalytics(String eventString) {
    final userId = ApiPrefs.getUser()?.id;
    final bundle = {
      AnalyticsParamConstants.DOMAIN_PARAM: ApiPrefs.getDomain(),
      AnalyticsParamConstants.USER_CONTEXT_ID: userId != null ? 'user_$userId' : null,
    };
    locator<Analytics>().logEvent(eventString, extras: bundle);
  }
}