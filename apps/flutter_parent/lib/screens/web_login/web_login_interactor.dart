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

import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class WebLoginInteractor {
  Future<MobileVerifyResult?> mobileVerify(String domain) {
    return locator<AuthApi>().mobileVerify(domain);
  }

  Future performLogin(MobileVerifyResult? result, String oAuthRequest) async {
    final tokens = await locator<AuthApi>().getTokens(result, oAuthRequest);

    Login login = Login((b) => b
      ..accessToken = tokens?.accessToken
      ..refreshToken = tokens?.refreshToken
      ..domain = result?.baseUrl
      ..clientId = result?.clientId
      ..clientSecret = result?.clientSecret
      ..user = tokens?.user?.toBuilder());

    ApiPrefs.addLogin(login);
    ApiPrefs.switchLogins(login);
  }
}
