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
import 'package:built_collection/built_collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/alarm.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/lock_info.dart';
import 'package:flutter_parent/models/locked_module.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_svg/svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:webview_flutter/webview_flutter.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  final courseId = '123';
  final assignmentId = '321';
  final studentId = '1337';

  final interactor = _MockAssignmentDetailsInteractor();

  final assignment = Assignment((b) => b
    ..id = assignmentId
    ..courseId = courseId
    ..assignmentGroupId = ''
    ..position = 0);

  setupTestLocator((locator) {
    locator.registerFactory<AssignmentDetailsInteractor>(() => interactor);
  });

  setUp(() {
    clearInteractions(interactor);
  });

  testWidgetsWithAccessibilityChecks('Shows loading', (tester) async {
    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
    ));

    await tester.pump();

    expect(find.byType(LoadingIndicator), findsOneWidget);
    expect(find.byType(FloatingActionButton), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows course name in app bar subtitle', (tester) async {
    final courseName = 'name';
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(
              courseName: courseName,
              assignment: assignment,
            ));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text(courseName), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Can swipe to refresh', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    // Should have the refresh indicator
    final matchedWidget = find.byType(RefreshIndicator);
    expect(matchedWidget, findsOneWidget);

    // Try to refresh
    await tester.drag(matchedWidget, const Offset(0, 200));
    await tester.pumpAndSettle();

    verify(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId)).called(2);
  });

  testWidgetsWithAccessibilityChecks('Can send a message', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    await tester.tap(find.byType(FloatingActionButton));

    // TODO: Test message is shown properly
  });

  testWidgetsWithAccessibilityChecks('shows error', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) => Future<AssignmentDetails>.error('Failed to get assignment'));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.byType(ErrorPandaWidget), findsOneWidget);
    await tester.tap(find.text(AppLocalizations().retry));
    await tester.pumpAndSettle();

    verify(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId)).called(2);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment data', (tester) async {
    setupPlatformChannels(config: PlatformConfig(initWebview: true));

    final assignmentName = 'Testing Assignment';
    final description = 'This is a description';
    final dueDate = DateTime.utc(2000);
    final points = 1.5;

    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(
            assignment: assignment.rebuild((b) => b
              ..name = assignmentName
              ..description = description
              ..pointsPossible = points
              ..dueAt = dueDate)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text(assignmentName), findsOneWidget);
    expect(find.text('Jan 1 at 12:00AM'), findsOneWidget);
    expect(find.text('1.5 pts'), findsOneWidget);
    expect(find.byIcon(Icons.do_not_disturb), findsOneWidget);
    expect((tester.widget(find.byIcon(Icons.do_not_disturb)) as Icon).color, ParentColors.licorice);
    expect(find.text(AppLocalizations().assignmentNotSubmittedLabel), findsOneWidget);
    expect((tester.widget(find.text(AppLocalizations().assignmentNotSubmittedLabel)) as Text).style.color,
        ParentColors.licorice);
    expect(find.text(AppLocalizations().assignmentRemindMeDescription), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, false);
    expect(find.text(AppLocalizations().assignmentDescriptionLabel), findsOneWidget);
    expect(find.byType(WebView), findsOneWidget);

    expect(find.text(AppLocalizations().assignmentLockLabel), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment data when submitted', (tester) async {
    final assignmentName = 'Testing Assignment';
    final dueDate = DateTime.utc(2000);
    final submission = Submission((b) => b
      ..assignmentId = assignmentId
      ..userId = studentId
      ..submittedAt = DateTime.now());

    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(
            assignment: assignment.rebuild((b) => b
              ..name = assignmentName
              ..pointsPossible = 1.0
              ..submissionList = BuiltList.of([submission]).toBuilder()
              ..dueAt = dueDate)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text('1 pts'), findsOneWidget);
    expect(find.byIcon(Icons.check_circle), findsOneWidget);
    expect((tester.widget(find.byIcon(Icons.check_circle)) as Icon).color, StudentColorSet.shamrock.lightHC);

    expect(find.text(AppLocalizations().assignmentSubmittedLabel), findsOneWidget);
    expect((tester.widget(find.text(AppLocalizations().assignmentSubmittedLabel)) as Text).style.color,
        StudentColorSet.shamrock.lightHC);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment with no due date', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment.rebuild((b) => b..dueAt = null)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().noDueDate), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows lock info with unlock_at', (tester) async {
    final unlockAt = DateTime(2000);
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment.rebuild((b) => b..unlockAt = unlockAt)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentLockLabel), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentLockedDate('Jan 1 at 12:00AM')), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows lock info with module name', (tester) async {
    final moduleName = 'Locked module';
    final lockInfo = LockInfo((b) => b..contextModule = LockedModule((m) => m..name = moduleName).toBuilder());
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId)).thenAnswer(
        (_) async => AssignmentDetails(assignment: assignment.rebuild((b) => b..lockInfo = lockInfo.toBuilder())));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentLockLabel), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentLockedModule(moduleName)), findsOneWidget);

    expect(find.byType(SvgPicture), findsOneWidget); // Show the locked panda
    expect(find.text(AppLocalizations().assignmentDueLabel), findsNothing); // Fully locked, no due date
    expect(find.text(AppLocalizations().assignmentDescriptionLabel), findsNothing); // Fully locked, no description
  });

  testWidgetsWithAccessibilityChecks('shows lock info with lock_explanation', (tester) async {
    final explanation = 'it is locked';
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId)).thenAnswer(
        (_) async => AssignmentDetails(assignment: assignment.rebuild((b) => b..lockExplanation = explanation)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentLockLabel), findsOneWidget);
    expect(find.text(explanation), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentDueLabel), findsOneWidget); // Not fully locked, show due date
    expect(find.text(AppLocalizations().assignmentDescriptionLabel), findsOneWidget); // Not fully locked, show desc

    expect(find.byType(SvgPicture), findsNothing); // Should not show the locked panda
  });

  testWidgetsWithAccessibilityChecks('shows Assignment with no description', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment.rebuild((b) => b..dueAt = null)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentNoDescriptionBody), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment instruction if quiz', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(
            assignment: assignment.rebuild((b) => b
              ..dueAt = null
              ..submissionTypes = BuiltList.of([SubmissionTypes.onlineQuiz]).toBuilder())));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentInstructionsLabel), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows alarm if set', (tester) async {
    final alarm = Alarm('0', DateTime(2000));
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment, alarm: alarm));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(courseId: courseId, assignmentId: assignmentId, studentId: studentId),
      highContrast: true,
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentRemindMeSet), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, true);
    expect(find.text('Jan 1 at 12:00AM'), findsOneWidget);
  });

  // TODO: test setting reminder and removing reminder
}

class _MockAssignmentDetailsInteractor extends Mock implements AssignmentDetailsInteractor {}
