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

import 'package:built_collection/built_collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grade.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/courses/details/course_grades_screen.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/test_app.dart';

const studentId = 123;
const courseId = 321;
const assignmentGroupId = 101;

void main() {
  _setupLocator({CourseDetailsInteractor interactor}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerFactory<CourseDetailsInteractor>(() => interactor ?? _MockCourseDetailsInteractor());
  }

  testWidgetsWithAccessibilityChecks('Can refresh course and group data', (tester) async {
    final model = CourseDetailsModel(studentId, courseId);
    final interactor = _MockCourseDetailsInteractor();
    _setupLocator(interactor: interactor);

    // Pump the widget
    await tester.pumpWidget(_testableWidget(model, highContrastMode: true));
    await tester.pump();

    // Should have the refresh indicator
    final matchedWidget = find.byType(RefreshIndicator);
    expect(matchedWidget, findsOneWidget);

    // Try to refresh
    await tester.drag(matchedWidget, const Offset(0, 200));
    await tester.pumpAndSettle(); // Loading indicator takes a lot of frames, pump and settle to wait

    verify(interactor.loadCourse(courseId)).called(1);
    verify(interactor.loadAssignmentGroups(courseId, studentId)).called(1);
  });

  testWidgetsWithAccessibilityChecks('Shows loading', (tester) async {
    final model = CourseDetailsModel(studentId, courseId);
    model.assignmentGroupFuture = Future.value([_mockAssignmentGroup()]);

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump();

    expect(find.byType(LoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows error', (tester) async {
    // Set the future to an error, expecting it to throw an uncaught exception. This expect just prevents the test from
    // failing, the exception doesn't break the runtime code. The reason this happens is there are no listeners for the
    // 'catchError' on the assignment group future, so flutter calls it 'unhandled' and fails the test even though it
    // will still perform the rest of the test.
    final model = CourseDetailsModel(studentId, courseId);
    expect(() => model.assignmentGroupFuture = Future<List<AssignmentGroup>>.error('ErRoR here'), throwsA(anything));

    await tester.pumpWidget(_testableWidget(model));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(AppLocalizations().unexpectedError), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows empty', (tester) async {
    final model = CourseDetailsModel(studentId, courseId);
    model.assignmentGroupFuture = Future.value([_mockAssignmentGroup()]); // Create an empty assignment group

    await tester.pumpWidget(_testableWidget(model, highContrastMode: true));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(AppLocalizations().noAssignmentsTitle), findsOneWidget);
    expect(find.text(AppLocalizations().noAssignmentsMessage), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows data', (tester) async {
    final grade = '1';
    final date = DateTime(2000);
    final group = _mockAssignmentGroup(assignments: [
      _mockAssignment(dueAt: date),
      _mockAssignment(id: 1, pointsPossible: 2.2, submission: _mockSubmission(grade: grade))
    ]);
    final model = CourseDetailsModel(studentId, courseId);
    model.assignmentGroupFuture = Future.value([group]);
    model.course = _mockCourse();

    await tester.pumpWidget(_testableWidget(model, highContrastMode: true));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(AppLocalizations().noGrade), findsOneWidget);

    expect(find.text(group.name), findsOneWidget);
    expect(find.text(group.assignments.first.name), findsOneWidget);
    expect(find.text('Due Jan 1 at 12:00AM'), findsOneWidget);
    expect(find.text('- / 0'), findsOneWidget);

    expect(find.text(group.assignments.last.name), findsOneWidget);
    expect(find.text('No Due Date'), findsOneWidget);
    expect(find.text('$grade / 2.2'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not show empty assignment groups', (tester) async {
    final groups = [
      _mockAssignmentGroup(),
      _mockAssignmentGroup(
          assignmentGroupId: assignmentGroupId + 1, assignments: [_mockAssignment(groupId: assignmentGroupId + 1)]),
    ];
    final model = CourseDetailsModel(studentId, courseId);
    model.course = _mockCourse();
    model.assignmentGroupFuture = Future.value(groups);

    await tester.pumpWidget(_testableWidget(model, highContrastMode: true));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(groups.first.name), findsNothing);

    expect(find.text(groups.last.name), findsOneWidget);
    expect(find.text(groups.last.assignments.first.name), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Does not show assignments when group is collapsed', (tester) async {
    final groups = [
      _mockAssignmentGroup(assignments: [_mockAssignment()])
    ];
    final model = CourseDetailsModel(studentId, courseId);
    model.course = _mockCourse();
    model.assignmentGroupFuture = Future.value(groups);

    await tester.pumpWidget(_testableWidget(model, highContrastMode: true));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    final groupHeader = find.text(groups.first.name);
    expect(groupHeader, findsOneWidget);
    expect(find.text(groups.first.assignments.first.name), findsOneWidget);

    await tester.tap(groupHeader);
    await tester.pumpAndSettle();

    expect(find.text(groups.first.assignments.first.name), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows assignment statuses', (tester) async {
    final date = DateTime(2000);
    final group = _mockAssignmentGroup(assignments: [
      _mockAssignment(dueAt: date), // Missing
      _mockAssignment(id: 1, submission: _mockSubmission(isLate: true)), // Late
      _mockAssignment(id: 2, submission: _mockSubmission(submittedAt: date)), // Submitted
      _mockAssignment(id: 2, dueAt: DateTime.now().add(Duration(days: 1)), submission: _mockSubmission()), // Not submitted
    ]);
    final model = CourseDetailsModel(studentId, courseId);
    model.assignmentGroupFuture = Future.value([group]);
    model.course = _mockCourse();

    await tester.pumpWidget(_testableWidget(model, highContrastMode: true));
    await tester.pump(); // Build the widget
    await tester.pump(); // Let the future finish

    expect(find.text(AppLocalizations().assignmentMissingSubmittedLabel), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentLateSubmittedLabel), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentSubmittedLabel), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentNotSubmittedLabel), findsOneWidget);
  });

  group('Shows course grade', () {
    testWidgetsWithAccessibilityChecks('from current score, max 2 digits', (tester) async {
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final model = CourseDetailsModel(studentId, courseId);
      model.course = _mockCourse(currentScore: 1.2345);
      model.assignmentGroupFuture = Future.value(groups);

      await tester.pumpWidget(_testableWidget(model, highContrastMode: true));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      expect(find.text('1.23%'), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('from current grade', (tester) async {
      final grade = 'Big fat F';
      final groups = [
        _mockAssignmentGroup(assignments: [_mockAssignment()])
      ];
      final model = CourseDetailsModel(studentId, courseId);
      model.course = _mockCourse(currentGrade: grade);
      model.assignmentGroupFuture = Future.value(groups);

      await tester.pumpWidget(_testableWidget(model, highContrastMode: true));
      await tester.pump(); // Build the widget
      await tester.pump(); // Let the future finish

      expect(find.text(grade), findsOneWidget);
    });
  });
}

Course _mockCourse({double currentScore, String currentGrade}) {
  return Course((b) => b
    ..id = courseId
    ..enrollments = BuiltList.of([
      Enrollment((enrollment) => enrollment
        ..userId = studentId
        ..courseId = courseId
        ..grades = (currentScore != null || currentGrade != null)
            ? _mockGrade(currentScore: currentScore, currentGrade: currentGrade)
            : null
        ..enrollmentState = 'active')
    ]).toBuilder());
}

GradeBuilder _mockGrade({double currentScore, double finalScore, String currentGrade, String finalGrade}) {
  return GradeBuilder()
    ..htmlUrl = ''
    ..currentScore = currentScore ?? 0
    ..finalScore = finalScore ?? 0
    ..currentGrade = currentGrade ?? ''
    ..finalGrade = finalGrade ?? '';
}

AssignmentGroup _mockAssignmentGroup(
    {int assignmentGroupId = assignmentGroupId, List<Assignment> assignments = const []}) {
  return AssignmentGroup((b) => b
    ..id = assignmentGroupId
    ..name = 'Group $assignmentGroupId'
    ..position = assignmentGroupId
    ..groupWeight = 0
    ..assignments = BuiltList.of(assignments).toBuilder());
}

Assignment _mockAssignment(
    {int id = 0, int groupId = assignmentGroupId, Submission submission, DateTime dueAt, double pointsPossible = 0}) {
  return Assignment((b) => b
    ..id = id
    ..name = 'Assignment $id'
    ..courseId = courseId
    ..assignmentGroupId = groupId
    ..position = id
    ..dueAt = dueAt
    ..submission = submission?.toBuilder()
    ..pointsPossible = pointsPossible
    ..published = true);
}

Submission _mockSubmission({int assignmentId = 0, String grade, bool isLate, DateTime submittedAt}) {
  return Submission((b) => b
    ..assignmentId = assignmentId
    ..grade = grade
    ..submittedAt = submittedAt
    ..late = isLate ?? false);
}

Widget _testableWidget(CourseDetailsModel model, {bool highContrastMode = false}) {
  return TestApp(
    Scaffold(
      body: ChangeNotifierProvider<CourseDetailsModel>.value(value: model, child: CourseGradesScreen()),
    ),
    highContrast: highContrastMode,
  );
}

class _MockCourseDetailsInteractor extends Mock implements CourseDetailsInteractor {}
