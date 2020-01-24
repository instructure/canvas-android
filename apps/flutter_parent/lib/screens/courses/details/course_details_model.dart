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

import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grading_period.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/utils/base_model.dart';
import 'package:flutter_parent/utils/service_locator.dart';

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
