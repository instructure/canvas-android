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

import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final api = MockAuthApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<AuthApi>(() => api);
  });

  setUp(() {
    reset(api);
  });

  test('mobileVerify calls to the api', () async {
    final domain = 'domain';

    await WebLoginInteractor().mobileVerify(domain);

    verify(api.mobileVerify(domain)).called(1);
  });

  test('performLogin updates the ApiPrefs', () async {
    await setupPlatformChannels();

    final request = 'request';
    final accessToken = 'accessToken';
    final user = User((b) => b..name = 'interactor student name');
    final mobileVerify = MobileVerifyResult();
    final tokens = CanvasToken((b) => b
      ..refreshToken = 'refresh'
      ..accessToken = accessToken
      ..user = user.toBuilder());

    when(api.getTokens(mobileVerify, request)).thenAnswer((_) async => tokens);

    await WebLoginInteractor().performLogin(mobileVerify, request);

    verify(api.getTokens(mobileVerify, request)).called(1);
    expect(ApiPrefs.getUser(), user);
  });
}
