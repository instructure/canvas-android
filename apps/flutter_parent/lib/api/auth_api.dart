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
import 'package:flutter_parent/api/utils/fetch.dart';
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';

class AuthApi {
  Future<CanvasToken> refreshToken() async {
    var dio = DioConfig.canvas(includeApiPath: false).dio;
    var params = {
      'client_id': ApiPrefs.getClientId(),
      'client_secret': ApiPrefs.getClientSecret(),
      'redirect_uri': 'urn:ietf:wg:oauth:2.0:oob',
      'grant_type': 'refresh_token',
      'refresh_token': ApiPrefs.getRefreshToken(),
    };
    return fetch(dio.post('login/oauth2/token', queryParameters: params));
  }

  Future<CanvasToken> getTokens(MobileVerifyResult verifyResult, String requestCode) async {
    var dio = DioConfig().dio;
    var params = {
      'client_id': verifyResult.clientId,
      'client_secret': verifyResult.clientSecret,
      'code': requestCode,
      'redirect_uri': 'urn:ietf:wg:oauth:2.0:oob',
    };
    return fetch(dio.post('${verifyResult.baseUrl}/login/oauth2/token', queryParameters: params));
  }

  Future<MobileVerifyResult> mobileVerify(String domain) async {
    Dio dio = DioConfig.core().dio;
    String userAgent = ApiPrefs.getUserAgent();
    return fetch(
      dio.get(
        'mobile_verify.json',
        queryParameters: {
          'domain': domain,
          'user_agent': userAgent,
        },
        options: Options(headers: {'User-Agent': userAgent}),
      ),
    );
  }
}
