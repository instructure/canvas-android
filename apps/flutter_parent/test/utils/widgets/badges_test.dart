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

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/utils/common_widgets/badges.dart';
import 'package:flutter_parent/utils/design/parent_theme.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_test/flutter_test.dart';

import '../accessibility_utils.dart';
import '../test_app.dart';

void main() {
  testWidgetsWithAccessibilityChecks('shows an indicator badge that is the student color', (tester) async {
    await tester.pumpWidget(TestApp(IndicatorBadge()));
    await tester.pumpAndSettle();

    var decoration = (tester.widgetList(find.byType(Container)).last as Container).decoration as BoxDecoration;
    expect(decoration.color, StudentColorSet.all[0].light);

    var state = tester.state(find.byType(MaterialApp));
    ParentTheme.of(state.context).studentIndex = 1;
    await tester.pumpAndSettle();

    decoration = (tester.widgetList(find.byType(Container)).last as Container).decoration as BoxDecoration;
    expect(decoration.color, StudentColorSet.all[1].light);
  });

  group('NumberBadge', () {
    testWidgetsWithAccessibilityChecks('shows a number', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(count: 1), highContrast: true));
      await tester.pumpAndSettle();

      expect(find.text('1'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows a number and a plus if less than the max count', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(count: 123, maxCount: 99), highContrast: true));
      await tester.pumpAndSettle();

      expect(find.text('99+'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows a large number when the max count is null', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(count: 987654321, maxCount: null), highContrast: true));
      await tester.pumpAndSettle();

      expect(find.text('987654321'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows no text when count is zero', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(count: 0), highContrast: true));
      await tester.pumpAndSettle();

      expect(find.text('0'), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('shows no text when count is null', (tester) async {
      await tester.pumpWidget(TestApp(NumberBadge(count: null), highContrast: true));
      await tester.pumpAndSettle();

      expect(find.text('null'), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('updates text when listenable updates', (tester) async {
      final listenable = ValueNotifier(1);

      await tester.pumpWidget(TestApp(NumberBadge(listenable: listenable), highContrast: true));
      await tester.pumpAndSettle();

      expect(find.text('1'), findsOneWidget);

      listenable.value = 2;
      await tester.pump();
      expect(find.text('2'), findsOneWidget);
    });
  });

  group('WidgetBadge', () {
    test('throws if null is passed in for the child icon', () {
      expect(() => WidgetBadge(null), throwsAssertionError);
    });

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

      await tester.pumpWidget(TestApp(WidgetBadge(child, countListenable: listenable), highContrast: true));
      await tester.pumpAndSettle();

      expect(find.byType(NumberBadge), findsOneWidget);
      expect(find.text('1'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows a number badge with no listenable but has a count', (tester) async {
      final child = Icon(Icons.error);

      await tester.pumpWidget(TestApp(WidgetBadge(child, count: 1), highContrast: true));
      await tester.pumpAndSettle();

      expect(find.byType(NumberBadge), findsOneWidget);
      expect(find.text('1'), findsOneWidget);
    });
  });
}
