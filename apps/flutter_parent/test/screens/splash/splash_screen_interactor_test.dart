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

import 'package:flutter_parent/models/account_permissions.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/splash/splash_screen_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/canvas_model_utils.dart';
import '../../utils/test_app.dart';

void main() {
  _MockDashboardInteractor dashboardInteractor = _MockDashboardInteractor();
  _MockAccountsApi accountsApi = _MockAccountsApi();

  Login login = Login((b) => b
    ..domain = 'domain'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  setupTestLocator((locator) {
    locator.registerFactory<DashboardInteractor>(() => dashboardInteractor);
    locator.registerLazySingleton<AccountsApi>(() => accountsApi);
  });

  setUp(() async {
    reset(dashboardInteractor);
    reset(accountsApi);
    ApiPrefs.clean();

    // Default return value for getStudents is an empty list
    when(dashboardInteractor.getStudents(forceRefresh: true)).thenAnswer((_) async => []);

    // Default return value for getAccountPermissions is default AccountPermissions
    when(accountsApi.getAccountPermissions()).thenAnswer((_) async => AccountPermissions());

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
    expect(ApiPrefs.getCurrentLogin().canMasquerade, isTrue);
  });

  test('Sets canMasquerade to false if getAccountPermissions returns false', () async {
    ApiPrefs.switchLogins(login);

    // canMasquerade should not be set at this point
    expect(ApiPrefs.getCurrentLogin().canMasquerade, isNull);

    await SplashScreenInteractor().getData();

    // canMasquerade should now be set to false
    expect(ApiPrefs.getCurrentLogin().canMasquerade, isFalse);
  });

  test('Sets canMasquerade to true if getAccountPermissions returns true', () async {
    when(accountsApi.getAccountPermissions()).thenAnswer((_) async => AccountPermissions((b) => b..becomeUser = true));
    ApiPrefs.switchLogins(login);

    // canMasquerade should not be set at this point
    expect(ApiPrefs.getCurrentLogin().canMasquerade, isNull);

    await SplashScreenInteractor().getData();

    // canMasquerade should now be set to false
    expect(ApiPrefs.getCurrentLogin().canMasquerade, isTrue);
  });

  test('Sets canMasquerade to false if getAccountPermissions call fails', () async {
    when(accountsApi.getAccountPermissions()).thenAnswer((_) async => throw 'Fake Error');
    ApiPrefs.switchLogins(login);

    // canMasquerade should not be set at this point
    expect(ApiPrefs.getCurrentLogin().canMasquerade, isNull);

    await SplashScreenInteractor().getData();

    // canMasquerade should now be set to false
    expect(ApiPrefs.getCurrentLogin().canMasquerade, isFalse);
  });

  test('getData returns false for isObserver if user is not observing any students', () async {
    ApiPrefs.switchLogins(login);
    var data = await SplashScreenInteractor().getData();

    // isObserver should be false
    expect(data.isObserver, isFalse);
  });

  test('getData returns true for isObserver if user is observing students', () async {
    when(dashboardInteractor.getStudents(forceRefresh: true)).thenAnswer((_) async => [
          CanvasModelTestUtils.mockUser(name: 'User 1'),
          CanvasModelTestUtils.mockUser(name: 'User 2'),
        ]);
    ApiPrefs.switchLogins(login);
    var data = await SplashScreenInteractor().getData();

    // isObserver should be true
    expect(data.isObserver, isTrue);
  });

  test('getData should return existing value for canMasquerade', () async {
    ApiPrefs.switchLogins(login.rebuild((b) => b..canMasquerade = true));
    var data = await SplashScreenInteractor().getData();

    // canMasquerade should be true
    expect(data.canMasquerade, isTrue);
  });
}

class _MockAccountsApi extends Mock implements AccountsApi {}

class _MockDashboardInteractor extends Mock implements DashboardInteractor {}
