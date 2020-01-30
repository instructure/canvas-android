// Copyright (C) 2019 - present Instructure, Inc.
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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_grades_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_summary_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_syllabus_screen.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  final studentId = '123';
  final courseId = '321';

  _setupLocator({CourseDetailsInteractor interactor, CreateConversationInteractor convoInteractor}) {
    final _locator = GetIt.instance;
    _locator.reset();

    _locator.registerFactory<CourseDetailsInteractor>(() => interactor ?? _MockCourseDetailsInteractor());
    _locator
        .registerFactory<CreateConversationInteractor>(() => convoInteractor ?? _MockCreateConversationInteractor());

    _locator.registerLazySingleton<QuickNav>(() => QuickNav());
  }

  testWidgetsWithAccessibilityChecks('Shows loading', (tester) async {
    _setupLocator();
    await tester.pumpWidget(TestApp(CourseDetailsScreen(studentId, '', courseId)));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows error and can refresh', (tester) async {
    final interactor = _MockCourseDetailsInteractor();
    when(interactor.loadCourse(courseId)).thenAnswer((_) => Future.error('This is an error'));
    _setupLocator(interactor: interactor);

    await tester.pumpWidget(TestApp(CourseDetailsScreen(studentId, '', courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    // Should have the error text
    expect(find.text(AppLocalizations().unexpectedError), findsOneWidget);

    // Should have the refresh indicator
    final matchedWidget = find.byType(RefreshIndicator);
    expect(matchedWidget, findsOneWidget);

    // Try to refresh
    await tester.drag(matchedWidget, const Offset(0, 200));
    await tester.pumpAndSettle(); // Loading indicator takes a lot of frames, pump and settle to wait

    verify(interactor.loadCourse(courseId)).called(2); // Once for initial load, another for the refresh
  });

  testWidgetsWithAccessibilityChecks('Shows course name when given a course', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..name = 'Course Name');
    _setupLocator();

    await tester.pumpWidget(TestApp(CourseDetailsScreen.withCourse(studentId, '', course)));
    await tester.pumpAndSettle(); // Widget creation

    expect(find.text(course.name), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Updates course name', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..name = 'Course Name');
    final interactor = _MockCourseDetailsInteractor();
    when(interactor.loadCourse(courseId)).thenAnswer((_) => Future.value(course));
    _setupLocator(interactor: interactor);

    await tester.pumpWidget(TestApp(CourseDetailsScreen(studentId, '', courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    expect(find.text(course.name), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows all course tabs with syllabus body', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..syllabusBody = ''
      ..name = 'Course Name');
    _setupLocator();

    await tester.pumpWidget(TestApp(CourseDetailsScreen.withCourse(studentId, '', course)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    expect(find.text(AppLocalizations().courseGradesLabel.toUpperCase()), findsOneWidget);
    expect(find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()), findsOneWidget);
    expect(find.text(AppLocalizations().courseSummaryLabel.toUpperCase()), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows all but syllabus course tabs when no syllabus', (tester) async {
    _setupLocator();

    await tester.pumpWidget(TestApp(CourseDetailsScreen(studentId, '', courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    expect(find.text(AppLocalizations().courseGradesLabel.toUpperCase()), findsOneWidget);
    expect(find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()), findsNothing);
    expect(find.text(AppLocalizations().courseSummaryLabel.toUpperCase()), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking grades tab shows the grades screen', (tester) async {
    _setupLocator();

    await tester.pumpWidget(TestApp(CourseDetailsScreen(studentId, '', courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    await tester.tap(find.text(AppLocalizations().courseGradesLabel.toUpperCase()));
    await tester.pumpAndSettle(); // Let the screen animate to the tab

    expect(find.byType(CourseGradesScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking syllabus tab shows the syllabus screen', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..syllabusBody = 'hi'
      ..name = 'Course Name');
    _setupLocator();

    await tester.pumpWidget(TestApp(
      CourseDetailsScreen.withCourse(studentId, '', course),
      platformConfig: PlatformConfig(initWebview: true),
    ));

    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    await tester.tap(find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()));
    await tester.pumpAndSettle(); // Let the screen animate to the tab

    expect(find.byType(CourseSyllabusScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking summary tab shows the summary screen', (tester) async {
    _setupLocator();

    await tester.pumpWidget(TestApp(CourseDetailsScreen(studentId, '', courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    await tester.tap(find.text(AppLocalizations().courseSummaryLabel.toUpperCase()));
    await tester.pumpAndSettle(); // Let the screen animate to the tab

    expect(find.byType(CourseSummaryScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Tapping message button while on grades tab shows message screen', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..name = 'Course Name'
      ..courseCode = '1234');

    final interactor = _MockCourseDetailsInteractor();
    when(interactor.loadCourse(courseId)).thenAnswer((_) => Future.value(course));

    final convoInteractor = _MockCreateConversationInteractor();
    when(convoInteractor.getAllRecipients(any)).thenAnswer((_) => Future.value([]));
    _setupLocator(interactor: interactor, convoInteractor: convoInteractor);

    String studentName = 'Panda';

    await tester.pumpWidget(TestApp(CourseDetailsScreen(studentId, studentName, courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    // Should show the fab
    final matchedWidget = find.byType(FloatingActionButton);
    expect(matchedWidget, findsOneWidget);

    // Tap the FAB
    await tester.tap(matchedWidget);
    await tester.pumpAndSettle(); // Let the new screen create itself

    // Check to make sure we're on the conversation screen
    expect(find.byType(CreateConversationScreen), findsOneWidget);

    // Check that we have the correct subject line
    expect(find.text(AppLocalizations().gradesSubjectMessage(studentName)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Tapping message button while on syllabus tab shows message screen',
      (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..name = 'Course Name'
      ..courseCode = '1234'
      ..syllabusBody = 'test');

    final interactor = _MockCourseDetailsInteractor();
    when(interactor.loadCourse(courseId)).thenAnswer((_) => Future.value(course));

    final convoInteractor = _MockCreateConversationInteractor();
    when(convoInteractor.getAllRecipients(any)).thenAnswer((_) => Future.value([]));
    _setupLocator(interactor: interactor, convoInteractor: convoInteractor);

    String studentName = 'Panda';

    await tester.pumpWidget(TestApp(CourseDetailsScreen(studentId, studentName, courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    // Should show the fab
    final matchedWidget = find.byType(FloatingActionButton);
    expect(matchedWidget, findsOneWidget);

    // Tap the Syllabus tab
    await tester.tap(
        find.ancestor(of: find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()), matching: find.byType(Tab)));
    await tester.pumpAndSettle(); // Let the screen creation settle

    // Tap the FAB
    await tester.tap(matchedWidget);
    await tester.pumpAndSettle(); // Let the new screen create itself

    // Check to make sure we're on the conversation screen
    expect(find.byType(CreateConversationScreen), findsOneWidget);

    // Check that we have the correct subject line
    expect(find.text(AppLocalizations().syllabusSubjectMessage(studentName)), findsOneWidget);
  });
}

class _MockCourseDetailsInteractor extends Mock implements CourseDetailsInteractor {}

class _MockCreateConversationInteractor extends Mock implements CreateConversationInteractor {}
