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
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class AssignmentDetailsInteractor {
  Future<AssignmentDetails?> loadAssignmentDetails(
    bool forceRefresh,
    String courseId,
    String assignmentId,
    String? studentId,
  ) async {
    final course = locator<CourseApi>().getCourse(courseId, forceRefresh: forceRefresh);
    final assignment = locator<AssignmentApi>().getAssignment(courseId, assignmentId, forceRefresh: forceRefresh);

    return AssignmentDetails(
      assignment: (await assignment),
      course: (await course),
    );
  }

  Future<AssignmentDetails> loadQuizDetails(
    bool forceRefresh,
    String courseId,
    String assignmentId,
    String studentId,
  ) async {
    final course = locator<CourseApi>().getCourse(courseId, forceRefresh: forceRefresh);
    final quiz = locator<AssignmentApi>().getAssignment(courseId, assignmentId, forceRefresh: forceRefresh);

    return AssignmentDetails(
      assignment: (await quiz),
      course: (await course),
    );
  }

  Future<Reminder?> loadReminder(String assignmentId) async {
    Reminder? reminder = null;
    String? domain = ApiPrefs.getDomain();
    String? id = ApiPrefs.getUser()?.id;
    if (domain != null && id != null) {
      reminder = await locator<ReminderDb>().getByItem(
        domain,
        id,
        Reminder.TYPE_ASSIGNMENT,
        assignmentId,
      );
    }

    /* If the user dismisses a reminder notification without tapping it, then NotificationUtil won't have a chance
       to remove it from the database. Given that we cannot time travel (yet), if the reminder we just retrieved
       has a date set in the past then we will opt to delete it here. */
    if (reminder?.date?.isBefore(DateTime.now()) == true) {
      await deleteReminder(reminder!);
      return null;
    }

    return reminder;
  }

  Future<void> createReminder(
    AppLocalizations l10n,
    DateTime date,
    String assignmentId,
    String courseId,
    String? title,
    String body,
  ) async {
    var reminder = Reminder((b) => b
          ..userDomain = ApiPrefs.getDomain()
          ..userId = ApiPrefs.getUser()?.id
          ..type = Reminder.TYPE_ASSIGNMENT
          ..itemId = assignmentId
          ..courseId = courseId
          ..date = date.toUtc() // built_value complains about non-utc timestamps
        );

    // Saving to the database will generate an ID for this reminder
    var insertedReminder = await locator<ReminderDb>().insert(reminder);
    if (insertedReminder != null) {
      reminder = insertedReminder;
      await locator<NotificationUtil>().scheduleReminder(l10n, title, body, reminder);
    }

  }

  Future<void> deleteReminder(Reminder? reminder) async {
    if (reminder == null) return;
    await locator<NotificationUtil>().deleteNotification(reminder.id!);
    await locator<ReminderDb>().deleteById(reminder.id!);
  }
}

class AssignmentDetails {
  final Course? course;
  final Assignment? assignment;

  AssignmentDetails({this.course, this.assignment});
}
