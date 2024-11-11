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

import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_settings.dart';
import 'package:flutter_parent/models/course_tab.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grading_period.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/utils/base_model.dart';
import 'package:flutter_parent/utils/core_extensions/list_extensions.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:tuple/tuple.dart';
import 'package:collection/collection.dart';

class CourseDetailsModel extends BaseModel {
  User? student;
  String courseId; // Could be routed to without a full course, only the id may be known
  Course? course;
  CourseSettings? courseSettings;
  List<CourseTab>? tabs = [];
  bool forceRefresh = true;
  GradingPeriod? _currentGradingPeriod;
  GradingPeriod? _nextGradingPeriod;

  CourseDetailsModel(this.student, this.courseId);

  // A convenience constructor when we already have the course data
  CourseDetailsModel.withCourse(this.student, this.course) : this.courseId = course!.id;

  /// Used only be the skeleton to load the course data for creating tabs and the app bar
  Future<void> loadData({bool refreshCourse = false}) {
    return work(() async {
      // Declare the futures so we can let both run asynchronously
      final courseFuture = (refreshCourse || course == null)
          ? _interactor().loadCourse(courseId, forceRefresh: refreshCourse)
          : Future.value(course);

      // Always force a refresh of tabs, it's small enough that we can do this every time
      final tabsFuture = _interactor().loadCourseTabs(courseId, forceRefresh: true);

      // Get course settings to know if course summary is allowed
      final settingsFuture = _interactor().loadCourseSettings(courseId, forceRefresh: true);

      // Await the results
      course = await courseFuture;
      tabs = await tabsFuture;
      courseSettings = await settingsFuture;

      // Set the _nextGradingPeriod to the current enrollment period (if active and if not already set)
      final enrollment =
          course?.enrollments?.firstWhereOrNull((enrollment) => enrollment.userId == student?.id);
      if (_nextGradingPeriod == null && enrollment?.hasActiveGradingPeriod() == true) {
        _nextGradingPeriod = GradingPeriod((b) => b
          ..id = enrollment?.currentGradingPeriodId
          ..title = enrollment?.currentGradingPeriodTitle);
      }
      return Future<void>.value();
    });
  }

  Future<GradeDetails> loadAssignments({bool forceRefresh = false}) async {
    if (forceRefresh) {
      course = await _interactor().loadCourse(courseId, forceRefresh: forceRefresh);
    }

    final groupFuture = _interactor()
        .loadAssignmentGroups(courseId, student?.id, _nextGradingPeriod?.id, forceRefresh: forceRefresh).then((groups) async {
      // Remove unpublished assignments to match web
      return groups?.map((group) => (group.toBuilder()..assignments.removeWhere((assignment) => !assignment.published || assignment.isHiddenInGradeBook == true)).build()).toList();
    });

    final gradingPeriodsFuture =
        _interactor().loadGradingPeriods(courseId, forceRefresh: forceRefresh).then((periods) {
      return periods?.gradingPeriods.toList();
    });

    // Get the grades for the term
    final enrollmentsFuture = _interactor()
        .loadEnrollmentsForGradingPeriod(courseId, student?.id, _nextGradingPeriod?.id, forceRefresh: forceRefresh).then((enrollments) {
      return enrollments != null && enrollments.length > 0 ? enrollments.first : null;
    }).catchError((_) => null); // Some 'legacy' parents can't read grades for students, so catch and return null

    final gradeDetails = GradeDetails(
      assignmentGroups: await groupFuture,
      gradingPeriods: await gradingPeriodsFuture,
      termEnrollment: await enrollmentsFuture,
    );

    // Set the current grading period to the next one, this way all the selected data shows at the same time
    if (_nextGradingPeriod != null) _currentGradingPeriod = _nextGradingPeriod;

    return gradeDetails;
  }

  Future<List<ScheduleItem>?> loadSummary({bool refresh = false}) async {
    // Get all assignment and calendar events
    List<List<ScheduleItem>?> results = await Future.wait([
      _interactor().loadScheduleItems(courseId, ScheduleItem.apiTypeCalendar, refresh),
      _interactor().loadScheduleItems(courseId, ScheduleItem.apiTypeAssignment, refresh),
    ]);

    // Flutter does not spin up its isolates in flutter-driver test mode, so a
    // call to compute() will never return.  So make the processSummaryItems() call
    // directly in debug builds.
    if (kDebugMode) {
      return Future(() {
        return processSummaryItems(Tuple2(results, student?.id));
      });
    } else {
      // Potentially heavy list operations going on here, so we'll use a background isolate
      return compute(processSummaryItems, Tuple2(results, student?.id));
    }
  }

  @visibleForTesting
  static List<ScheduleItem> processSummaryItems(Tuple2<List<List<ScheduleItem>?>, String?> input) {
    var results = input.item1.nonNulls;
    var studentId = input.item2;

    // Flat map to a single list
    List<ScheduleItem> items = results.expand((it) => it).toList();

    /* For assignments with one or more overrides, the API will return multiple items with the same ID - one with the
    base dates and one for each override with the override dates. If one of the overrides applies to the current student,
    we only want to keep that one. If none of the overrides apply, we only want to keep the item with the base dates. */
    var overrides = items.where((item) => item.assignmentOverrides != null).toList();
    overrides.forEach((item) {
      if (item.assignmentOverrides?.any((it) => it.studentIds.contains(studentId)) == true) {
        // This item applies to the current student. Remove all other items that have the same ID.
        items.retainWhere((it) => it == item || it.id != item.id);
      } else {
        // This item doesn't apply to the current student. Remove it from the list.
        items.remove(item);
      }
    });

    // Sort by ascending date, using a future date as a fallback so that undated items appear at the end
    // If dates match (which will be the case for undated items), then sort by title
    return items.sortBySelector([
          (it) => it?.startAt ?? it?.allDayDate,
          (it) => it?.title,
    ])?.toList().nonNulls.toList() ?? [];
  }

  CourseDetailsInteractor _interactor() => locator<CourseDetailsInteractor>();

  int tabCount() {
    if (hasHomePageAsFrontPage) return 2;
    if (hasHomePageAsSyllabus) return showSummary ? 3 : 2; // Summary is only shown with syllabus
    return 1; // Just show the grades tab
  }

  bool get hasHomePageAsFrontPage => course?.homePage == HomePage.wiki;

  bool get hasHomePageAsSyllabus =>
      course?.syllabusBody?.isNotEmpty == true &&
      (course?.homePage == HomePage.syllabus ||
          (course?.homePage != HomePage.wiki && tabs?.any((tab) => tab.id == HomePage.syllabus.name) == true));

  bool get showSummary => hasHomePageAsSyllabus && (courseSettings?.courseSummary == true);

  bool get restrictQuantitativeData => courseSettings?.restrictQuantitativeData == true;

  GradingPeriod? currentGradingPeriod() => _currentGradingPeriod;

  /// This sets the next grading period to use when loadAssignments is called. [currentGradingPeriod] won't be updated
  /// until the load call is finished, this way the grading period isn't updated in the ui until the rest of the data
  /// updates to reflect the new grading period.
  updateGradingPeriod(GradingPeriod? period) {
    _nextGradingPeriod = period;
  }
}

class GradeDetails {
  final List<AssignmentGroup>? assignmentGroups;
  final List<GradingPeriod>? gradingPeriods;
  final Enrollment? termEnrollment;

  GradeDetails({this.assignmentGroups, this.gradingPeriods, this.termEnrollment});
}
