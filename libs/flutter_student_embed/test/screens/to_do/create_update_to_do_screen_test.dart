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

import 'package:flutter/material.dart';
import 'package:flutter_student_embed/l10n/app_localizations.dart';
import 'package:flutter_student_embed/models/course.dart';
import 'package:flutter_student_embed/models/plannable.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/screens/to_do/create_update_to_do_screen.dart';
import 'package:flutter_student_embed/screens/to_do/create_update_to_do_screen_interactor.dart';
import 'package:flutter_student_embed/utils/core_extensions/date_time_extensions.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../testutils/accessibility_utils.dart';
import '../../testutils/dummy_widget.dart';
import '../../testutils/test_app.dart';

void main() {
  CreateUpdateToDoScreenInteractor interactor = _MockCreateUpdateToDoScreenInteractor();

  AppLocalizations l10n = AppLocalizations();

  List<Course> userCourses = [
    Course((b) => b
      ..id = 'course_123'
      ..name = 'Course 123'),
    Course((b) => b
      ..id = 'course_456'
      ..name = 'Course 456'),
    Course((b) => b
      ..id = 'course_789'
      ..name = 'Course 789'),
  ];

  setupTestLocator((locator) {
    locator.registerLazySingleton<CreateUpdateToDoScreenInteractor>(() => interactor);
  });

  setUp(() {
    reset(interactor);
    when(interactor.getCoursesForUser()).thenAnswer((realInvocation) async => userCourses);
  });

  testWidgetsWithAccessibilityChecks('Displays correct defaults when creating with initial date', (tester) async {
    DateTime date = DateTime(2000, 1, 1);
    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(initialDate: date)));
    await tester.pumpAndSettle();

    expect(find.text(l10n.toDoTitleHint), findsOneWidget);
    expect(find.text(l10n.toDoCourseLabel), findsOneWidget);
    expect(find.text(l10n.date), findsOneWidget);
    expect(find.text(date.l10nFormat(l10n.dateAtTime)), findsOneWidget);
    expect(find.text(l10n.toDoDescriptionHint), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays correct defaults when creating without initial date', (tester) async {
    DateTime date = DateTime.now();
    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen()));
    await tester.pumpAndSettle();

    expect(find.text(l10n.toDoTitleHint), findsOneWidget);
    expect(find.text(l10n.toDoCourseLabel), findsOneWidget);
    expect(find.text(l10n.date), findsOneWidget);
    expect(find.text(date.l10nFormat(l10n.dateAtTime)), findsOneWidget);
    expect(find.text(l10n.toDoDescriptionHint), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays course options', (tester) async {
    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen()));
    await tester.pumpAndSettle();

    // Tap course dropdown
    await tester.tap(find.text(l10n.toDoCourseLabel));
    await tester.pumpAndSettle();

    // Should display 'None' and all courses
    expect(find.text(l10n.toDoCourseNone), findsOneWidget);
    userCourses.forEach((course) {
      expect(find.text(course.name), findsWidgets);
    });
  });

  testWidgetsWithAccessibilityChecks('Selects a course', (tester) async {
    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen()));
    await tester.pumpAndSettle();

    // Tap course dropdown
    await tester.tap(find.text(l10n.toDoCourseLabel));
    await tester.pumpAndSettle();

    // Select the second course
    var course = userCourses[1];
    await tester.tap(find.text(course.name).last);
    await tester.pumpAndSettle();

    // Should show the course name now
    expect(find.text(course.name), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Selects date', (tester) async {
    DateTime date = DateTime(2000, 1, 1);
    DateTime expectedDate = DateTime(2000, 1, 5);

    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(initialDate: date)));
    await tester.pumpAndSettle();

    // Tap date
    await tester.tap(find.text(l10n.date));
    await tester.pumpAndSettle();

    // Select the 5th
    await tester.tap(find.text('5'));
    await tester.pumpAndSettle();

    // Tap 'Ok" to confirm the date and show the time picker
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();

    // Tap 'Ok" to confirm the time
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();

    // Show show selected date
    expect(find.text(expectedDate.l10nFormat(l10n.dateAtTime)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays error when title is missing', (tester) async {
    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen()));
    await tester.pumpAndSettle();

    // Enter description, but no title
    await tester.enterText(find.byKey(Key('description-input')), 'Test description');
    await tester.pumpAndSettle();

    // Tap save button
    await tester.tap(find.text(l10n.save.toUpperCase()));
    await tester.pumpAndSettle();

    // Should show title error
    expect(find.text(l10n.titleEmptyErrorMessage), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays unsaved changes dialog when creating', (tester) async {
    DateTime date = DateTime(2000, 1, 1);
    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(initialDate: date)));
    await tester.pumpAndSettle();

    // Change the date
    await tester.tap(find.text(l10n.date));
    await tester.pumpAndSettle();
    await tester.tap(find.text('5'));
    await tester.pumpAndSettle();
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();

    // Try to go back
    TestApp.navigatorKey.currentState.maybePop();
    await tester.pumpAndSettle();

    expect(find.byType(AlertDialog), findsOneWidget);
    expect(find.text(l10n.unsavedChangesDialogTitle), findsOneWidget);
    expect(find.text(l10n.unsavedChangesDialogBody), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays unsaved changes dialog when updating', (tester) async {
    String title = 'Test title';
    String description = 'Test description';
    DateTime date = DateTime(2000, 1, 1);
    Course course = userCourses[2];

    Plannable plannable = Plannable((b) => b
      ..id = 'plannable_123'
      ..title = title
      ..courseId = course.id
      ..details = description
      ..toDoDate = date);

    PlannerItem item = PlannerItem((b) => b
      ..plannableType = 'planner_note'
      ..plannableDate = plannable.toDoDate
      ..plannable = plannable.toBuilder());

    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(editToDo: item)));
    await tester.pumpAndSettle();

    // Change the date
    await tester.tap(find.text(l10n.date));
    await tester.pumpAndSettle();
    await tester.tap(find.text('5'));
    await tester.pumpAndSettle();
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();
    await tester.tap(find.text(DefaultMaterialLocalizations().okButtonLabel));
    await tester.pumpAndSettle();

    // Try to go back
    TestApp.navigatorKey.currentState.maybePop();
    await tester.pumpAndSettle();

    expect(find.byType(AlertDialog), findsOneWidget);
    expect(find.text(l10n.unsavedChangesDialogTitle), findsOneWidget);
    expect(find.text(l10n.unsavedChangesDialogBody), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Canceling unsaved changes dialog does not pop screen', (tester) async {
    DateTime date = DateTime(2000, 1, 1);
    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(initialDate: date)));
    await tester.pumpAndSettle();

    // Add a title
    await tester.enterText(find.byKey(Key('title-input')), 'Test title');
    await tester.pumpAndSettle();

    // Try to go back
    TestApp.navigatorKey.currentState.maybePop();
    await tester.pumpAndSettle();

    // Tap the 'NO' button
    await tester.tap(find.text(l10n.no.toUpperCase()));
    await tester.pumpAndSettle();

    // Change focus to the description input so the text selection handle (which has no semantic label) not longer shows
    await tester.tap(find.byKey(Key('description-input')));
    await tester.pumpAndSettle();

    expect(find.byType(AlertDialog), findsNothing);
    expect(find.byType(CreateUpdateToDoScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Confirming unsaved changes dialog pops screen', (tester) async {
    await tester.pumpWidget(TestApp(DummyWidget()));
    await tester.pumpAndSettle();

    BuildContext context = tester.state(find.byType(DummyWidget)).context;
    QuickNav().push(context, CreateUpdateToDoScreen());
    await tester.pumpAndSettle();

    // Add a title
    await tester.enterText(find.byKey(Key('title-input')), 'Test title');
    await tester.pumpAndSettle();

    // Try to go back
    TestApp.navigatorKey.currentState.maybePop();
    await tester.pumpAndSettle();

    // Tap the 'YES' button
    await tester.tap(find.text(l10n.yes.toUpperCase()));
    await tester.pumpAndSettle();

    expect(find.byType(CreateUpdateToDoScreen), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Calls interactor with correct data when creating', (tester) async {
    String title = 'Test title';
    String description = 'Test description';
    DateTime date = DateTime(2000, 1, 1);
    Course course = userCourses[2];

    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(initialDate: date)));
    await tester.pumpAndSettle();

    // Enter and description
    await tester.enterText(find.byKey(Key('title-input')), title);
    await tester.enterText(find.byKey(Key('description-input')), description);
    await tester.pumpAndSettle();

    // Select a course
    await tester.tap(find.text(l10n.toDoCourseLabel));
    await tester.pumpAndSettle();
    await tester.tap(find.text(course.name).last);
    await tester.pumpAndSettle();

    // Tap save button
    await tester.tap(find.text(l10n.save.toUpperCase()));
    await tester.pumpAndSettle();

    verify(interactor.createToDo(title, description, date, course.id));
  });

  testWidgetsWithAccessibilityChecks('Calls interactor with correct data when updating', (tester) async {
    String title = 'Test title';
    String description = 'Test description';
    DateTime date = DateTime(2000, 1, 1);
    Course course = userCourses[2];

    Plannable plannable = Plannable((b) => b
      ..id = 'plannable_123'
      ..title = title
      ..courseId = course.id
      ..details = description
      ..toDoDate = date);

    PlannerItem item = PlannerItem((b) => b
      ..plannableType = 'planner_note'
      ..plannableDate = plannable.toDoDate
      ..plannable = plannable.toBuilder());

    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(editToDo: item)));
    await tester.pumpAndSettle();

    // Tap save button
    await tester.tap(find.text(l10n.save.toUpperCase()));
    await tester.pumpAndSettle();

    verify(interactor.updateToDo(plannable.id, title, description, date, course.id));
  });

  testWidgetsWithAccessibilityChecks('Displays error when saving fails', (tester) async {
    when(interactor.createToDo(any, any, any, any)).thenAnswer((realInvocation) => Future.error('Fake error'));

    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen()));
    await tester.pumpAndSettle();

    // Enter title
    await tester.enterText(find.byKey(Key('title-input')), 'Test title');
    await tester.pumpAndSettle();

    // Tap save button
    await tester.tap(find.text(l10n.save.toUpperCase()));
    await tester.pumpAndSettle();

    // Should show title error
    expect(find.text(l10n.errorSavingToDo), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays correct defaults when editing', (tester) async {
    Plannable plannable = Plannable((b) => b
      ..id = 'plannable_123'
      ..title = "To Do Title"
      ..courseId = userCourses[0].id
      ..details = 'To Do Details'
      ..toDoDate = DateTime(2000, 1, 1));
    PlannerItem item = PlannerItem((b) => b
      ..plannableType = 'planner_note'
      ..plannableDate = plannable.toDoDate
      ..plannable = plannable.toBuilder());
    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(editToDo: item)));
    await tester.pumpAndSettle();

    expect(find.text(plannable.title), findsOneWidget);
    expect(find.text(plannable.details), findsOneWidget);
    expect(find.text(userCourses[0].name), findsOneWidget);
    expect(find.text(plannable.toDoDate.l10nFormat(l10n.dateAtTime)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Deselects the course', (tester) async {
    var course = userCourses[0];
    Plannable plannable = Plannable((b) => b
      ..id = 'plannable_123'
      ..title = "To Do Title"
      ..courseId = course.id
      ..details = 'To Do Details'
      ..toDoDate = DateTime(2000, 1, 1));
    PlannerItem item = PlannerItem((b) => b
      ..plannableType = 'planner_note'
      ..plannableDate = plannable.toDoDate
      ..plannable = plannable.toBuilder());
    await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(editToDo: item)));
    await tester.pumpAndSettle();

    // Tap course dropdown
    await tester.tap(find.text(course.name));
    await tester.pumpAndSettle();

    // Tap 'None'
    await tester.tap(find.text(l10n.toDoCourseNone));
    await tester.pumpAndSettle();

    // Should show 'Course (optional)' label
    expect(find.text(l10n.toDoCourseLabel), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks(
    'Displays course optional label when editing an item without a course',
    (tester) async {
      Plannable plannable = Plannable((b) => b
        ..id = 'plannable_123'
        ..title = "To Do Title"
        ..details = 'To Do Details'
        ..toDoDate = DateTime(2000, 1, 1));
      PlannerItem item = PlannerItem((b) => b
        ..plannableType = 'planner_note'
        ..plannableDate = plannable.toDoDate
        ..plannable = plannable.toBuilder());
      await tester.pumpWidget(TestApp(CreateUpdateToDoScreen(editToDo: item)));
      await tester.pumpAndSettle();

      expect(find.text(plannable.title), findsOneWidget);
      expect(find.text(plannable.details), findsOneWidget);
      expect(find.text(l10n.toDoCourseLabel), findsOneWidget);
      expect(find.text(plannable.toDoDate.l10nFormat(l10n.dateAtTime)), findsOneWidget);
    },
  );
}

class _MockCreateUpdateToDoScreenInteractor extends Mock implements CreateUpdateToDoScreenInteractor {}
