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
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/service_locator.dart';

class EventDetailsInteractor {
  Future<ScheduleItem?> loadEvent(String? eventId, bool forceRefresh) {
    return locator<CalendarEventsApi>().getEvent(eventId, forceRefresh);
  }

  Future<Reminder?> loadReminder(String? eventId) async {
    final reminder = await locator<ReminderDb>().getByItem(
      ApiPrefs.getDomain(),
      ApiPrefs.getUser()?.id,
      Reminder.TYPE_EVENT,
      eventId,
    );

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
    String? eventId,
    String? courseId,
    String? title,
    String body,
  ) async {
    var reminder = Reminder((b) => b
          ..userDomain = ApiPrefs.getDomain()
          ..userId = ApiPrefs.getUser()?.id
          ..type = Reminder.TYPE_EVENT
          ..itemId = eventId
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
