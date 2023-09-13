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
import 'package:flutter_parent/models/course_settings.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/utils/alert_helper.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import 'test_app.dart';
import 'test_helpers/mock_helpers.dart';
import 'test_helpers/mock_helpers.mocks.dart';

void main() {
  final courseApi = MockCourseApi();

  final course = Course((b) => b..settings = CourseSettings((b) => b..restrictQuantitativeData = false).toBuilder());

  final restrictedCourse = Course((b) => b..settings = CourseSettings((b) => b..restrictQuantitativeData = true).toBuilder());

  setupTestLocator((_locator) {
    _locator.registerFactory<CourseApi>(() => courseApi);
  });

  setUp(() {
    reset(courseApi);
  });

  test('filter course grade alerts if restrictQuantitativeData is true in course settings', () async {
    List<Alert> alerts = [
      Alert((b) => b
        ..id = '1'
        ..contextId = '1'
        ..alertType = AlertType.courseGradeLow
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '2'
        ..contextId = '2'
        ..alertType = AlertType.courseGradeHigh
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '3'
        ..contextId = '3'
        ..alertType = AlertType.unknown
        ..lockedForUser = false),
    ];

    final alertsHelper = AlertsHelper();

    when(courseApi.getCourse(any)).thenAnswer((_) => Future.value(restrictedCourse));

    expect((await alertsHelper.filterAlerts(alerts)), alerts.sublist(2, 3));
  });

  test('keep course grade alerts if restrictQuantitativeData is false in course settings', () async {
    List<Alert> alerts = [
      Alert((b) => b
        ..id = '1'
        ..contextId = '1'
        ..alertType = AlertType.courseGradeLow
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '2'
        ..contextId = '2'
        ..alertType = AlertType.courseGradeHigh
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '3'
        ..contextId = '3'
        ..alertType = AlertType.unknown
        ..lockedForUser = false),
    ];

    final alertsHelper = AlertsHelper();

    when(courseApi.getCourse(any)).thenAnswer((_) => Future.value(course));

    expect((await alertsHelper.filterAlerts(alerts)), alerts);
  });

  test('filter assignment grade alerts if restrictQuantitativeData is true in course settings', () async {
    List<Alert> alerts = [
      Alert((b) => b
        ..id = '1'
        ..contextId = '1'
        ..alertType = AlertType.assignmentGradeLow
        ..htmlUrl = 'https://canvas.instructure.com/courses/1/assignments/1'
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '2'
        ..contextId = '2'
        ..alertType = AlertType.assignmentGradeHigh
        ..htmlUrl = 'https://canvas.instructure.com/courses/2/assignments/2'
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '3'
        ..contextId = '3'
        ..alertType = AlertType.unknown
        ..lockedForUser = false),
    ];

    final alertsHelper = AlertsHelper();

    when(courseApi.getCourse(any)).thenAnswer((_) => Future.value(restrictedCourse));

    expect((await alertsHelper.filterAlerts(alerts)), alerts.sublist(2, 3));
  });

  test('keep assignment grade alerts if restrictQuantitativeData is false in course settings', () async {
    List<Alert> alerts = [
      Alert((b) => b
        ..id = '1'
        ..contextId = '1'
        ..alertType = AlertType.assignmentGradeLow
        ..htmlUrl = 'https://canvas.instructure.com/courses/1/assignments/1'
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '2'
        ..contextId = '2'
        ..alertType = AlertType.assignmentGradeHigh
        ..htmlUrl = 'https://canvas.instructure.com/courses/2/assignments/2'
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '3'
        ..contextId = '3'
        ..alertType = AlertType.unknown
        ..lockedForUser = false),
    ];

    final alertsHelper = AlertsHelper();

    when(courseApi.getCourse(any)).thenAnswer((_) => Future.value(course));

    expect((await alertsHelper.filterAlerts(alerts)), alerts);
  });

  test('keep non-grade alerts', () async {
    List<Alert> alerts = [
      Alert((b) => b
        ..id = '1'
        ..contextId = '1'
        ..alertType = AlertType.courseGradeHigh
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '2'
        ..contextId = '2'
        ..alertType = AlertType.assignmentGradeHigh
        ..htmlUrl = 'https://canvas.instructure.com/courses/2/assignments/2'
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '3'
        ..contextId = '3'
        ..alertType = AlertType.assignmentMissing
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '4'
        ..contextId = '4'
        ..alertType = AlertType.courseAnnouncement
        ..lockedForUser = false),
      Alert((b) => b
        ..id = '5'
        ..contextId = '5'
        ..alertType = AlertType.institutionAnnouncement
        ..lockedForUser = false),
    ];

    final alertsHelper = AlertsHelper();

    when(courseApi.getCourse(any)).thenAnswer((_) => Future.value(restrictedCourse));

    expect((await alertsHelper.filterAlerts(alerts)), alerts.sublist(2));
  });
}
