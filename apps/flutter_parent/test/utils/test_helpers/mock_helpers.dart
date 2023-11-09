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
import 'package:flutter_parent/network/api/announcement_api.dart';
import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/error_report_api.dart';
import 'package:flutter_parent/network/api/features_api.dart';
import 'package:flutter_parent/network/api/file_api.dart';
import 'package:flutter_parent/network/api/help_links_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/api/oauth_api.dart';
import 'package:flutter_parent/network/api/page_api.dart';
import 'package:flutter_parent/network/api/planner_api.dart';
import 'package:flutter_parent/network/api/user_api.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/authentication_interceptor.dart';
import 'package:flutter_parent/screens/account_creation/account_creation_interactor.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_interactor.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/aup/acceptable_use_policy_interactor.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_model.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_interactor.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/dashboard/inbox_notifier.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_interactor.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/screens/help/help_screen_interactor.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_picker_interactor.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/conversation_details_interactor.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_interactor.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_parent/screens/inbox/reply/conversation_reply_interactor.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_interactor.dart';
import 'package:flutter_parent/screens/manage_students/student_color_picker_interactor.dart';
import 'package:flutter_parent/screens/masquerade/masquerade_screen_interactor.dart';
import 'package:flutter_parent/screens/pairing/pairing_interactor.dart';
import 'package:flutter_parent/screens/pairing/pairing_util.dart';
import 'package:flutter_parent/screens/qr_login/qr_login_tutorial_screen_interactor.dart';
import 'package:flutter_parent/screens/remote_config/remote_config_interactor.dart';
import 'package:flutter_parent/screens/settings/settings_interactor.dart';
import 'package:flutter_parent/screens/splash/splash_screen_interactor.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/fetcher/attachment_fetcher_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/audio_video_attachment_viewer_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_content_interactor.dart';
import 'package:flutter_parent/utils/db/calendar_filter_db.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/db/user_colors_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/old_app_migration.dart';
import 'package:flutter_parent/utils/permission_handler.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/url_launcher.dart';
import 'package:flutter_parent/utils/veneers/android_intent_veneer.dart';
import 'package:flutter_parent/utils/veneers/barcode_scan_veneer.dart';
import 'package:flutter_parent/utils/veneers/flutter_downloader_veneer.dart';
import 'package:flutter_parent/utils/veneers/flutter_snackbar_veneer.dart';
import 'package:flutter_parent/utils/veneers/path_provider_veneer.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:sqflite/sqflite.dart';
import 'package:video_player/video_player.dart';
import 'package:webview_flutter/webview_flutter.dart';

@GenerateNiceMocks([
  MockSpec<AccountsApi>(),
  MockSpec<AccountCreationInteractor>(),
  MockSpec<Analytics>(),
  MockSpec<AndroidIntentVeneer>(),
  MockSpec<AlertsApi>(),
  MockSpec<AlertCountNotifier>(),
  MockSpec<AssignmentApi>(),
  MockSpec<AssignmentDetailsInteractor>(),
  MockSpec<AuthApi>(),
  MockSpec<BarcodeScanVeneer>(),
  MockSpec<CalendarEventsApi>(),
  MockSpec<CalendarFilterDb>(),
  MockSpec<CalendarFilterListInteractor>(),
  MockSpec<CourseApi>(),
  MockSpec<CourseDetailsInteractor>(),
  MockSpec<CoursesInteractor>(),
  MockSpec<CourseDetailsModel>(),
  MockSpec<CourseRoutingShellInteractor>(),
  MockSpec<CreateConversationInteractor>(),
  MockSpec<Database>(),
  MockSpec<Dio>(),
  MockSpec<EnrollmentsApi>(),
  MockSpec<ErrorReportApi>(),
  MockSpec<ErrorReportInteractor>(),
  MockSpec<EventDetailsInteractor>(),
  MockSpec<FirebaseCrashlytics>(),
  MockSpec<HttpClient>(),
  MockSpec<HttpClientRequest>(),
  MockSpec<HttpClientResponse>(),
  MockSpec<HttpHeaders>(),
  MockSpec<InboxApi>(),
  MockSpec<NavigatorObserver>(),
  MockSpec<NotificationUtil>(),
  MockSpec<OAuthApi>(),
  MockSpec<PairingInteractor>(),
  MockSpec<PageApi>(),
  MockSpec<AndroidFlutterLocalNotificationsPlugin>(),
  MockSpec<PairingUtil>(),
  MockSpec<QuickNav>(),
  MockSpec<ReminderDb>(),
  MockSpec<FirebaseRemoteConfig>(),
  MockSpec<FlutterSnackbarVeneer>(),
  MockSpec<StudentAddedNotifier>(),
  MockSpec<UrlLauncher>(),
  MockSpec<UserColorsDb>(),
  MockSpec<WebLoginInteractor>(),
  MockSpec<WebContentInteractor>(),
  MockSpec<AlertThresholdsInteractor>(),
  MockSpec<AlertsInteractor>(),
  MockSpec<AnnouncementDetailsInteractor>(),
  MockSpec<AnnouncementApi>(),
  MockSpec<AcceptableUsePolicyInteractor>(),
  MockSpec<PlannerApi>(),
  MockSpec<HelpScreenInteractor>(),
  MockSpec<FileApi>(),
  MockSpec<PathProviderVeneer>(),
  MockSpec<InboxCountNotifier>(),
  MockSpec<BuildContext>(),
  MockSpec<ConversationDetailsInteractor>(),
  MockSpec<ConversationListInteractor>(),
  MockSpec<ConversationReplyInteractor>(),
  MockSpec<DomainSearchInteractor>(),
  MockSpec<DashboardInteractor>(),
  MockSpec<QRLoginTutorialScreenInteractor>(),
  MockSpec<ManageStudentsInteractor>(),
  MockSpec<StudentColorPickerInteractor>(),
  MockSpec<UserApi>(),
  MockSpec<MasqueradeScreenInteractor>(),
  MockSpec<SettingsInteractor>(),
  MockSpec<FeaturesApi>(),
  MockSpec<SplashScreenInteractor>(),
  MockSpec<AttachmentFetcherInteractor>(),
  MockSpec<CancelToken>(),
  MockSpec<AudioVideoAttachmentViewerInteractor>(),
  MockSpec<VideoPlayerController>(),
  MockSpec<PermissionHandler>(),
  MockSpec<FlutterDownloaderVeneer>(),
  MockSpec<ViewAttachmentInteractor>(),
  MockSpec<AuthenticationInterceptor>(),
  MockSpec<ErrorInterceptorHandler>(),
  MockSpec<AttachmentPickerInteractor>(),
  MockSpec<AttachmentHandler>(),
  MockSpec<OldAppMigration>(),
  MockSpec<HelpLinksApi>(),
  MockSpec<RemoteConfigInteractor>(),
  MockSpec<WebViewPlatformController>(),
  MockSpec<WebViewPlatform>()
])
import 'mock_helpers.mocks.dart';

MockFirebaseRemoteConfig setupMockRemoteConfig({Map<String, String>? valueSettings = null}) {
  final mockRemoteConfig = MockFirebaseRemoteConfig();
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