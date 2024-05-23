// Copyright (C) 2023 - present Instructure, Inc.
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

import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AlertsHelper {
  Future<List<Alert>?> filterAlerts(List<Alert>? list) async {
    List<Alert> filteredList = [];
    if (list == null) return null;
    for (var element in list) {
      var courseId = element.getCourseIdForGradeAlerts();
      if (courseId == null) {
        filteredList.add(element);
      } else {
        Course? course = await locator<CourseApi>().getCourse(courseId, forceRefresh: false);
        if (!(course?.settings?.restrictQuantitativeData ?? false)) {
          filteredList.add(element);
        }
      }
    }
    return filteredList;
  }
}
