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

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/assignment_override.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_settings.dart';
import 'package:flutter_parent/models/course_tab.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grading_period.dart';
import 'package:flutter_parent/models/grading_period_response.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';
import 'package:tuple/tuple.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

const _studentId = '123';
const _studentName = 'billy jean';
const _courseId = '321';

final _student = User((b) => b
  ..id = _studentId
  ..name = _studentName);

final _course = Course((b) => b..id = _courseId);

void main() {
  final interactor = MockCourseDetailsInteractor();

  setupTestLocator((locator) {
    locator.registerFactory<CourseDetailsInteractor>(() => interactor);
  });

  setUp(() {
    reset(interactor);
  });

  test('constructing with a course updates the course id', () {
    final model = CourseDetailsModel.withCourse(_student, _course);

    expect(model.courseId, _courseId);
  });

  group('loadData for course', () {
    test('does not refresh course if it has data', () async {
      final model = CourseDetailsModel.withCourse(_student, _course);

      await model.loadData();

      verifyNever(interactor.loadCourse(_courseId));
      expect(model.course, _course);
    });

    test('refreshes course if course refresh forced', () async {
      final expected = null;
      when(interactor.loadCourse(_courseId, forceRefresh: true)).thenAnswer((_) => Future.value(expected));
      final model = CourseDetailsModel.withCourse(_student, _course);

      await model.loadData(refreshCourse: true);

      verify(interactor.loadCourse(_courseId, forceRefresh: true)).called(1);
      expect(model.course, expected);
    });

    test('refreshes course if course is null', () async {
      final expected = null;
      when(interactor.loadCourse(_courseId)).thenAnswer((_) => Future.value(expected));
      final model = CourseDetailsModel(_student, _courseId);

      await model.loadData();

      verify(interactor.loadCourse(_courseId)).called(1);
      expect(model.course, expected);
    });

    test('sets grading period to enrollments active period', () async {
      final gradingPeriodId = '101';
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..currentGradingPeriodId = '101'
        ..multipleGradingPeriodsEnabled = true
        ..userId = _studentId);
      final course = _course.rebuild((b) => b..enrollments = ListBuilder([enrollment]));
      final model = CourseDetailsModel.withCourse(_student, course);

      await model.loadData();
      await model.loadAssignments();

      verify(interactor.loadAssignmentGroups(_courseId, _studentId, gradingPeriodId)).called(1);
    });

    test('does not set grading period when enrollment has no active period', () async {
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..currentGradingPeriodId = null
        ..multipleGradingPeriodsEnabled = true
        ..userId = _studentId);
      final course = _course.rebuild((b) => b..enrollments = ListBuilder([enrollment]));
      final model = CourseDetailsModel.withCourse(_student, course);

      await model.loadData();
      await model.loadAssignments();

      verify(interactor.loadAssignmentGroups(_courseId, _studentId, null)).called(1);
    });

    test('does not set grading period to enrollments active period if already set', () async {
      final gradingPeriodId = '101';
      final badGradingPeriodId = '202';
      final enrollment = Enrollment((b) => b
        ..enrollmentState = 'active'
        ..currentGradingPeriodId = '101'
        ..multipleGradingPeriodsEnabled = true
        ..userId = _studentId);
      final course = _course.rebuild((b) => b..enrollments = ListBuilder([enrollment]));
      final model = CourseDetailsModel.withCourse(_student, course);

      when(interactor.loadCourse(_courseId)).thenAnswer((_) async => course.rebuild((b) =>
          b..enrollments = ListBuilder([enrollment.rebuild((e) => e..currentGradingPeriodId = badGradingPeriodId)])));
      await model.loadData();
      await model.loadData(refreshCourse: true);
      await model.loadAssignments();

      verify(interactor.loadAssignmentGroups(_courseId, _studentId, gradingPeriodId)).called(1);
      verifyNever(interactor.loadAssignmentGroups(_courseId, _studentId, badGradingPeriodId));
    });
  });

  group('loadAssignments', () {
    test('returns grade details', () async {
      // Initial setup
      final termEnrollment = Enrollment((b) => b
        ..id = '10'
        ..enrollmentState = 'active');
      final gradingPeriods = [
        GradingPeriod((b) => b
          ..id = '123'
          ..title = 'Grade Period 1')
      ];
      final assignmentGroups = [
        AssignmentGroup((b) => b
          ..id = '111'
          ..name = 'Assignment Group 1')
      ];

      // Mock the data
      when(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => [termEnrollment, termEnrollment.rebuild((b) => b..id = '20')]);
      when(interactor.loadGradingPeriods(_courseId)).thenAnswer(
          (_) async => GradingPeriodResponse((b) => b..gradingPeriods = BuiltList.of(gradingPeriods).toBuilder()));
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => assignmentGroups);

      // Make the call to test
      final model = CourseDetailsModel.withCourse(_student, _course);
      final gradeDetails = await model.loadAssignments();

      expect(gradeDetails.termEnrollment, termEnrollment); // Should match only the first enrollment
      expect(gradeDetails.gradingPeriods, gradingPeriods);
      expect(gradeDetails.assignmentGroups, assignmentGroups);
    });

    test('Does not fail with an empty group response', () async {
      // Mock the data with null response
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => Future.value(null));

      // Make the call to test
      final model = CourseDetailsModel.withCourse(_student, _course);
      var gradeDetails = await model.loadAssignments();

      expect(gradeDetails.assignmentGroups, null);

      // Test again with empty array
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => []);
      gradeDetails = await model.loadAssignments();

      expect(gradeDetails.assignmentGroups, []);
    });

    test('Removes unpublished assignments from assignment groups', () async {
      // Init setup
      final publishedAssignments = [
        Assignment((b) => b
          ..id = '101'
          ..courseId = _courseId
          ..assignmentGroupId = '111'
          ..position = 0
          ..published = true)
      ];
      final unpublishedAssignments = [
        Assignment((b) => b
          ..id = '102'
          ..courseId = _courseId
          ..assignmentGroupId = '222'
          ..position = 0)
      ];
      final publishedGroup = AssignmentGroup((b) => b
        ..id = '111'
        ..name = 'Group 1'
        ..assignments = BuiltList.of(publishedAssignments).toBuilder());
      final unpublishedGroup = AssignmentGroup((b) => b
        ..id = '222'
        ..name = 'Group 2'
        ..assignments = BuiltList.of(unpublishedAssignments).toBuilder());

      final assignmentGroups = [
        publishedGroup,
        unpublishedGroup,
      ];

      // Mock the data with null response
      when(interactor.loadAssignmentGroups(_courseId, _studentId, null, forceRefresh: false))
          .thenAnswer((_) async => assignmentGroups);

      // Make the call to test
      final model = CourseDetailsModel.withCourse(_student, _course);
      final gradeDetails = await model.loadAssignments();

      expect(gradeDetails.assignmentGroups, [
        publishedGroup,
        unpublishedGroup.rebuild((b) => b..assignments = BuiltList.of(<Assignment>[]).toBuilder())
      ]);
    });

    test('Updates currentGradingPeriod when load finishes', () async {
      // Init setup
      final gradingPeriod = GradingPeriod((b) => b
        ..id = '1'
        ..title = 'Period 1');

      // Create the model
      final model = CourseDetailsModel.withCourse(_student, _course);

      // Update the grading period, but it shouldn't percolate until a load is called
      model.updateGradingPeriod(gradingPeriod);
      expect(model.currentGradingPeriod(), null);

      await model.loadAssignments();
      expect(model.currentGradingPeriod(), gradingPeriod);

      // Verify the updated grading period was used in api calls
      verify(interactor.loadAssignmentGroups(_courseId, _studentId, gradingPeriod.id, forceRefresh: false)).called(1);
      verify(interactor.loadEnrollmentsForGradingPeriod(_courseId, _studentId, gradingPeriod.id, forceRefresh: false))
          .called(1);
    });
  });

  group('Load summary items', () {
    test('loadSummary calls interactor with correct parameters', () async {
      // Mock the data
      final itemA = ScheduleItem((s) => s..title = 'A');
      final itemB = ScheduleItem((s) => s..title = 'B');

      when(interactor.loadScheduleItems(_courseId, ScheduleItem.apiTypeCalendar, any)).thenAnswer((_) async => [itemA]);
      when(interactor.loadScheduleItems(_courseId, ScheduleItem.apiTypeAssignment, any))
          .thenAnswer((_) async => [itemB]);

      // Use the model
      final model = CourseDetailsModel.withCourse(_student, _course);

      final expected = [itemA, itemB];
      final actual = await model.loadSummary(refresh: true);

      expect(actual, expected);
      verify(interactor.loadScheduleItems(_courseId, ScheduleItem.apiTypeCalendar, true));
      verify(interactor.loadScheduleItems(_courseId, ScheduleItem.apiTypeAssignment, true));
    });
  });

  group('Process summary items', () {
    test('processSummaryItems sorts by date', () {
      List<ScheduleItem> sourceItems = [
        ScheduleItem((s) => s..startAt = DateTime.now()),
        ScheduleItem((s) => s..startAt = DateTime.now().subtract(Duration(days: 2))),
        ScheduleItem((s) => s..startAt = DateTime.now().subtract(Duration(days: 1))),
        ScheduleItem((s) => s..startAt = DateTime.now().subtract(Duration(days: 3))),
      ];

      var expected = [sourceItems[3], sourceItems[1], sourceItems[2], sourceItems[0]];
      var actual = CourseDetailsModel.processSummaryItems(Tuple2([sourceItems], ''));

      expect(actual, expected);
    });

    test('processSummaryItems sorts by title if items are undated', () {
      List<ScheduleItem> sourceItems = [
        ScheduleItem((s) => s..title = 'D'),
        ScheduleItem((s) => s..title = 'B'),
        ScheduleItem((s) => s..title = 'C'),
        ScheduleItem((s) => s..title = 'A'),
      ];

      var expected = [sourceItems[3], sourceItems[1], sourceItems[2], sourceItems[0]];
      var actual = CourseDetailsModel.processSummaryItems(Tuple2([sourceItems], ''));

      expect(actual, expected);
    });

    test('processSummaryItems places undated items at end', () {
      List<ScheduleItem> sourceItems = [
        ScheduleItem((s) => s..title = 'C'),
        ScheduleItem((s) => s
          ..title = 'B'
          ..startAt = DateTime.now()),
        ScheduleItem((s) => s..title = 'A'),
        ScheduleItem((s) => s
          ..title = 'D'
          ..startAt = DateTime.now().subtract(Duration(days: 1))),
      ];

      var expected = [sourceItems[3], sourceItems[1], sourceItems[2], sourceItems[0]];
      var actual = CourseDetailsModel.processSummaryItems(Tuple2([sourceItems], ''));

      expect(actual, expected);
    });

    test('processSummaryItems combines multiple sources', () {
      List<ScheduleItem> sourceItems1 = [
        ScheduleItem((s) => s..title = 'A'),
        ScheduleItem((s) => s..title = 'B'),
      ];
      List<ScheduleItem> sourceItems2 = [
        ScheduleItem((s) => s..title = 'C'),
        ScheduleItem((s) => s..title = 'D'),
      ];

      var expected = [sourceItems1[0], sourceItems1[1], sourceItems2[0], sourceItems2[1]];
      var actual = CourseDetailsModel.processSummaryItems(Tuple2([sourceItems1, sourceItems2], ''));

      expect(actual, expected);
    });

    test('processSummaryItems keeps duplicate item with override that matches student id', () {
      String studentId = 'student1';
      List<ScheduleItem> sourceItems = [
        ScheduleItem((s) => s
          ..id = 'assignment_a'
          ..title = 'A'),
        ScheduleItem((s) => s
          ..id = 'assignment_a'
          ..title = 'A'
          ..assignmentOverrides = ListBuilder([
            AssignmentOverride((o) => o..studentIds = ListBuilder([studentId]))
          ])),
        ScheduleItem((s) => s
          ..id = 'assignment_a'
          ..title = 'A'
          ..assignmentOverrides = ListBuilder([
            AssignmentOverride((o) => o..studentIds = ListBuilder(['student2']))
          ])),
        ScheduleItem((s) => s
          ..id = 'assignment_b'
          ..title = 'B'),
      ];

      var expected = [sourceItems[1], sourceItems[3]];
      var actual = CourseDetailsModel.processSummaryItems(Tuple2([sourceItems], studentId));

      expect(actual, expected);
    });

    test('processSummaryItems only keeps base item if no overrides match the student id', () {
      String studentId = 'student1';
      List<ScheduleItem> sourceItems = [
        ScheduleItem((s) => s
          ..id = 'assignment_a'
          ..title = 'A'),
        ScheduleItem((s) => s
          ..id = 'assignment_a'
          ..title = 'A'
          ..assignmentOverrides = ListBuilder([
            AssignmentOverride((o) => o..studentIds = ListBuilder(['student2']))
          ])),
        ScheduleItem((s) => s
          ..id = 'assignment_a'
          ..title = 'A'
          ..assignmentOverrides = ListBuilder([
            AssignmentOverride((o) => o..studentIds = ListBuilder(['student3']))
          ])),
        ScheduleItem((s) => s
          ..id = 'assignment_b'
          ..title = 'B'),
      ];

      var expected = [sourceItems[0], sourceItems[3]];
      var actual = CourseDetailsModel.processSummaryItems(Tuple2([sourceItems], studentId));

      expect(actual, expected);
    });
  });

  group('tab count', () {
    test('returns 1 when no home page or syllabus', () {
      final course = _course.rebuild((b) => b
        ..syllabusBody = null
        ..homePage = null);
      final model = CourseDetailsModel.withCourse(_student, course);

      expect(model.tabCount(), 1);
    });

    test('returns 2 when home page is front page', () {
      final course = _course.rebuild((b) => b
        ..syllabusBody = 'body'
        ..homePage = HomePage.wiki);
      final model = CourseDetailsModel.withCourse(_student, course);

      expect(model.tabCount(), 2);
    });

    test('returns 3 when home page is syllabus', () {
      final course = _course.rebuild((b) => b
        ..syllabusBody = 'body'
        ..homePage = HomePage.syllabus);
      final model = CourseDetailsModel.withCourse(_student, course);
      model.courseSettings = CourseSettings((b) => b..courseSummary = true);

      expect(model.tabCount(), 3);
    });

    test('returns 3 when home page is not front page with a valid syllabus', () async {
      final course = _course.rebuild((b) => b
        ..syllabusBody = 'body'
        ..homePage = null);
      final model = CourseDetailsModel.withCourse(_student, course);

      when(interactor.loadCourseTabs(_courseId, forceRefresh: true)).thenAnswer((_) async => [
            CourseTab((b) => b..id = HomePage.syllabus.name),
          ]);
      when(interactor.loadCourseSettings(_courseId, forceRefresh: true)).thenAnswer((_) async {
        return CourseSettings((b) => b..courseSummary = true);
      });
      await model.loadData();

      expect(model.tabCount(), 3);
    });

    test('returns 1 when no home page and syllabus tab is not visible', () async {
      final course = _course.rebuild((b) => b
        ..syllabusBody = 'body'
        ..homePage = null);
      final model = CourseDetailsModel.withCourse(_student, course);

      when(interactor.loadCourseTabs(_courseId, forceRefresh: true)).thenAnswer((_) async => [
            CourseTab((b) => b..id = HomePage.assignments.name),
          ]);
      await model.loadData();

      expect(model.tabCount(), 1);
    });
  });
  group('tab visibility', () {
    test('returns syllabus and summary if the course has a syllabus home page', () {
      final course = _course.rebuild((b) => b
        ..syllabusBody = 'body'
        ..homePage = HomePage.syllabus);
      final model = CourseDetailsModel.withCourse(_student, course);
      model.courseSettings = CourseSettings((b) => b..courseSummary = true);

      expect(model.hasHomePageAsSyllabus, true);
      expect(model.showSummary, true);
      expect(model.hasHomePageAsFrontPage, false);
    });

    test('returns syllabus but no summary summary if summary is disabled in course settings', () {
      final course = _course.rebuild((b) => b
        ..syllabusBody = 'body'
        ..homePage = HomePage.syllabus);
      final model = CourseDetailsModel.withCourse(_student, course);
      model.courseSettings = CourseSettings((b) => b..courseSummary = false);

      expect(model.hasHomePageAsSyllabus, true);
      expect(model.showSummary, false);
      expect(model.hasHomePageAsFrontPage, false);
    });

    test('returns syllabus and summary if the course has a syllabus without a home page', () async {
      final course = _course.rebuild((b) => b
        ..syllabusBody = 'body'
        ..homePage = null);
      final model = CourseDetailsModel.withCourse(_student, course);

      when(interactor.loadCourseTabs(_courseId, forceRefresh: true)).thenAnswer((_) async => [
            CourseTab((b) => b..id = HomePage.syllabus.name),
          ]);
      when(interactor.loadCourseSettings(_courseId, forceRefresh: true)).thenAnswer((_) async {
        return CourseSettings((b) => b..courseSummary = true);
      });
      await model.loadData();

      expect(model.hasHomePageAsSyllabus, true);
      expect(model.showSummary, true);
      expect(model.hasHomePageAsFrontPage, false);
    });

    test('returns false for all if the course has a syllabus without a syllabus tab', () async {
      final course = _course.rebuild((b) => b
        ..syllabusBody = 'body'
        ..homePage = null);
      final model = CourseDetailsModel.withCourse(_student, course);

      when(interactor.loadCourseTabs(_courseId, forceRefresh: true)).thenAnswer((_) async => []);
      await model.loadData();

      expect(model.hasHomePageAsSyllabus, false);
      expect(model.showSummary, false);
      expect(model.hasHomePageAsFrontPage, false);
    });

    test('returns front page if the course has a home page page', () {
      final course = _course.rebuild((b) => b..homePage = HomePage.wiki);
      final model = CourseDetailsModel.withCourse(_student, course);

      expect(model.hasHomePageAsFrontPage, true);
      expect(model.hasHomePageAsSyllabus, false);
      expect(model.showSummary, false);
    });

    test('returns front page if the course has a home page page and a syllabus', () {
      final course = _course.rebuild((b) => b
        ..homePage = HomePage.wiki
        ..syllabusBody = 'body');
      final model = CourseDetailsModel.withCourse(_student, course);

      expect(model.hasHomePageAsFrontPage, true);
      expect(model.hasHomePageAsSyllabus, false);
      expect(model.showSummary, false);
    });

    test('returns false for all if the course does not have a home page page nor a syllabus body', () {
      final course = _course.rebuild((b) => b
        ..homePage = null
        ..syllabusBody = null);
      final model = CourseDetailsModel.withCourse(_student, course);

      expect(model.hasHomePageAsFrontPage, false);
      expect(model.hasHomePageAsSyllabus, false);
      expect(model.showSummary, false);
    });

    test('returns false for all if the course is null', () {
      final model = CourseDetailsModel(_student, _courseId);

      expect(model.hasHomePageAsFrontPage, false);
      expect(model.hasHomePageAsSyllabus, false);
      expect(model.showSummary, false);
    });
  });
}
