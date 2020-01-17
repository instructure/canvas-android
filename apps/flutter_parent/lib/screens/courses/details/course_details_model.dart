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
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/utils/base_model.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class CourseDetailsModel extends BaseModel {
  String studentId;
  String courseId; // Could be routed to without a full course, only the id may be known
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

      return Future<void>.value();
    });
  }

  Future<List<AssignmentGroup>> _loadAssignments({bool forceRefresh = false}) {
    final groupFuture = _interactor().loadAssignmentGroups(courseId, studentId);
    return groupFuture?.then((groups) async {
      if (groups == null || groups.isEmpty) return groups;

      // Remove unpublished assignments to match web
      return groups
          .map((group) => (group.toBuilder()..assignments.removeWhere((assignment) => !assignment.published)).build())
          .toList();
    });
  }

  CourseDetailsInteractor _interactor() => locator<CourseDetailsInteractor>();

  bool hasSyllabus() => course?.syllabusBody != null;
}
