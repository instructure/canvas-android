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
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/calendar_filter.dart';
import 'package:flutter_student_embed/models/course.dart';
import 'package:flutter_student_embed/models/login.dart';
import 'package:flutter_student_embed/models/user.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_screen.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_screen.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_student_embed/utils/db/calendar_filter_db.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../testutils/accessibility_utils.dart';
import '../../testutils/canvas_model_utils.dart';
import '../../testutils/mock_helpers.dart';
import '../../testutils/platform_config.dart';
import '../../testutils/test_app.dart';

void main() {
  PlannerApi plannerApi = MockPlannerApi();
  CalendarFilterDb filterDb = MockCalendarFilterDb();
  CalendarFilterListInteractor filterInteractor = MockCalendarFilterListInteractor();

  final String userDomain = 'user_domain';
  final String userId = 'user_123';
  final Set<String> contexts = {'course_123'};

  setupTestLocator((locator) {
    locator.registerLazySingleton<CalendarFilterDb>(() => filterDb);
    locator.registerLazySingleton<CalendarFilterListInteractor>(() => filterInteractor);
    locator.registerLazySingleton<PlannerApi>(() => plannerApi);
    locator.registerLazySingleton<QuickNav>(() => QuickNav());
  });

  setUp(() {
    reset(plannerApi);

    // Reset db mock
    reset(filterDb);
    when(filterDb.getForUser(any, any)).thenAnswer((_) async {
      return CalendarFilter((b) => b
        ..userDomain = userDomain
        ..userId = userId
        ..filters = SetBuilder(contexts));
    });
  });

  tearDown(() {
    ApiPrefs.clean();
  });

  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows the calendar widget', (tester) async {
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(plannerApi.getUserPlannerItems(any, any, any,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      expect(find.byType(CalendarWidget), findsOneWidget);
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('clicking on filter navigates to filter screen', (tester) async {
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(plannerApi.getUserPlannerItems(any, any, any,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      when(filterInteractor.getCoursesForUser(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      // Tap on the calendar filter button
      await tester.tap(find.text(AppLocalizations().calendars));
      await tester.pump();
      await tester.pump();

      // Check for the filter screen
      expect(find.byType(CalendarFilterListScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('filter screen returns updated contexts', (tester) async {
      var completer = Completer<List<Course>>();

      final observer = MockNavigatorObserver();

      when(filterInteractor.getCoursesForUser(isRefresh: anyNamed('isRefresh'))).thenAnswer((_) => completer.future);

      await tester.pumpWidget(_testableMaterialWidget(observer: observer));
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(plannerApi.getUserPlannerItems(any, any, any,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      // Tap on the calendar filter button
      await tester.tap(find.text(AppLocalizations().calendars));
      await tester.pump();
      await tester.pump();

      // Setup the capture of navigation arguments
      Route pushedRoute = verify(observer.didPush(captureAny, any)).captured[1];
      Set<String> result = {};
      pushedRoute.popped.then((value) {
        result = value;
      });

      completer.complete(Future.value(_mockCourses()));
      await tester.pumpAndSettle();

      // Check for the filter screen
      expect(find.byType(CalendarFilterListScreen), findsOneWidget);

      // Tap on a context item
      await tester.tap(find.text('Course2'));
      await tester.pumpAndSettle();

      // Tap on the back button
      await tester.pageBack();
      await tester.pumpAndSettle(Duration(seconds: 1));

      // Verify that the list of selected items was updated correctly
      expect(result, ['course_123', 'course_234']);
    });

    testWidgetsWithAccessibilityChecks('planner updated if user filtered different contexts', (tester) async {
      var completer = Completer<List<Course>>();

      final observer = MockNavigatorObserver();

      when(filterInteractor.getCoursesForUser(isRefresh: anyNamed('isRefresh'))).thenAnswer((_) => completer.future);

      await tester.pumpWidget(_testableMaterialWidget(observer: observer));
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(plannerApi.getUserPlannerItems(any, any, any,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      // Tap on the calendar filter button
      await tester.tap(find.text(AppLocalizations().calendars));
      await tester.pump();
      await tester.pump();

      // Setup the capture of navigation arguments
      Route pushedRoute = verify(observer.didPush(captureAny, any)).captured[1];
      Set<String> result = {};
      pushedRoute.popped.then((value) {
        result = value;
      });

      completer.complete(Future.value(_mockCourses()));
      await tester.pumpAndSettle();

      // Check for the filter screen
      expect(find.byType(CalendarFilterListScreen), findsOneWidget);

      // Tap on a context item
      await tester.tap(find.text('Course2'));
      await tester.pumpAndSettle();

      // Tap on the back button
      await tester.pageBack();
      await tester.pumpAndSettle(Duration(seconds: 1));

      // Verify that the list of selected items was updated correctly
      expect(result, ['course_123', 'course_234']);
      verify(filterDb.insertOrUpdate(any)).called(1);
    });

    testWidgetsWithAccessibilityChecks('planner not updated if user did not change filtered contexts', (tester) async {
      var completer = Completer<List<Course>>();

      final observer = MockNavigatorObserver();

      when(filterInteractor.getCoursesForUser(isRefresh: anyNamed('isRefresh'))).thenAnswer((_) => completer.future);

      await tester.pumpWidget(_testableMaterialWidget(observer: observer));
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(plannerApi.getUserPlannerItems(any, any, any,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      // Tap on the calendar filter button
      await tester.tap(find.text(AppLocalizations().calendars));
      await tester.pump();
      await tester.pump();

      // Setup the capture of navigation arguments
      Route pushedRoute = verify(observer.didPush(captureAny, any)).captured[1];
      Set<String> result = {};
      pushedRoute.popped.then((value) {
        result = value;
      });

      completer.complete(Future.value(_mockCourses()));
      await tester.pumpAndSettle();

      // Check for the filter screen
      expect(find.byType(CalendarFilterListScreen), findsOneWidget);

      // Tap on the back button
      await tester.pageBack();
      await tester.pumpAndSettle(Duration(seconds: 1));

      // Verify that the list of selected items was updated correctly
      expect(result, ['course_123']);
      verifyNever(filterDb.insertOrUpdate(any));
    });
  });

  testWidgetsWithAccessibilityChecks('filter returns empty list if all items selected', (tester) async {
    var completer = Completer<List<Course>>();

    final observer = MockNavigatorObserver();

    when(filterInteractor.getCoursesForUser(isRefresh: anyNamed('isRefresh'))).thenAnswer((_) => completer.future);

    await tester.pumpWidget(_testableMaterialWidget(observer: observer));
    await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([]));

    // Tap on the calendar filter button
    await tester.tap(find.text(AppLocalizations().calendars));
    await tester.pump();
    await tester.pump();

    // Setup the capture of navigation arguments
    Route pushedRoute = verify(observer.didPush(captureAny, any)).captured[1];
    Set<String> result = {};
    pushedRoute.popped.then((value) {
      result = value;
    });

    completer.complete(Future.value(_mockCourses()));
    await tester.pumpAndSettle();

    // Check for the filter screen
    expect(find.byType(CalendarFilterListScreen), findsOneWidget);

    // Tap on unselected context items
    await tester.tap(find.text('Course2'));
    await tester.tap(find.text('Course3'));
    await tester.pumpAndSettle();

    // Tap on the back button
    await tester.pageBack();
    await tester.pumpAndSettle(Duration(seconds: 1));

    // Verify that the list of selected items was updated correctly
    expect(result, <String>[]);
  });
}

Widget _testableMaterialWidget({Widget widget, NavigatorObserver observer}) {
  var login = Login((b) => b
    ..uuid = 'uuid'
    ..domain = 'domain'
    ..accessToken = 'token'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  return TestApp(
    Scaffold(body: widget ?? CalendarScreen()),
    platformConfig: PlatformConfig(
      initLoggedInUser: login,
    ),
    navigatorObservers: observer != null ? [observer] : [],
  );
}

User _mockStudent(String userId) => User((b) => b
  ..id = userId
  ..name = 'UserName'
  ..sortableName = 'Sortable Name'
  ..build());

List<Course> _mockCourses() {
  return [
    Course((b) => b
      ..id = '123'
      ..name = 'Course1'),
    Course((b) => b
      ..id = '234'
      ..name = 'Course2'),
    Course((b) => b
      ..id = '345'
      ..name = 'Course3')
  ];
}
