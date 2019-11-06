/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:convert';

import 'package:flutter_parent/api/utils/api_prefs.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:http/http.dart' as http;
import 'package:flutter_parent/models/mobile_verify_result.dart';

// TODO: Convert these http calls over to Dio, once the login flow is implemented. Would be good to consolidate, but too
// hard to test without login working. Copied from the example project for now to get this in.
class AuthApi {
  Future<String> refreshToken(String domain, String refreshToken) async {
    return mobileVerify(domain).then((result) async {
      final url =
          "${result.baseUrl}/login/oauth2/token?client_id=${result.clientId}&client_secret=${result.clientSecret}&redirect_uri=urn:ietf:wg:oauth:2.0:oob&grant_type=refresh_token&refresh_token=$refreshToken";
      var response = await http.post(url);

      if (response.statusCode == 200) {
        final parsed = json.decode(response.body);
        return parsed['access_token'];
      } else {
        return Future.error("Unknown Error");
      }
    });
  }

  Future<String> getToken(MobileVerifyResult verifyResult, String requestCode) async {
    final url =
        "${verifyResult.baseUrl}/login/oauth2/token?client_id=${verifyResult.clientId}&client_secret=${verifyResult.clientSecret}&code=$requestCode&redirect_uri=urn:ietf:wg:oauth:2.0:oob";
    var response = await http.post(url);

    if (response.statusCode == 200) {
      final parsed = json.decode(response.body);
      return parsed['access_token'];
    } else {
      return Future.error("Unknown Error");
    }
  }

  Future<MobileVerifyResult> mobileVerify(String domain) async {
    var userAgent = ApiPrefs.getUserAgent();
    var encodedAgent = Uri.encodeQueryComponent(userAgent);
    var encodedDomain = Uri.encodeQueryComponent(domain);
    var url = "https://canvas.instructure.com/api/v1/mobile_verify.json?domain=$encodedDomain&user_agent=$encodedAgent";
    var response = await http.get(url, headers: {'User-Agent': userAgent});

    if (response.statusCode == 200) {
      return deserialize<MobileVerifyResult>(json.decode(response.body));
    } else {
      return Future.error("Unknown Error");
    }
  }
}
