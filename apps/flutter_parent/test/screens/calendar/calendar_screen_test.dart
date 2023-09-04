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
import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/calendar_filter.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/calendar/calendar_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_click_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_screen.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/canvas_model_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  MockCalendarEventsApi calendarApi = MockCalendarEventsApi();
  MockCalendarFilterDb filterDb = MockCalendarFilterDb();
  MockCalendarFilterListInteractor filterInteractor = MockCalendarFilterListInteractor();
  MockCoursesInteractor coursesInteractor = MockCoursesInteractor();

  when(filterDb.getByObserveeId(any, any, any))
      .thenAnswer((_) => Future.value(CalendarFilter((b) => b.filters = SetBuilder({'course_123'}))));

  final String userDomain = 'user_domain';
  final String userId = 'user_123';
  final String observeeId = 'observee_123';
  final Set<String> contexts = {'course_123'};

  setupTestLocator((locator) {
    locator.registerLazySingleton<CalendarFilterDb>(() => filterDb);
    locator.registerLazySingleton<CalendarFilterListInteractor>(() => filterInteractor);
    locator.registerLazySingleton<CoursesInteractor>(() => coursesInteractor);
    locator.registerLazySingleton<CalendarTodayClickNotifier>(() => CalendarTodayClickNotifier());
    locator.registerLazySingleton<CalendarTodayNotifier>(() => CalendarTodayNotifier());
    locator.registerLazySingleton<CalendarEventsApi>(() => calendarApi);
    locator.registerLazySingleton<QuickNav>(() => QuickNav());
  });

  setUp(() {
    reset(calendarApi);

    // Reset db mock
    reset(filterDb);
    when(filterDb.getByObserveeId(any, any, any)).thenAnswer((_) async {
      return CalendarFilter((b) => b
        ..userDomain = userDomain
        ..userId = userId
        ..observeeId = observeeId
        ..filters = SetBuilder(contexts));
    });

    when(coursesInteractor.getCourses(isRefresh: anyNamed('isRefresh')))
        .thenAnswer((_) => Future.value(_mockCourses()));
  });

  tearDown(() {
    ApiPrefs.clean();
  });

  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows the calendar widget', (tester) async {
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(calendarApi.getUserCalendarItems(any, any, any, any,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      expect(find.byType(CalendarWidget), findsOneWidget);
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('clicking on filter navigates to filter screen', (tester) async {
      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(calendarApi.getUserCalendarItems(any, any, any, any,
              contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
          .thenAnswer((_) => Future.value([]));

      when(filterInteractor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value(_mockCourses()));

      // Tap on the calendar filter button
      await tester.tap(find.text(AppLocalizations().calendars));
      await tester.pump();
      await tester.pump();
      await tester.pumpAndSettle();

      // Check for the filter screen
      expect(find.byType(CalendarFilterListScreen), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('filter screen returns updated contexts', (tester) async {
      var completer = Completer<List<Course>>();

      final observer = MockNavigatorObserver();

      when(filterInteractor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => completer.future);

      await tester.pumpWidget(_testableMaterialWidget(observer: observer));
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(calendarApi.getUserCalendarItems(any, any, any, any,
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

      when(filterInteractor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => completer.future);

      await tester.pumpWidget(_testableMaterialWidget(observer: observer));
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(calendarApi.getUserCalendarItems(any, any, any, any,
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

      when(filterInteractor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => completer.future);

      await tester.pumpWidget(_testableMaterialWidget(observer: observer));
      await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

      when(calendarApi.getUserCalendarItems(any, any, any, any,
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

  /*
  testWidgetsWithAccessibilityChecks('filter returns empty list if all items selected', (tester) async {
    var completer = Completer<List<Course>>();

    final observer = _MockNavigatorObserver();

    when(filterInteractor.getCoursesForSelectedStudent(isRefresh: anyNamed('isRefresh')))
        .thenAnswer((_) => completer.future);

    await tester.pumpWidget(_testableMaterialWidget(observer: observer));
    await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

    when(calendarApi.getUserCalendarItems(any, any, any, any,
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
   */
}

Widget _testableMaterialWidget({Widget? widget, SelectedStudentNotifier? notifier = null, NavigatorObserver? observer}) {
  var login = Login((b) => b
    ..uuid = 'uuid'
    ..domain = 'domain'
    ..accessToken = 'token'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  return TestApp(
    ChangeNotifierProvider<SelectedStudentNotifier>(
      create: (context) => notifier ?? SelectedStudentNotifier()
        ..value = _mockStudent('1'),
      child: Consumer<SelectedStudentNotifier>(
        builder: (context, model, _) {
          return Scaffold(body: widget ?? CalendarScreen());
        },
      ),
    ),
    darkMode: true,
    navigatorObservers: observer != null ? [observer] : [],
    platformConfig: PlatformConfig(
      initLoggedInUser: login,
      mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(_mockStudent('123')))},
    ),
  );
}

User _mockStudent(String userId) => User((b) => b
  ..id = userId
  ..name = 'UserName'
  ..sortableName = 'Sortable Name'
  ..build());

final _enrollment = Enrollment((b) => b
  ..enrollmentState = 'active'
  ..userId = '123');

List<Course> _mockCourses() {
  return [
    Course((b) => b
      ..id = '123'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course1'),
    Course((b) => b
      ..id = '234'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course2'),
    Course((b) => b
      ..id = '345'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3')
  ];
}

List<Course> _mockCoursesBigList() {
  return [
    Course((b) => b
      ..id = '123'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course1'),
    Course((b) => b
      ..id = '234'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course2'),
    Course((b) => b
      ..id = '345'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3'),
    Course((b) => b
      ..id = '678'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3'),
    Course((b) => b
      ..id = '678'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3'),
    Course((b) => b
      ..id = '1234'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3'),
    Course((b) => b
      ..id = '2345'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3'),
    Course((b) => b
      ..id = '3456'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3'),
    Course((b) => b
      ..id = '4567'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3'),
    Course((b) => b
      ..id = '5678'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3'),
    Course((b) => b
      ..id = '6789'
      ..enrollments = ListBuilder([_enrollment])
      ..name = 'Course3'),
  ];
}
