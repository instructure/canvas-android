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
///
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/screens/theme_viewer_screen.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_test/flutter_test.dart';

import '../utils/test_app.dart';

void main() {
  Finder darkToggle() => find.text('Dark Mode');
  Finder hcToggle() => find.text('High Contrast Mode');
  Finder title() => find.text('Theme configuration');
  Finder subtitle() => find.text('Play around with some values');
  Finder studentColor() => find.byKey(ThemeViewerScreen.studentColorKey);

  testWidgets('Changes text color for dark and high-contrast modes', (tester) async {
    await tester.pumpWidget(TestApp(ThemeViewerScreen()));
    await tester.pumpAndSettle();

    // Open the drawer
    ThemeViewerScreen.scaffoldKey.currentState?.openDrawer();
    await tester.pumpAndSettle();

    // Light mode, normal contrast. Title should be dark, subtitle should be gray.
    expect(tester.widget<Text>(title()).style!.color, ParentColors.licorice);
    expect(tester.widget<Text>(subtitle()).style!.color, ParentColors.oxford);

    // Enable dark mode
    await tester.tap(darkToggle());
    await tester.pumpAndSettle();

    // Dark mode, normal contrast. Title should be light, subtitle should be gray.
    expect(tester.widget<Text>(title()).style!.color, ParentColors.tiara);
    expect(tester.widget<Text>(subtitle()).style!.color, ParentColors.ash);

    // Enable High-Contrast mode
    await tester.tap(hcToggle());
    await tester.pumpAndSettle();

    // Dark mode, high contrast. Both title and subtitle should be light
    expect(tester.widget<Text>(title()).style!.color, ParentColors.tiara);
    expect(tester.widget<Text>(subtitle()).style!.color, ParentColors.tiara);

    // Disable dark mode
    await tester.tap(darkToggle());
    await tester.pumpAndSettle();

    // Light mode, high contrast. Both title and subtitle should be dark
    expect(tester.widget<Text>(title()).style!.color, ParentColors.licorice);
    expect(tester.widget<Text>(subtitle()).style!.color, ParentColors.licorice);
  });

  testWidgets('Set and returns correct values for dark and high-contrast modes', (tester) async {
    await tester.pumpWidget(TestApp(ThemeViewerScreen()));
    await tester.pumpAndSettle();

    var state = ParentTheme.of(ThemeViewerScreen.scaffoldKey.currentContext!);

    state!.isDarkMode = false;
    state.isHC = false;
    expect(state.isLightNormal, isTrue);
    expect(state.isLightHC, isFalse);
    expect(state.isDarkNormal, isFalse);
    expect(state.isDarkHC, isFalse);

    state.isDarkMode = true;
    state.isHC = false;
    expect(state.isLightNormal, isFalse);
    expect(state.isLightHC, isFalse);
    expect(state.isDarkNormal, isTrue);
    expect(state.isDarkHC, isFalse);

    state.isDarkMode = false;
    state.isHC = true;
    expect(state.isLightNormal, isFalse);
    expect(state.isLightHC, isTrue);
    expect(state.isDarkNormal, isFalse);
    expect(state.isDarkHC, isFalse);

    state.isDarkMode = true;
    state.isHC = true;
    expect(state.isLightNormal, isFalse);
    expect(state.isLightHC, isFalse);
    expect(state.isDarkNormal, isFalse);
    expect(state.isDarkHC, isTrue);
  });

  testWidgets('Uses correct student color variants', (tester) async {
    setupTestLocator((_) {});
    await tester.pumpWidget(TestApp(ThemeViewerScreen()));
    await tester.pumpAndSettle();

    // Open the drawer
    ThemeViewerScreen.scaffoldKey.currentState?.openDrawer();
    await tester.pumpAndSettle();

    // Switch student color to 'raspberry'
    await tester.tap(find.text('Student Color 1'));
    await tester.pump();
    await tester.tap(find.text('Student Color 4').last);
    await tester.pumpAndSettle();

    StudentColorSet expected = StudentColorSet.raspberry;
    Color actualColor() => tester.widget<Container>(studentColor()).color!;

    // Light mode, normal contrast.
    expect(actualColor(), expected.light);

    // Enable dark mode
    await tester.tap(darkToggle());
    await tester.pumpAndSettle();

    // Dark mode, normal contrast.
    expect(actualColor(), expected.dark);

    // Enable High-Contrast mode
    await tester.tap(hcToggle());
    await tester.pumpAndSettle();

    // Dark mode, high contrast.
    expect(actualColor(), expected.darkHC);

    // Disable dark mode
    await tester.tap(darkToggle());
    await tester.pumpAndSettle();

    // Light mode, high contrast
    expect(actualColor(), expected.lightHC);
  });
}
