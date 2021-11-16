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
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:firebase_remote_config/firebase_remote_config.dart';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/error_report_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/api/oauth_api.dart';
import 'package:flutter_parent/network/api/page_api.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/screens/account_creation/account_creation_interactor.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_interactor.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_parent/screens/pairing/pairing_interactor.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_parent/utils/veneers/android_intent_veneer.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';
import 'package:flutter_parent/utils/veneers/flutter_snackbar_veneer.dart';
import 'package:mockito/mockito.dart';
import 'package:sqflite/sqflite.dart';

MockRemoteConfig setupMockRemoteConfig({Map<String, String> valueSettings = null}) {
  final mockRemoteConfig = MockRemoteConfig();
  when(mockRemoteConfig.fetch()).thenAnswer((_) => Future.value());
  when(mockRemoteConfig.activate())
      .thenAnswer((_) => Future.value(valueSettings != null));
  when(mockRemoteConfig.settings).thenAnswer((realInvocation) =>
      RemoteConfigSettings(
          fetchTimeout: Duration(milliseconds: 100),
          minimumFetchInterval: Duration(milliseconds: 100)));
  if (valueSettings != null) {
    valueSettings.forEach((key, value) {
      when(mockRemoteConfig.getString(key)).thenAnswer((_) => value);
    });
  }

  return mockRemoteConfig;
}

class MockAccountsApi extends Mock implements AccountsApi {}

class MockAccountCreationInteractor extends Mock implements AccountCreationInteractor {}

class MockAnalytics extends Mock implements Analytics {}

class MockAndroidIntentVeneer extends Mock implements AndroidIntentVeneer {}

class MockAlertsApi extends Mock implements AlertsApi {}

class MockAlertCountNotifier extends Mock implements AlertCountNotifier {}

class MockAssignmentApi extends Mock implements AssignmentApi {}

class MockAssignmentDetailsInteractor extends Mock implements AssignmentDetailsInteractor {}

class MockAuthApi extends Mock implements AuthApi {}

class MockBarcodeScanner extends Mock implements BarcodeScanVeneer {}

class MockCalendarApi extends Mock implements CalendarEventsApi {}

class MockCalendarFilterDb extends Mock implements CalendarFilterDb {}

class MockCalendarFilterListInteractor extends Mock implements CalendarFilterListInteractor {}

class MockCourseApi extends Mock implements CourseApi {}

class MockCourseDetailsInteractor extends Mock implements CourseDetailsInteractor {}

class MockCoursesInteractor extends Mock implements CoursesInteractor {}

class MockCourseModel extends Mock implements CourseDetailsModel {}

class MockCourseRoutingShellInteractor extends Mock implements CourseRoutingShellInteractor {}

class MockCreateConversationInteractor extends Mock implements CreateConversationInteractor {}

class MockDatabase extends Mock implements Database {}

class MockDio extends Mock implements Dio {}

class MockEnrollmentsApi extends Mock implements EnrollmentsApi {}

class MockErrorReportApi extends Mock implements ErrorReportApi {}

class MockErrorReportInteractor extends Mock implements ErrorReportInteractor {}

class MockEventDetailsInteractor extends Mock implements EventDetailsInteractor {}

class MockFirebase extends Mock implements FirebaseCrashlytics {}

class MockHttpClient extends Mock implements HttpClient {}

class MockHttpClientRequest extends Mock implements HttpClientRequest {}

class MockHttpClientResponse extends Mock implements HttpClientResponse {}

class MockHttpHeaders extends Mock implements HttpHeaders {}

class MockInboxApi extends Mock implements InboxApi {}

class MockNav extends Mock implements QuickNav {}

class MockNavigatorObserver extends Mock implements NavigatorObserver {}

class MockNotificationUtil extends Mock implements NotificationUtil {}

class MockOAuthApi extends Mock implements OAuthApi {}

class MockPairingInteractor extends Mock implements PairingInteractor {}

class MockPageApi extends Mock implements PageApi {}

class MockPlugin extends Mock implements FlutterLocalNotificationsPlugin {}

class MockPairingUtil extends Mock implements PairingUtil {}

class MockQuickNav extends Mock implements QuickNav {}

class MockReminderDb extends Mock implements ReminderDb {}

class MockRemoteConfig extends Mock implements RemoteConfig {}

class MockSnackbar extends Mock implements FlutterSnackbarVeneer {}

class MockStudentAddedNotifier extends Mock implements StudentAddedNotifier {}

class MockUrlLauncher extends Mock implements UrlLauncher {}

class MockUserColorsDb extends Mock implements UserColorsDb {}

class MockWebLoginInteractor extends Mock implements WebLoginInteractor {}

class MockWebContentInteractor extends Mock implements WebContentInteractor {}
