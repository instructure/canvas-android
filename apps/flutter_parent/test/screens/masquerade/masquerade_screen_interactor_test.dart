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
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/masquerade/masquerade_screen_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/canvas_model_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  MockUserApi userApi = MockUserApi();

  setupTestLocator((locator) {
    locator.registerLazySingleton<UserApi>(() => userApi);
  });

  setUp(() async {
    reset(userApi);
    await setupPlatformChannels();
  });

  test('Gets domain from ApiPrefs', () {
    String expectedDomain = 'domain';
    ApiPrefs.switchLogins(Login((b) => b..domain = expectedDomain));

    String? actualDomain = MasqueradeScreenInteractor().getDomain();
    expect(actualDomain, expectedDomain);
  });

  group('startMasquerading', () {
    test('startMasquerading returns fails on API error', () async {
      when(userApi.getUserForDomain(any, any)).thenAnswer((_) => Future.error('fake error'));

      bool result = await MasqueradeScreenInteractor().startMasquerading('', '');
      expect(result, isFalse);
    });

    test('startMasquerading calls API with correct userId and domain', () async {
      String userId = 'user_123';
      String domain = 'testDomain';
      when(userApi.getUserForDomain(any, any)).thenAnswer((_) => Future.error('fake error'));

      await MasqueradeScreenInteractor().startMasquerading(userId, domain);
      verify(userApi.getUserForDomain(domain, userId));
    });

    test('startMasquerading returns true on success and updates current Login', () async {
      String originalDomain = 'domain';
      User originalUser = CanvasModelTestUtils.mockUser(name: 'Original User');
      Login originalLogin = Login((b) => b
        ..domain = originalDomain
        ..user = originalUser.toBuilder());

      String masqueradeUserId = 'user_123';
      String masqueradeDomain = 'masqueradeDomain';
      User masqueradeUser = CanvasModelTestUtils.mockUser(name: 'Masked User', id: masqueradeUserId);
      Login masqueradeLogin = originalLogin.rebuild((b) => b
        ..masqueradeDomain = masqueradeDomain
        ..masqueradeUser = masqueradeUser.toBuilder());

      when(userApi.getUserForDomain(any, any)).thenAnswer((_) async => masqueradeUser);
      ApiPrefs.switchLogins(originalLogin);

      bool result = await MasqueradeScreenInteractor().startMasquerading(masqueradeUserId, masqueradeDomain);
      expect(result, isTrue);
      expect(ApiPrefs.getCurrentLogin(), masqueradeLogin);
    });
  });

  group('sanitizeDomain', () {
    test('sanitizeDomain removes white space', () {
      String input = ' htt ps://example . instructure. com ';
      String expected = 'https://example.instructure.com';

      String actual = MasqueradeScreenInteractor().sanitizeDomain(input);
      expect(actual, expected);
    });

    test('sanitizeDomain adds ".instructure.com" if missing', () {
      String input = 'https://example';
      String expected = 'https://example.instructure.com';

      String actual = MasqueradeScreenInteractor().sanitizeDomain(input);
      expect(actual, expected);
    });

    test('sanitizeDomain adds ".instructure.com" if missing on beta domains', () {
      String input = 'https://example.beta';
      String expected = 'https://example.beta.instructure.com';

      String actual = MasqueradeScreenInteractor().sanitizeDomain(input);
      expect(actual, expected);
    });

    test('sanitizeDomain adds https protocol if missing', () {
      String input = 'example.instructure.com';
      String expected = 'https://example.instructure.com';

      String actual = MasqueradeScreenInteractor().sanitizeDomain(input);
      expect(actual, expected);
    });

    test('sanitizeDomain does not add https protocol if a protocol is already present', () {
      String input = 'http://example';
      String expected = 'http://example.instructure.com';

      String actual = MasqueradeScreenInteractor().sanitizeDomain(input);
      expect(actual, expected);
    });

    test('sanitizeDomain returns unmodified input if already sanitized', () {
      String input = 'https://example.instructure.com';
      String expected = 'https://example.instructure.com';

      String actual = MasqueradeScreenInteractor().sanitizeDomain(input);
      expect(actual, expected);
    });

    test('sanitizeDomain returns an empty string for blank input', () {
      String input = '';
      String expected = '';

      String actual = MasqueradeScreenInteractor().sanitizeDomain(input);
      expect(actual, expected);
    });

    test('sanitizeDomain returns an empty string for null input', () {
      String? input = null;
      String expected = '';

      String actual = MasqueradeScreenInteractor().sanitizeDomain(input);
      expect(actual, expected);
    });

    test('sanitizeDomain returns an empty string for invalid input', () {
      String input = 'www.🎯.com';
      String expected = '';

      String actual = MasqueradeScreenInteractor().sanitizeDomain(input);
      expect(actual, expected);
    });
  });
}