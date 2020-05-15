// Copyright (C) 2020 - present Instructure, Inc.
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

import 'package:built_value/built_value.dart';
import 'package:flutter_student_embed/models/course.dart';
import 'package:flutter_student_embed/models/plannable.dart';
import 'package:flutter_student_embed/network/api/course_api.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/utils/service_locator.dart';

class CreateUpdateToDoScreenInteractor {
  Future<List<Course>> getCoursesForUser({bool isRefresh = false}) async {
    return await locator<CourseApi>().getCourses(forceRefresh: isRefresh);
  }

  Future<Plannable> createToDo(
    String title,
    @nullable String description,
    DateTime date,
    @nullable String courseId,
  ) {
    return locator<PlannerApi>().createPlannerNote(title, description, date, courseId);
  }

  Future<Plannable> updateToDo(
    String id,
    String title,
    @nullable String description,
    DateTime date,
    @nullable String courseId,
  ) {
    return locator<PlannerApi>().updatePlannerNote(id, title, description, date, courseId);
  }
}
