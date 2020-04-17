/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// Create a mocked RemoteConfig object.
// If valueSettings != null, then (1) a mocked settings fetch will occur, and (2) the retrieved
// settings will correspond the specified values.
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:firebase_remote_config/firebase_remote_config.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/error_report_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/api/oauth_api.dart';
import 'package:flutter_parent/network/api/page_api.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_interactor.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:mockito/mockito.dart';
import 'package:sqflite/sqflite.dart';

MockRemoteConfig setupMockRemoteConfig({Map<String, String> valueSettings = null}) {
  final mockRemoteConfig = MockRemoteConfig();
  when(mockRemoteConfig.fetch()).thenAnswer((_) => Future.value());
  when(mockRemoteConfig.activateFetched()).thenAnswer((_) => Future.value(valueSettings != null));
  if (valueSettings != null) {
    valueSettings.forEach((key, value) {
      when(mockRemoteConfig.getString(key)).thenAnswer((_) => value);
    });
  }

  return mockRemoteConfig;
}

class MockAnalytics extends Mock implements Analytics {}

class MockAlertsApi extends Mock implements AlertsApi {}

class MockAlertCountNotifier extends Mock implements AlertCountNotifier {}

class MockAuthApi extends Mock implements AuthApi {}

class MockCalendarFilterDb extends Mock implements CalendarFilterDb {}

class MockDatabase extends Mock implements Database {}

class MockDio extends Mock implements Dio {}

class MockEnrollmentsApi extends Mock implements EnrollmentsApi {}

class MockErrorReportApi extends Mock implements ErrorReportApi {}

class MockErrorReportInteractor extends Mock implements ErrorReportInteractor {}

class MockHttpClient extends Mock implements HttpClient {}

class MockHttpClientRequest extends Mock implements HttpClientRequest {}

class MockHttpClientResponse extends Mock implements HttpClientResponse {}

class MockHttpHeaders extends Mock implements HttpHeaders {}

class MockInboxApi extends Mock implements InboxApi {}

class MockNotificationUtil extends Mock implements NotificationUtil {}

class MockOAuthApi extends Mock implements OAuthApi {}

class MockPlugin extends Mock implements FlutterLocalNotificationsPlugin {}

class MockReminderDb extends Mock implements ReminderDb {}

class MockRemoteConfig extends Mock implements RemoteConfig {}

class MockUrlLauncher extends Mock implements UrlLauncher {}

class MockWebLoginInteractor extends Mock implements WebLoginInteractor {}

class MockCourseApi extends Mock implements CourseApi {}

class MockPageApi extends Mock implements PageApi {}

class MockCourseRoutingShellInteractor extends Mock implements CourseRoutingShellInteractor {}

class MockWebViewInteractor extends Mock implements WebContentInteractor {}
