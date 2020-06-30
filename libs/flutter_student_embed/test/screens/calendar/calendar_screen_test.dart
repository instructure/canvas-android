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
import 'package:flutter/services.dart';
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/calendar_filter.dart';
import 'package:flutter_student_embed/models/course.dart';
import 'package:flutter_student_embed/models/login.dart';
import 'package:flutter_student_embed/models/plannable.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/models/serializers.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_day_planner.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_screen.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_screen.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_widget.dart';
import 'package:flutter_student_embed/screens/to_do/create_update_to_do_screen.dart';
import 'package:flutter_student_embed/screens/to_do/to_do_details_screen.dart';
import 'package:flutter_student_embed/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_student_embed/utils/db/calendar_filter_db.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
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

    // Some tests use a mocked QuickNav, so we reset to default implementation here
    GetIt.instance.registerLazySingleton<QuickNav>(() => QuickNav());
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

  testWidgetsWithAccessibilityChecks('Calls method channel: open drawer', (tester) async {
    String channelId = 'CalendarScreenChannel';

    MethodCall methodCall;

    await tester.pumpWidget(_testableMaterialWidget(channelId: channelId));
    await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

    MethodChannel(channelId).setMockMethodCallHandler((MethodCall call) async {
      methodCall = call;
      return null;
    });
    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([]));

    // Tap on the hamburger menu button
    await tester.tap(find.byIcon(Icons.menu));
    await tester.pump();

    expect(methodCall.method, 'openDrawer');
    expect(methodCall.arguments, isNull);
  });

  testWidgetsWithAccessibilityChecks('Calls method channel: route to native item', (tester) async {
    String channelId = 'CalendarScreenChannel';

    PlannerItem item = PlannerItem((b) => b
      ..plannableType = 'test_type'
      ..plannableDate = DateTime.now().toUtc()
      ..plannable = Plannable((p) => p
        ..id = 'plannable_123'
        ..title = 'Plannable 123').toBuilder());

    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([item]));

    await tester.pumpWidget(_testableMaterialWidget(channelId: channelId));
    await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

    MethodCall methodCall;
    MethodChannel(channelId).setMockMethodCallHandler((MethodCall call) async {
      methodCall = call;
      return null;
    });

    // Tap on the planner item
    await tester.tap(find.text(item.plannable.title));
    await tester.pump();

    expect(methodCall.method, 'routeToItem');

    var payload = json.encode(serialize<PlannerItem>(item));
    expect(methodCall.arguments, payload);
  });

  testWidgetsWithAccessibilityChecks('Today button moves calendar to the current date', (tester) async {
    DateTime today = DateTime.now().withTimeAtMidnight();
    DateTime tomorrow = DateTime(today.year, today.month, today.day + 1);

    PlannerItem todayItem = PlannerItem((b) => b
      ..plannableType = 'test_type'
      ..plannableDate = today
      ..plannable = Plannable((p) => p
        ..id = 'today_item'
        ..title = 'Today Item').toBuilder());

    PlannerItem tomorrowItem = PlannerItem((b) => b
      ..plannableType = 'test_type'
      ..plannableDate = tomorrow
      ..plannable = Plannable((p) => p
        ..id = 'tomorrow_item'
        ..title = 'Tomorrow Item').toBuilder());

    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((inv) async {
      DateTime monthStart = inv.positionalArguments[1];
      return [todayItem, tomorrowItem].where((item) => item.plannableDate.month == monthStart.month).toList();
    });

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

    // Should show today's item
    expect(find.text(todayItem.plannable.title), findsOneWidget);
    expect(find.text(tomorrowItem.plannable.title), findsNothing);

    // Should not show the 'today' button
    expect(find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel), findsNothing);

    // Swipe to tomorrow
    await tester.fling(find.byType(CalendarDayPlanner), Offset(-50, 0), 300);
    await tester.pumpAndSettle();

    // Should show tomorrows's item
    expect(find.text(todayItem.plannable.title), findsNothing);
    expect(find.text(tomorrowItem.plannable.title), findsOneWidget);

    // Should show the 'today' button
    expect(find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel), findsOneWidget);

    // Tapping the 'today' button should move back to today
    await tester.tap(find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel));
    await tester.pumpAndSettle();

    // Should show today's item
    expect(find.text(todayItem.plannable.title), findsOneWidget);
    expect(find.text(tomorrowItem.plannable.title), findsNothing);

    // Should not show the 'today' button
    expect(find.bySemanticsLabel(AppLocalizations().gotoTodayButtonLabel), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Tapping FAB routes to create To Do screen', (tester) async {
    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([]));

    QuickNav nav = MockQuickNav();
    GetIt.instance.registerLazySingleton<QuickNav>(() => nav);

    when(nav.push(any, any)).thenAnswer((_) => Completer<Set<String>>().future);

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets
    await tester.tap(find.byType(FloatingActionButton));
    await tester.pumpAndSettle();

    verify(nav.push(any, argThat(isA<CreateUpdateToDoScreen>())));
  });

  testWidgetsWithAccessibilityChecks('Tapping planner note routes to details', (tester) async {
    PlannerItem item = PlannerItem((b) => b
      ..plannableType = 'planner_note'
      ..plannableDate = DateTime.now().toUtc()
      ..plannable = Plannable((p) => p
        ..id = 'plannable_note_123'
        ..title = 'Planner Note').toBuilder());

    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([item]));

    QuickNav nav = MockQuickNav();
    GetIt.instance.registerLazySingleton<QuickNav>(() => nav);

    when(nav.push(any, any)).thenAnswer((_) => Completer<List<DateTime>>().future);

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

    // Tap the item
    await tester.tap(find.text(item.plannable.title));
    await tester.pumpAndSettle();

    verify(nav.push(any, argThat(isA<ToDoDetailsScreen>())));
  });

  testWidgetsWithAccessibilityChecks('New planner notes propagate to other calendar screen instances', (tester) async {
    PlannerItem item = PlannerItem((b) => b
      ..plannableType = 'test_type'
      ..plannableDate = DateTime.now().toUtc()
      ..plannable = Plannable((p) => p
        ..id = 'plannable_123'
        ..title = 'Plannable 123').toBuilder());

    // Return nothing from API at first
    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([]));

    QuickNav nav = MockQuickNav();
    when(nav.push(any, any)).thenAnswer((_) async => [item.plannableDate]);
    GetIt.instance.registerLazySingleton<QuickNav>(() => nav);

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

    // Push second CalendarScreen
    BuildContext context = tester.state(find.byType(CalendarScreen)).context;
    QuickNav().push(context, CalendarScreen());
    await tester.pumpAndSettle();

    // Now start returning the 'newly created' item from the API
    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([item]));

    // Tap the FAB to fake 'creating' the new item
    await tester.tap(find.byType(FloatingActionButton));
    await tester.pumpAndSettle();

    // Should have refreshed to show new item
    expect(find.text(item.plannable.title), findsOneWidget);

    // Go back to fist calendar screen
    TestApp.navigatorKey.currentState.pop();
    await tester.pumpAndSettle();

    // First calendar screen should also show new item
    expect(find.byType(CalendarScreen), findsOneWidget);
    expect(find.text(item.plannable.title), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Planner note updates propagate to other calendar screens', (tester) async {
    PlannerItem oldItem = PlannerItem((b) => b
      ..plannableType = 'planner_note'
      ..plannableDate = DateTime.now().toUtc()
      ..plannable = Plannable((p) => p
        ..id = 'planner_note'
        ..title = 'Old Title').toBuilder());

    PlannerItem newItem =
        oldItem.rebuild((b) => b..plannable = oldItem.plannable.rebuild((p) => p..title = 'New Title').toBuilder());

    // Return old item from API at first
    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([oldItem]));

    QuickNav nav = MockQuickNav();
    when(nav.push(any, any)).thenAnswer((_) async => [oldItem.plannableDate]);
    GetIt.instance.registerLazySingleton<QuickNav>(() => nav);

    await tester.pumpWidget(_testableMaterialWidget());
    await tester.pumpAndSettle(Duration(seconds: 1)); // Wait for the timers in the calendar day widgets

    // Push second CalendarScreen
    BuildContext context = tester.state(find.byType(CalendarScreen)).context;
    QuickNav().push(context, CalendarScreen());
    await tester.pumpAndSettle();

    // Now start returning the updated item from the API
    when(plannerApi.getUserPlannerItems(any, any, any,
            contexts: anyNamed('contexts'), forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value([newItem]));

    // Tap the item and fake 'updating' it
    await tester.tap(find.text(oldItem.plannable.title));
    await tester.pumpAndSettle();

    // Should have refreshed to show new item
    expect(find.text(newItem.plannable.title), findsOneWidget);

    // Go back to fist calendar screen
    TestApp.navigatorKey.currentState.pop();
    await tester.pumpAndSettle();

    // First calendar screen should also show new item
    expect(find.byType(CalendarScreen), findsOneWidget);
    expect(find.text(newItem.plannable.title), findsOneWidget);
  });
}

Widget _testableMaterialWidget({Widget widget, NavigatorObserver observer, String channelId}) {
  var login = Login((b) => b
    ..uuid = 'uuid'
    ..domain = 'domain'
    ..accessToken = 'token'
    ..user = CanvasModelTestUtils.mockUser().toBuilder());

  return TestApp(
    Scaffold(body: widget ?? CalendarScreen(channelId: channelId)),
    platformConfig: PlatformConfig(
      initLoggedInUser: login,
    ),
    navigatorObservers: observer != null ? [observer] : [],
  );
}

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
