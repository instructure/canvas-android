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

import 'package:built_collection/built_collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/screens/courses/courses_screen.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_screen.dart';
import 'package:flutter_parent/screens/dashboard/selected_student_notifier.dart';
import 'package:flutter_parent/utils/common_widgets/empty_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';
import 'package:provider/provider.dart';

import '../../utils/accessibility_utils.dart';
import '../../utils/platform_config.dart';
import '../../utils/test_app.dart';

void main() {
  AppLocalizations l10n = AppLocalizations();

  _setupLocator(CoursesInteractor mockInteractor, {SelectedStudentNotifier notifier}) {
    final _locator = GetIt.instance;
    _locator.reset();
    _locator.registerFactory<CoursesInteractor>(() => mockInteractor);
    _locator.registerFactory<CourseDetailsInteractor>(() => _MockCourseDetailsInteractor());
    _locator.registerFactory<AssignmentApi>(() => _MockAssignmentApi());
    _locator.registerFactory<CourseApi>(() => _MockCourseApi());
    _locator.registerFactory<QuickNav>(() => QuickNav());
    _locator.registerLazySingleton<Analytics>(() => Analytics());

    _locator.registerLazySingleton<SelectedStudentNotifier>(() => notifier ?? SelectedStudentNotifier());
  }

  Widget _testableMaterialWidget({Widget widget, highContrast = false, SelectedStudentNotifier notifier = null}) =>
      TestApp(
        ChangeNotifierProvider<SelectedStudentNotifier>(
            create: (context) => notifier ?? SelectedStudentNotifier()
              ..update(_mockStudent('1')),
            child: Consumer<SelectedStudentNotifier>(
              builder: (context, model, _) {
                return Scaffold(body: widget ?? CoursesScreen());
              },
            )),
        highContrast: highContrast,
        platformConfig: PlatformConfig(mockPrefs: null),
      );

  group('Render', () {
    testWidgetsWithAccessibilityChecks('shows loading indicator when retrieving courses', (tester) async {
      _setupLocator(_MockedCoursesInteractor());

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pump();

      final loadingWidget = find.byType(CircularProgressIndicator);
      expect(loadingWidget, findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('does not show loading when courses are loaded', (tester) async {
      var student = _mockStudent('1');
      var courses = generateCoursesForStudent(student.id);

      _setupLocator(_MockedCoursesInteractor(courses: courses));

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      final loadingWidget = find.byType(CircularProgressIndicator);
      expect(loadingWidget, findsNothing);
    });

    testWidgetsWithAccessibilityChecks('shows courses after load', (tester) async {
      var student = _mockStudent('1');
      var courses = generateCoursesForStudent(student.id);

      _setupLocator(_MockedCoursesInteractor(courses: courses));

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      final listTileWidget = find.byType(ListTile);
      expect(listTileWidget, findsNWidgets(courses.length));
    });

    testWidgetsWithAccessibilityChecks('shows empty message after load', (tester) async {
      _setupLocator(_MockedCoursesInteractor(courses: []));

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      expect(find.text(l10n.noCoursesTitle), findsOneWidget);
      expect(find.text(l10n.noCoursesMessage), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Shows error state and performs refresh', (tester) async {
      var interactor = _MockedCoursesInteractor(courses: []);

      _setupLocator(interactor);

      interactor.error = true;

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      expect(find.byType(ErrorPandaWidget), findsOneWidget);
      expect(find.text(l10n.errorLoadingCourses), findsOneWidget);

      interactor.error = false;
      await tester.tap(find.text(l10n.retry));
      await tester.pumpAndSettle();

      expect(find.text(l10n.noCoursesTitle), findsOneWidget);
      expect(find.text(l10n.noCoursesMessage), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('shows no grade if there is no current grade', (tester) async {
      var student = _mockStudent('1');
      var courses = generateCoursesForStudent(student.id);

      _setupLocator(_MockedCoursesInteractor(courses: courses));

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      final gradeWidget = find.text(l10n.noGrade);
      expect(gradeWidget, findsNWidgets(courses.length));
    });

    testWidgetsWithAccessibilityChecks('shows grade if there is a current grade', (tester) async {
      var student = _mockStudent('1');
      var courses = List.generate(
        1,
        (idx) => _mockCourse(
          idx.toString(),
          enrollments: ListBuilder<Enrollment>(
            [_mockEnrollment(idx.toString(), userId: student.id, computedCurrentGrade: 'A')],
          ),
        ),
      );

      _setupLocator(_MockedCoursesInteractor(courses: courses));

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      final gradeWidget = find.text('A');
      expect(gradeWidget, findsNWidgets(courses.length));
    });

    testWidgetsWithAccessibilityChecks('shows score if there is a grade but no grade string', (tester) async {
      var student = _mockStudent('1');
      var courses = List.generate(
        1,
        (idx) => _mockCourse(
          idx.toString(),
          enrollments: ListBuilder<Enrollment>(
            [_mockEnrollment(idx.toString(), userId: student.id, computedCurrentScore: 90)],
          ),
        ),
      );

      _setupLocator(_MockedCoursesInteractor(courses: courses));

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      final gradeWidget = find.text('90%');
      expect(gradeWidget, findsNWidgets(courses.length));
    });

    testWidgetsWithAccessibilityChecks('only shows courses for selected user', (tester) async {
      var numCourses1 = 3;
      var numCourses2 = 1;
      var student1 = _mockStudent('1');
      var student2 = _mockStudent('2');
      var courses1 = generateCoursesForStudent(student1.id, numberOfCourses: numCourses1);
      var courses2 = generateCoursesForStudent(student2.id, numberOfCourses: numCourses2);

      SelectedStudentNotifier notifier = SelectedStudentNotifier();
      _setupLocator(_MockedCoursesInteractor(courses: courses1..addAll(courses2)), notifier: notifier);

      await tester.pumpWidget(_testableMaterialWidget(notifier: notifier));
      await tester.pumpAndSettle();

      // First student will be selected, verify that we show their courses
      final listTileWidget = find.byType(ListTile);
      expect(listTileWidget, findsNWidgets(numCourses1));

      // Select second student
      notifier.update(student2);
      await tester.pump();
      await tester.pumpAndSettle();

      // Check for the courses of the second student
      expect(listTileWidget, findsNWidgets(numCourses2));
    });

    testWidgetsWithAccessibilityChecks('updates courses when selected user changes', (tester) async {
      var numCourses1 = 3;
      var numCourses2 = 1;
      var student1 = _mockStudent('1');
      var student2 = _mockStudent('2');
      var courses1 = generateCoursesForStudent(student1.id, numberOfCourses: numCourses1);
      var courses2 = generateCoursesForStudent(student2.id, numberOfCourses: numCourses2);

      SelectedStudentNotifier notifier = SelectedStudentNotifier();
      var interactor = _MockCoursesInteractor();
      when(interactor.getCourses(isRefresh: anyNamed('isRefresh')))
          .thenAnswer((_) => Future.value([...courses1, ...courses2]));

      _setupLocator(interactor, notifier: notifier);

      await tester.pumpWidget(_testableMaterialWidget(notifier: notifier));
      await tester.pumpAndSettle();

      // First student will be selected, verify that we show their courses
      expect(find.byType(ListTile), findsNWidgets(numCourses1));

      // Select second student
      notifier.update(student2);
      await tester.pumpAndSettle();

      // Check for the courses of the second student
      expect(find.byType(ListTile), findsNWidgets(numCourses2));

      // Verify that we called getCourses twice, once on initial load and again when we switched students
      verify(interactor.getCourses(isRefresh: true)).called(2);
    });
  });

  group('Interaction', () {
    testWidgetsWithAccessibilityChecks('Pulls to refresh', (tester) async {
      var student = _mockStudent('1');
      var courses = generateCoursesForStudent(student.id);
      var interactor = _MockedCoursesInteractor(courses: []);

      _setupLocator(interactor);

      await tester.pumpWidget(_testableMaterialWidget());
      await tester.pumpAndSettle();

      expect(find.byType(EmptyPandaWidget), findsOneWidget);

      interactor.courses = courses;
      await tester.drag(find.byType(RefreshIndicator), Offset(0, 300));
      await tester.pumpAndSettle();

      expect(find.byType(EmptyPandaWidget), findsNothing);
      expect(find.byType(ListTile), findsNWidgets(courses.length));
    });

    testWidgetsWithAccessibilityChecks('launches course detail screen when tapping on a course', (tester) async {
      var student = _mockStudent('1');
      await setupPlatformChannels(
          config: PlatformConfig(mockPrefs: {ApiPrefs.KEY_CURRENT_STUDENT: json.encode(serialize(student))}));
      var courses = List.generate(
        1,
        (idx) => _mockCourse(
          idx.toString(),
          enrollments: ListBuilder<Enrollment>(
            [_mockEnrollment(idx.toString(), userId: student.id, computedCurrentScore: 90)],
          ),
        ),
      );

      _setupLocator(_MockedCoursesInteractor(courses: courses));

      await tester.pumpWidget(_testableMaterialWidget(highContrast: true));
      await tester.pumpAndSettle();

      final matchedWidget = find.text(courses.first.name);
      expect(matchedWidget, findsOneWidget);
      await tester.tap(matchedWidget);
      await tester.pumpAndSettle();

      expect(find.byType(CourseDetailsScreen), findsOneWidget);

      ApiPrefs.clean();
    });
  });
}

class _MockedCoursesInteractor extends CoursesInteractor {
  List<Course> courses;

  bool error = false;

  _MockedCoursesInteractor({this.courses});

  @override
  Future<List<Course>> getCourses({bool isRefresh = false}) async {
    if (error) throw '';
    return courses ?? [_mockCourse('1')];
  }
}

class _MockCoursesInteractor extends Mock implements CoursesInteractor {}

class _MockCourseDetailsInteractor extends CourseDetailsInteractor {}

class _MockAssignmentApi extends Mock implements AssignmentApi {}

class _MockCourseApi extends Mock implements CourseApi {}

List<Course> generateCoursesForStudent(String userId, {int numberOfCourses = 1}) {
  var student = _mockStudent(userId);
  return List.generate(
    numberOfCourses,
    (idx) => _mockCourse(
      idx.toString(),
      enrollments: ListBuilder<Enrollment>([_mockEnrollment(idx.toString(), userId: student.id)]),
    ),
  );
}

Enrollment _mockEnrollment(
  String courseId, {
  String userId = '0',
  String computedCurrentGrade,
  double computedCurrentScore,
}) =>
    Enrollment((b) => b
      ..courseId = courseId
      ..userId = userId
      ..courseSectionId = '0'
      ..enrollmentState = ''
      ..computedCurrentGrade = computedCurrentGrade
      ..computedCurrentScore = computedCurrentScore
      ..build());

Course _mockCourse(String courseId,
        {ListBuilder<Enrollment> enrollments, bool hasActiveGradingPeriod, double currentScore, String currentGrade}) =>
    Course((b) => b
      ..id = courseId
      ..name = 'CourseName'
      ..imageDownloadUrl = ''
      ..enrollments = enrollments ?? ListBuilder<Enrollment>([_mockEnrollment('0')])
      ..hasGradingPeriods = hasActiveGradingPeriod ?? false
      ..currentScore = currentScore
      ..currentGrade = currentGrade
      ..build());

User _mockStudent(String userId) => User((b) => b
  ..id = userId
  ..name = 'UserName'
  ..sortableName = 'Sortable Name'
  ..build());
