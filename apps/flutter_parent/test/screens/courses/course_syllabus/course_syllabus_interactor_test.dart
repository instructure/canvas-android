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

import 'package:flutter_parent/models/authenticated_url.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/network/api/oauth_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/courses/details/course_syllabus/course_syllabus_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../../utils/platform_config.dart';
import '../../../utils/test_app.dart';

void main() {
  test('_getAuthedUrl calls OAuth api for an authenticated url', () async {
    final api = _MockOAuthApi();
    setupTestLocator((locator) => locator.registerLazySingleton<OAuthApi>(() => api));
    when(api.getAuthenticatedUrl(any)).thenAnswer((_) async => null);

    await CourseSyllabusInteractor().getAuthedUrl('');

    verify(api.getAuthenticatedUrl(''));
  });

  test('getUrl calls getAuthedUrl if url is in domain', () async {
    final api = _MockOAuthApi();
    when(api.getAuthenticatedUrl(any))
        .thenAnswer((_) async => Future.value(AuthenticatedUrl((b) => b.sessionUrl = '')));

    setupTestLocator((locator) {
      locator.registerFactory<OAuthApi>(() => api);
    });

    final login = Login((b) => b.domain = 'https://www.instructure.com');
    await setupPlatformChannels(config: PlatformConfig(mockPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    final url = 'https://www.instructure.com/pandafest';
    await CourseSyllabusInteractor().getUrl(url);

    verify(api.getAuthenticatedUrl(url));
  });

  test('getUrl returns url if it is not in the domain', () async {
    var url = 'https://www.pandas.com';
    final api = _MockOAuthApi();
    when(api.getAuthenticatedUrl(any))
        .thenAnswer((_) async => Future.value(AuthenticatedUrl((b) => b.sessionUrl = url)));

    setupTestLocator((locator) {
      locator.registerFactory<OAuthApi>(() => api);
    });

    final login = Login((b) => b.domain = 'https://www.instructure.com');
    await setupPlatformChannels(config: PlatformConfig(mockPrefs: {ApiPrefs.KEY_CURRENT_LOGIN_UUID: login.uuid}));
    await ApiPrefs.addLogin(login);

    var actual = await CourseSyllabusInteractor().getUrl(url);

    expect(actual, url);
  });
}

class _MockOAuthApi extends Mock implements OAuthApi {}
