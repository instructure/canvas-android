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
import 'package:flutter_parent/screens/domain_search/domain_search_interactor.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
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

  test('perform search calls the api', () {
    final search = 'xyz';
    DomainSearchInteractor().performSearch(search);
    verify(api.searchDomains(search)).called(1);
  });

  test('open canvas guides calls the url launcher', () {
    DomainSearchInteractor().openCanvasGuides();
    verify(
      launcher.launch('https://community.canvaslms.com/docs/DOC-9902-canvas-parent-android-guide-table-of-contents'),
    ).called(1);
  });

  test('open canvas support calls the url launcher', () {
    DomainSearchInteractor().openCanvasSupport();
    verify(launcher.launch('https://community.canvaslms.com/docs/DOC-17624-how-to-contact-canvas-support')).called(1);
  });
}
