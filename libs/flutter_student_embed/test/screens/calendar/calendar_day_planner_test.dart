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

import 'package:flutter/material.dart';
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/plannable.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_day_planner.dart';
import 'package:flutter_student_embed/screens/calendar/planner_fetcher.dart';
import 'package:flutter_student_embed/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_student_embed/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_student_embed/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_student_embed/utils/db/calendar_filter_db.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';

import '../../testutils/accessibility_utils.dart';
import '../../testutils/canvas_model_utils.dart';
import '../../testutils/mock_helpers.dart';
import '../../testutils/test_app.dart';

void main() {
  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows loading indicator when loading', (tester) async {
      var api = MockPlannerApi();
      var student = CanvasModelTestUtils.mockUser(name: 'Panda');
      await setupTestLocator((locator) {
        locator.registerLazySingleton<PlannerApi>(() => api);
        locator.registerLazySingleton<CalendarFilterDb>(() => MockCalendarFilterDb());
      });

      PlannerFetcher fetcher = PlannerFetcher(userId: student.id, userDomain: '');

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
      var api = MockPlannerApi();
      when(api.getUserPlannerItems(any, any, any, forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([_createPlannerItem(contextName: 'blank')]));
      var student = CanvasModelTestUtils.mockUser(name: 'Panda');
      await setupTestLocator((locator) {
        locator.registerLazySingleton<PlannerApi>(() => api);
        locator.registerLazySingleton<CalendarFilterDb>(() => MockCalendarFilterDb());
      });

      PlannerFetcher fetcher = PlannerFetcher(userId: student.id, userDomain: '');

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
      var student = CanvasModelTestUtils.mockUser(name: 'Panda');
      var dateTime = DateTime.now();

      var api = MockPlannerApi();
      Completer completer = Completer<List<PlannerItem>>();
      when(api.getUserPlannerItems(any, any, any, forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => completer.future);

      await setupTestLocator((locator) {
        locator.registerLazySingleton<PlannerApi>(() => api);
        locator.registerLazySingleton<CalendarFilterDb>(() => MockCalendarFilterDb());
      });

      PlannerFetcher fetcher = PlannerFetcher(userId: student.id, userDomain: '');

      await tester.pumpWidget(TestApp(
        ChangeNotifierProvider<PlannerFetcher>(
          create: (_) => fetcher,
          child: CalendarDayPlanner(dateTime),
        ),
      ));
      await tester.pump();
      await tester.pump();

      verify(api.getUserPlannerItems(student.id, any, any, contexts: [], forceRefresh: false));

      completer.completeError('Error');
      await tester.pumpAndSettle();

      expect(find.byType(ErrorPandaWidget), findsOneWidget);
      expect(find.text(AppLocalizations().errorLoadingEvents), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows empty panda view when we have no events', (tester) async {
      var student = CanvasModelTestUtils.mockUser(name: 'Panda');
      var dateTime = DateTime.now();

      var api = MockPlannerApi();
      Completer completer = Completer<List<PlannerItem>>();
      when(api.getUserPlannerItems(any, any, any, forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => completer.future);

      await setupTestLocator((locator) {
        locator.registerLazySingleton<PlannerApi>(() => api);
        locator.registerLazySingleton<CalendarFilterDb>(() => MockCalendarFilterDb());
      });

      PlannerFetcher fetcher = PlannerFetcher(userId: student.id, userDomain: '');

      await tester.pumpWidget(TestApp(
        ChangeNotifierProvider<PlannerFetcher>(
          create: (_) => fetcher,
          child: CalendarDayPlanner(dateTime),
        ),
      ));
      await tester.pump();
      await tester.pump();

      verify(api.getUserPlannerItems(student.id, any, any, contexts: [], forceRefresh: false)).called(1);

      completer.complete(<PlannerItem>[]);
      await tester.pumpAndSettle();

      expect(find.byType(EmptyPandaWidget), findsOneWidget);
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('pull to refresh refreshes list', (tester) async {
      var student = CanvasModelTestUtils.mockUser(name: 'Panda');
      var dateTime = DateTime.now();

      var api = MockPlannerApi();
      when(api.getUserPlannerItems(any, any, any, forceRefresh: anyNamed('forceRefresh'))).thenAnswer((_) async => []);

      await setupTestLocator((locator) {
        locator.registerLazySingleton<PlannerApi>(() => api);
        locator.registerLazySingleton<CalendarFilterDb>(() => MockCalendarFilterDb());
      });

      PlannerFetcher fetcher = PlannerFetcher(userId: student.id, userDomain: '');

      await tester.pumpWidget(TestApp(
        ChangeNotifierProvider<PlannerFetcher>(
          create: (_) => fetcher,
          child: CalendarDayPlanner(dateTime),
        ),
      ));
      await tester.pumpAndSettle();

      // Empty state
      expect(find.byType(CalendarDayList), findsNothing);
      expect(find.byType(EmptyPandaWidget), findsOneWidget);

      when(api.getUserPlannerItems(any, any, any, forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) async => [_createPlannerItem(contextName: 'blank')]);

      // PTR
      await tester.drag(find.byType(RefreshIndicator), Offset(0, 300));
      await tester.pumpAndSettle();

      // Should now show list
      expect(find.byType(EmptyPandaWidget), findsNothing);
      expect(find.byType(CalendarDayList), findsOneWidget);
    });
  });
}

Plannable _createPlannable() => Plannable((b) => b
  ..id = ''
  ..title = '');

PlannerItem _createPlannerItem({String contextName}) => PlannerItem((b) => b
  ..courseId = ''
  ..plannable = _createPlannable().toBuilder()
  ..contextType = ''
  ..plannableDate = DateTime.now()
  ..contextName = contextName ?? ''
  ..htmlUrl = ''
  ..plannableType = 'assignment');
