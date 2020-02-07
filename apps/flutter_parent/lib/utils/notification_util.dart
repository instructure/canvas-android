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

import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/notification_payload.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/service_locator.dart';

/* Concerning MBL-13819 (Set up router): The structure and function of NotificationUtil is not final as it is assumed
  that significant alterations must be made to wire up notification payloads for routing. If you are implementing
  routing, do no hesitate to change this class and its tests as necessary.

  Regarding initialization of the FlutterLocalNotificationsPlugin, if the app has been started in response to the user
  tapping on a notification then the onSelectNotification callback will be invoked as soon as initialization occurs. As
  such, rather than calling NotificationUtil.init() from main.dart, it may ultimately make more sense to initialize as
  part of the router setup or as part of a component in the widget tree where it will be more convenient and/or timely
  to capture the notification payload.

  Additionally, FlutterLocalNotificationsPlugin.getNotificationAppLaunchDetails() can be called at any time (including
  prior to plugin initialization) to determine if a notification tap caused the app to be launched, and to retrieve
  the payload of that notification. */
class NotificationUtil {
  static const notificationChannelReminders = 'com.instructure.parentapp/reminders';

  static FlutterLocalNotificationsPlugin _plugin;

  @visibleForTesting
  static initForTest(FlutterLocalNotificationsPlugin plugin) {
    _plugin = plugin;
  }

  static Future<void> init() async {
    var initializationSettings = InitializationSettings(
      AndroidInitializationSettings('ic_notification_canvas_logo'),
      null,
    );

    if (_plugin == null) {
      _plugin = FlutterLocalNotificationsPlugin();
    }

    await _plugin.initialize(
      initializationSettings,
      onSelectNotification: (rawPayload) async {
        await handlePayload(rawPayload);
      },
    );
  }

  @visibleForTesting
  static Future<void> handlePayload(String rawPayload) async {
    try {
      NotificationPayload payload = deserialize(json.decode(rawPayload));
      switch (payload.type) {
        case NotificationPayloadType.reminder:
          await handleReminder(payload);
          break;
        case NotificationPayloadType.other:
          break;
      }
    } catch (e) {
      print(e);
    }
  }

  @visibleForTesting
  static Future<void> handleReminder(NotificationPayload payload) async {
    Reminder reminder = Reminder.fromNotification(payload);

    // Delete reminder from db
    await locator<ReminderDb>().deleteById(reminder.id);

    // Route to event or assignment
    // TODO: MBL-13819
  }

  Future<void> scheduleReminder(AppLocalizations l10n, String title, String body, Reminder reminder) {
    final payload = NotificationPayload((b) => b
      ..type = NotificationPayloadType.reminder
      ..domain = ApiPrefs.getDomain()
      ..userId = ApiPrefs.getUser().id
      ..data = json.encode(serialize(reminder)));

    final notificationDetails = NotificationDetails(
      AndroidNotificationDetails(
        notificationChannelReminders,
        l10n.remindersNotificationChannelName,
        l10n.remindersNotificationChannelDescription,
      ),
      null,
    );

    return _plugin.schedule(
      reminder.id,
      title,
      body,
      reminder.date,
      notificationDetails,
      payload: json.encode(serialize(payload)),
    );
  }

  Future<void> deleteNotification(int id) => _plugin.cancel(id);

  Future<void> deleteNotifications(List<int> ids) async {
    for (int id in ids) await _plugin.cancel(id);
  }
}
