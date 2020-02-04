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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/screens/splash/splash_screen.dart';
import 'package:flutter_parent/utils/common_widgets/avatar.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/test_app.dart';

void main() {
  setupLocator();

  testWidgetsWithAccessibilityChecks('Opens domain search screen', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().findSchoolOrDistrict), findsOneWidget);
    await tester.tap(find.text(AppLocalizations().findSchoolOrDistrict));
    await tester.pumpAndSettle();

    expect(find.byType(DomainSearchScreen), findsOneWidget);

    // TODO: Remove this back press once DomainSearchScreen is passing accessibility checks
    await tester.pageBack();
  });

  testWidgetsWithAccessibilityChecks('Displays Snicker Doodles drawer', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen()));
    await tester.pumpAndSettle();

    var size = tester.getSize(find.byType(LoginLandingScreen));
    await tester.flingFrom(Offset(size.width - 5, size.height / 2), Offset(-size.width / 2, 0), 1000);
    await tester.pumpAndSettle();

    expect(find.byType(Drawer), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not display login list if there are no previous logins', (tester) async {
    await tester.pumpWidget(TestApp(LoginLandingScreen(), highContrast: true));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().previousLogins), findsNothing);
    expect(find.byKey(Key('previous-logins')), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Displays login list if there are previous logins', (tester) async {
    List<Login> logins = [
      Login((b) => b
        ..domain = 'domain1'
        ..user = CanvasModelTestUtils.mockUser(name: 'user 1').toBuilder()),
      Login((b) => b
        ..domain = 'domain2'
        ..user = CanvasModelTestUtils.mockUser(name: 'user 2').toBuilder()),
    ];

    await tester.pumpWidget(TestApp(LoginLandingScreen(), highContrast: true));
    await ApiPrefs.saveLogins(logins);
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().previousLogins), findsOneWidget);
    expect(find.byKey(Key('previous-logins')), findsOneWidget);

    expect(find.text(logins[0].user.name), findsOneWidget);
    expect(find.text(logins[1].user.name), findsOneWidget);

    expect(find.text(logins[0].domain), findsOneWidget);
    expect(find.text(logins[1].domain), findsOneWidget);

    expect(find.byType(Avatar), findsNWidgets(2));
    expect(find.bySemanticsLabel(AppLocalizations().delete), findsNWidgets(2));
  });

  testWidgetsWithAccessibilityChecks('Clearing previous login removes it from the list', (tester) async {
    List<Login> logins = [
      Login((b) => b
        ..domain = 'domain1'
        ..user = CanvasModelTestUtils.mockUser(name: 'user 1').toBuilder()),
      Login((b) => b
        ..domain = 'domain2'
        ..user = CanvasModelTestUtils.mockUser(name: 'user 2').toBuilder()),
    ];

    await tester.pumpWidget(TestApp(LoginLandingScreen(), highContrast: true));
    await ApiPrefs.saveLogins(logins);
    await tester.pumpAndSettle();

    expect(find.byKey(Key('previous-logins')), findsOneWidget);
    expect(find.text(logins[0].user.name), findsOneWidget);
    expect(find.text(logins[1].user.name), findsOneWidget);

    // Remove second login
    await tester.tap(find.bySemanticsLabel(AppLocalizations().delete).last);
    await tester.pumpAndSettle();

    expect(find.byKey(Key('previous-logins')), findsOneWidget);
    expect(find.text(logins[0].user.name), findsOneWidget);
    expect(find.text(logins[1].user.name), findsNothing);

    // Remove first login
    await tester.tap(find.bySemanticsLabel(AppLocalizations().delete));
    await tester.pumpAndSettle();

    expect(find.byKey(Key('previous-logins')), findsNothing);
    expect(find.text(logins[0].user.name), findsNothing);
    expect(find.text(logins[1].user.name), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Tapping a login sets the current login and loads splash screen', (tester) async {
    List<Login> logins = [
      Login((b) => b
        ..domain = 'domain1'
        ..user = CanvasModelTestUtils.mockUser(name: 'user 1').toBuilder()),
    ];

    var nav = _MockNav();
    setupTestLocator((locator) => locator.registerLazySingleton<QuickNav>(() => nav));

    await tester.pumpWidget(TestApp(LoginLandingScreen(), highContrast: true));
    await ApiPrefs.saveLogins(logins);
    await tester.pumpAndSettle();

    await tester.tap(find.text(logins[0].user.name));
    await tester.pumpAndSettle();

    expect(ApiPrefs.getCurrentLogin(), logins[0]);
    verify(nav.push(any, argThat(isA<SplashScreen>())));
  });
}

class _MockNav extends Mock implements QuickNav {}
