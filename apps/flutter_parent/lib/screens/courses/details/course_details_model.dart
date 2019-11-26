//  Copyright (C) 2019 - present Instructure, Inc.
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, version 3 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/assignment.dart';

import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/utils/base_model.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class CourseDetailsModel extends BaseModel {
  int studentId;
  int courseId; // Could be routed to without a full course, only the id may be known
  Course course;
  Future<List<AssignmentGroup>> assignmentGroupFuture;

  CourseDetailsModel(this.studentId, this.courseId);

  // A convenience constructor when we already have the course data
  CourseDetailsModel.withCourse(this.studentId, this.course) : this.courseId = course.id;

  Future<void> loadData(
      {bool refreshCourse = false, bool refreshAssignmentGroups = false, bool refreshSummaryList = false}) {
    return work(() async {
      // Declare the futures so we can let both run asynchronously
      final courseFuture =
          (refreshCourse || course == null) ? _interactor().loadCourse(courseId) : Future.value(course);
      if (refreshAssignmentGroups || assignmentGroupFuture == null) assignmentGroupFuture = _loadAssignments();

      // Await the results
      course = await courseFuture;
    });
  }

  // observed_users doesn't work right now on the groups endpoint, so we have to pull submissions manually
  // TODO: Remove everything except the loadAssignmentGroups call (and removing unpublished) once LA-274 is implemented
  Future<List<AssignmentGroup>> _loadAssignments({bool forceRefresh = false}) {
//    if (true) return Future.error("fail plz");
    final groupFuture = _interactor().loadAssignmentGroups(courseId, studentId);
    return groupFuture?.then((groups) async {
      if (groups == null || groups.isEmpty) return groups;

      // Remove unpublished assignments to match web
      groups = groups.map((group) => (group.toBuilder()..assignments.removeWhere((assignment) => !assignment.published)).build()).toList();

      // The load submissions endpoint will fail for assignments that aren't published, so filter those out
      final ids = groups
          .expand((group) => group.assignments.map((assignment) => assignment.id))
          .toList();

      if (ids.isEmpty) return groups; // If there are no ids to load submissions for, just return

      final submissions = await _interactor().loadSubmissions(courseId, studentId, ids, forceRefresh: forceRefresh);

      return groups.map((group) {
        return (group.toBuilder()..assignments = _copyAssignmentsWithSubmissions(group.assignments, submissions))
            .build();
      }).toList();
    });
  }

  // TODO: Remove once LA-274 is implemented
  ListBuilder<Assignment> _copyAssignmentsWithSubmissions(
    BuiltList<Assignment> assignments,
    List<Submission> submissions,
  ) {
    final builder = assignments.toBuilder();
    builder.map((assignment) {
      final submission =
          submissions.firstWhere((submission) => submission.assignmentId == assignment.id, orElse: () => null);
      if (submission == null) return assignment;
      return (assignment.toBuilder()..submission = submission.toBuilder()).build();
    });

    return builder;
  }

  CourseDetailsInteractor _interactor() => locator<CourseDetailsInteractor>();
}
