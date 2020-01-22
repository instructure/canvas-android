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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/settings/settings_interactor.dart';
import 'package:flutter_parent/screens/settings/settings_screen.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

void main() {
  SettingsInteractor interactor;
  AppLocalizations l10n = AppLocalizations();

  themeViewerButton() => find.byKey(Key('theme-viewer'));
  darkModeButton() => find.byKey(Key('dark-mode-button'));
  lightModeButton() => find.byKey(Key('light-mode-button'));
  hcToggle() => find.text(l10n.highContrastLabel);

  setUpAll(() {
    interactor = _MockInteractor();
    when(interactor.isDebugMode()).thenReturn(true);
    when(interactor.toggleDarkMode(any, any)).thenAnswer((invocation) {
      ParentTheme.of(invocation.positionalArguments[0]).toggleDarkMode();
    });
    when(interactor.toggleHCMode(any, any)).thenAnswer((invocation) {
      ParentTheme.of(invocation.positionalArguments[0]).toggleHC();
    });
    setupTestLocator((locator) {
      locator.registerFactory<SettingsInteractor>(() => interactor);
    });
  });

  testWidgetsWithAccessibilityChecks('Displays theme viewer button in debug mode', (tester) async {
    await tester.pumpWidget(TestApp(SettingsScreen(), highContrast: true));
    await tester.pumpAndSettle();
    expect(themeViewerButton(), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Hides theme viewer button in non-debug mode', (tester) async {
    when(interactor.isDebugMode()).thenReturn(false);
    await tester.pumpWidget(TestApp(SettingsScreen(), highContrast: true));
    await tester.pumpAndSettle();
    expect(themeViewerButton(), findsNothing);
  });

  testWidgetsWithAccessibilityChecks(
    '(In light mode) Dark mode button is enabled, light mode button is disabled',
    (tester) async {
      await tester.pumpWidget(TestApp(SettingsScreen(), highContrast: true));
      await tester.pumpAndSettle();

      InkWell lightMode = tester.widget<InkWell>(lightModeButton());
      InkWell darkMode = tester.widget<InkWell>(darkModeButton());

      expect(darkMode.onTap, isNotNull);
      expect(lightMode.onTap, isNull);
    },
  );

  testWidgetsWithAccessibilityChecks(
    '(In dark mode) Dark mode button is disabled, light mode button is enabled',
    (tester) async {
      await tester.pumpWidget(TestApp(SettingsScreen(), highContrast: true, darkMode: true));
      await tester.pumpAndSettle();

      InkWell lightMode = tester.widget<InkWell>(lightModeButton());
      InkWell darkMode = tester.widget<InkWell>(darkModeButton());

      expect(darkMode.onTap, isNull);
      expect(lightMode.onTap, isNotNull);
    },
  );

  testWidgetsWithAccessibilityChecks('Switches to dark mode', (tester) async {
    await tester.pumpWidget(TestApp(SettingsScreen(), highContrast: true));
    await tester.pumpAndSettle();

    var state = tester.state(find.byType(SettingsScreen));
    expect(ParentTheme.of(state.context).isDarkMode, isFalse);

    await tester.tap(darkModeButton());
    await tester.pumpAndSettle();

    state = tester.state(find.byType(SettingsScreen));
    expect(ParentTheme.of(state.context).isDarkMode, isTrue);
  });

  testWidgetsWithAccessibilityChecks('Switches to light mode', (tester) async {
    await tester.pumpWidget(TestApp(SettingsScreen(), highContrast: true, darkMode: true));
    await tester.pumpAndSettle();

    var state = tester.state(find.byType(SettingsScreen));
    expect(ParentTheme.of(state.context).isDarkMode, isTrue);

    await tester.tap(lightModeButton());
    await tester.pumpAndSettle();

    state = tester.state(find.byType(SettingsScreen));
    expect(ParentTheme.of(state.context).isDarkMode, isFalse);
  });

  testWidgetsWithAccessibilityChecks('Enables high contrast mode', (tester) async {
    await tester.pumpWidget(TestApp(SettingsScreen()));
    await tester.pumpAndSettle();

    await runAccessibilityTests(tester);
    appearanceText(WidgetTester tester) => tester.widget<Text>(find.text(l10n.appearance.toUpperCase()));

    var state = tester.state(find.byType(SettingsScreen));
    expect(ParentTheme.of(state.context).isHC, isFalse);
    expect(appearanceText(tester).style.color, ParentColors.ash);

    await tester.tap(hcToggle());
    await tester.pumpAndSettle();

    state = tester.state(find.byType(SettingsScreen));
    expect(ParentTheme.of(state.context).isHC, isTrue);
    expect(appearanceText(tester).style.color, ParentColors.licorice);
  });
}

class _MockInteractor extends Mock implements SettingsInteractor {}
