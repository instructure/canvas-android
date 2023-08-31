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

import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:faker/faker.dart';
import 'package:flutter/services.dart';
import 'package:flutter_parent/models/dataseeding/create_user_info.dart';
import 'package:flutter_parent/models/dataseeding/oauth_token.dart';
import 'package:flutter_parent/models/dataseeding/pairing_code.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/fetch.dart';

import '../../lib/network/api/auth_api.dart';

const _REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

const _createUserEndpoint = "accounts/self/users";

class UserSeedApi {
  static const authCodeChannel = const MethodChannel("GET_AUTH_CODE");

  static Future<SeededUser?> createUser() async {
    var url = baseSeedingUrl + _createUserEndpoint;

    var lastName = faker.person.lastName();
    var firstName = faker.person.firstName();
    var userData = CreateUserInfo((b) => b
      ..user.name = "$firstName $lastName"
      ..user.shortName = firstName
      ..user.sortableName = "$lastName, $firstName"
      ..pseudonym.uniqueId = Guid(RandomGenerator()).guid()
      ..pseudonym.password = Guid(RandomGenerator()).guid()
      // Don't care about CommunicationChannel initialization for now
      ..build());

    var postBody = json.encode(serialize(userData));
    print("New user postBody: $postBody");

    await ApiPrefs.init();

    var dio = seedingDio();

    var response = await dio.post(_createUserEndpoint, data: postBody);

    if (response.statusCode == 200) {
      //print("Create User response: ${response.data}");
      var result = deserialize<SeededUser>(response.data);
      result = result!.rebuild((b) => b
        ..loginId = userData.pseudonym.uniqueId
        ..password = userData.pseudonym.password
        ..domain = response.requestOptions.uri.host);

      var verifyResult = await AuthApi().mobileVerify(result.domain!, forceBetaDomain: true);
      var authCode = await _getAuthCode(result, verifyResult);
      var token = await _getToken(result, verifyResult, authCode!);

      result = result.rebuild((b) => b..token = token);
      return result;
    } else {
      print("error request:" + response.requestOptions.toString() + ", headers: ${response.requestOptions.headers.toString()}");
      print(
          "error response body: ${response.data}, status: ${response.statusCode}, message: ${response.statusMessage} ");
      return null;
    }
  }

  // Get the token for the SeededUser, given MobileVerifyResult and authCode
  static Future<String?> _getToken(SeededUser user, MobileVerifyResult? verifyResult, String authCode) async {
    var dio = await seedingDio(baseUrl: "https://${user.domain}/");

    var response = await dio.post('login/oauth2/token', queryParameters: {
      "client_id": verifyResult?.clientId,
      "client_secret": verifyResult?.clientSecret,
      "code": authCode,
      "redirect_uri": _REDIRECT_URI
    });

    if (response.statusCode == 200) {
      var parsedResponse = deserialize<OAuthToken>(response.data);
      var token = parsedResponse?.accessToken;

      return token;
    } else {
      print("Token fetch FAILED, status=${response.statusCode}");
      return null;
    }
  }

  // Get the authCode for the SeededUser, using the clientId from verifyResult.
  // This one is a little tricky as we have to call into native Android jsoup logic.
  static Future<String?> _getAuthCode(SeededUser user, MobileVerifyResult? verifyResult) async {
    try {
      var result = await authCodeChannel.invokeMethod('getAuthCode', <String, dynamic>{
        'domain': user.domain,
        'clientId': verifyResult?.clientId,
        'redirectUrl': _REDIRECT_URI,
        'login': user.loginId,
        'password': user.password
      });

      return result;
    } on PlatformException catch (e) {
      print("authCode platform exception: $e");
      return null;
    } on MissingPluginException catch (e) {
      print("authCode missing plugin exception: $e");
      return null;
    }
  }

  /// Obtain a pairing code for the indicated observee.
  /// Will only work if the observee has been enrolled in a course as a student.
  /// Returns a PairingCode structure, which contains a "code" field.
  static Future<PairingCode?> createObserverPairingCode(String observeeId) async {
    var dio = await seedingDio();
    return fetch(dio.post('users/$observeeId/observer_pairing_codes'));
  }

  /// Add [observer] as an observer for [observee], using the indicated pairingCode.
  static Future<bool> addObservee(SeededUser observer, SeededUser observee, String? pairingCode) async {
    try {
      var dio = await seedingDio();
      var pairingResponse = await dio.post('users/${observer.id}/observees',
          queryParameters: {'pairing_code': pairingCode, 'access_token': observee.token});
      return (pairingResponse.statusCode == 200 || pairingResponse.statusCode == 201);
    } on DioError {
      return false;
    }
  }
}
