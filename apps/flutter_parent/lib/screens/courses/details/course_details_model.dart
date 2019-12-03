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
  int studentId;
  int courseId; // Could be routed to without a full course, only the id may be known
  Course course;
  List<AssignmentGroup> assignmentGroups;

  CourseDetailsModel(this.studentId, this.courseId, {this.assignmentGroups});

  // A convenience constructor when we already have the course data
  CourseDetailsModel.withCourse(this.studentId, this.course, {this.assignmentGroups}) : this.courseId = course.id;

  Future<void> loadData({bool refreshCourse = false, bool refreshAssignments = false}) {
    return work(() async {
      // Declare the futures so we can let both run asynchronously
      final courseFuture =
          (refreshCourse || course == null) ? _interactor().loadCourse(courseId) : Future.value(course);
      final assignmentsFuture = (refreshAssignments || assignmentGroups == null)
          ? _interactor().loadAssignmentGroups(courseId, studentId)
          : Future.value(assignmentGroups);

      // Await the results
      course = await courseFuture;
      assignmentGroups = await assignmentsFuture;
    });
  }

  CourseDetailsInteractor _interactor() => locator<CourseDetailsInteractor>();
}
