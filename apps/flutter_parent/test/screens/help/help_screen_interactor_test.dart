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
import 'package:flutter_parent/models/help_link.dart';
import 'package:flutter_parent/models/help_links.dart';
import 'package:flutter_parent/network/api/help_links_api.dart';
import 'package:flutter_parent/screens/help/help_screen_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  test('getObserverCustomHelpLinks calls to HelpLinksApi', () async {
    var api = MockHelpLinksApi();
    await setupTestLocator((locator) => locator.registerLazySingleton<HelpLinksApi>(() => api));
    when(api.getHelpLinks(forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) => Future.value(createHelpLinks()));

    HelpScreenInteractor().getObserverCustomHelpLinks();

    verify(api.getHelpLinks(forceRefresh: false)).called(1);
  });

  test('getObserverCustomHelpLinks only returns links for observers', () async {
    var api = MockHelpLinksApi();
    var customLinks = [
      createHelpLink(availableTo: [AvailableTo.observer]),
      createHelpLink(availableTo: [AvailableTo.user]),
      createHelpLink(availableTo: [AvailableTo.unenrolled]),
      createHelpLink(availableTo: [AvailableTo.teacher])
    ];

    await setupTestLocator((locator) => locator.registerLazySingleton<HelpLinksApi>(() => api));
    when(api.getHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value(createHelpLinks(customLinks: customLinks)));

    var list = await HelpScreenInteractor().getObserverCustomHelpLinks();

    // Verify that we called the api
    verify(api.getHelpLinks(forceRefresh: false)).called(1);

    // Make sure the link available only to teachers is not in the returned list, but everything else is
    expect(list.any((l) => l.availableTo.contains(AvailableTo.teacher)), false);
    expect(list.any((l) => l.availableTo.contains(AvailableTo.observer)), true);
    expect(list.any((l) => l.availableTo.contains(AvailableTo.user)), true);
    expect(list.any((l) => l.availableTo.contains(AvailableTo.unenrolled)), false);
  });

  test('containsObserverLinks returns true when observer links present in list, false otherwise', () async {
    var observerLinks = [
      createHelpLink(availableTo: [AvailableTo.observer]),
      createHelpLink(availableTo: [AvailableTo.user]),
      createHelpLink(availableTo: [AvailableTo.unenrolled]),
      createHelpLink(availableTo: [AvailableTo.teacher]),
    ];

    var nonObserverLinks = [
      createHelpLink(availableTo: [AvailableTo.student]),
      createHelpLink(availableTo: [AvailableTo.teacher]),
      createHelpLink(availableTo: [AvailableTo.admin]),
      createHelpLink(availableTo: [AvailableTo.admin]),
    ];

    expect(HelpScreenInteractor().containsObserverLinks(BuiltList.from(observerLinks)), true);
    expect(HelpScreenInteractor().containsObserverLinks(BuiltList.from(nonObserverLinks)), false);
  });

  test('filterObserverLinks only returns observer links', () async {
    var observerLinks = [
      createHelpLink(availableTo: [AvailableTo.observer]),
      createHelpLink(availableTo: [AvailableTo.user]),
    ];

    var nonObserverLinks = [
      createHelpLink(availableTo: [AvailableTo.student]),
      createHelpLink(availableTo: [AvailableTo.teacher]),
      createHelpLink(availableTo: [AvailableTo.admin]),
      createHelpLink(availableTo: [AvailableTo.admin]),
      createHelpLink(availableTo: [AvailableTo.unenrolled]),
    ];

    expect(HelpScreenInteractor().filterObserverLinks(BuiltList.from([...observerLinks, ...nonObserverLinks])),
        observerLinks);
  });

  test('filterObserverLinks only returns links that has text and url', () async {
    var validLinks = [
      createHelpLink(availableTo: [AvailableTo.observer]),
      createHelpLink(availableTo: [AvailableTo.user]),
    ];

    var invalidLinks = [
      createNullableHelpLink(url: 'url', availableTo: [AvailableTo.observer]),
      createNullableHelpLink(text: 'text', availableTo: [AvailableTo.observer]),
    ];

    expect(HelpScreenInteractor().filterObserverLinks(BuiltList.from([...validLinks, ...invalidLinks])),
        validLinks);
  });

  test('custom list is returned if there are any custom lists', () async {
    var api = MockHelpLinksApi();
    var customLinks = [
      createHelpLink(availableTo: [AvailableTo.observer]),
      createHelpLink(availableTo: [AvailableTo.user]),
    ];

    var defaultLinks = [
      createHelpLink(availableTo: [AvailableTo.unenrolled]),
      createHelpLink(availableTo: [AvailableTo.student]),
      createHelpLink(availableTo: [AvailableTo.teacher]),
      createHelpLink(availableTo: [AvailableTo.admin]),
      createHelpLink(availableTo: [AvailableTo.admin]),
    ];

    await setupTestLocator((locator) => locator.registerLazySingleton<HelpLinksApi>(() => api));
    when(api.getHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value(createHelpLinks(customLinks: customLinks, defaultLinks: defaultLinks)));

    expect(await HelpScreenInteractor().getObserverCustomHelpLinks(), customLinks);
  });

  test('default list is returned if there are no custom lists', () async {
    var api = MockHelpLinksApi();
    var defaultLinks = [
      createHelpLink(availableTo: [AvailableTo.user]),
      createHelpLink(availableTo: [AvailableTo.observer]),
    ];

    await setupTestLocator((locator) => locator.registerLazySingleton<HelpLinksApi>(() => api));
    when(api.getHelpLinks(forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value(createHelpLinks(customLinks: [], defaultLinks: defaultLinks)));

    expect(await HelpScreenInteractor().getObserverCustomHelpLinks(), defaultLinks);
  });
}

HelpLinks createHelpLinks({List<HelpLink>? customLinks, List<HelpLink>? defaultLinks}) => HelpLinks((b) => b
  ..customHelpLinks = ListBuilder(customLinks != null ? customLinks : [createHelpLink()])
  ..defaultHelpLinks = ListBuilder(defaultLinks != null ? defaultLinks : [createHelpLink()]));

HelpLink createHelpLink({String? id, String? text, String? url, List<AvailableTo>? availableTo}) => HelpLink((b) => b
  ..id = id ?? ''
  ..type = ''
  ..availableTo = ListBuilder(availableTo != null ? availableTo : <AvailableTo>[])
  ..url = url ?? 'https://www.instructure.com'
  ..text = text ?? 'text'
  ..subtext = 'subtext');

HelpLink createNullableHelpLink({String? id, String? text, String? url, List<AvailableTo>? availableTo}) => HelpLink((b) => b
  ..id = id
  ..type = ''
  ..availableTo = ListBuilder(availableTo != null ? availableTo : <AvailableTo>[])
  ..url = url
  ..text = text
  ..subtext = 'subtext');