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

import 'package:built_collection/built_collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/screens/calendar/calendar_day_planner.dart';
import 'package:flutter_parent/screens/calendar/planner_fetcher.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';
import '../courses/course_summary_screen_test.dart';

void main() {
  var fetcher;

  final User student = CanvasModelTestUtils.mockUser(name: 'Panda');
  final String courseName = 'hodor';
  final course = Course((b) => b
    ..id = '123'
    ..name = courseName
    ..enrollments = BuiltList.of([
      Enrollment((enrollment) => enrollment
        ..userId = '123'
        ..courseId = '123'
        ..enrollmentState = 'active')
    ]).toBuilder());

  final MockCalendarEventsApi api = MockCalendarEventsApi();
  final MockCoursesInteractor interactor = MockCoursesInteractor();

  setupTestLocator((locator) {
    locator.registerLazySingleton<CalendarEventsApi>(() => api);
    locator.registerLazySingleton<CalendarFilterDb>(() => MockCalendarFilterDb());
    locator.registerFactory<CoursesInteractor>(() => interactor);
  });

  setUp(() {
    reset(api);
    fetcher = PlannerFetcher(userId: '', userDomain: '', observeeId: student.id);
    when(interactor.getCourses(isRefresh: anyNamed('isRefresh'))).thenAnswer((_) => Future.value(List.of([course])));
  });

  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows loading indicator when loading', (tester) async {
      await tester.pumpWidget(TestApp(
        ChangeNotifierProvider<PlannerFetcher>(
          create: (_) => fetcher,
          child: CalendarDayPlanner(DateTime.now()),
        ),
      ));
      await tester.pump();

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows calendar day planner list', (tester) async {
      when(api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeAssignment,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([_createScheduleItem(contextName: courseName)]));

      when(api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeCalendar,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      await tester.pumpWidget(TestApp(
        ChangeNotifierProvider<PlannerFetcher>(
          create: (_) => fetcher,
          child: CalendarDayPlanner(DateTime.now()),
        ),
      ));
      await tester.pump();
      await tester.pump();

      expect(find.byType(CalendarDayList), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows error panda view when we fail to retrieve events', (tester) async {
      Completer<List<ScheduleItem>> completer = Completer<List<ScheduleItem>>();
      when(api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeAssignment,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => completer.future);

      when(api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeCalendar,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      await tester.pumpWidget(TestApp(
        ChangeNotifierProvider<PlannerFetcher>(
          create: (_) => fetcher,
          child: CalendarDayPlanner(DateTime.now()),
        ),
      ));
      await tester.pump();
      await tester.pump();

      completer.completeError('Error');
      await tester.pumpAndSettle();

      expect(find.byType(ErrorPandaWidget), findsOneWidget);
      expect(find.text(l10n.errorLoadingEvents), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows empty panda view when we have no events', (tester) async {
      when(api.getUserCalendarItems(any, any, any, any,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      await tester.pumpWidget(TestApp(
        ChangeNotifierProvider<PlannerFetcher>(
          create: (_) => fetcher,
          child: CalendarDayPlanner(DateTime.now()),
        ),
      ));

      await tester.pumpAndSettle();

      expect(find.byType(EmptyPandaWidget), findsOneWidget);
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('pull to refresh refreshes list', (tester) async {
      when(api.getUserCalendarItems(any, any, any, any,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) async => []);

      await tester.pumpWidget(TestApp(
        ChangeNotifierProvider<PlannerFetcher>(
          create: (_) => fetcher,
          child: CalendarDayPlanner(DateTime.now()),
        ),
      ));
      await tester.pumpAndSettle();

      // Empty state
      expect(find.byType(CalendarDayList), findsNothing);
      expect(find.byType(EmptyPandaWidget), findsOneWidget);

      when(api.getUserCalendarItems(any, any, any, ScheduleItem.apiTypeAssignment,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) async => [_createScheduleItem(contextName: courseName)]);

      // PTR
      await tester.drag(find.byType(RefreshIndicator), Offset(0, 300));
      await tester.pumpAndSettle();

      // Should now show list
      expect(find.byType(EmptyPandaWidget), findsNothing);
      expect(find.byType(CalendarDayList), findsOneWidget);
    });
  });
}

ScheduleItem _createScheduleItem({String? contextName, String type = ScheduleItem.apiTypeAssignment}) =>
    ScheduleItem((b) => b
      ..id = ''
      ..title = ''
      ..contextCode = 'course_123'
      ..type = type
      ..htmlUrl = ''
      ..startAt = DateTime.now());
