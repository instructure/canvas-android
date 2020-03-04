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

import 'dart:async';

import 'package:flutter_parent/models/plannable.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/network/api/planner_api.dart';
import 'package:flutter_parent/screens/calendar/calendar_day_planner.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/test_app.dart';

void main() {
  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows loading indicator when loading', (tester) async {
      var api = MockPlannerApi();
      var student = CanvasModelTestUtils.mockUser(name: 'Panda');
      setupTestLocator((locator) => locator.registerLazySingleton<PlannerApi>(() => api));

      await tester.pumpWidget(TestApp(
        CalendarDayPlanner(student, DateTime.now()),
        highContrast: true,
      ));
      await tester.pump();

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows calendar day planner list', (tester) async {
      var api = MockPlannerApi();
      when(api.getUserPlannerItems(any, any, any, forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([_createPlannerItem()]));
      var student = CanvasModelTestUtils.mockUser(name: 'Panda');
      setupTestLocator((locator) => locator.registerLazySingleton<PlannerApi>(() => api));

      await tester.pumpWidget(TestApp(
        CalendarDayPlanner(student, DateTime.now()),
        highContrast: true,
      ));
      await tester.pump();
      await tester.pump();

      expect(find.byType(CalendarDayList), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows error panda view when we fail to retrieve events', (tester) async {
      var student = CanvasModelTestUtils.mockUser(name: 'Panda');
      var dateTime = DateTime.now();

      var api = MockPlannerApi();
      Completer completer = Completer<List<PlannerItem>>();
      when(api.getUserPlannerItems(any, any, any, forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => completer.future);

      setupTestLocator((locator) => locator.registerLazySingleton<PlannerApi>(() => api));

      await tester.pumpWidget(TestApp(
        CalendarDayPlanner(student, dateTime),
        highContrast: true,
      ));
      await tester.pump();
      await tester.pump();

      verify(api.getUserPlannerItems(student.id, dateTime, dateTime.add(Duration(days: 1)), forceRefresh: true))
          .called(1);

      completer.completeError('Error');
      await tester.pumpAndSettle();

      expect(find.byType(ErrorPandaWidget), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows empty panda view when we have no events', (tester) async {
      var student = CanvasModelTestUtils.mockUser(name: 'Panda');
      var dateTime = DateTime.now();

      var api = MockPlannerApi();
      Completer completer = Completer<List<PlannerItem>>();
      when(api.getUserPlannerItems(any, any, any, forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => completer.future);

      setupTestLocator((locator) => locator.registerLazySingleton<PlannerApi>(() => api));

      await tester.pumpWidget(TestApp(
        CalendarDayPlanner(student, dateTime),
        highContrast: true,
      ));
      await tester.pump();
      await tester.pump();

      verify(api.getUserPlannerItems(student.id, dateTime, dateTime.add(Duration(days: 1)), forceRefresh: true))
          .called(1);

      completer.complete(<PlannerItem>[]);
      await tester.pumpAndSettle();

      expect(find.byType(EmptyPandaWidget), findsOneWidget);
    });
  });

  group('Interaction', () {
    // TODO
//    testWidgetsWithAccessibilityChecks('pull to refresh refreshes list', (tester) async {});
  });
}

Plannable _createPlannable() => Plannable((b) => b
  ..id = ''
  ..title = '');

PlannerItem _createPlannerItem() => PlannerItem((b) => b
  ..courseId = ''
  ..plannable = _createPlannable().toBuilder()
  ..contextType = ''
  ..contextName = ''
  ..plannableType = 'assignment');

class MockPlannerApi extends Mock implements PlannerApi {}
