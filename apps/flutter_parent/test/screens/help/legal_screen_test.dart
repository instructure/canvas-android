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
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/screens/help/legal_screen.dart';
import 'package:flutter_parent/screens/help/terms_of_use_screen.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:url_launcher_platform_interface/url_launcher_platform_interface.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  final l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('displays all options', (tester) async {
    await TestApp.showWidgetFromTap(tester, (context) => QuickNav().push(context, LegalScreen()));

    expect(find.text(l10n.helpLegalLabel), findsOneWidget);

    expect(find.text(l10n.privacyPolicy), findsOneWidget);
    expect(find.text(l10n.termsOfUse), findsOneWidget);
    expect(find.text(l10n.canvasOnGithub), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping privacy policy launches url', (tester) async {
    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await TestApp.showWidgetFromTap(tester, (context) => QuickNav().push(context, LegalScreen()));

    await tester.tap(find.text(l10n.privacyPolicy));
    await tester.pumpAndSettle();

    verify(
      mockLauncher.launch(
        'https://www.instructure.com/policies/privacy/',
        useSafariVC: anyNamed('useSafariVC'),
        useWebView: anyNamed('useWebView'),
        enableJavaScript: anyNamed('enableJavaScript'),
        enableDomStorage: anyNamed('enableDomStorage'),
        universalLinksOnly: anyNamed('universalLinksOnly'),
        headers: anyNamed('headers'),
      ),
    ).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping github launches url', (tester) async {
    var mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = mockLauncher;

    await TestApp.showWidgetFromTap(tester, (context) => QuickNav().push(context, LegalScreen()));

    await tester.tap(find.text(l10n.canvasOnGithub));
    await tester.pumpAndSettle();

    verify(
      mockLauncher.launch(
        'https://github.com/instructure/canvas-android',
        useSafariVC: anyNamed('useSafariVC'),
        useWebView: anyNamed('useWebView'),
        enableJavaScript: anyNamed('enableJavaScript'),
        enableDomStorage: anyNamed('enableDomStorage'),
        universalLinksOnly: anyNamed('universalLinksOnly'),
        headers: anyNamed('headers'),
      ),
    ).called(1);
  });

  testWidgetsWithAccessibilityChecks('tapping terms of use navigates to Terms of Use screen', (tester) async {
    final nav = _MockNav();
    setupTestLocator((locator) {
      locator.registerSingleton<QuickNav>(nav);
      locator.registerLazySingleton<AccountsApi>(() => _MockAccountsApi());
    });

    await TestApp.showWidgetFromTap(tester, (context) => QuickNav().push(context, LegalScreen()), highContrast: true);

    await tester.tap(find.text(l10n.termsOfUse));
    await tester.pump();
    await tester.pump();

    expect(find.byType(TermsOfUseScreen), findsOneWidget);
  });
}

class _MockUrlLauncherPlatform extends Mock with MockPlatformInterfaceMixin implements UrlLauncherPlatform {}

class _MockNav extends Mock implements QuickNav {}

class _MockAccountsApi extends Mock implements AccountsApi {}
