//  Copyright (C) 2019 - present Instructure, Inc.
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, version 3 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_settings.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grade.dart';
import 'package:flutter_parent/models/grading_period.dart';
import 'package:flutter_parent/models/grading_period_response.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/models/submission_wrapper.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/courses/details/course_grades_screen.dart';
import 'package:flutter_parent/screens/courses/details/grading_period_modal.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

const _studentId = '123';
const _courseId = '321';
const _assignmentGroupId = '101';
const _studentName = 'billy jean';

final _student = User((b) => b
  ..id = _studentId
  ..name = _studentName);

void main() {
  final interactor = MockCourseDetailsInteractor();

  setupTestLocator((locator) {
    locator.registerFactory<CourseDetailsInteractor>(() => interactor);
    locator.registerFactory<AssignmentDetailsInteractor>(() => MockAssignmentDetailsInteractor());
    locator.registerLazySingleton<QuickNav>(() => QuickNav());
  });

  setUp(() {
    reset(interactor);
  });

  testWidgetsWithAccessibilityChecks('Can refresh course and group data', (tester) async {
    final model = CourseDetailsModel(_student, _courseId);

    // Pump the widget
    await tester.pumpWidget(_testableWidget(model));
    await tester.pump();

    // Should have the refresh indicator
    final matchedWidget = find.byType(RefreshIndicator);
    expect(matchedWidget, findsOneWidget);

    // Try to refresh
    await tester.drag(matchedWidget, const Offset(0, 200));
    await tester.pumpAndSettle(); // Loading indicator takes a lot of frames, pump and settle to wait

    verify(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: false)).called(1);
  });

  testWidgetsWithAccessibilityChecks('Shows loading', (tester) async {
    final model = CourseDetailsModel(_student, _courseId);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump();

    expect(find.byType(LoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows error', (tester) async {
    // Set the future to an error, expecting it to throw an uncaught exception. This expect just prevents the test from
    // failing, the exception doesn't break the runtime code. The reason this happens is there are no listeners for the
    // 'catchError' on the assignment group future, so flutter calls it 'unhandled' and fails the test even though it
    // will still perform the rest of the test.
    final model = CourseDetailsModel(_student, _courseId);
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null))
        .thenAnswer((_) async => Future<List<AssignmentGroup>>.error('Error getting assignment groups'));

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.byType(ErrorPandaWidget), findsOneWidget);
    await tester.tap(find.text(AppLocalizations().retry));
    await tester.pumpAndSettle(); // Pump and settle since refresh animation

    verify(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: true)).called(1);
  });

  // We still want to show the grades page even if we can't get the term enrollment
  testWidgetsWithAccessibilityChecks('Does not show error for term enrollment failure', (tester) async {
    final model = CourseDetailsModel(_student, _courseId);
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, any, forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) async => Future<List<Enrollment>>.error('Error getting term enrollment'));

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(AppLocalizations().noAssignmentsTitle), findsOneWidget);
    expect(find.text(AppLocalizations().noAssignmentsMessage), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows empty', (tester) async {
    final model = CourseDetailsModel(_student, _courseId);
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => <AssignmentGroup>[]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(AppLocalizations().noAssignmentsTitle), findsOneWidget);
    expect(find.text(AppLocalizations().noAssignmentsMessage), findsOneWidget);
  });

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('Shows empty with period header', (tester) async {
    final model = CourseDetailsModel(_student, _courseId);

    final gradingPeriod = GradingPeriod((b) => b
      ..id = '123'
      ..title = 'test period');
    final enrollment = Enrollment((b) => b
      ..enrollmentState = 'active'
      ..grades = _mockGrade(currentScore: 12)
      ..multipleGradingPeriodsEnabled = true);
    model.updateGradingPeriod(gradingPeriod);
    model.course = _mockCourse().rebuild((b) => b..hasGradingPeriods = true);

    // Mock stuff
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => <AssignmentGroup>[]);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
        (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of([gradingPeriod]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    // Verify that we are showing the course grade when not locked
    expect(find.text(AppLocalizations().courseTotalGradeLabel), findsOneWidget);
    expect(find.text('test period'), findsOneWidget);

    // Verify that we are showing the empty message
    expect(find.text(AppLocalizations().noAssignmentsTitle), findsOneWidget);
    expect(find.text(AppLocalizations().noAssignmentsMessage), findsOneWidget);
  }, skip: true);

  testWidgetsWithAccessibilityChecks('Shows empty without period header', (tester) async {
    final model = CourseDetailsModel(_student, _courseId);

    GradingPeriod? gradingPeriod = null;
    final enrollment = Enrollment((b) => b
      ..enrollmentState = 'active'
      ..grades = _mockGrade(currentScore: 12)
      ..multipleGradingPeriodsEnabled = true);
    model.updateGradingPeriod(gradingPeriod);
    model.course = _mockCourse().rebuild((b) => b..hasGradingPeriods = true);

    // Mock stuff
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => <AssignmentGroup>[]);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
        (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList<GradingPeriod>.of([]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    // Verify that we are showing the course grade when not locked
    expect(find.text(AppLocalizations().courseTotalGradeLabel), findsNothing);
    expect(find.text('test period'), findsNothing);

    // Verify that we are showing the empty message
    expect(find.text(AppLocalizations().noAssignmentsTitle), findsOneWidget);
    expect(find.text(AppLocalizations().noAssignmentsMessage), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows data', (tester) async {
    final grade = '1';
    final date = DateTime(2000);
    final group = _mockAssignmentGroup(assignments: [
      _mockAssignment(dueAt: date),
      _mockAssignment(id: '1', pointsPossible: 2.2, submission: _mockSubmission(grade: grade))
    ]);
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');

    final model = CourseDetailsModel(_student, _courseId);
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => [group]);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
        (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);
    model.course = _mockCourse();

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(AppLocalizations().noGrade), findsOneWidget);

    expect(find.text(group.name), findsOneWidget);
    expect(find.text(group.assignments.first.name!), findsOneWidget);
    expect(find.text('Due Jan 1 at 12:00 AM'), findsOneWidget);
    expect(find.text('- / 0'), findsOneWidget);

    expect(find.text(group.assignments.last.name!), findsOneWidget);
    expect(find.text('No Due Date'), findsOneWidget);
    expect(find.text('$grade / 2.2'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not show empty assignment groups', (tester) async {
    final groups = [
      _mockAssignmentGroup(),
      _mockAssignmentGroup(
          assignmentGroupId: _assignmentGroupId + '1',
          assignments: [_mockAssignment(groupId: _assignmentGroupId + '1')]),
    ];
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');

    final model = CourseDetailsModel(_student, _courseId);
    model.course = _mockCourse();
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
        (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(groups.first.name), findsNothing);

    expect(find.text(groups.last.name), findsOneWidget);
    expect(find.text(groups.last.assignments.first.name!), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not show assignments when group is collapsed', (tester) async {
    final groups = [
      _mockAssignmentGroup(assignments: [_mockAssignment()])
    ];
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');

    final model = CourseDetailsModel(_student, _courseId);
    model.course = _mockCourse();
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
        (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    final groupHeader = find.text(groups.first.name);
    expect(groupHeader, findsOneWidget);
    expect(find.text(groups.first.assignments.first.name!), findsOneWidget);

    await tester.tap(groupHeader);
    await tester.pumpAndSettle();

    expect(find.text(groups.first.assignments.first.name!), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows assignment statuses', (tester) async {
    final date = DateTime(2000);
    final group = _mockAssignmentGroup(assignments: [
      _mockAssignment(dueAt: date), // Missing
      _mockAssignment(id: '1', submission: _mockSubmission(isLate: true)), // Late
      _mockAssignment(id: '2', submission: _mockSubmission(submittedAt: date)), // Submitted
      _mockAssignment(
          id: '2', dueAt: DateTime.now().add(Duration(days: 1)), submission: _mockSubmission()), // Not submitted
    ]);
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');

    final model = CourseDetailsModel(_student, _courseId);
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => [group]);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
        (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);
    model.course = _mockCourse();

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(AppLocalizations().assignmentMissingSubmittedLabel), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentLateSubmittedLabel), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentSubmittedLabel), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentNotSubmittedLabel), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows grade with possible points if not restricted', (tester) async {
    final grade = 'FFF';
    final group = _mockAssignmentGroup(assignments: [_mockAssignment(id: '1', pointsPossible: 2.2, submission: _mockSubmission(grade: grade))]);
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');

    final model = CourseDetailsModel(_student, _courseId);
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => [group]);
    when(interactor.loadGradingPeriods(_courseId))
        .thenAnswer((_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);
    model.course = _mockCourse();

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text('$grade / 2.2'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows grade without possible points if restricted', (tester) async {
    final grade = 'FFF';
    final group = _mockAssignmentGroup(assignments: [_mockAssignment(id: '1', pointsPossible: 2.2, submission: _mockSubmission(grade: grade))]);
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');

    final model = CourseDetailsModel(_student, _courseId);
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => [group]);
    when(interactor.loadGradingPeriods(_courseId))
        .thenAnswer((_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);
    model.course = _mockCourse().rebuild((b) => b..settings.restrictQuantitativeData = true);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text('$grade'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows grade by score and grading scheme if restricted', (tester) async {
    final group = _mockAssignmentGroup(assignments: [
      _mockAssignment(id: '1', pointsPossible: 10, gradingType: GradingType.points, submission: _mockSubmission(grade: '', score: 1.0))
    ]);
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');

    final model = CourseDetailsModel(_student, _courseId);
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => [group]);
    when(interactor.loadGradingPeriods(_courseId))
        .thenAnswer((_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);
    model.course = _mockCourse().rebuild((b) => b..settings.restrictQuantitativeData = true);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text('F'), findsOneWidget);
  });

  group('CourseGradeHeader', () {
    testWidgetsWithAccessibilityChecks('from current score, max 2 digits', (tester) async {
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..grades = _mockGrade(currentScore: 1.2345));

      final model = CourseDetailsModel(_student, _courseId);
      model.course = _mockCourse();
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer((_) async =>
          GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null))
          .thenAnswer((_) async => [enrollment]);

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      expect(find.text('1.23%'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('from current grade', (tester) async {
      final grade = 'Big fat F';
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..grades = _mockGrade(currentGrade: grade));
      final model = CourseDetailsModel(_student, _courseId);
      model.course = _mockCourse();
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer((_) async =>
          GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null))
          .thenAnswer((_) async => [enrollment]);

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      expect(find.text(grade), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('from current grade and score', (tester) async {
      final grade = 'Big fat F';
      final score = 15.15;
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..grades = _mockGrade(currentScore: score, currentGrade: grade));
      final model = CourseDetailsModel(_student, _courseId);
      model.course = _mockCourse();
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer((_) async =>
          GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null))
          .thenAnswer((_) async => [enrollment]);

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      expect(find.text("$grade $score%"), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('is not shown when locked', (tester) async {
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..multipleGradingPeriodsEnabled = true);
      final model = CourseDetailsModel(_student, _courseId);
      model.course = _mockCourse().rebuild((b) => b..hasGradingPeriods = true);
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer((_) async =>
          GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null))
          .thenAnswer((_) async => [enrollment]);

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      // Verify that we aren't showing the course grade when locked
      expect(find.text(AppLocalizations().courseTotalGradeLabel), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('is not shown when restricted and its a score', (tester) async {
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..grades = _mockGrade(currentScore: 12));
      final model = CourseDetailsModel(_student, _courseId);
      model.course = _mockCourse();
      model.courseSettings = CourseSettings((b) => b..restrictQuantitativeData = true);
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId))
          .thenAnswer((_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      // Verify that we are not showing the course score if restricted
      expect(find.text(AppLocalizations().courseTotalGradeLabel), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('is shown when restricted and its a grade', (tester) async {
      final grade = 'Big fat F';
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..grades = _mockGrade(currentGrade: grade));
      final model = CourseDetailsModel(_student, _courseId);
      model.course = _mockCourse();
      model.courseSettings = CourseSettings((b) => b..restrictQuantitativeData = true);
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId))
          .thenAnswer((_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      // Verify that we are showing the course grade when restricted
      expect(find.text(grade), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('only grade is shown when restricted', (tester) async {
      final grade = 'Big fat F';
      final score = 15.15;
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..grades = _mockGrade(currentScore: score, currentGrade: grade));
      final model = CourseDetailsModel(_student, _courseId);
      model.course = _mockCourse();
      model.courseSettings = CourseSettings((b) => b..restrictQuantitativeData = true);
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId))
          .thenAnswer((_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      // Verify that we are showing the course grade when restricted
      expect(find.text(grade), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('is shown when looking at a grading period', (tester) async {
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final gradingPeriod = GradingPeriod((b) => b
        ..id = '123'
        ..title = 'test period');
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..grades = _mockGrade(currentScore: 12)
        ..multipleGradingPeriodsEnabled = true);
      final model = CourseDetailsModel(_student, _courseId);
      model.updateGradingPeriod(gradingPeriod);
      model.course = _mockCourse().rebuild((b) => b..hasGradingPeriods = true);

      // Mock stuff
      when(interactor.loadAssignmentGroups(_courseId, _studentId, gradingPeriod.id)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
          (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of([gradingPeriod]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null))
          .thenAnswer((_) async => [enrollment]);

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      // Verify that we are showing the course grade when not locked
      expect(find.text(AppLocalizations().courseTotalGradeLabel), findsOneWidget);
      expect(find.text('test period'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('is shown when looking at all grading periods with an active period set',
        (tester) async {
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final gradingPeriod = GradingPeriod((b) => b
        ..id = null
        ..title = 'All Grading Periods');
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..role = 'observer'
        ..userId = _studentId
        ..currentGradingPeriodId = '1212'
        ..totalsForAllGradingPeriodsOption = true
        ..multipleGradingPeriodsEnabled = true
        ..currentPeriodComputedCurrentScore = 12
        ..computedCurrentScore = 1);
      final model = CourseDetailsModel(_student, _courseId);
      model.updateGradingPeriod(gradingPeriod);
      model.course = _mockCourse().rebuild((b) => b
        ..hasGradingPeriods = true
        ..enrollments = ListBuilder([enrollment]));

      // Mock stuff
      when(interactor.loadAssignmentGroups(_courseId, _studentId, gradingPeriod.id)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
          (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of([gradingPeriod]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => Future.value(null));

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      // Verify that we are showing the course grade when not locked
      expect(find.text(AppLocalizations().courseTotalGradeLabel), findsOneWidget);
      expect(find.text('1%'), findsOneWidget);
      expect(find.text(AppLocalizations().noGrade), findsNothing);
    });

    testWidgetsWithAccessibilityChecks(
        'is shown when looking at a period with an active period set (given no enrollment response)', (tester) async {
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final gradingPeriod = GradingPeriod((b) => b
        ..id = '1212'
        ..title = 'All Grading Periods');
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..role = 'observer'
        ..userId = _studentId
        ..currentGradingPeriodId = gradingPeriod.id
        ..totalsForAllGradingPeriodsOption = true
        ..multipleGradingPeriodsEnabled = true
        ..currentPeriodComputedCurrentScore = 12
        ..computedCurrentScore = 1);
      final model = CourseDetailsModel(_student, _courseId);
      model.updateGradingPeriod(gradingPeriod);
      model.course = _mockCourse().rebuild((b) => b
        ..hasGradingPeriods = true
        ..enrollments = ListBuilder([enrollment]));

      // Mock stuff
      when(interactor.loadAssignmentGroups(_courseId, _studentId, gradingPeriod.id)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
          (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of([gradingPeriod]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => Future.value(null));

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      // Verify that we are showing the course grade when not locked
      expect(find.text(AppLocalizations().courseTotalGradeLabel), findsOneWidget);
      expect(find.text('12%'), findsOneWidget);
      expect(find.text(AppLocalizations().noGrade), findsNothing);
    });

    testWidgetsWithAccessibilityChecks(
        'is not shown when looking at all grading periods with an active period set and final grades hidden',
        (tester) async {
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final gradingPeriod = GradingPeriod((b) => b
        ..id = null
        ..title = 'All Grading Periods');
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..role = 'observer'
        ..userId = _studentId
        ..currentGradingPeriodId = '1212'
        ..totalsForAllGradingPeriodsOption = true
        ..multipleGradingPeriodsEnabled = true
        ..currentPeriodComputedCurrentScore = 12
        ..computedCurrentScore = 1);
      final model = CourseDetailsModel(_student, _courseId);
      model.updateGradingPeriod(gradingPeriod);
      model.course = _mockCourse().rebuild((b) => b
        ..hasGradingPeriods = true
        ..hideFinalGrades = true
        ..enrollments = ListBuilder([enrollment]));

      // Mock stuff
      when(interactor.loadAssignmentGroups(_courseId, _studentId, gradingPeriod.id)).thenAnswer((_) async => groups);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
          (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of([gradingPeriod]).toBuilder()));
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => Future.value(null));

      await tester.pumpWidget(_testableWidget(model));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      // Verify that we are showing the course grade when not locked
      expect(find.text(AppLocalizations().courseTotalGradeLabel), findsNothing);
    });
  });

  testWidgetsWithAccessibilityChecks('grading period not shown when not multiple grading periods', (tester) async {
    final groups = [
      _mockAssignmentGroup(assignments: [_mockAssignment()])
    ];
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');
    final model = CourseDetailsModel(_student, _courseId);
    model.course = _mockCourse();
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
        (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    // Verify that we aren't showing the grading period header when there are no periods
    expect(find.text(AppLocalizations().filter), findsNothing);
    expect(find.text(AppLocalizations().allGradingPeriods), findsNothing);
  });

  // TODO Fix test
  testWidgetsWithAccessibilityChecks(
      'grading period is shown for multiple grading periods when all grading periods is selected and no assignments exist',
      (tester) async {
    final groups = <AssignmentGroup>[];
    final gradingPeriod = GradingPeriod((b) => b
      ..id = '123'
      ..title = 'Other');
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');
    final model = CourseDetailsModel(_student, _courseId);
    model.course = _mockCourse();
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer((_) async =>
        GradingPeriodResponse((b) => b..gradingPeriods = BuiltList<GradingPeriod>.from([gradingPeriod]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    // Verify that we aren't showing the grading period header when there are no periods
    expect(find.byType(EmptyPandaWidget), findsOneWidget);
    expect(find.text(AppLocalizations().filter), findsOneWidget);
    expect(find.text(AppLocalizations().allGradingPeriods), findsOneWidget);
  }, skip: true);

  testWidgetsWithAccessibilityChecks('filter tap shows grading period modal', (tester) async {
    final grade = '1';
    final date = DateTime(2000);
    final group = _mockAssignmentGroup(assignments: [
      _mockAssignment(dueAt: date),
      _mockAssignment(id: '1', pointsPossible: 2.2, submission: _mockSubmission(grade: grade))
    ]);
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');
    final gradingPeriods = [GradingPeriod((b) => b..title = 'period 1'), GradingPeriod((b) => b..title = 'period 2')];

    final model = CourseDetailsModel(_student, _courseId);
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => [group]);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
        (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(gradingPeriods).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);
    model.course = _mockCourse();

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    // Expect all grading periods by default
    expect(find.text(AppLocalizations().allGradingPeriods), findsOneWidget);

    await tester.tap(find.text(AppLocalizations().filter));
    await tester.pumpAndSettle(); // Wait for the inkwell and the modal showing to finish

    final modal = find.byType(GradingPeriodModal);
    expect(modal, findsOneWidget);
    expect(find.descendant(of: modal, matching: find.text(AppLocalizations().filterBy)), findsOneWidget);
    expect(find.descendant(of: modal, matching: find.text(AppLocalizations().allGradingPeriods)), findsOneWidget);
    expect(find.descendant(of: modal, matching: find.text('period 1')), findsOneWidget);
    expect(find.descendant(of: modal, matching: find.text('period 2')), findsOneWidget);

    // Tap the new period and assert it's now shown
    await tester.tap(find.text('period 1'));
    await tester.pumpAndSettle();
    expect(find.text('period 1'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Tapping an assignment shows the details screen', (tester) async {
    final groups = [
      _mockAssignmentGroup(assignments: [_mockAssignment()])
    ];
    final enrollment = Enrollment((b) => b..enrollmentState = 'active');

    final model = CourseDetailsModel(_student, _courseId);
    model.course = _mockCourse();
    when(interactor.loadAssignmentGroups(_courseId, _studentId, null)).thenAnswer((_) async => groups);
    when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
        (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(<GradingPeriod>[]).toBuilder()));
    when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null)).thenAnswer((_) async => [enrollment]);

    await tester.pumpWidget(_testableWidget(model,
        platformConfig:
            PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(_student))})));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    await tester.tap(find.text(groups.first.assignments.first.name!));
    await tester.pump(); // Process the tap
    await tester.pump(); // Show the widget

    expect(find.byType(AssignmentDetailsScreen), findsOneWidget);
  });
}

final _gradingSchemeBuilder = ListBuilder<JsonObject>()
  ..add(JsonObject(["A", 0.9]))
  ..add(JsonObject(["F", 0.0]));

Course _mockCourse() {
  return Course((b) => b
    ..id = _courseId
    ..courseCode = 'Instructure 101'
    ..enrollments = BuiltList.of([
      Enrollment((enrollment) => enrollment
        ..userId = _studentId
        ..courseId = _courseId
        ..enrollmentState = 'active')
    ]).toBuilder()
    ..gradingScheme = _gradingSchemeBuilder);
}

GradeBuilder _mockGrade({double? currentScore, double? finalScore, String? currentGrade, String? finalGrade}) {
  return GradeBuilder()
    ..htmlUrl = ''
    ..currentScore = currentScore
    ..finalScore = finalScore ?? 0
    ..currentGrade = currentGrade ?? ''
    ..finalGrade = finalGrade ?? '';
}

AssignmentGroup _mockAssignmentGroup({
  String assignmentGroupId = _assignmentGroupId,
  List<Assignment> assignments = const [],
}) {
  return AssignmentGroup((b) => b
    ..id = assignmentGroupId
    ..name = 'Group $assignmentGroupId'
    ..position = int.parse(assignmentGroupId)
    ..groupWeight = 0
    ..assignments = BuiltList.of(assignments).toBuilder());
}

Assignment _mockAssignment({
  String id = '0',
  String groupId = _assignmentGroupId,
  Submission? submission,
  DateTime? dueAt,
  double pointsPossible = 0,
  GradingType? gradingType
}) {
  return Assignment((b) => b
    ..id = id
    ..name = 'Assignment $id'
    ..courseId = _courseId
    ..assignmentGroupId = groupId
    ..position = int.parse(id)
    ..dueAt = dueAt
    ..submissionWrapper =
        SubmissionWrapper((b) => b..submissionList = BuiltList<Submission>.from(submission != null ? [submission] : []).toBuilder()).toBuilder()
    ..pointsPossible = pointsPossible
    ..published = true
    ..gradingType = gradingType);
}

Submission _mockSubmission({String assignmentId = '', String? grade, bool? isLate, DateTime? submittedAt, double? score}) {
  return Submission((b) => b
    ..userId = _studentId
    ..assignmentId = assignmentId
    ..grade = grade
    ..submittedAt = submittedAt
    ..isLate = isLate ?? false
    ..score = score ?? 0);
}

Widget _testableWidget(CourseDetailsModel model, {PlatformConfig platformConfig = const PlatformConfig()}) {
  return TestApp(
    Scaffold(
      body: ChangeNotifierProvider<CourseDetailsModel>.value(value: model, child: CourseGradesScreen()),
    ),
    platformConfig: platformConfig,
  );
}
