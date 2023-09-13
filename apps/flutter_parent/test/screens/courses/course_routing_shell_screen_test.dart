/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/canvas_page.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_interactor.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_screen.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/canvas_web_view.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final interactor = MockCourseRoutingShellInteractor();

  final course = Course((b) => b
    ..name = 'course name'
    ..syllabusBody = 'hodor syllabus');

  final page = CanvasPage((b) => b
    ..id = '123'
    ..body = 'hodor front page'
    ..hideFromStudents = false
    ..frontPage = true
    ..published = true);

  setupTestLocator((locator) {
    locator.registerFactory<CourseRoutingShellInteractor>(() => interactor);
    locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());
  });

  setUp(() {
    reset(interactor);
  });

  testWidgetsWithAccessibilityChecks('syllabus type loads syllabus', (tester) async {
    final result = CourseShellData(course);
    when(interactor.loadCourseShell(CourseShellType.syllabus, any)).thenAnswer((_) => Future.value(result));

    await tester.pumpWidget(TestApp(CourseRoutingShellScreen(course.id, CourseShellType.syllabus),
        platformConfig: PlatformConfig(initWebview: true)));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()), findsOneWidget);
    expect(find.text(course.name), findsOneWidget);
    expect(find.byType(CanvasWebView), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('frontPage type loads frontPage', (tester) async {
    final result = CourseShellData(course, frontPage: page);
    when(interactor.loadCourseShell(CourseShellType.frontPage, any)).thenAnswer((_) => Future.value(result));

    await tester.pumpWidget(TestApp(CourseRoutingShellScreen(course.id, CourseShellType.frontPage),
        platformConfig: PlatformConfig(initWebview: true)));
    await tester.pumpAndSettle();

    expect(find.text(AppLocalizations().courseFrontPageLabel.toUpperCase()), findsOneWidget);
    expect(find.text(course.name), findsOneWidget);
    expect(find.byType(CanvasWebView), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('loading state displays loading indicator', (tester) async {
    Completer<CourseShellData> completer = Completer();
    when(interactor.loadCourseShell(any, any)).thenAnswer((_) => completer.future);
    await tester.pumpWidget(TestApp(CourseRoutingShellScreen(course.id, CourseShellType.syllabus)));
    await tester.pump();

    expect(find.byType(LoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows error and can refresh', (tester) async {
    when(interactor.loadCourseShell(CourseShellType.syllabus, course.id))
        .thenAnswer((_) => Future.error('This is an error'));

    await tester.pumpWidget(TestApp(CourseRoutingShellScreen(course.id, CourseShellType.syllabus)));
    await tester.pumpAndSettle();

    // Should have the error text
    expect(find.text(AppLocalizations().unexpectedError), findsOneWidget);

    // Should have the error widgets button for refresh
    final matchedWidget = find.byType(TextButton);
    expect(matchedWidget, findsOneWidget);

    // Try to refresh
    await tester.tap(matchedWidget);
    await tester.pumpAndSettle();

    verify(interactor.loadCourseShell(any, any, forceRefresh: anyNamed('forceRefresh'))).called(2); // Once for initial load, another for the refresh
  });

  testWidgetsWithAccessibilityChecks('Refresh displays loading indicator and loads state', (tester) async {
    final result = CourseShellData(course);
    when(interactor.loadCourseShell(CourseShellType.syllabus, any, forceRefresh: anyNamed('forceRefresh')))
        .thenAnswer((_) => Future.value(result));

    await tester.pumpWidget(TestApp(CourseRoutingShellScreen(course.id, CourseShellType.syllabus),
        platformConfig: PlatformConfig(initWebview: true)));
    await tester.pumpAndSettle();

    await tester.tap(find.byType(IconButton));
    await tester.pump();

    expect(find.byType(LoadingIndicator), findsOneWidget);

    await tester.pumpAndSettle();

    verify(interactor.loadCourseShell(any, any, forceRefresh: true)).called(1);
    expect(find.text(AppLocalizations().courseSyllabusLabel.toUpperCase()), findsOneWidget);
    expect(find.text(course.name), findsOneWidget);
    expect(find.byType(CanvasWebView), findsOneWidget);
    expect(find.text(AppLocalizations().unexpectedError), findsNothing);
    expect(find.byType(LoadingIndicator), findsNothing);
  });
}
