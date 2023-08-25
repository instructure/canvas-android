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
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';
import '../../utils/test_helpers/mock_helpers.dart';
import '../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final assignmentId = '123';
  final courseId = '321';
  final studentId = '1337';

  final assignmentApi = MockAssignmentApi();
  final courseApi = MockCourseApi();
  final reminderDb = MockReminderDb();
  final notificationUtil = MockNotificationUtil();
  final login = Login((b) => b
    ..domain = 'test-domain'
    ..user = User((u) => u..id = '123').toBuilder());

  setupTestLocator((locator) {
    locator.registerLazySingleton<AssignmentApi>(() => assignmentApi);
    locator.registerLazySingleton<CourseApi>(() => courseApi);
    locator.registerLazySingleton<ReminderDb>(() => reminderDb);
    locator.registerLazySingleton<NotificationUtil>(() => notificationUtil);
  });

  // Reset the interactions for the shared mocks
  setUp(() async {
    reset(assignmentApi);
    reset(courseApi);
    reset(reminderDb);
    reset(notificationUtil);
    await setupPlatformChannels();
    await ApiPrefs.switchLogins(login);
  });

  group('loadAssignmentDetails', () {
    test('returns the course name', () async {
      final course = Course((b) => b..name = 'course name');
      when(courseApi.getCourse(courseId)).thenAnswer((_) async => course);
      final details =
          await AssignmentDetailsInteractor().loadAssignmentDetails(false, courseId, assignmentId, studentId);

      expect(details?.course, course);
    });

    test('loadReminder calls ReminderDb', () async {
      final assignmentId = 'assignment-123';
      when(reminderDb.getByItem(any, any, any, any)).thenAnswer((_) async => null);

      await AssignmentDetailsInteractor().loadReminder(assignmentId);

      verify(reminderDb.getByItem(login.domain, login.user.id, Reminder.TYPE_ASSIGNMENT, assignmentId));
    });

    test('loadReminder returns reminder if reminder date has not passed', () async {
      final expected = Reminder((b) => b..date = DateTime.now().add(Duration(minutes: 1)));
      when(reminderDb.getByItem(any, any, any, any)).thenAnswer((_) async => expected);

      final actual = await AssignmentDetailsInteractor().loadReminder('');

      expect(actual, expected);
    });

    test('loadReminder returns null if none exist for the assignment', () async {
      when(reminderDb.getByItem(any, any, any, any)).thenAnswer((_) async => null);

      final actual = await AssignmentDetailsInteractor().loadReminder('');

      expect(actual, isNull);
    });

    test('loadReminder returns null if reminder date has passed', () async {
      final reminder = Reminder((b) => b
        ..id = 123
        ..date = DateTime.now().subtract(Duration(minutes: 1)));
      when(reminderDb.getByItem(any, any, any, any)).thenAnswer((_) async => reminder);

      final actual = await AssignmentDetailsInteractor().loadReminder('');

      expect(actual, isNull);
      verify(notificationUtil.deleteNotification(reminder.id));
      verify(reminderDb.deleteById(reminder.id));
    });

    test('deleteReminder deletes reminder from database and notifications', () async {
      final reminder = Reminder((b) => b..id = 123);

      await AssignmentDetailsInteractor().deleteReminder(reminder);

      verify(notificationUtil.deleteNotification(reminder.id));
      verify(reminderDb.deleteById(reminder.id));
    });

    test('createReminder inserts reminder into database and schedules a notification', () async {
      final date = DateTime.now();
      final formattedDate = 'Febtember 34, 3031';
      final assignment = Assignment((b) => b
        ..name = 'Assignment name'
        ..id = assignmentId
        ..courseId = courseId
        ..assignmentGroupId = ''
        ..position = 0);

      final reminder = Reminder((b) => b
        ..userDomain = login.domain
        ..userId = login.user.id
        ..type = Reminder.TYPE_ASSIGNMENT
        ..itemId = assignment.id
        ..courseId = courseId
        ..date = date.toUtc());

      final savedReminder = reminder.rebuild((b) => b..id = 123);
      when(reminderDb.insert(reminder)).thenAnswer((_) async => savedReminder);

      final l10n = AppLocalizations();
      await AssignmentDetailsInteractor().createReminder(
        l10n,
        date,
        assignment.id,
        courseId,
        assignment.name,
        formattedDate,
      );

      verify(reminderDb.insert(reminder));
      verify(notificationUtil.scheduleReminder(l10n, assignment.name, formattedDate, savedReminder));
    });

    test('returns an assignment', () async {
      final assignment = Assignment((b) => b
        ..id = assignmentId
        ..courseId = courseId
        ..assignmentGroupId = ''
        ..position = 0);
      when(courseApi.getCourse(courseId)).thenAnswer((_) async => Course((b) => b..name = ''));
      when(assignmentApi.getAssignment(courseId, assignmentId, forceRefresh: false))
          .thenAnswer((_) async => assignment);
      final details =
          await AssignmentDetailsInteractor().loadAssignmentDetails(false, courseId, assignmentId, studentId);

      expect(details?.assignment, assignment);
    });
  });
}
