// Copyright (C) 2019 - present Instructure, Inc.
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

import 'package:firebase_crashlytics/firebase_crashlytics.dart';
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
import 'package:flutter_parent/screens/account_creation/account_creation_interactor.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_interactor.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/aup/acceptable_use_policy_interactor.dart';
import 'package:flutter_parent/screens/calendar/calendar_today_click_notifier.dart';
import 'package:flutter_parent/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/courses/routing_shell/course_routing_shell_interactor.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/dashboard/inbox_notifier.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_interactor.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/screens/help/help_screen_interactor.dart';
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
import 'package:flutter_parent/screens/qr_login/qr_login_util.dart';
import 'package:flutter_parent/screens/remote_config/remote_config_interactor.dart';
import 'package:flutter_parent/screens/settings/settings_interactor.dart';
import 'package:flutter_parent/screens/splash/splash_screen_interactor.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/utils/alert_helper.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_interactor.dart';
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
import 'package:get_it/get_it.dart';
import 'package:sqflite/sqflite.dart';

import 'common_widgets/view_attachment/fetcher/attachment_fetcher_interactor.dart';
import 'db/db_util.dart';

GetIt locator = GetIt.instance;

void setupLocator() {
  // APIs
  locator.registerLazySingleton<AccountsApi>(() => AccountsApi());
  locator.registerLazySingleton<AlertsApi>(() => AlertsApi());
  locator.registerLazySingleton<AnnouncementApi>(() => AnnouncementApi());
  locator.registerLazySingleton<AssignmentApi>(() => AssignmentApi());
  locator.registerLazySingleton<AuthApi>(() => AuthApi());
  locator.registerLazySingleton<CalendarEventsApi>(() => CalendarEventsApi());
  locator.registerLazySingleton<CourseApi>(() => CourseApi());
  locator.registerLazySingleton<EnrollmentsApi>(() => EnrollmentsApi());
  locator.registerLazySingleton<ErrorReportApi>(() => ErrorReportApi());
  locator.registerLazySingleton<FileApi>(() => FileApi());
  locator.registerLazySingleton<HelpLinksApi>(() => HelpLinksApi());
  locator.registerLazySingleton<InboxApi>(() => InboxApi());
  locator.registerLazySingleton<OAuthApi>(() => OAuthApi());
  locator.registerLazySingleton<PageApi>(() => PageApi());
  locator.registerLazySingleton<PlannerApi>(() => PlannerApi());
  locator.registerLazySingleton<UserApi>(() => UserApi());
  locator.registerLazySingleton<FeaturesApi>(() => FeaturesApi());

  // DB helpers
  locator.registerLazySingleton<Database>(() => DbUtil.instance);
  locator.registerLazySingleton<CalendarFilterDb>(() => CalendarFilterDb());
  locator.registerLazySingleton<ReminderDb>(() => ReminderDb());
  locator.registerLazySingleton<UserColorsDb>(() => UserColorsDb());

  // Interactors
  locator.registerFactory<AcceptableUsePolicyInteractor>(() => AcceptableUsePolicyInteractor());
  locator.registerFactory<AccountCreationInteractor>(() => AccountCreationInteractor());
  locator.registerFactory<AlertsInteractor>(() => AlertsInteractor());
  locator.registerFactory<AlertThresholdsInteractor>(() => AlertThresholdsInteractor());
  locator.registerFactory<AnnouncementDetailsInteractor>(() => AnnouncementDetailsInteractor());
  locator.registerFactory<AttachmentFetcherInteractor>(() => AttachmentFetcherInteractor());
  locator.registerFactory<AssignmentDetailsInteractor>(() => AssignmentDetailsInteractor());
  locator.registerFactory<AttachmentPickerInteractor>(() => AttachmentPickerInteractor());
  locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => AudioVideoAttachmentViewerInteractor());
  locator.registerFactory<CalendarFilterListInteractor>(() => CalendarFilterListInteractor());
  locator.registerFactory<ConversationDetailsInteractor>(() => ConversationDetailsInteractor());
  locator.registerFactory<ConversationListInteractor>(() => ConversationListInteractor());
  locator.registerFactory<ConversationReplyInteractor>(() => ConversationReplyInteractor());
  locator.registerFactory<CourseDetailsInteractor>(() => CourseDetailsInteractor());
  locator.registerFactory<CoursesInteractor>(() => CoursesInteractor());
  locator.registerFactory<CreateConversationInteractor>(() => CreateConversationInteractor());
  locator.registerFactory<CourseRoutingShellInteractor>(() => CourseRoutingShellInteractor());
  locator.registerFactory<DashboardInteractor>(() => DashboardInteractor());
  locator.registerFactory<DomainSearchInteractor>(() => DomainSearchInteractor());
  locator.registerFactory<ErrorReportInteractor>(() => ErrorReportInteractor());
  locator.registerFactory<EventDetailsInteractor>(() => EventDetailsInteractor());
  locator.registerFactory<HelpScreenInteractor>(() => HelpScreenInteractor());
  locator.registerFactory<ManageStudentsInteractor>(() => ManageStudentsInteractor());
  locator.registerFactory<MasqueradeScreenInteractor>(() => MasqueradeScreenInteractor());
  locator.registerFactory<PairingInteractor>(() => PairingInteractor());
  locator.registerFactory<QRLoginTutorialScreenInteractor>(() => QRLoginTutorialScreenInteractor());
  locator.registerFactory<RemoteConfigInteractor>(() => RemoteConfigInteractor());
  locator.registerFactory<SettingsInteractor>(() => SettingsInteractor());
  locator.registerFactory<SplashScreenInteractor>(() => SplashScreenInteractor());
  locator.registerFactory<StudentColorPickerInteractor>(() => StudentColorPickerInteractor());
  locator.registerFactory<ViewAttachmentInteractor>(() => ViewAttachmentInteractor());
  locator.registerFactory<WebLoginInteractor>(() => WebLoginInteractor());
  locator.registerFactory<WebContentInteractor>(() => WebContentInteractor());

  // Veneers and mockable dependencies
  locator.registerLazySingleton<AndroidIntentVeneer>(() => AndroidIntentVeneer());
  locator.registerLazySingleton<BarcodeScanVeneer>(() => BarcodeScanVeneer());
  locator.registerLazySingleton<FlutterDownloaderVeneer>(() => FlutterDownloaderVeneer());
  locator.registerLazySingleton<FlutterSnackbarVeneer>(() => FlutterSnackbarVeneer());
  locator.registerLazySingleton<FirebaseCrashlytics>(() => FirebaseCrashlytics.instance);
  locator.registerLazySingleton<PathProviderVeneer>(() => PathProviderVeneer());
  locator.registerLazySingleton<PermissionHandler>(() => PermissionHandler());
  locator.registerLazySingleton<UrlLauncher>(() => UrlLauncher());

  // Other
  locator.registerLazySingleton<AlertCountNotifier>(() => AlertCountNotifier());
  locator.registerLazySingleton<Analytics>(() => Analytics());
  locator.registerLazySingleton<CalendarTodayClickNotifier>(() => CalendarTodayClickNotifier());
  locator.registerLazySingleton<InboxCountNotifier>(() => InboxCountNotifier());
  locator.registerLazySingleton<NotificationUtil>(() => NotificationUtil());
  locator.registerLazySingleton<OldAppMigration>(() => OldAppMigration());
  locator.registerLazySingleton<PairingUtil>(() => PairingUtil());
  locator.registerLazySingleton<QRLoginUtil>(() => QRLoginUtil());
  locator.registerLazySingleton<QuickNav>(() => QuickNav());
  locator.registerLazySingleton<StudentAddedNotifier>(() => StudentAddedNotifier());
  locator.registerLazySingleton<AlertsHelper>(() => AlertsHelper());
}
