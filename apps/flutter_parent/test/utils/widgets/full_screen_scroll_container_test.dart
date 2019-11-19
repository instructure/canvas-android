/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter/material.dart';
import 'package:flutter_parent/utils/widgets/full_screen_scroll_container.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../accessibility_utils.dart';

void main() {
  group('Single child', () {
    testWidgetsWithAccessibilityChecks('is visible', (tester) async {
      final children = [Text('a')];

      // Pump the widget
      await tester.pumpWidget(_refreshingWidget(children));
      await tester.pumpAndSettle();

      // Should show the text
      expect(find.text('a'), findsOneWidget);

      // Should be at the center of the screen
      final positionA = tester.getCenter(find.text('a'));
      final positionApp = tester.getCenter(find.byType(MaterialApp));
      expect(positionA.dx, positionApp.dx);
      expect(positionA.dy, positionApp.dy);
    });

    testWidgetsWithAccessibilityChecks('can swipe to refresh', (tester) async {
      final children = [Text('a')];
      final refresher = _Refresher();

      // Pump the widget
      await tester.pumpWidget(_refreshingWidget(children, refresher: refresher));
      await tester.pumpAndSettle();

      // Should have the refresh indicator
      final matchedWidget = find.byType(RefreshIndicator);
      expect(matchedWidget, findsOneWidget);

      // Try to refresh
      await tester.drag(matchedWidget, const Offset(0, 200));
      await tester.pump();

      // Verify we had our refresh called
      verify(refresher.refresh()).called(1);
    });
  });

  group('Multiple children', () {
    testWidgetsWithAccessibilityChecks('all are visible', (tester) async {
      final children = [Text('a'), Text('b'), Text('c')];

      // Pump the widget
      await tester.pumpWidget(_refreshingWidget(children));
      await tester.pumpAndSettle();

      // The widgets should all be visible
      expect(find.text('a'), findsOneWidget);
      expect(find.text('b'), findsOneWidget);
      expect(find.text('c'), findsOneWidget);

      // The middle widget should be at the center of the screen
      final positionA = tester.getCenter(find.text('b'));
      final positionApp = tester.getCenter(find.byType(MaterialApp));
      expect(positionA.dx, positionApp.dx);
      expect(positionA.dy, positionApp.dy);
    });

    testWidgetsWithAccessibilityChecks('can swipe to refresh', (tester) async {
      final children = [Text('a'), Text('b'), Text('c')];
      final refresher = _Refresher();

      // Pump the widget
      await tester.pumpWidget(_refreshingWidget(children, refresher: refresher));
      await tester.pumpAndSettle();

      // Should have the refresh indicator
      final matchedWidget = find.byType(RefreshIndicator);
      expect(matchedWidget, findsOneWidget);

      // Try to refresh
      await tester.drag(matchedWidget, const Offset(0, 200));
      await tester.pump();

      // Verify we had our refresh called
      verify(refresher.refresh()).called(1);
    });
  });
}

class _Refresher extends Mock {
  void refresh();
}

Widget _refreshingWidget(List<Widget> children, {_Refresher refresher}) {
  return MaterialApp(
    home: Scaffold(
      body: RefreshIndicator(
        child: FullScreenScrollContainer(children: children),
        onRefresh: () {
          refresher?.refresh();
          return Future.value();
        },
      ),
    ),
  );
}
