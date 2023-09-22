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

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/account_permissions.dart';
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/feature_flags.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/user_colors.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/api/features_api.dart';
import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/splash/splash_screen_interactor.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/canvas_model_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  MockDashboardInteractor dashboardInteractor = MockDashboardInteractor();
  MockAccountsApi accountsApi = MockAccountsApi();
  MockAuthApi authApi = MockAuthApi();
  final mockScanner = MockBarcodeScanVeneer();
  MockUserApi userApi = MockUserApi();
  MockFeaturesApi featuresApi = MockFeaturesApi();

  Login login = Login((b) => b
    ..domain = 'domain'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  MobileVerifyResult mobileVerifyResult = MobileVerifyResult((b) => b
    ..authorized = true
    ..result = VerifyResultEnum.success
    ..clientId = '123'
    ..clientSecret = '123'
    ..apiKey = '123'
    ..baseUrl = '123');

  CanvasToken canvasToken = CanvasToken((b) => b
    ..accessToken = '123'
    ..refreshToken = '123'
    ..user = CanvasModelTestUtils.mockUser().toBuilder()
    ..realUser = null);

  setupTestLocator((locator) {
    locator.registerFactory<DashboardInteractor>(() => dashboardInteractor);
    locator.registerLazySingleton<AccountsApi>(() => accountsApi);
    locator.registerLazySingleton<AuthApi>(() => authApi);
    locator.registerLazySingleton<BarcodeScanVeneer>(() => mockScanner);
    locator.registerLazySingleton<UserApi>(() => userApi);
    locator.registerLazySingleton<FeaturesApi>(() => featuresApi);
  });

  setUp(() async {
    reset(dashboardInteractor);
    reset(accountsApi);
    reset(authApi);
    reset(userApi);
    ApiPrefs.clean();

    // Default return value for getStudents is an empty list
    when(dashboardInteractor.getStudents(forceRefresh: true)).thenAnswer((_) async => []);

    // Default return value for getAccountPermissions is default AccountPermissions
    when(accountsApi.getAccountPermissions()).thenAnswer((_) async => AccountPermissions());

    // Default return value for auth apis
    when(authApi.mobileVerify(any)).thenAnswer((_) async => mobileVerifyResult);
    when(authApi.getTokens(any, any)).thenAnswer((_) async => canvasToken);

    // Default return value for user api
    when(userApi.getUserColors()).thenAnswer((_) async => UserColors());

    when(featuresApi.getFeatureFlags()).thenAnswer((_) async => FeatureFlags());

    await setupPlatformChannels();
  });

  test('getData uses same getStudents call as DashboardInteractor', () async {
    ApiPrefs.switchLogins(login);
    await SplashScreenInteractor().getData();

    // getStudents should have been called
    verify(dashboardInteractor.getStudents(forceRefresh: true));
  });

  test('Calls getAccountPermissions if canMasquerade is not set and domain is not siteadmin', () async {
    ApiPrefs.switchLogins(login);
    await SplashScreenInteractor().getData();

    // getAccountPermissions should have been called
    verify(accountsApi.getAccountPermissions());
  });

  test('Does not call getAccountPermissions if canMasquerade is not set and domain is siteadmin', () async {
    ApiPrefs.switchLogins(login.rebuild((b) => b..domain = 'https://siteadmin.instructure.com'));
    await SplashScreenInteractor().getData();

    // getAccountPermissions should not have been called
    verifyNever(accountsApi.getAccountPermissions());

    // canMasquerade should be set to true
    expect(ApiPrefs.getCurrentLogin()?.canMasquerade, isTrue);
  });

  test('Sets canMasquerade to false if getAccountPermissions returns false', () async {
    ApiPrefs.switchLogins(login);

    // canMasquerade should not be set at this point
    expect(ApiPrefs.getCurrentLogin()?.canMasquerade, isNull);

    await SplashScreenInteractor().getData();

    // canMasquerade should now be set to false
    expect(ApiPrefs.getCurrentLogin()?.canMasquerade, isFalse);
  });

  test('Sets canMasquerade to true if getAccountPermissions returns true', () async {
    when(accountsApi.getAccountPermissions()).thenAnswer((_) async => AccountPermissions((b) => b..becomeUser = true));
    ApiPrefs.switchLogins(login);

    // canMasquerade should not be set at this point
    expect(ApiPrefs.getCurrentLogin()?.canMasquerade, isNull);

    await SplashScreenInteractor().getData();

    // canMasquerade should now be set to false
    expect(ApiPrefs.getCurrentLogin()?.canMasquerade, isTrue);
  });

  test('Sets canMasquerade to false if getAccountPermissions call fails', () async {
    when(accountsApi.getAccountPermissions()).thenAnswer((_) async => throw 'Fake Error');
    ApiPrefs.switchLogins(login);

    // canMasquerade should not be set at this point
    expect(ApiPrefs.getCurrentLogin()?.canMasquerade, isNull);

    await SplashScreenInteractor().getData();

    // canMasquerade should now be set to false
    expect(ApiPrefs.getCurrentLogin()?.canMasquerade, isFalse);
  });

  test('getData returns false for isObserver if user is not observing any students', () async {
    ApiPrefs.switchLogins(login);
    var data = await SplashScreenInteractor().getData();

    // isObserver should be false
    expect(data?.isObserver, isFalse);
  });

  test('getData returns true for isObserver if user is observing students', () async {
    when(dashboardInteractor.getStudents(forceRefresh: true)).thenAnswer((_) async => [
          CanvasModelTestUtils.mockUser(name: 'User 1'),
          CanvasModelTestUtils.mockUser(name: 'User 2'),
        ]);
    ApiPrefs.switchLogins(login);
    var data = await SplashScreenInteractor().getData();

    // isObserver should be true
    expect(data?.isObserver, isTrue);
  });

  test('getData should return existing value for canMasquerade', () async {
    ApiPrefs.switchLogins(login.rebuild((b) => b..canMasquerade = true));
    var data = await SplashScreenInteractor().getData();

    // canMasquerade should be true
    expect(data?.canMasquerade, isTrue);
  });

  test('getData returns QRLoginError for invalid qrLoginUrl', () async {
    bool fail = false;
    await SplashScreenInteractor().getData(qrLoginUrl: 'https://hodor.com').catchError((_) {
      fail = true; // Don't return, just update the flag
      return Future.value(null);
    });
    expect(fail, isTrue);
  });

  test('getData returns valid data for valid qrLoginUrl', () async {
    when(dashboardInteractor.getStudents(forceRefresh: true)).thenAnswer((_) async => [
          CanvasModelTestUtils.mockUser(name: 'User 1'),
          CanvasModelTestUtils.mockUser(name: 'User 2'),
        ]);
    ApiPrefs.switchLogins(login);
    final url = 'https://sso.canvaslms.com/canvas/login?code_android_parent=1234&domain=mobiledev.instructure.com';
    var data = await SplashScreenInteractor().getData(qrLoginUrl: url);
    expect(data?.isObserver, isTrue);
    expect(data?.canMasquerade, isFalse);
  });

  test('getData returns valid data for valid qrLoginUrl, canMasquerade true for real user', () async {
    CanvasToken altToken = CanvasToken((b) => b
      ..accessToken = '123'
      ..refreshToken = '123'
      ..user = CanvasModelTestUtils.mockUser().toBuilder()
      ..realUser = CanvasModelTestUtils.mockUser().toBuilder());
    when(dashboardInteractor.getStudents(forceRefresh: true)).thenAnswer((_) async => [
          CanvasModelTestUtils.mockUser(name: 'User 1'),
          CanvasModelTestUtils.mockUser(name: 'User 2'),
        ]);
    when(authApi.getTokens(any, any)).thenAnswer((_) async => altToken);
    ApiPrefs.switchLogins(login);
    final url = 'https://sso.canvaslms.com/canvas/login?code_android_parent=1234&domain=mobiledev.instructure.com';
    var data = await SplashScreenInteractor().getData(qrLoginUrl: url);
    expect(data?.isObserver, isTrue);
    expect(data?.canMasquerade, isTrue);
  });

  test('getData returns QRLoginError for invalid auth code', () async {
    when(authApi.getTokens(any, any)).thenAnswer((_) async => Future.error(''));
    final url = 'https://sso.canvaslms.com/canvas/login?code_android_parent=1234&domain=mobiledev.instructure.com';
    bool fail = false;
    await SplashScreenInteractor().getData(qrLoginUrl: url).catchError((_) {
      fail = true; // Don't return, just update the flag
      return Future.value(null);
    });
    expect(fail, isTrue);
  });

  test('getData returns QRLoginError for error in mobile verify', () async {
    when(authApi.mobileVerify(any)).thenAnswer((_) async => Future.error(''));
    final url = 'https://sso.canvaslms.com/canvas/login?code_android_parent=1234&domain=mobiledev.instructure.com';
    bool fail = false;
    await SplashScreenInteractor().getData(qrLoginUrl: url).catchError((_) {
      fail = true; // Don't return, just update the flag
      return Future.value(null);
    });
    expect(fail, isTrue);
  });

  test('getCameraCount returns valid camera count and sets ApiPrefs', () async {
    when(mockScanner.getNumberOfCameras()).thenAnswer((_) => Future.value(2));

    final count = await SplashScreenInteractor().getCameraCount();

    final prefCount = await ApiPrefs.getCameraCount();

    expect(count, prefCount);
  });

  test('updateUserColors calls UserApi and saves result to database', () async {
    var expectedColors = UserColors((b) => b..customColors = MapBuilder({'user_1234': '#FFFFFFFF'}));
    when(userApi.getUserColors(refresh: true)).thenAnswer((_) async => expectedColors);
    await ApiPrefs.switchLogins(login);

    await SplashScreenInteractor().updateUserColors();

    verify(userApi.getUserColors(refresh: true));

    var db = (locator<UserColorsDb>() as MockUserColorsDb);
    verify(db.insertOrUpdateAll(login.domain, login.user.id, expectedColors));
  });
}