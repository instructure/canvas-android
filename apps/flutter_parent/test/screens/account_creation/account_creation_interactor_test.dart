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

import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/screens/account_creation/account_creation_interactor.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final launcher = MockUrlLauncher();
  final api = MockAccountsApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<AccountsApi>(() => api);
    locator.registerLazySingleton<UrlLauncher>(() => launcher);
  });

  setUp(() {
    reset(launcher);
    reset(api);
  });

  test('getToSForAccount calls the api', () {
    AccountCreationInteractor().getToSForAccount('123', 'hodor.com');
    verify(api.getTermsOfServiceForAccount(any, any));
  });

  test('createNewAccount calls the api', () {
    AccountCreationInteractor().createNewAccount('123', '12345', 'hodor', 'hodor@hodor.com', 'hodor', 'hodor.com');
    verify(api.createNewAccount(any, any, any, any, any, any));
  });

  test('launchDefaultToS calls the url launcher', () {
    AccountCreationInteractor().launchDefaultToS();
    verify(
      launcher.launch('https://www.instructure.com/policies/terms-of-use-canvas/'),
    ).called(1);
  });

  test('launchPrivacyPolicy calls the url launcher', () {
    AccountCreationInteractor().launchPrivacyPolicy();
    verify(
      launcher.launch('https://www.instructure.com/policies/product-privacy-policy'),
    ).called(1);
  });
}
