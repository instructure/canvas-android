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
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_test/flutter_test.dart';

import '../accessibility_utils.dart';
import '../test_app.dart';

void main() {
  testWidgetsWithAccessibilityChecks('shows an indicator badge that is the student color', (tester) async {
    setupTestLocator((_) {});

    await tester.pumpWidget(TestApp(IndicatorBadge()));
    await tester.pumpAndSettle();

    var decoration = (tester.widgetList(find.byType(Container)).last as Container).decoration as BoxDecoration;
    expect(decoration.color, StudentColorSet.all[0].light);

    var state = tester.state(find.byType(MaterialApp));
    ParentTheme.of(state.context)?.setSelectedStudent('1');
    await tester.pumpAndSettle();

    decoration = (tester.widgetList(find.byType(Container)).last as Container).decoration as BoxDecoration;
    expect(decoration.color, StudentColorSet.all[1].light);
  });

  group('NumberBadge', () {
    testWidgetsWithAccessibilityChecks('shows a number', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(options: BadgeOptions(count: 1))));
      await tester.pumpAndSettle();

      expect(find.text('1'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows a number and a plus if less than the max count', (tester) async {
      await tester.pumpWidget(TestApp(
        NumberBadge(options: BadgeOptions(count: 123, maxCount: 99)),
      ));
      await tester.pumpAndSettle();

      expect(find.text('99+'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows a large number when the max count is null', (tester) async {
      await tester.pumpWidget(TestApp(
        NumberBadge(options: BadgeOptions(count: 987654321, maxCount: null)),
      ));
      await tester.pumpAndSettle();

      expect(find.text('987654321'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows no text when count is zero', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(options: BadgeOptions(count: 0))));
      await tester.pumpAndSettle();

      expect(find.text('0'), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('shows no text when count is null', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(options: BadgeOptions(count: null))));
      await tester.pumpAndSettle();

      expect(find.text('null'), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('updates text when listenable updates', (tester) async {
      final listenable = ValueNotifier(1);

      await tester.pumpWidget(TestApp(NumberBadge(listenable: listenable)));
      await tester.pumpAndSettle();

      expect(find.text('1'), findsOneWidget);

      listenable.value = 2;
      await tester.pump();
      expect(find.text('2'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('does not display border when disabled', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(options: BadgeOptions(count: 1, includeBorder: false))));
      await tester.pump();

      final border = tester.widget(find.byKey(NumberBadge.borderKey)) as Container;

      // For ease, border is always getting shown, but with zero padding when 'hidden'
      expect(border.padding, EdgeInsets.zero);
      expect(find.byKey(NumberBadge.backgroundKey), findsOneWidget);
      expect(find.text('1'), findsOneWidget);
    });
  });

  group('Colors', () {
    testWidgetsWithAccessibilityChecks('has a white border with a blue background', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(options: BadgeOptions(count: 1, includeBorder: true))));
      await tester.pump();

      final border = tester.widget(find.byKey(NumberBadge.borderKey)) as Container;
      final background = tester.widget(find.byKey(NumberBadge.backgroundKey)) as Container;
      final text = tester.widget(find.text('1')) as Text;

      expect((border.decoration as BoxDecoration).color, Colors.white);
      expect((background.decoration as BoxDecoration).color, StudentColorSet.electric.light);
      expect(text.style?.color, Colors.white);
    });

    testWidgetsWithAccessibilityChecks('has a white border with a high contrast blue background', (tester) async {
      await tester.pumpWidget(TestApp(
        NumberBadge(options: BadgeOptions(count: 1, includeBorder: true)),
        highContrast: true,
      ));
      await tester.pump();

      final border = tester.widget(find.byKey(NumberBadge.borderKey)) as Container;
      final background = tester.widget(find.byKey(NumberBadge.backgroundKey)) as Container;
      final text = tester.widget(find.text('1')) as Text;

      expect((border.decoration as BoxDecoration).color, Colors.white);
      expect((background.decoration as BoxDecoration).color, StudentColorSet.electric.lightHC);
      expect(text.style?.color, Colors.white);
    });

    testWidgetsWithAccessibilityChecks('has a black border with a blue background in dark mode', (tester) async {
      await tester.pumpWidget(TestApp(
        NumberBadge(options: BadgeOptions(count: 1, includeBorder: true)),
        darkMode: true,
      ));
      await tester.pump();

      final border = tester.widget(find.byKey(NumberBadge.borderKey)) as Container;
      final background = tester.widget(find.byKey(NumberBadge.backgroundKey)) as Container;
      final text = tester.widget(find.text('1')) as Text;

      expect((border.decoration as BoxDecoration).color, Colors.black);
      expect((background.decoration as BoxDecoration).color, StudentColorSet.electric.dark);
      expect(text.style?.color, Colors.black);
    });

    testWidgetsWithAccessibilityChecks('has a black border with a high contrast blue background in dark mode',
        (tester) async {
      await tester.pumpWidget(TestApp(
        NumberBadge(options: BadgeOptions(count: 1, includeBorder: true)),
        darkMode: true,
        highContrast: true,
      ));
      await tester.pump();

      final border = tester.widget(find.byKey(NumberBadge.borderKey)) as Container;
      final background = tester.widget(find.byKey(NumberBadge.backgroundKey)) as Container;
      final text = tester.widget(find.text('1')) as Text;

      expect((border.decoration as BoxDecoration).color, Colors.black);
      expect((background.decoration as BoxDecoration).color, StudentColorSet.electric.darkHC);
      expect(text.style?.color, Colors.black);
    });

    // HAMBURGER TESTS
    testWidgetsWithAccessibilityChecks('hamburger has a blue border with a white background', (tester) async {
      await tester.pumpWidget(TestApp(
        NumberBadge(options: BadgeOptions(count: 1, includeBorder: true, onPrimarySurface: true)),
      ));
      await tester.pump();

      final border = tester.widget(find.byKey(NumberBadge.borderKey)) as Container;
      final background = tester.widget(find.byKey(NumberBadge.backgroundKey)) as Container;
      final text = tester.widget(find.text('1')) as Text;

      expect((border.decoration as BoxDecoration).color, StudentColorSet.electric.light);
      expect((background.decoration as BoxDecoration).color, Colors.white);
      expect(text.style?.color, StudentColorSet.electric.light);
    });

    testWidgetsWithAccessibilityChecks('hamburger has a high contrast blue border with a white background',
        (tester) async {
      await tester.pumpWidget(TestApp(
        NumberBadge(options: BadgeOptions(count: 1, includeBorder: true, onPrimarySurface: true)),
        highContrast: true,
      ));
      await tester.pump();

      final border = tester.widget(find.byKey(NumberBadge.borderKey)) as Container;
      final background = tester.widget(find.byKey(NumberBadge.backgroundKey)) as Container;
      final text = tester.widget(find.text('1')) as Text;

      expect((border.decoration as BoxDecoration).color, StudentColorSet.electric.lightHC);
      expect((background.decoration as BoxDecoration).color, Colors.white);
      expect(text.style?.color, StudentColorSet.electric.lightHC);
    });

    testWidgetsWithAccessibilityChecks('hamburger has a black border with a tiara background in dark mode',
        (tester) async {
      await tester.pumpWidget(TestApp(
        NumberBadge(options: BadgeOptions(count: 1, includeBorder: true, onPrimarySurface: true)),
        darkMode: true,
      ));
      await tester.pump();

      final border = tester.widget(find.byKey(NumberBadge.borderKey)) as Container;
      final background = tester.widget(find.byKey(NumberBadge.backgroundKey)) as Container;
      final text = tester.widget(find.text('1')) as Text;

      expect((border.decoration as BoxDecoration).color, Colors.black);
      expect((background.decoration as BoxDecoration).color, ParentColors.tiara);
      expect(text.style?.color, Colors.black);
    });

    testWidgetsWithAccessibilityChecks('hamburger has a black border with a tiara background in dark mode and HC',
        (tester) async {
      await tester.pumpWidget(TestApp(
        NumberBadge(options: BadgeOptions(count: 1, includeBorder: true, onPrimarySurface: true)),
        darkMode: true,
      ));
      await tester.pump();

      final border = tester.widget(find.byKey(NumberBadge.borderKey)) as Container;
      final background = tester.widget(find.byKey(NumberBadge.backgroundKey)) as Container;
      final text = tester.widget(find.text('1')) as Text;

      expect((border.decoration as BoxDecoration).color, Colors.black);
      expect((background.decoration as BoxDecoration).color, ParentColors.tiara);
      expect(text.style?.color, Colors.black);
    });
  });

  group('WidgetBadge', () {
    testWidgetsWithAccessibilityChecks('shows the widget passed in', (tester) async {
      final child = Icon(Icons.error);
      await tester.pumpWidget(TestApp(WidgetBadge(child)));
      await tester.pumpAndSettle();

      expect(find.byType(Icon), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows an indicator badge with no count or listenable', (tester) async {
      final child = Icon(Icons.error);
      await tester.pumpWidget(TestApp(WidgetBadge(child)));
      await tester.pumpAndSettle();

      expect(find.byType(IndicatorBadge), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows a number badge with no count but has a listenable', (tester) async {
      final child = Icon(Icons.error);
      final listenable = ValueNotifier(1);

      await tester.pumpWidget(TestApp(WidgetBadge(child, countListenable: listenable)));
      await tester.pumpAndSettle();

      expect(find.byType(NumberBadge), findsOneWidget);
      expect(find.text('1'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows a number badge with no listenable but has a count', (tester) async {
      final child = Icon(Icons.error);

      await tester.pumpWidget(TestApp(WidgetBadge(child, options: BadgeOptions(count: 1))));
      await tester.pumpAndSettle();

      expect(find.byType(NumberBadge), findsOneWidget);
      expect(find.text('1'), findsOneWidget);
    });
  });
}
