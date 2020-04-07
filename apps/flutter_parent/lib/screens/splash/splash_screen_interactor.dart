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

import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/masquerade/masquerade_screen_interactor.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class SplashScreenInteractor {
  Future<SplashScreenData> getData({String qrLoginUrl}) async {
    if(qrLoginUrl != null) {
      final qrLoginUri = Uri.parse(qrLoginUrl);
      // Double check the loginUrl
      if(!QRUtils.verifySSOLogin(qrLoginUri)) {
        return Future.error(QRLoginError);
      } else {
        final qrSuccess = await _performSSOLogin(qrLoginUri);
        // Error out if the login fails, otherwise continue
        if(!qrSuccess) return Future.error(QRLoginError);
      }
      return Future.error(QRLoginError);
    }

    // Use same call as the dashboard so results will be cached
    var students = await locator<DashboardInteractor>().getStudents(forceRefresh: true);
    var isObserver = students.isNotEmpty;

    // Check for masquerade permissions if we haven't already
    if (ApiPrefs.getCurrentLogin().canMasquerade == null) {
      if (ApiPrefs.getDomain().contains(MasqueradeScreenInteractor.siteAdminDomain)) {
        ApiPrefs.updateCurrentLogin((b) => b..canMasquerade = true);
      } else {
        try {
          var permissions = await locator<AccountsApi>().getAccountPermissions();
          ApiPrefs.updateCurrentLogin((b) => b..canMasquerade = permissions.becomeUser);
        } catch (e) {
          ApiPrefs.updateCurrentLogin((b) => b..canMasquerade = false);
        }
      }
    }

    return SplashScreenData(isObserver, ApiPrefs.getCurrentLogin().canMasquerade);
  }

  Future<bool> _performSSOLogin(Uri qrLoginUri) async {
    final domain = qrLoginUri.queryParameters[QRUtils.QR_DOMAIN];
    final oAuthCode = qrLoginUri.queryParameters[QRUtils.QR_AUTH_CODE];

    final mobileVerifyResult = await locator<AuthApi>().mobileVerify(domain);

    if(mobileVerifyResult.result != VerifyResultEnum.success) {
      return Future.value(false);
    }

    final tokenResponse = await locator<AuthApi>().getTokens(mobileVerifyResult, oAuthCode);

    // Key here is that realUser represents a masquerading attempt
    var isMasquerading = tokenResponse.realUser != null;
    Login login = Login((b) => b
      ..accessToken = tokenResponse.accessToken
      ..refreshToken = tokenResponse.refreshToken
      ..domain = mobileVerifyResult.baseUrl
      ..clientId = mobileVerifyResult.clientId
      ..clientSecret = mobileVerifyResult.clientSecret
      ..masqueradeUser = isMasquerading ? tokenResponse.user.toBuilder() : null
      ..masqueradeDomain = isMasquerading ? mobileVerifyResult.baseUrl : null
      ..isMasqueradingFromQRCode = isMasquerading
      ..canMasquerade = isMasquerading
      ..user = tokenResponse.user.toBuilder());

    ApiPrefs.addLogin(login);
    ApiPrefs.switchLogins(login);
    await DioConfig().clearCache();

    return Future.value(true);
  }
}

class SplashScreenData {
  final bool isObserver;
  final bool canMasquerade;

  SplashScreenData(this.isObserver, this.canMasquerade);
}

class QRLoginError {}
