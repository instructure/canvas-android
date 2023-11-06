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

import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/notification_payload.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/service_locator.dart';
import 'package:timezone/data/latest_all.dart' as tz;
import 'package:timezone/timezone.dart' as tz;

class NotificationUtil {
  static const notificationChannelReminders =
      'com.instructure.parentapp/reminders';

  static AndroidFlutterLocalNotificationsPlugin? _plugin;

  @visibleForTesting
  static initForTest(AndroidFlutterLocalNotificationsPlugin plugin) {
    _plugin = plugin;
  }

  static Future<void> init(Completer<void>? appCompleter) async {
    var initializationSettings = AndroidInitializationSettings('ic_notification_canvas_logo');

    if (_plugin == null) {
      _plugin = AndroidFlutterLocalNotificationsPlugin();
    }

    await _plugin!.initialize(
      initializationSettings,
      onDidReceiveNotificationResponse: (rawPayload) async {
        await handlePayload(rawPayload.payload ?? '', appCompleter);
      },
    );
  }

  @visibleForTesting
  static Future<void> handlePayload(
      String rawPayload, Completer<void>? appCompleter) async {
    try {
      NotificationPayload? payload = deserialize(json.decode(rawPayload));
      switch (payload?.type) {
        case NotificationPayloadType.reminder:
          await handleReminder(payload!, appCompleter);
          break;
        case NotificationPayloadType.other:
          break;
      }
    } catch (e) {
      print(e);
    }
  }

  @visibleForTesting
  static Future<void> handleReminder(
      NotificationPayload payload, Completer<void>? appCompleter) async {
    Reminder? reminder = Reminder.fromNotification(payload);

    // Delete reminder from db
    await locator<ReminderDb>().deleteById(reminder?.id);

    // Create route
    String? route;
    switch (reminder?.type) {
      case Reminder.TYPE_ASSIGNMENT:
        route =
            PandaRouter.assignmentDetails(reminder!.courseId, reminder.itemId);
        break;
      case Reminder.TYPE_EVENT:
        route = PandaRouter.eventDetails(reminder!.courseId, reminder.itemId);
        break;
    }

    // Push route, but only after the app has finished building
    if (route != null) appCompleter?.future.then((_) => WidgetsBinding.instance.handlePushRoute(route!));
  }

  Future<void> scheduleReminder(
      AppLocalizations l10n, String? title, String body, Reminder reminder) {
    final payload = NotificationPayload((b) => b
      ..type = NotificationPayloadType.reminder
      ..data = json.encode(serialize(reminder)));

    final notificationDetails = AndroidNotificationDetails(
        notificationChannelReminders,
        l10n.remindersNotificationChannelName,
        channelDescription: l10n.remindersNotificationChannelDescription
    );

    if (reminder.type == Reminder.TYPE_ASSIGNMENT) {
      locator<Analytics>()
          .logEvent(AnalyticsEventConstants.REMINDER_ASSIGNMENT_CREATE);
    } else {
      locator<Analytics>()
          .logEvent(AnalyticsEventConstants.REMINDER_EVENT_CREATE);
    }

    tz.initializeTimeZones();
    var d = reminder.date!.toUtc();
    var date = tz.TZDateTime.utc(d.year, d.month, d.day, d.hour, d.minute, d.second);

    return _plugin!.zonedSchedule(
      reminder.id!,
      title,
      body,
      date,
      notificationDetails,
      scheduleMode: AndroidScheduleMode.exactAllowWhileIdle,
      payload: json.encode(serialize(payload))
    );
  }

  Future<void> deleteNotification(int id) => _plugin!.cancel(id);

  Future<void> deleteNotifications(List<int> ids) async {
    for (int id in ids) await _plugin!.cancel(id);
  }

  Future<bool?> requestScheduleExactAlarmPermission() async {
    return await _plugin?.requestExactAlarmsPermission();
  }
}
