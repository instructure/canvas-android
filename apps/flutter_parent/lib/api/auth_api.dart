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
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/serializers.dart';

class AuthApi {
  static Future<CanvasToken> refreshToken() async {
    var response = await Dio().post(
      "${ApiPrefs.getDomain()}/login/oauth2/token",
      queryParameters: {
        'client_id': ApiPrefs.getClientId(),
        'client_secret': ApiPrefs.getClientSecret(),
        'redirect_uri': 'urn:ietf:wg:oauth:2.0:oob',
        'grant_type': 'refresh_token',
        'refresh_token': ApiPrefs.getRefreshToken(),
      },
    );

    if (response.statusCode == 200) {
      return deserialize<CanvasToken>(response.data);
    } else {
      return Future.error("Unknown Error");
    }
  }

  static Future<CanvasToken> getTokens(MobileVerifyResult verifyResult, String requestCode) async {
    var response = await Dio().post(
      "${verifyResult.baseUrl}/login/oauth2/token",
      queryParameters: {
        'client_id': verifyResult.clientId,
        'client_secret': verifyResult.clientSecret,
        'code': requestCode,
        'redirect_uri': 'urn:ietf:wg:oauth:2.0:oob',
      },
    );

    if (response.statusCode == 200 || response.statusCode == 201) {
      return deserialize<CanvasToken>(response.data);
    } else {
      return Future.error(response.statusMessage);
    }
  }

  static Future<MobileVerifyResult> mobileVerify(String domain) async {
    var userAgent = ApiPrefs.getUserAgent();
    var encodedAgent = Uri.encodeQueryComponent(userAgent);
    var encodedDomain = Uri.encodeQueryComponent(domain);

    var response = await Dio().get(
      "https://canvas.instructure.com/api/v1/mobile_verify.json",
      queryParameters: {
        'domain': encodedDomain,
        'user_agent': encodedAgent,
      },
      options: Options(headers: {'User-Agent': userAgent}),
    );

    if (response.statusCode == 200) {
      return deserialize<MobileVerifyResult>(response.data);
    } else {
      return Future.error("Unknown Error");
    }
  }
}
