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

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/canvas_page.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_settings.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_front_page_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_grades_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_summary_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_syllabus_screen.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/design/canvas_icons_solid.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final studentId = '123';
  final courseId = '321';
  final studentName = 'Panda';
  final student = User((b) => b
    ..id = studentId
    ..name = studentName);

  final courseInteractor = MockCourseDetailsInteractor();
  final convoInteractor = MockCreateConversationInteractor();

  setupTestLocator((_locator) {
    _locator.registerFactory<CourseDetailsInteractor>(() => courseInteractor);
    _locator.registerFactory<CreateConversationInteractor>(() => convoInteractor);
    _locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());

    _locator.registerLazySingleton<QuickNav>(() => QuickNav());
  });

  setUp(() async {
    await setupPlatformChannels(
        config: PlatformConfig(
            mockApiPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}, initWebview: true));

    reset(courseInteractor);
    reset(convoInteractor);
  });

  tearDown(() {
    ApiPrefs.clean();
  });

  testWidgetsWithAccessibilityChecks('Shows loading', (tester) async {
    await tester.pumpWidget(TestApp(CourseDetailsScreen(courseId)));
    await tester.pump();

    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows error and can refresh', (tester) async {
    when(courseInteractor.loadCourse(courseId)).thenAnswer((_) => Future.error('This is an error'));

    await tester.pumpWidget(TestApp(CourseDetailsScreen(courseId)));
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

    // Once for initial load, another for the refresh
    verify(courseInteractor.loadCourse(courseId, forceRefresh: anyNamed('forceRefresh'))).called(2);
  });

  testWidgetsWithAccessibilityChecks('Shows course name when given a course', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..name = 'Course Name');

    await tester.pumpWidget(TestApp(CourseDetailsScreen.withCourse(course)));
    await tester.pumpAndSettle(); // Widget creation

    expect(find.text(course.name), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Updates course name', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..name = 'Course Name');
    when(courseInteractor.loadCourse(courseId)).thenAnswer((_) => Future.value(course));

    await tester.pumpWidget(TestApp(CourseDetailsScreen(courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    expect(find.text(course.name), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows all course tabs with syllabus body', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..syllabusBody = 'body'
      ..homePage = HomePage.syllabus
      ..name = 'Course Name');

    when(courseInteractor.loadCourseSettings(courseId, forceRefresh: true)).thenAnswer((_) async {
      return CourseSettings((b) => b..courseSummary = true);
    });

    await tester.pumpWidget(TestApp(CourseDetailsScreen.withCourse(course)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    expect(find.text(AppLocalizations().courseGradesLabel.toUpperCase()), findsOneWidget);
    expect(find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()), findsOneWidget);
    expect(find.text(AppLocalizations().courseSummaryLabel.toUpperCase()), findsOneWidget);
    expect(find.byIcon(CanvasIconsSolid.refresh), findsOneWidget);

    expect(find.text(AppLocalizations().courseFrontPageLabel.toUpperCase()), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Does not show summary tab if summary disable in course settings', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..syllabusBody = 'body'
      ..homePage = HomePage.syllabus
      ..name = 'Course Name');

    when(courseInteractor.loadCourseSettings(courseId, forceRefresh: true)).thenAnswer((_) async {
      return CourseSettings((b) => b..courseSummary = false);
    });

    await tester.pumpWidget(TestApp(CourseDetailsScreen.withCourse(course)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    expect(find.text(AppLocalizations().courseGradesLabel.toUpperCase()), findsOneWidget);
    expect(find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()), findsOneWidget);
    expect(find.text(AppLocalizations().courseSummaryLabel.toUpperCase()), findsNothing);
    expect(find.byIcon(CanvasIconsSolid.refresh), findsOneWidget);

    expect(find.text(AppLocalizations().courseFrontPageLabel.toUpperCase()), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows course tabs with home page', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..homePage = HomePage.wiki
      ..name = 'Course Name');

    await tester.pumpWidget(TestApp(CourseDetailsScreen.withCourse(course)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    expect(find.text(AppLocalizations().courseGradesLabel.toUpperCase()), findsOneWidget);
    expect(find.text(AppLocalizations().courseFrontPageLabel.toUpperCase()), findsOneWidget);
    expect(find.byIcon(CanvasIconsSolid.refresh), findsOneWidget);

    expect(find.text(AppLocalizations().courseSummaryLabel.toUpperCase()), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Shows no tabs when no syllabus and no front page', (tester) async {
    await tester.pumpWidget(TestApp(CourseDetailsScreen(courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    // Should show the view
    expect(find.byType(CourseGradesScreen), findsOneWidget);

    // Should not show any tabs
    expect(find.text(AppLocalizations().courseGradesLabel.toUpperCase()), findsNothing);
    expect(find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()), findsNothing);
    expect(find.text(AppLocalizations().courseSummaryLabel.toUpperCase()), findsNothing);
    expect(find.byIcon(CanvasIconsSolid.refresh), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Clicking grades tab shows the grades screen', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..syllabusBody = 'hi'
      ..homePage = HomePage.syllabus
      ..name = 'Course Name');

    await tester.pumpWidget(TestApp(CourseDetailsScreen.withCourse(course)));
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
      ..homePage = HomePage.syllabus
      ..name = 'Course Name');

    await tester.pumpWidget(TestApp(
      CourseDetailsScreen.withCourse(course),
      platformConfig: PlatformConfig(initWebview: true),
    ));

    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    await tester.tap(find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()));
    await tester.pumpAndSettle(); // Let the screen animate to the tab

    expect(find.byType(CourseSyllabusScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking front page tab shows the front page screen', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..homePage = HomePage.wiki
      ..name = 'Course Name');

    when(courseInteractor.loadFrontPage(courseId)).thenAnswer((_) async => CanvasPage((b) => b
      ..id = '1'
      ..body = 'hodor'));

    await tester.pumpWidget(TestApp(
      CourseDetailsScreen.withCourse(course),
      platformConfig: PlatformConfig(initWebview: true),
    ));

    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    await tester.tap(find.text(AppLocalizations().courseFrontPageLabel.toUpperCase()));
    await tester.pumpAndSettle(); // Let the screen animate to the tab

    expect(find.byType(CourseFrontPageScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Clicking summary tab shows the summary screen', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..syllabusBody = 'hi'
      ..homePage = HomePage.syllabus
      ..name = 'Course Name');

    when(courseInteractor.loadCourseSettings(courseId, forceRefresh: true)).thenAnswer((_) async {
      return CourseSettings((b) => b..courseSummary = true);
    });

    await tester.pumpWidget(TestApp(
      CourseDetailsScreen.withCourse(course),
    ));
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

    when(courseInteractor.loadCourse(courseId)).thenAnswer((_) => Future.value(course));
    when(convoInteractor.loadData(any, any)).thenAnswer((_) async => CreateConversationData(course, []));

    String studentName = 'Panda';

    await tester.pumpWidget(TestApp(CourseDetailsScreen(courseId)));
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
      ..homePage = HomePage.syllabus
      ..syllabusBody = 'test');

    when(courseInteractor.loadCourse(courseId)).thenAnswer((_) => Future.value(course));
    when(convoInteractor.loadData(any, any)).thenAnswer((_) async => CreateConversationData(course, []));

    String studentName = 'Panda';

    await tester.pumpWidget(TestApp(CourseDetailsScreen(courseId)));
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

  testWidgetsWithAccessibilityChecks('Tapping message button while on front page tab shows message screen',
      (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..name = 'Course Name'
      ..courseCode = '1234'
      ..homePage = HomePage.wiki);

    when(courseInteractor.loadCourse(courseId)).thenAnswer((_) => Future.value(course));
    when(courseInteractor.loadFrontPage(courseId)).thenAnswer((_) async => CanvasPage((b) => b
      ..id = '1'
      ..body = 'hodor'));
    when(convoInteractor.loadData(any, any)).thenAnswer((_) async => CreateConversationData(course, []));

    String studentName = 'Panda';

    await tester.pumpWidget(TestApp(CourseDetailsScreen(courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    // Should show the fab
    final matchedWidget = find.byType(FloatingActionButton);
    expect(matchedWidget, findsOneWidget);

    // Tap the Front Page tab
    await tester.tap(find.ancestor(
        of: find.text(AppLocalizations().courseFrontPageLabel.toUpperCase()), matching: find.byType(Tab)));
    await tester.pumpAndSettle(); // Let the screen creation settle

    // Tap the FAB
    await tester.tap(matchedWidget);
    await tester.pumpAndSettle(); // Let the new screen create itself

    // Check to make sure we're on the conversation screen
    expect(find.byType(CreateConversationScreen), findsOneWidget);

    // Check that we have the correct subject line
    expect(find.text(AppLocalizations().frontPageSubjectMessage(studentName)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks(
      'Tapping message button after selecting front page tab and then grades tab shows message screen', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..name = 'Course Name'
      ..courseCode = '1234'
      ..homePage = HomePage.wiki);

    when(courseInteractor.loadCourse(courseId)).thenAnswer((_) => Future.value(course));
    when(courseInteractor.loadFrontPage(courseId)).thenAnswer((_) async => CanvasPage((b) => b
      ..id = '1'
      ..body = 'hodor'));
    when(convoInteractor.loadData(any, any)).thenAnswer((_) async => CreateConversationData(course, []));

    String studentName = 'Panda';

    await tester.pumpWidget(TestApp(CourseDetailsScreen(courseId)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    // Should show the fab
    final matchedWidget = find.byType(FloatingActionButton);
    expect(matchedWidget, findsOneWidget);

    // Tap the Front Page tab
    await tester.tap(find.ancestor(
        of: find.text(AppLocalizations().courseFrontPageLabel.toUpperCase()), matching: find.byType(Tab)));
    await tester.pumpAndSettle(); // Let the screen creation settle

    // Tap back to the grades tab
    await tester.tap(
        find.ancestor(of: find.text(AppLocalizations().courseGradesLabel.toUpperCase()), matching: find.byType(Tab)));
    await tester.pumpAndSettle();

    // Tap the FAB
    await tester.tap(matchedWidget);
    await tester.pumpAndSettle(); // Let the new screen create itself

    // Check to make sure we're on the conversation screen
    expect(find.byType(CreateConversationScreen), findsOneWidget);

    // Check that we have the correct subject line
    expect(find.text(AppLocalizations().gradesSubjectMessage(studentName)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Can refresh using app bar button', (tester) async {
    final course = Course((b) => b
      ..id = courseId
      ..homePage = HomePage.wiki
      ..name = 'Course Name');

    when(courseInteractor.loadCourse(courseId, forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value(course));
    when(courseInteractor.loadFrontPage(courseId)).thenAnswer((_) async => CanvasPage((b) => b
      ..id = '1'
      ..body = 'hodor'));
    
    await tester.pumpWidget(TestApp(CourseDetailsScreen.withCourse(course)));
    await tester.pump(); // Widget creation
    await tester.pump(); // Future resolved

    await tester.tap(find.byIcon(CanvasIconsSolid.refresh));
    await tester.pumpAndSettle();

    verify(courseInteractor.loadCourse(courseId, forceRefresh: true)).called(1); // Refresh load
  });
}