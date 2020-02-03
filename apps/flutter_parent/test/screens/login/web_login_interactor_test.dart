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
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';

import '../../utils/test_app.dart';

void main() {
  _setupLocator({AuthApi authApi}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerLazySingleton<AuthApi>(() => authApi ?? _MockAuthApi());
  }

  test('mobileVerify calls to the api', () async {
    final domain = 'domain';
    final api = _MockAuthApi();
    _setupLocator(authApi: api);

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

    final api = _MockAuthApi();
    when(api.getTokens(mobileVerify, request)).thenAnswer((_) async => tokens);
    _setupLocator(authApi: api);

    await WebLoginInteractor().performLogin(mobileVerify, request);

    verify(api.getTokens(mobileVerify, request)).called(1);
    expect(ApiPrefs.getUser(), user);
  });
}

class _MockAuthApi extends Mock implements AuthApi {}
