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

import 'dart:convert';

import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/notification_payload.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import 'test_app.dart';

void main() {
  final plugin = _MockPlugin();
  final database = _MockDatabase();
  final analytics = _MockAnalytics();

  setupTestLocator((locator) {
    locator.registerLazySingleton<ReminderDb>(() => database);
    locator.registerLazySingleton<Analytics>(() => analytics);
  });

  setUp(() {
    reset(plugin);
    reset(database);
    NotificationUtil.initForTest(plugin);
  });

  test('initializes plugin with expected parameters', () async {
    await NotificationUtil.init();

    final verification = verify(plugin.initialize(
      captureAny,
      onSelectNotification: captureAnyNamed('onSelectNotification'),
    ));

    InitializationSettings initSettings = verification.captured[0];
    expect(initSettings.android.defaultIcon, 'ic_notification_canvas_logo');
    expect(initSettings.ios, null);

    SelectNotificationCallback callback = verification.captured[1];
    expect(callback, isNotNull);
    expect(await callback(''), isNull); // Callback should complete w/o errors
  });

  test('handleReminder deletes reminder from database', () async {
    final reminder = Reminder((b) => b
      ..id = 123
      ..date = DateTime.now().toUtc());
    final payload = NotificationPayload((b) => b
      ..type = NotificationPayloadType.reminder
      ..data = json.encode(serialize(reminder)));

    await NotificationUtil.handleReminder(payload);

    verify(database.deleteById(reminder.id));
  });

  test('handlePayload handles reminders', () async {
    final reminder = Reminder((b) => b
      ..id = 123
      ..date = DateTime.now().toUtc());
    final payload = NotificationPayload((b) => b
      ..type = NotificationPayloadType.reminder
      ..data = json.encode(serialize(reminder)));

    final rawPayload = json.encode(serialize(payload));

    await NotificationUtil.handlePayload(rawPayload);

    verify(database.deleteById(reminder.id));
  });

  test('handlePayload handles other types', () async {
    final payload = NotificationPayload((b) => b..type = NotificationPayloadType.other);

    final rawPayload = json.encode(serialize(payload));

    await NotificationUtil.handlePayload(rawPayload);
    // Nothing uses 'other' notification types at the moment, so this test should simply complete without errors
  });

  test('handlePayload catches deserialization errors', () async {
    await NotificationUtil.handlePayload('');
    // No error should be thrown and test should complete successfully
  });

  test('deleteNotification calls cancel on the plugin', () async {
    final notificationId = 123;
    await NotificationUtil().deleteNotification(notificationId);

    verify(plugin.cancel(notificationId));
  });

  test('deleteNotifications calls cancel on the plugin for each id', () async {
    final notificationIds = [123, 234, 345];
    await NotificationUtil().deleteNotifications(notificationIds);

    verify(plugin.cancel(notificationIds[0]));
    verify(plugin.cancel(notificationIds[1]));
    verify(plugin.cancel(notificationIds[2]));
  });

  test('scheduleReminder calls plugin with expected parameters for an event', () async {
    await setupPlatformChannels();

    final reminder = Reminder((b) => b
      ..id = 123
      ..date = DateTime.now().toUtc());

    final expectedPayload = NotificationPayload((b) => b
      ..type = NotificationPayloadType.reminder
      ..data = json.encode(serialize(reminder)));

    await NotificationUtil().scheduleReminder(AppLocalizations(), 'title', 'body', reminder);

    final NotificationDetails details = verify(plugin.schedule(
      reminder.id,
      'title',
      'body',
      reminder.date,
      captureAny,
      payload: json.encode(serialize(expectedPayload)),
    )).captured.first;

    expect(details.iOS, isNull);
    expect(details.android.channelId, NotificationUtil.notificationChannelReminders);
    expect(details.android.channelName, AppLocalizations().remindersNotificationChannelName);
    expect(details.android.channelDescription, AppLocalizations().remindersNotificationChannelDescription);

    verify(analytics.logEvent(AnalyticsEventConstants.REMINDER_EVENT_CREATE));
  });

  test('scheduleReminder calls plugin with expected parameters for an assignment', () async {
    await setupPlatformChannels();

    final reminder = Reminder((b) => b
      ..id = 123
      ..type = Reminder.TYPE_ASSIGNMENT
      ..date = DateTime.now().toUtc());

    final expectedPayload = NotificationPayload((b) => b
      ..type = NotificationPayloadType.reminder
      ..data = json.encode(serialize(reminder)));

    await NotificationUtil().scheduleReminder(AppLocalizations(), 'title', 'body', reminder);

    final NotificationDetails details = verify(plugin.schedule(
      reminder.id,
      'title',
      'body',
      reminder.date,
      captureAny,
      payload: json.encode(serialize(expectedPayload)),
    )).captured.first;

    expect(details.iOS, isNull);
    expect(details.android.channelId, NotificationUtil.notificationChannelReminders);
    expect(details.android.channelName, AppLocalizations().remindersNotificationChannelName);
    expect(details.android.channelDescription, AppLocalizations().remindersNotificationChannelDescription);

    verify(analytics.logEvent(AnalyticsEventConstants.REMINDER_ASSIGNMENT_CREATE));
  });

  // For coverage only
  test('initializes', () async {
    NotificationUtil.initForTest(null);
    await NotificationUtil.init();
  });
}

class _MockPlugin extends Mock implements FlutterLocalNotificationsPlugin {}

class _MockDatabase extends Mock implements ReminderDb {}

class _MockAnalytics extends Mock implements Analytics {}
