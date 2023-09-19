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
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/courses/details/course_summary_screen.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/screens/events/event_details_screen.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
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

final studentId = '1234';
final studentName = 'billy jean';

final login = Login((b) => b
  ..domain = 'domain'
  ..accessToken = 'token'
  ..user = CanvasModelTestUtils.mockUser().toBuilder());

final student = User((b) => b
  ..id = studentId
  ..name = studentName);
AppLocalizations l10n = AppLocalizations();

void main() {
  tearDown(() {
    ApiPrefs.clean();
  });

  testWidgetsWithAccessibilityChecks('Loads data using model', (tester) async {
    final model = MockCourseDetailsModel();
    when(model.loadSummary(refresh: false)).thenAnswer((_) async => []);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump();

    verify(model.loadSummary(refresh: false));
  });

  testWidgetsWithAccessibilityChecks('Displays loading indicator', (tester) async {
    final model = MockCourseDetailsModel();

    Completer<List<ScheduleItem>> completer = Completer();
    when(model.loadSummary(refresh: false)).thenAnswer((_) => completer.future);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump();

    expect(find.byType(LoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays empty state', (tester) async {
    final model = MockCourseDetailsModel();
    when(model.loadSummary(refresh: false)).thenAnswer((_) async => []);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(l10n.noCourseSummaryTitle), findsOneWidget);
    expect(find.text(l10n.noCourseSummaryMessage), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays error state', (tester) async {
    final model = MockCourseDetailsModel();

    when(model.loadSummary(refresh: false)).thenAnswer((_) => Future.error(''));

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    expect(find.byType(ErrorPandaWidget), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Refreshes from error state', (tester) async {
    final model = MockCourseDetailsModel();

    when(model.loadSummary(refresh: false)).thenAnswer((_) => Future.error(''));

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    when(model.loadSummary(refresh: true)).thenAnswer((_) async => []);

    await tester.tap(find.text(l10n.retry));
    await tester.pumpAndSettle();

    // Should no longer show error widget
    expect(find.byType(ErrorPandaWidget), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Refreshes from empty state', (tester) async {
    final model = MockCourseDetailsModel();

    when(model.loadSummary(refresh: false)).thenAnswer((_) async => []);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    when(model.loadSummary(refresh: true)).thenAnswer((_) => Future.error(''));

    await tester.drag(find.byType(RefreshIndicator), Offset(0, 300));
    await tester.pumpAndSettle();

    // Should now show error widgets and not empty widget
    expect(find.byType(EmptyPandaWidget), findsNothing);
    expect(find.byType(ErrorPandaWidget), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays details for calendar event', (tester) async {
    final model = MockCourseDetailsModel();

    final event = ScheduleItem((s) => s
      ..type = ScheduleItem.apiTypeCalendar
      ..title = 'Calendar Event'
      ..startAt = DateTime.now());
    when(model.loadSummary(refresh: false)).thenAnswer((_) async => [event]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    expect(find.text(event.title!), findsOneWidget);
    expect(find.text(event.startAt.l10nFormat(l10n.dateAtTime)!), findsOneWidget);
    expect(find.byIcon(CanvasIcons.calendar_month), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays details for assignment', (tester) async {
    final model = MockCourseDetailsModel();

    final event = ScheduleItem((s) => s
      ..type = ScheduleItem.apiTypeAssignment
      ..title = 'Normal Assignment'
      ..startAt = DateTime.now()
      ..assignment = (AssignmentBuilder()
        ..id = ''
        ..courseId = ''
        ..assignmentGroupId = ''
        ..position = 0
        ..submissionTypes = ListBuilder([])));
    when(model.loadSummary(refresh: false)).thenAnswer((_) async => [event]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    expect(find.text(event.title!), findsOneWidget);
    expect(find.text(event.startAt.l10nFormat(l10n.dateAtTime)!), findsOneWidget);
    expect(find.byIcon(CanvasIcons.assignment), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays details for discussion assignment', (tester) async {
    final model = MockCourseDetailsModel();

    final event = ScheduleItem((s) => s
      ..type = ScheduleItem.apiTypeAssignment
      ..title = 'Discussion Assignment'
      ..startAt = DateTime.now()
      ..assignment = (AssignmentBuilder()
        ..id = ''
        ..courseId = ''
        ..assignmentGroupId = ''
        ..position = 0
        ..submissionTypes = ListBuilder([SubmissionTypes.discussionTopic])));
    when(model.loadSummary(refresh: false)).thenAnswer((_) async => [event]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    expect(find.text(event.title!), findsOneWidget);
    expect(find.text(event.startAt.l10nFormat(l10n.dateAtTime)!), findsOneWidget);
    expect(find.byIcon(CanvasIcons.discussion), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays details for quiz assignment', (tester) async {
    final model = MockCourseDetailsModel();

    final event = ScheduleItem((s) => s
      ..type = ScheduleItem.apiTypeAssignment
      ..title = 'Quiz Assignment'
      ..startAt = DateTime.now()
      ..assignment = (AssignmentBuilder()
        ..id = ''
        ..courseId = ''
        ..assignmentGroupId = ''
        ..position = 0
        ..submissionTypes = ListBuilder([SubmissionTypes.onlineQuiz])));
    when(model.loadSummary(refresh: false)).thenAnswer((_) async => [event]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    expect(find.text(event.title!), findsOneWidget);
    expect(find.text(event.startAt.l10nFormat(l10n.dateAtTime)!), findsOneWidget);
    expect(find.byIcon(CanvasIcons.quiz), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays details for locked assignment', (tester) async {
    final model = MockCourseDetailsModel();

    final event = ScheduleItem((s) => s
      ..type = ScheduleItem.apiTypeAssignment
      ..title = 'Locked Assignment'
      ..startAt = DateTime.now()
      ..assignment = (AssignmentBuilder()
        ..id = ''
        ..courseId = ''
        ..assignmentGroupId = ''
        ..position = 0
        ..lockedForUser = true
        ..submissionTypes = ListBuilder([SubmissionTypes.discussionTopic])));
    when(model.loadSummary(refresh: false)).thenAnswer((_) async => [event]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    expect(find.text(event.title!), findsOneWidget);
    expect(find.text(event.startAt.l10nFormat(l10n.dateAtTime)!), findsOneWidget);
    expect(find.byIcon(CanvasIcons.lock), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays details for undated assignment', (tester) async {
    final model = MockCourseDetailsModel();

    final event = ScheduleItem((s) => s
      ..type = ScheduleItem.apiTypeAssignment
      ..title = 'Undated Assignment'
      ..startAt = null);
    when(model.loadSummary(refresh: false)).thenAnswer((_) async => [event]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    expect(find.text(event.title!), findsOneWidget);
    expect(find.text(l10n.noDueDate), findsOneWidget);
    expect(find.byIcon(CanvasIcons.assignment), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Tapping assignment item loads assignment details', (tester) async {
    await tester.runAsync(() async {
        final event = ScheduleItem((s)
      =>
      s
      ..type = ScheduleItem.apiTypeAssignment
      ..title = 'Normal Assignment'
      ..startAt = DateTime.now()
      ..assignment = (AssignmentBuilder()
        ..id = 'assignment_123'
        ..courseId = ''
        ..assignmentGroupId = ''
        ..position = 0
        ..submissionTypes = ListBuilder([]))
      );

      final model = MockCourseDetailsModel();
      when(model.courseId).thenReturn('course_123');
      when(model.student).thenReturn(student);
      when(model.course).thenReturn(Course((c) => c..courseCode = 'CRS 123'));
      when(model.loadSummary(refresh: false)).thenAnswer((_) async => [event]);

      var interactor = MockAssignmentDetailsInteractor();
      setupTestLocator((locator) {
      locator.registerFactory<AssignmentDetailsInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => QuickNav());
      });
      when(interactor.loadAssignmentDetails(any, 'course_123', 'assignment_123', studentId))
          .thenAnswer((_) async => Future<AssignmentDetails>.value(AssignmentDetails(course: model.course, assignment: event.assignment)));

      var observer = MockNavigatorObserver();
      await tester.pumpWidget(_testableWidget(
      model,
      observers: [observer],
      platformConfig: PlatformConfig(
      mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}, initLoggedInUser: login),
      ));
      await tester.pump(Duration(seconds: 2));
      await tester.pumpAndSettle();

      await tester.tap(find.text(event.title!));

      await tester.pump(Duration(seconds: 2));
      await tester.pump();
      await tester.pump();
      await tester.pumpAndSettle();

      expect(find.byType(AssignmentDetailsScreen), findsOneWidget);
    });
  });

  testWidgetsWithAccessibilityChecks('Tapping calendar event item loads event details', (tester) async {
    final event = ScheduleItem((s) => s
      ..type = ScheduleItem.apiTypeCalendar
      ..title = 'Normal Event'
      ..startAt = DateTime.now());

    final model = MockCourseDetailsModel();
    when(model.loadSummary(refresh: false)).thenAnswer((_) async => [event]);
    when(model.student).thenAnswer((_) => student);

    var interactor = MockEventDetailsInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<EventDetailsInteractor>(() => interactor);
      locator.registerLazySingleton<QuickNav>(() => QuickNav());
    });

    await tester.pumpWidget(_testableWidget(model));
    await tester.pumpAndSettle();

    await tester.tap(find.text(event.title!));

    await tester.pump();
    await tester.pump();

    expect(find.byType(EventDetailsScreen), findsOneWidget);
  });
}

Widget _testableWidget(CourseDetailsModel model,
    {List<NavigatorObserver> observers = const [], PlatformConfig platformConfig = const PlatformConfig()}) {
  return TestApp(
    Scaffold(
      body: ChangeNotifierProvider<CourseDetailsModel>.value(value: model, child: CourseSummaryScreen()),
    ),
    platformConfig: PlatformConfig(
        mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}, initLoggedInUser: login),
    navigatorObservers: observers,
  );
}
