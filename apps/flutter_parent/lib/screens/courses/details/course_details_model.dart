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

import 'package:flutter/foundation.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grading_period.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/utils/base_model.dart';
import 'package:flutter_parent/utils/core_extensions/list_extensions.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:tuple/tuple.dart';

class CourseDetailsModel extends BaseModel {
  static int selectedTab = 0;
  String studentId;
  String studentName;
  String courseId; // Could be routed to without a full course, only the id may be known
  Course course;
  bool forceRefresh = true;
  GradingPeriod _currentGradingPeriod;
  GradingPeriod _nextGradingPeriod;

  CourseDetailsModel(this.studentId, this.studentName, this.courseId);

  // A convenience constructor when we already have the course data
  CourseDetailsModel.withCourse(this.studentId, this.studentName, this.course) : this.courseId = course.id;

  /// gradingPeriodId is optional, but only used if refreshAssignmentGroups is true
  Future<void> loadData({bool refreshCourse = false, bool refreshSummaryList = false}) {
    return work(() async {
      // Declare the futures so we can let both run asynchronously
      final courseFuture =
          (refreshCourse || course == null) ? _interactor().loadCourse(courseId) : Future.value(course);

      // Await the results
      course = await courseFuture;

      return Future<void>.value();
    });
  }

  Future<GradeDetails> loadAssignments({bool forceRefresh = false}) async {
    final groupFuture = _interactor()
        .loadAssignmentGroups(courseId, studentId, _nextGradingPeriod?.id, forceRefresh: forceRefresh)
        ?.then((groups) async {
      // Remove unpublished assignments to match web
      return groups
          ?.map((group) => (group.toBuilder()..assignments.removeWhere((assignment) => !assignment.published)).build())
          ?.toList();
    });

    final gradingPeriodsFuture =
        _interactor().loadGradingPeriods(courseId, forceRefresh: forceRefresh)?.then((periods) {
      return periods.gradingPeriods.toList();
    });

    // Get the grades for the term
    final enrollmentsFuture = _interactor()
        .loadEnrollmentsForGradingPeriod(courseId, studentId, _nextGradingPeriod?.id, forceRefresh: forceRefresh)
        ?.then((enrollments) {
      return enrollments.length > 0 ? enrollments.first : null;
    });

    final gradeDetails = GradeDetails(
      assignmentGroups: await groupFuture,
      gradingPeriods: await gradingPeriodsFuture,
      termEnrollment: await enrollmentsFuture,
    );

    // Set the current grading period to the next one, this way all the selected data shows at the same time
    if (_nextGradingPeriod != null) _currentGradingPeriod = _nextGradingPeriod;

    return gradeDetails;
  }

  Future<List<ScheduleItem>> loadSummary({bool refresh: false}) async {
    // Get all assignment and calendar events
    List<List<ScheduleItem>> results = await Future.wait([
      _interactor().loadScheduleItems(courseId, ScheduleItem.typeCalendar, refresh),
      _interactor().loadScheduleItems(courseId, ScheduleItem.typeAssignment, refresh),
    ]);

    // Potentially heavy list operations going on here, so we'll use a background isolate
    return compute(processSummaryItems, Tuple2(results, studentId));
  }

  @visibleForTesting
  static List<ScheduleItem> processSummaryItems(Tuple2<List<List<ScheduleItem>>, String> input) {
    var results = input.item1;
    var studentId = input.item2;

    // Flat map to a single list
    List<ScheduleItem> items = results.expand((it) => it).toList();

    /* For assignments with one or more overrides, the API will return multiple items with the same ID - one with the
    base dates and one for each override with the override dates. If one of the overrides applies to the current student,
    we only want to keep that one. If none of the overrides apply, we only want to keep the item with the base dates. */
    var overrides = items.where((item) => item.assignmentOverrides != null).toList();
    overrides.forEach((item) {
      if (item.assignmentOverrides.any((it) => it.studentIds.contains(studentId))) {
        // This item applies to the current student. Remove all other items that have the same ID.
        items.retainWhere((it) => it == item || it.id != item.id);
      } else {
        // This item doesn't apply to the current student. Remove it from the list.
        items.remove(item);
      }
    });

    // Sort by ascending date, using a future date as a fallback so that undated items appear at the end
    // If dates match (which will be the case for undated items), then sort by title
    return items.sortBy([
      (it) => it.startAt ?? it.allDayDate,
      (it) => it.title,
    ]);
  }

  CourseDetailsInteractor _interactor() => locator<CourseDetailsInteractor>();

  bool hasSyllabus() => course?.syllabusBody != null;

  GradingPeriod currentGradingPeriod() => _currentGradingPeriod;

  /// This sets the next grading period to use when loadAssignments is called. [currentGradingPeriod] won't be updated
  /// until the load call is finished, this way the grading period isn't updated in the ui until the rest of the data
  /// updates to reflect the new grading period.
  updateGradingPeriod(GradingPeriod period) {
    _nextGradingPeriod = period;
  }
}

class GradeDetails {
  final List<AssignmentGroup> assignmentGroups;
  final List<GradingPeriod> gradingPeriods;
  final Enrollment termEnrollment;

  GradeDetails({this.assignmentGroups, this.gradingPeriods, this.termEnrollment});
}
