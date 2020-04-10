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
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/screens/settings/settings_interactor.dart';
import 'package:flutter_parent/screens/theme_viewer_screen.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  test('Returns true for debug mode', () {
    // This test is just for coverage since there's no way to change the debug flag at test time
    expect(SettingsInteractor().isDebugMode(), isTrue);
  });

  test('routeToThemeViewer call through to navigator', () {
    var nav = _MockNav();
    setupTestLocator((locator) {
      locator.registerLazySingleton<QuickNav>(() => nav);
    });

    var context = _MockContext();
    SettingsInteractor().routeToThemeViewer(context);

    var screen = verify(nav.push(context, captureAny)).captured[0];
    expect(screen, isA<ThemeViewerScreen>());
  });

  testNonWidgetsWithContext('toggle dark mode sets dark mode to true', (tester) async {
    await setupPlatformChannels();
    final analytics = _MockAnalytics();

    setupTestLocator((locator) => locator.registerLazySingleton<Analytics>(() => analytics));

    await tester.pumpWidget(TestApp(Container()));
    await tester.pumpAndSettle();

    final context = tester.state(find.byType(MaterialApp)).context;
    expect(ParentTheme.of(context).isDarkMode, false);

    SettingsInteractor().toggleDarkMode(context, null);
    expect(ParentTheme.of(context).isDarkMode, true);

    SettingsInteractor().toggleDarkMode(context, null);
    expect(ParentTheme.of(context).isDarkMode, false);

    verify(analytics.logEvent(AnalyticsEventConstants.DARK_MODE_OFF)).called(1);
    verify(analytics.logEvent(AnalyticsEventConstants.DARK_MODE_ON)).called(1);
  });

  testNonWidgetsWithContext('toggle hc mode sets hc mode to true', (tester) async {
    await setupPlatformChannels();
    final analytics = _MockAnalytics();

    setupTestLocator((locator) => locator.registerLazySingleton<Analytics>(() => analytics));

    await tester.pumpWidget(TestApp(Container()));
    await tester.pumpAndSettle();

    final context = tester.state(find.byType(MaterialApp)).context;
    expect(ParentTheme.of(context).isHC, false);

    SettingsInteractor().toggleHCMode(context, null);
    expect(ParentTheme.of(context).isHC, true);

    SettingsInteractor().toggleHCMode(context, null);
    expect(ParentTheme.of(context).isHC, false);

    verify(analytics.logEvent(AnalyticsEventConstants.HC_MODE_OFF)).called(1);
    verify(analytics.logEvent(AnalyticsEventConstants.HC_MODE_ON)).called(1);
  });
}

class _MockNav extends Mock implements QuickNav {}

class _MockContext extends Mock implements BuildContext {}

class _MockAnalytics extends Mock implements Analytics {}
