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

import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/api/oauth_api.dart';
import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/masquerade/masquerade_screen_interactor.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
import 'package:flutter_parent/utils/features_utils.dart';
import 'package:flutter_parent/utils/qr_utils.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';

class SplashScreenInteractor {
  Future<SplashScreenData?> getData({String? qrLoginUrl}) async {
    if (qrLoginUrl != null) {
      // Double check the loginUrl
      final qrLoginUri = QRUtils.verifySSOLogin(qrLoginUrl);
      if (qrLoginUri == null) {
        locator<Analytics>().logEvent(AnalyticsEventConstants.QR_LOGIN_FAILURE);
        return Future.error(QRLoginError());
      } else {
        final qrSuccess = await _performSSOLogin(qrLoginUri);
        // Error out if the login fails, otherwise continue
        if (!qrSuccess) {
          locator<Analytics>().logEvent(
            AnalyticsEventConstants.QR_LOGIN_FAILURE,
            extras: {AnalyticsParamConstants.DOMAIN_PARAM: qrLoginUri.host},
          );
          return Future.error(QRLoginError());
        } else {
          locator<Analytics>().logEvent(
            AnalyticsEventConstants.QR_LOGIN_SUCCESS,
            extras: {AnalyticsParamConstants.DOMAIN_PARAM: qrLoginUri.host},
          );
        }
      }
    }

    // Use same call as the dashboard so results will be cached
    var students = await locator<DashboardInteractor>().getStudents(forceRefresh: true);
    var isObserver = students?.isNotEmpty ?? false;

    // Check for masquerade permissions if we haven't already
    if (ApiPrefs.getCurrentLogin()?.canMasquerade == null) {
      if (ApiPrefs.getDomain()?.contains(MasqueradeScreenInteractor.siteAdminDomain) == true) {
        ApiPrefs.updateCurrentLogin((b) => b..canMasquerade = true);
      } else {
        try {
          var permissions = await locator<AccountsApi>().getAccountPermissions();
          ApiPrefs.updateCurrentLogin((b) => b..canMasquerade = permissions?.becomeUser);
        } catch (e) {
          ApiPrefs.updateCurrentLogin((b) => b..canMasquerade = false);
        }
      }
    }

    SplashScreenData data = SplashScreenData(isObserver, ApiPrefs.getCurrentLogin()?.canMasquerade == true);

    if (data.isObserver || data.canMasquerade) await updateUserColors();

    await FeaturesUtils.checkUsageMetricFeatureFlag();

    return data;
  }

  Future<void> updateUserColors() async {
    var colors = await locator<UserApi>().getUserColors(refresh: true);
    if (colors == null) return;
    await locator<UserColorsDb>().insertOrUpdateAll(ApiPrefs.getDomain(), ApiPrefs.getUser()?.id, colors);
  }

  Future<int> getCameraCount() async {
    if (ApiPrefs.getCameraCount() == null) {
      int cameraCount = await locator<BarcodeScanVeneer>().getNumberOfCameras();
      await ApiPrefs.setCameraCount(cameraCount);
      return cameraCount;
    } else {
      return ApiPrefs.getCameraCount() ?? 0;
    }
  }

  Future<bool> _performSSOLogin(Uri qrLoginUri) async {
    final domain = qrLoginUri.queryParameters[QRUtils.QR_DOMAIN] ?? '';
    final oAuthCode = qrLoginUri.queryParameters[QRUtils.QR_AUTH_CODE] ?? '';

    final mobileVerifyResult = await locator<AuthApi>().mobileVerify(domain);

    if (mobileVerifyResult?.result != VerifyResultEnum.success) {
      return Future.value(false);
    }

    CanvasToken? tokenResponse;
    try {
      tokenResponse = await locator<AuthApi>().getTokens(mobileVerifyResult, oAuthCode);
    } catch (e) {
      return Future.value(false);
    }

    // Key here is that realUser represents a masquerading attempt
    var isMasquerading = tokenResponse?.realUser != null;
    Login login = Login((b) => b
      ..accessToken = tokenResponse?.accessToken
      ..refreshToken = tokenResponse?.refreshToken
      ..domain = mobileVerifyResult?.baseUrl
      ..clientId = mobileVerifyResult?.clientId
      ..clientSecret = mobileVerifyResult?.clientSecret
      ..masqueradeUser = isMasquerading ? tokenResponse?.user?.toBuilder() : null
      ..masqueradeDomain = isMasquerading ? mobileVerifyResult?.baseUrl : null
      ..isMasqueradingFromQRCode = isMasquerading ? true : null
      ..canMasquerade = isMasquerading ? true : null
      ..user = tokenResponse?.user?.toBuilder());

    ApiPrefs.addLogin(login);
    ApiPrefs.switchLogins(login);
    await DioConfig().clearCache();

    return Future.value(true);
  }

  Future<bool?> _requiresTermsAcceptance(String targetUrl) async {
    return (await locator.get<OAuthApi>().getAuthenticatedUrl(targetUrl))?.requiresTermsAcceptance;
  }

  Future<bool?> isTermsAcceptanceRequired() async {
    final targetUrl = '${ApiPrefs.getCurrentLogin()?.domain}/users/self';
    String? domain = ApiPrefs.getDomain();
    if (domain == null) {
      return false;
    } else if (targetUrl.contains(domain)) {
      return _requiresTermsAcceptance(targetUrl);
    } else {
      return false;
    }
  }
}

class SplashScreenData {
  final bool isObserver;
  final bool canMasquerade;

  SplashScreenData(this.isObserver, this.canMasquerade);
}

class QRLoginError {}
