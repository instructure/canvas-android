/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/router/router_error_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/utils/logger.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:url_launcher_platform_interface/url_launcher_platform_interface.dart';

import '../utils/accessibility_utils.dart';
import '../utils/test_app.dart';

void main() {
  final String _domain = 'https://test.instructure.com';

  testWidgetsWithAccessibilityChecks('router error renders correctly with url', (tester) async {
    await tester.pumpWidget(TestApp(
      RouterErrorScreen(_domain),
      highContrast: true,
    ));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().routerErrorTitle), findsOneWidget);
    expect(find.text(AppLocalizations().routerErrorMessage), findsOneWidget);
    expect(find.text(AppLocalizations().openInBrowser), findsOneWidget);
    expect(find.text(AppLocalizations().switchUsers), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('router error screen open in browser calls launch with url', (tester) async {
    final _mockLauncher = _MockUrlLauncherPlatform();
    UrlLauncherPlatform.instance = _mockLauncher;

    when(_mockLauncher.launch(
      _domain,
      useSafariVC: anyNamed('useSafariVC'),
      useWebView: anyNamed('useWebView'),
      enableJavaScript: anyNamed('enableJavaScript'),
      enableDomStorage: anyNamed('enableDomStorage'),
      universalLinksOnly: anyNamed('universalLinksOnly'),
      headers: anyNamed('headers'),
    )).thenAnswer((_) => Future.value(true));

    await tester.pumpWidget(TestApp(
      RouterErrorScreen(_domain),
      highContrast: true,
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.text(AppLocalizations().openInBrowser));
    await tester.pump();

    verify(_mockLauncher.launch(
      _domain,
      useSafariVC: anyNamed('useSafariVC'),
      useWebView: anyNamed('useWebView'),
      enableJavaScript: anyNamed('enableJavaScript'),
      enableDomStorage: anyNamed('enableDomStorage'),
      universalLinksOnly: anyNamed('universalLinksOnly'),
      headers: anyNamed('headers'),
    )).called(1);
  });

  testWidgetsWithAccessibilityChecks('router error screen switch users', (tester) async {
    setupPlatformChannels();
    setupTestLocator((locator) {
      locator.registerLazySingleton<QuickNav>(() => QuickNav());
      locator.registerLazySingleton<Logger>(() => Logger());
    });

    await tester.pumpWidget(TestApp(
      RouterErrorScreen(_domain),
      highContrast: true,
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.text(AppLocalizations().switchUsers));
    await tester.pumpAndSettle();

    expect(find.byType(LoginLandingScreen), findsOneWidget);
    expect(ApiPrefs.isLoggedIn(), false);
  });
}

class _MockUrlLauncherPlatform extends Mock with MockPlatformInterfaceMixin implements UrlLauncherPlatform {}
