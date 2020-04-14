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
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../utils/test_app.dart';

void main() {
  // Setup
  final eventsApi = _MockEventsApi();
  final reminderDb = _MockReminderDb();
  final notificationUtil = _MockNotificationUtil();
  final login = Login((b) => b
    ..domain = 'test-domain'
    ..user = User((u) => u..id = '123').toBuilder());

  setupTestLocator((locator) {
    locator.registerLazySingleton<CalendarEventsApi>(() => eventsApi);
    locator.registerLazySingleton<ReminderDb>(() => reminderDb);
    locator.registerLazySingleton<NotificationUtil>(() => notificationUtil);
  });

  setUp(() async {
    reset(eventsApi);
    reset(reminderDb);
    reset(notificationUtil);
    await setupPlatformChannels();
    await ApiPrefs.switchLogins(login);
  });

  // Start tests
  test('loadEvent calls api', () {
    final itemId = 'id';
    final interactor = EventDetailsInteractor();
    interactor.loadEvent(itemId, true);
    verify(eventsApi.getEvent(itemId, true)).called(1);
  });

  test('loadReminder calls ReminderDb', () async {
    final eventId = 'event-123';
    when(reminderDb.getByItem(any, any, any, any)).thenAnswer((_) async => null);

    await EventDetailsInteractor().loadReminder(eventId);

    verify(reminderDb.getByItem(login.domain, login.user.id, Reminder.TYPE_EVENT, eventId));
  });

  test('loadReminder returns reminder if reminder date has not passed', () async {
    final expected = Reminder((b) => b..date = DateTime.now().add(Duration(minutes: 1)));
    when(reminderDb.getByItem(any, any, any, any)).thenAnswer((_) async => expected);

    final actual = await EventDetailsInteractor().loadReminder('');

    expect(actual, expected);
  });

  test('loadReminder returns null if none exist for the event', () async {
    when(reminderDb.getByItem(any, any, any, any)).thenAnswer((_) async => null);

    final actual = await EventDetailsInteractor().loadReminder('');

    expect(actual, isNull);
  });

  test('loadReminder returns null if reminder date has passed', () async {
    final reminder = Reminder((b) => b
      ..id = 123
      ..date = DateTime.now().subtract(Duration(minutes: 1)));
    when(reminderDb.getByItem(any, any, any, any)).thenAnswer((_) async => reminder);

    final actual = await EventDetailsInteractor().loadReminder('');

    expect(actual, isNull);
    verify(notificationUtil.deleteNotification(reminder.id));
    verify(reminderDb.deleteById(reminder.id));
  });

  test('deleteReminder deletes reminder from database and notifications', () async {
    final reminder = Reminder((b) => b..id = 123);

    await EventDetailsInteractor().deleteReminder(reminder);

    verify(notificationUtil.deleteNotification(reminder.id));
    verify(reminderDb.deleteById(reminder.id));
  });

  test('createReminder inserts reminder into database and schedules a notification', () async {
    final date = DateTime.now();
    final formattedDate = 'Febtember 34, 3031';
    final courseId = 'course_123';
    final event = ScheduleItem((b) => b
      ..id = 'event-123'
      ..title = 'Event title');

    final reminder = Reminder((b) => b
      ..userDomain = login.domain
      ..userId = login.user.id
      ..type = Reminder.TYPE_EVENT
      ..itemId = event.id
      ..courseId = courseId
      ..date = date.toUtc());

    final savedReminder = reminder.rebuild((b) => b..id = 123);
    when(reminderDb.insert(reminder)).thenAnswer((_) async => savedReminder);

    final l10n = AppLocalizations();
    await EventDetailsInteractor().createReminder(l10n, date, event.id, courseId, event.title, formattedDate);

    verify(reminderDb.insert(reminder));
    verify(notificationUtil.scheduleReminder(l10n, event.title, formattedDate, savedReminder));
  });
}

class _MockEventsApi extends Mock implements CalendarEventsApi {}

class _MockReminderDb extends Mock implements ReminderDb {}

class _MockNotificationUtil extends Mock implements NotificationUtil {}
