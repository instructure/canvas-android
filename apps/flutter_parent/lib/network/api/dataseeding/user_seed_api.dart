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
import 'dart:convert' as convert;

import 'package:faker/faker.dart';
import 'package:flutter/services.dart';
import 'package:flutter_parent/models/dataseeding/create_user_info.dart';
import 'package:flutter_parent/models/dataseeding/oauth_token.dart';
import 'package:flutter_parent/models/dataseeding/seeded_user.dart';
import 'package:flutter_parent/models/dataseeding/user_name_data.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:dio/dio.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/network/utils/private_consts.dart';

import '../auth_api.dart';

const _REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

const _createUserEndpoint = "accounts/self/users";

class UserSeedApi {
  static const authCodeChannel = const MethodChannel("GET_AUTH_CODE");

  static Future<SeededUser> createUser() async {
    var url = ApiPrefs.baseSeedingUrl + _createUserEndpoint;

    var lastName = faker.person.lastName();
    var firstName = faker.person.firstName();
    var userData = CreateUserInfo((b) => b
      ..user.name = "$firstName $lastName"
      ..user.shortName = firstName
      ..user.sortableName = "$lastName, $firstName"
      ..pseudonym.uniqueId = Guid().guid()
      ..pseudonym.password = Guid().guid()
      // Don't care about CommunicationChannel initialization for now
      ..build()
    );

//    }) new CreateUserInfo(
//        user:
//        new UserNameData(name: "$firstName $lastName", shortName: firstName, sortableName: "$lastName, $firstName"),
//        pseudonym: new Pseudonym(uniqueId: Guid().guid(), password: Guid().guid()),
//        communicationChannel: new CommunicationChannel());

    var postBody = json.encode(serialize(userData));
    print("New user postBody: $postBody");

    await ApiPrefs.init();

    var dio =  seedingDio();

    var response = await dio.post(_createUserEndpoint, data: postBody);
//    var response = await http.post(url,
//        headers: ApiPrefs.getHeaderMap(
//            forceDeviceLanguage: true,
//            token: _DATA_SEEDING_ADMIN_TOKEN,
//            extraHeaders: {'Content-type': 'application/json', 'Accept': 'application/json'}),
//        body: postBody);

    if (response.statusCode == 200) {
      print("Create User response: ${response.data}");
      var result = deserialize<SeededUser>(response.data);
      result = result.rebuild((b) => b
        ..loginId = userData.pseudonym.uniqueId
        ..password = userData.pseudonym.password
        ..domain = response.request.uri.host
        ..token = "TODO"); // TODO: Make API call to get token

      print("request headers: ${response.request.headers.toString()}");

      var verifyResult = await AuthApi().mobileVerify(result.domain);
      print("verifyResult = $verifyResult");

      var authCode = await _getAuthCode(result, verifyResult);
      print("authCode = $authCode");

      var token = await _getToken(result, verifyResult, authCode);

      result = result.rebuild((b) => b
        ..token = token
      );
      return result;
    } else {
      print("error request:" + response.request.toString() + ", headers: ${response.request.headers.toString()}");
      print(
          "error response body: ${response.data}, status: ${response.statusCode}, message: ${response.statusMessage} ");
      return null;
    }
  }

  static Future<String> _getToken(SeededUser user, MobileVerifyResult verifyResult, String authCode) async {

    var dio = seedingDio(baseUrl: "https://${user.domain}/");

    var response = await dio.post(
      'login/oauth2/token',
      queryParameters: {
        "client_id" : verifyResult.clientId,
        "client_secret" : verifyResult.clientSecret,
        "code" : authCode,
        "redirect_uri" : _REDIRECT_URI
      }
    );
//    var url = 'https://${user.domain}/login/oauth2/token'
//        '?client_id=${verifyResult.clientId}'
//        '&client_secret=${verifyResult.clientSecret}'
//        '&code=${authCode}'
//        '&redirect_uri=${_REDIRECT_URI}';
//
//    var response = await http.post(
//      url,
//      headers: ApiPrefs.getHeaderMap(
//          forceDeviceLanguage: true,
//          token: _DATA_SEEDING_ADMIN_TOKEN,
//          extraHeaders: {'Content-type': 'application/json', 'Accept': 'application/json'}),
//    );

    if (response.statusCode == 200) {
      var parsedResponse = deserialize<OAuthToken>(response.data);
      var token = parsedResponse.accessToken;

      print("getToken result = $token");

      return token;
    } else {
      print("Token fetch FAILED, status=${response.statusCode}");
      return null;
    }
  }

  static Future<String> _getAuthCode(SeededUser user, MobileVerifyResult verifyResult) async {
    try {
      var result = await authCodeChannel.invokeMethod('getAuthCode', <String, dynamic>{
        'domain': user.domain,
        'clientId': verifyResult.clientId,
        'redirectUrl': _REDIRECT_URI,
        'login': user.loginId,
        'password': user.password
      });

      print("authCode from native jsoup call: " + result);
      return result;
    } on PlatformException catch (e) {
      print("authCode platform exception: $e");
      return null;
    } on MissingPluginException catch (e) {
      print("authCode missing plugin exception: $e");
      return null;
    }
  }
}
