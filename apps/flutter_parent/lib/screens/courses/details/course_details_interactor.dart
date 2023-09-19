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
import 'package:flutter_parent/models/canvas_page.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_settings.dart';
import 'package:flutter_parent/models/course_tab.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grading_period_response.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/page_api.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class CourseDetailsInteractor {
  Future<Course?> loadCourse(String courseId, {bool forceRefresh = false}) {
    return locator<CourseApi>().getCourse(courseId, forceRefresh: forceRefresh);
  }

  Future<List<CourseTab>?> loadCourseTabs(String courseId, {bool forceRefresh = false}) =>
      locator<CourseApi>().getCourseTabs(courseId, forceRefresh: forceRefresh);

  Future<CourseSettings?> loadCourseSettings(String courseId, {bool forceRefresh = false}) =>
      locator<CourseApi>().getCourseSettings(courseId, forceRefresh: forceRefresh);

  Future<List<AssignmentGroup>?> loadAssignmentGroups(String courseId, String? studentId, String? gradingPeriodId,
      {bool forceRefresh = false}) {
    return locator<AssignmentApi>().getAssignmentGroupsWithSubmissionsDepaginated(courseId, studentId, gradingPeriodId,
        forceRefresh: forceRefresh);
  }

  Future<GradingPeriodResponse?> loadGradingPeriods(String courseId, {bool forceRefresh = false}) {
    return locator<CourseApi>().getGradingPeriods(courseId, forceRefresh: forceRefresh);
  }

  Future<List<Enrollment>?> loadEnrollmentsForGradingPeriod(String courseId, String? studentId, String? gradingPeriodId,
      {bool forceRefresh = false}) {
    return locator<EnrollmentsApi>()
        .getEnrollmentsByGradingPeriod(courseId, studentId, gradingPeriodId, forceRefresh: forceRefresh);
  }

  Future<List<ScheduleItem>?> loadScheduleItems(String courseId, String type, bool refresh) {
    return locator<CalendarEventsApi>().getAllCalendarEvents(
      allEvents: true,
      type: type,
      contexts: ['course_$courseId'],
      forceRefresh: refresh,
    );
  }

  Future<CanvasPage?> loadFrontPage(String courseId, {bool forceRefresh = false}) =>
      locator<PageApi>().getCourseFrontPage(courseId, forceRefresh: forceRefresh);
}
