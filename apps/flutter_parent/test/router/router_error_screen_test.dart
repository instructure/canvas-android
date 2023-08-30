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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/network/utils/dio_config.dart';
import 'package:flutter_parent/router/router_error_screen.dart';
import 'package:flutter_parent/screens/login_landing_screen.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/features_utils.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../screens/courses/course_summary_screen_test.dart';
import '../utils/accessibility_utils.dart';
import '../utils/platform_config.dart';
import '../utils/test_app.dart';
import '../utils/test_helpers/mock_helpers.dart';
import '../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final String _domain = 'https://test.instructure.com';

  setUp(() async {
    final mockRemoteConfig = setupMockRemoteConfig(valueSettings: {'qr_login_enabled_parent': 'true'});
    await setupPlatformChannels(config: PlatformConfig(initRemoteConfig: mockRemoteConfig));
    ApiPrefs.init();
  });

  tearDown(() {
    RemoteConfigUtils.clean();
  });

  testWidgetsWithAccessibilityChecks('router error renders correctly with url', (tester) async {
    await tester.pumpWidget(TestApp(
      RouterErrorScreen(_domain),
    ));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().routerErrorTitle), findsOneWidget);
    expect(find.text(AppLocalizations().routerErrorMessage), findsOneWidget);
    expect(find.text(AppLocalizations().openInBrowser), findsOneWidget);
    expect(find.text(AppLocalizations().switchUsers), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('router error screen open in browser calls launch with url', (tester) async {
    final _mockLauncher = MockUrlLauncher();
    setupTestLocator((locator) => locator.registerLazySingleton<UrlLauncher>(() => _mockLauncher));

    when(_mockLauncher.launch(
      _domain,
    )).thenAnswer((_) => Future.value(true));

    await tester.pumpWidget(TestApp(
      RouterErrorScreen(_domain),
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.text(AppLocalizations().openInBrowser));
    await tester.pump();

    verify(_mockLauncher.launch(_domain)).called(1);
  });

  testWidgetsWithAccessibilityChecks('router error screen switch users', (tester) async {
      setupTestLocator((locator) {
        locator.registerLazySingleton<QuickNav>(() => QuickNav());
        locator.registerLazySingleton<CalendarFilterDb>(() => MockCalendarFilterDb());
      });

      await tester.pumpWidget(TestApp(
        RouterErrorScreen(_domain),
      ));
      await tester.pumpAndSettle();
      await tester.tap(find.text(l10n.switchUsers));
      await tester.pumpAndSettle();

      expect(find.byType(LoginLandingScreen), findsOneWidget);
      expect(ApiPrefs.isLoggedIn(), false);

  });
}
