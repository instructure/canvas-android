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
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';

class AuthApi {
  Future<CanvasToken?> refreshToken() async {
    var dio = DioConfig.canvas(includeApiPath: false).dio;
    var params = {
      'client_id': ApiPrefs.getClientId(),
      'client_secret': ApiPrefs.getClientSecret(),
      'redirect_uri': 'urn:ietf:wg:oauth:2.0:oob',
      'grant_type': 'refresh_token',
      'refresh_token': ApiPrefs.getRefreshToken(),
    };
    return fetch(dio.post('login/oauth2/token', data: params));
  }

  Future<CanvasToken?> getTokens(MobileVerifyResult? verifyResult, String requestCode) async {
    var dio = DioConfig().dio;
    var params = {
      'client_id': verifyResult?.clientId,
      'client_secret': verifyResult?.clientSecret,
      'code': requestCode,
      'redirect_uri': 'urn:ietf:wg:oauth:2.0:oob',
    };
    return fetch(dio.post('${verifyResult?.baseUrl}/login/oauth2/token', data: params));
  }

  Future<MobileVerifyResult?> mobileVerify(String domain, {bool forceBetaDomain = false}) async {
    // We only want to switch over to the beta mobile verify domain if either:
    //   (1) we are forcing the beta domain (typically in UI tests) OR
    //   (2) the remote firebase config setting for MOBILE_VERIFY_BETA_ENABLED is true
    // AND we are trying to use a beta domain
    var mobileVerifyBetaEnabled = (forceBetaDomain ||
            RemoteConfigUtils.getStringValue(RemoteConfigParams.MOBILE_VERIFY_BETA_ENABLED)?.toLowerCase() == 'true') &&
        domain.contains('.beta.');

    Dio dio = DioConfig.core(useBetaDomain: mobileVerifyBetaEnabled).dio;

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

  Future<void> deleteToken(String domain, String token) async {
    Dio dio = DioConfig().dio;
    var params = {'access_token': token};
    await dio.delete('$domain/login/oauth2/token', data: params);
  }
}
