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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/screens/settings/legal_screen.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('displays all options', (tester) async {
    await tester.pumpWidget(TestApp(LegalScreen()));
    await tester.pump();

    expect(find.text(l10n.helpLegalLabel), findsOneWidget);

    expect(find.text(l10n.privacyPolicy), findsOneWidget);
    expect(find.text(l10n.termsOfUse), findsOneWidget);
    expect(find.text(l10n.canvasOnGithub), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping privacy policy launches url', (tester) async {
    var mockLauncher = MockUrlLauncher();
    setupTestLocator((locator) => locator.registerLazySingleton<UrlLauncher>(() => mockLauncher));

    await tester.pumpWidget(TestApp(LegalScreen()));
    await tester.pump();

    await tester.tap(find.text(l10n.privacyPolicy));
    await tester.pumpAndSettle();

    verify(mockLauncher.launch('https://www.instructure.com/policies/product-privacy-policy')).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping github launches url', (tester) async {
    var mockLauncher = MockUrlLauncher();
    setupTestLocator((locator) => locator.registerLazySingleton<UrlLauncher>(() => mockLauncher));

    await tester.pumpWidget(TestApp(LegalScreen()));
    await tester.pump();

    await tester.tap(find.text(l10n.canvasOnGithub));
    await tester.pumpAndSettle();

    verify(mockLauncher.launch('https://github.com/instructure/canvas-android')).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping terms of use navigates to Terms of Use screen', (tester) async {
    final nav = MockQuickNav();
    setupTestLocator((locator) => locator.registerSingleton<QuickNav>(nav));

    await tester.pumpWidget(TestApp(LegalScreen()));
    await tester.pump();

    await tester.tap(find.text(l10n.termsOfUse));
    await tester.pumpAndSettle();

    verify(nav.pushRoute(any, argThat(matches(PandaRouter.termsOfUse()))));
  });
}