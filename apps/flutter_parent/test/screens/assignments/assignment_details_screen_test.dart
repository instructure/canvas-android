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
import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/lock_info.dart';
import 'package:flutter_parent/models/locked_module.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/models/submission_wrapper.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_screen.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/html_description_tile.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/design/parent_colors.dart';
import 'package:flutter_parent/utils/design/student_color_set.dart';
import 'package:flutter_parent/utils/permission_handler.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_svg/svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';
import 'package:permission_handler/permission_handler.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final courseId = '123';
  final assignmentId = '321';
  final studentId = '1337';
  final studentName = 'billy jean';
  final assignmentName = 'Instructure 101';
  final assignmentUrl = 'https://www.instructure.com';

  final interactor = MockAssignmentDetailsInteractor();
  final convoInteractor = MockCreateConversationInteractor();
  final permissionHandler = MockPermissionHandler();

  final student = User((b) => b
    ..id = studentId
    ..name = studentName);

  final assignment = Assignment((b) => b
    ..id = assignmentId
    ..courseId = courseId
    ..name = assignmentName
    ..htmlUrl = assignmentUrl
    ..assignmentGroupId = ''
    ..position = 0);

  final reminder = Reminder((b) => b
    ..id = 123
    ..userId = 'user-123'
    ..type = 'type'
    ..itemId = 'item-123'
    ..date = DateTime.now()
    ..userDomain = 'domain');

  setupTestLocator((locator) {
    locator.registerFactory<AssignmentDetailsInteractor>(() => interactor);
    locator.registerFactory<CreateConversationInteractor>(() => convoInteractor);
    locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());
    locator.registerFactory<QuickNav>(() => QuickNav());
    locator.registerFactory<PermissionHandler>(() => permissionHandler);
  });

  setUp(() {
    reset(interactor);
  });

  testWidgetsWithAccessibilityChecks('Shows loading', (tester) async {
    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    await tester.pump();

    expect(find.byType(LoadingIndicator), findsOneWidget);
    expect(find.byType(FloatingActionButton), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows course name in app bar subtitle', (tester) async {
    final courseName = 'name';
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(
              course: Course((b) => b..name = courseName),
              assignment: assignment,
            ));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(courseName), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Can swipe to refresh', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
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
    await tester.runAsync(() async {
      when(convoInteractor.loadData(any, any)).thenAnswer((_) async => CreateConversationData(Course(), []));
      when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
          .thenAnswer((_) async => AssignmentDetails(assignment: assignment));

      await tester.pumpWidget(TestApp(
        AssignmentDetailsScreen(
          courseId: courseId,
          assignmentId: assignmentId,
        ),
        platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
      ));

      await tester.pumpAndSettle();

      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();

      // Check to make sure we're on the conversation screen
      expect(find.byType(CreateConversationScreen), findsOneWidget);

      // Check that we have the correct subject line
      expect(find.text(AppLocalizations().assignmentSubjectMessage(studentName, assignmentName)), findsOneWidget);
    });
  });

  testWidgetsWithAccessibilityChecks('shows error', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) => Future<AssignmentDetails?>.error('Failed to get assignment'));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    await tester.pumpAndSettle();

    expect(find.byType(ErrorPandaWidget), findsOneWidget);
    await tester.tap(find.text(AppLocalizations().retry));
    await tester.pumpAndSettle();

    verify(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId)).called(2);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment data', (tester) async {
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
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(assignmentName), findsOneWidget);
    expect(find.text(dueDate.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
    expect(find.text('1.5 pts'), findsOneWidget);
    expect(find.byIcon(Icons.do_not_disturb), findsOneWidget);
    expect((tester.widget(find.byIcon(Icons.do_not_disturb)) as Icon).color, ParentColors.oxford);
    expect(find.text(AppLocalizations().assignmentNotSubmittedLabel), findsOneWidget);
    expect((tester.widget(find.text(AppLocalizations().assignmentNotSubmittedLabel)) as Text).style!.color,
        ParentColors.oxford);
    expect(find.text(AppLocalizations().assignmentRemindMeDescription), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, false);
    expect(find.text(AppLocalizations().descriptionTitle), findsOneWidget);
    expect(find.byType(HtmlDescriptionTile), findsOneWidget);

    expect(find.text(AppLocalizations().assignmentLockLabel), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment data when submitted', (tester) async {
    final assignmentName = 'Testing Assignment';
    final dueDate = DateTime.utc(2000);
    final submission = Submission((b) => b
      ..assignmentId = assignmentId
      ..userId = studentId
      ..submittedAt = DateTime.now());

    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId)).thenAnswer((_) async =>
        AssignmentDetails(
            assignment: assignment.rebuild((b) => b
              ..name = assignmentName
              ..pointsPossible = 1.0
              ..submissionWrapper =
                  SubmissionWrapper((b) => b..submissionList = BuiltList<Submission>.from([submission]).toBuilder())
                      .toBuilder()
              ..dueAt = dueDate)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text('1 pts'), findsOneWidget);

    var icon = find.byIcon(CanvasIconsSolid.check);
    expect(icon, findsOneWidget);
    expect((tester.widget(icon) as Icon).color, Colors.white);

    var iconContainer = tester.widget<Container>(find.ancestor(of: icon, matching: find.byType(Container)).first);
    expect((iconContainer.decoration as BoxDecoration).color, StudentColorSet.shamrock.light);

    expect(find.text(AppLocalizations().assignmentSubmittedLabel), findsOneWidget);
    expect((tester.widget(find.text(AppLocalizations().assignmentSubmittedLabel)) as Text).style!.color,
        StudentColorSet.shamrock.light);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment data with no submission', (tester) async {
    final assignmentName = 'Testing Assignment';
    final dueDate = DateTime.utc(2000);

    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId)).thenAnswer((_) async =>
        AssignmentDetails(
            assignment: assignment.rebuild((b) => b
              ..name = assignmentName
              ..pointsPossible = 1.0
              ..submissionWrapper =
                  SubmissionWrapper((b) => b..submissionList = BuiltList<Submission>.from([]).toBuilder()).toBuilder()
              ..submissionTypes = ListBuilder([SubmissionTypes.none])
              ..dueAt = dueDate)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(assignmentName), findsOneWidget);
    expect(find.text('1 pts'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment with no due date', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment.rebuild((b) => b..dueAt = null)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(AppLocalizations().noDueDate), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows lock info with module name', (tester) async {
    final moduleName = 'Locked module';
    final lockInfo = LockInfo((b) => b
      ..contextModule = LockedModule((m) => m
        ..id = ''
        ..contextId = ''
        ..isRequireSequentialProgress = false
        ..name = moduleName).toBuilder());
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId)).thenAnswer(
        (_) async => AssignmentDetails(assignment: assignment.rebuild((b) => b..lockInfo = lockInfo.toBuilder())));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentLockLabel), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentLockedModule(moduleName)), findsOneWidget);

    expect(find.byType(SvgPicture), findsOneWidget); // Show the locked panda
    expect(find.text(AppLocalizations().assignmentDueLabel), findsNothing); // Fully locked, no due date
    expect(find.text(AppLocalizations().descriptionTitle), findsNothing); // Fully locked, no description
  });

  testWidgetsWithAccessibilityChecks('shows lock info with unlock date', (tester) async {
    final lockExplanation = 'Locked date';
    final unlockAt = DateTime.now().add(Duration(days: 1));
    final lockInfo = LockInfo((b) => b..unlockAt = unlockAt);
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(
              assignment: assignment.rebuild((b) => b
                ..lockExplanation = lockExplanation
                ..lockInfo = lockInfo.toBuilder()
                ..unlockAt = unlockAt),
            ));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentLockLabel), findsOneWidget);
    expect(find.text(lockExplanation), findsOneWidget);

    expect(find.byType(SvgPicture), findsOneWidget); // Show the locked panda
    expect(find.text(AppLocalizations().assignmentDueLabel), findsNothing); // Fully locked, no due date
    expect(find.text(AppLocalizations().descriptionTitle), findsNothing); // Fully locked, no description
  });

  testWidgetsWithAccessibilityChecks('shows lock info with lock_explanation', (tester) async {
    final explanation = 'it is locked';
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(
              assignment: assignment.rebuild((b) => b
                ..lockExplanation = explanation
                ..lockAt = DateTime.now().add(Duration(days: -1))),
            ));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(AppLocalizations().assignmentLockLabel), findsOneWidget);
    expect(find.text(explanation), findsOneWidget);
    expect(find.text(AppLocalizations().assignmentDueLabel), findsOneWidget); // Not fully locked, show due date
    expect(find.text(AppLocalizations().descriptionTitle), findsOneWidget); // Not fully locked, show desc

    expect(find.byType(SvgPicture), findsNothing); // Should not show the locked panda
  });

  testWidgetsWithAccessibilityChecks('does not show lock info with lock_explanation if not yet locked', (tester) async {
    final explanation = 'it is locked';
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(
              assignment: assignment.rebuild((b) => b
                ..lockExplanation = explanation
                ..lockAt = DateTime.now().add(Duration(days: 1))),
            ));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    // Should not show locked info since it is not yet locked
    expect(find.text(AppLocalizations().assignmentLockLabel), findsNothing);
    expect(find.text(explanation), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment with no description', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment.rebuild((b) => b..dueAt = null)));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(AppLocalizations().noDescriptionBody), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows Assignment instruction if quiz', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(
            assignment: assignment.rebuild((b) => b
              ..dueAt = null
              ..submissionTypes = BuiltList.of([SubmissionTypes.onlineQuiz]).toBuilder())));

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(AppLocalizations().assignmentInstructionsLabel), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows reminder if set', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment));

    when(interactor.loadReminder(any)).thenAnswer((_) async => reminder);

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(AppLocalizations().assignmentRemindMeSet), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, true);
    expect(find.text(reminder.date.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('creates reminder without due date', (tester) async {
    when(permissionHandler.checkPermissionStatus(Permission.scheduleExactAlarm)).thenAnswer((realInvocation) => Future.value(PermissionStatus.granted));
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment));

    when(interactor.loadReminder(any)).thenAnswer((_) async => null);

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentRemindMeDescription), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, false);

    when(interactor.loadReminder(any)).thenAnswer((_) async => reminder);

    // Tap on switch to open date picker
    await tester.tap(find.byType(Switch));
    await tester.pumpAndSettle();

    // Tap on 'OK' button in date picker to open time picker
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();

    // Tap on 'OK' button in time picker
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();

    verify(interactor.createReminder(any, any, assignmentId, courseId, assignment.name, AppLocalizations().noDueDate));

    expect(find.text(AppLocalizations().assignmentRemindMeSet), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, true);
    expect(find.text(reminder.date.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('creates reminder with due date', (tester) async {
    when(permissionHandler.checkPermissionStatus(Permission.scheduleExactAlarm)).thenAnswer((realInvocation) => Future.value(PermissionStatus.granted));
    final date = DateTime.now().add(Duration(hours: 1));
    when(interactor.loadAssignmentDetails(any, any, any, any))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment.rebuild((b) => b..dueAt = date)));

    when(interactor.loadReminder(any)).thenAnswer((_) async => null);

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentRemindMeDescription), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, false);

    when(interactor.loadReminder(any)).thenAnswer((_) async => reminder);

    // Tap on switch to open date picker
    await tester.tap(find.byType(Switch));
    await tester.pumpAndSettle();

    // Tap on 'OK' button in date picker to open time picker
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();

    // Tap on 'OK' button in time picker
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();

    var expectedBody = date.l10nFormat(AppLocalizations().dueDateAtTime);
    verify(interactor.createReminder(any, any, assignmentId, courseId, assignment.name, expectedBody));

    expect(find.text(AppLocalizations().assignmentRemindMeSet), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, true);
    expect(find.text(reminder.date.l10nFormat(AppLocalizations().dateAtTime)!), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('deletes reminder', (tester) async {
    when(interactor.loadAssignmentDetails(any, courseId, assignmentId, studentId))
        .thenAnswer((_) async => AssignmentDetails(assignment: assignment));

    when(interactor.loadReminder(any)).thenAnswer((_) async => reminder);

    await tester.pumpWidget(TestApp(
      AssignmentDetailsScreen(
        courseId: courseId,
        assignmentId: assignmentId,
      ),
      platformConfig: PlatformConfig(mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}),
    ));

    // Pump for a duration since we're delaying webview load for the animation
    await tester.pumpAndSettle(Duration(seconds: 1));

    expect(find.text(AppLocalizations().assignmentRemindMeSet), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, true);

    when(interactor.loadReminder(any)).thenAnswer((_) async => null);

    await tester.tap(find.byType(Switch));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().assignmentRemindMeDescription), findsOneWidget);
    expect((tester.widget(find.byType(Switch)) as Switch).value, false);
  });
}
