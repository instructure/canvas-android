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

import 'package:flutter_parent/network/api/accounts_api.dart';
import 'package:flutter_parent/network/api/alert_api.dart';
import 'package:flutter_parent/network/api/announcement_api.dart';
import 'package:flutter_parent/network/api/assignment_api.dart';
import 'package:flutter_parent/network/api/auth_api.dart';
import 'package:flutter_parent/network/api/calendar_events_api.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/error_report_api.dart';
import 'package:flutter_parent/network/api/file_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/network/api/oauth_api.dart';
import 'package:flutter_parent/network/api/page_api.dart';
import 'package:flutter_parent/network/api/planner_api.dart';
import 'package:flutter_parent/screens/alert_thresholds/alert_thresholds_interactor.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/announcements/announcement_details_interactor.dart';
import 'package:flutter_parent/screens/assignments/assignment_details_interactor.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/dashboard/alert_notifier.dart';
import 'package:flutter_parent/screens/dashboard/dashboard_interactor.dart';
import 'package:flutter_parent/screens/dashboard/inbox_notifier.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_interactor.dart';
import 'package:flutter_parent/screens/events/event_details_interactor.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_picker_interactor.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/conversation_details_interactor.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_interactor.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_parent/screens/inbox/reply/conversation_reply_interactor.dart';
import 'package:flutter_parent/screens/manage_students/manage_students_interactor.dart';
import 'package:flutter_parent/screens/settings/settings_interactor.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/error_report/error_report_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/audio_video_attachment_viewer_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/web_view/web_view_interactor.dart';
import 'package:flutter_parent/utils/db/reminder_db.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_parent/utils/veneers/AndroidIntentVeneer.dart';
import 'package:flutter_parent/utils/veneers/flutter_downloader_veneer.dart';
import 'package:flutter_parent/utils/veneers/path_provider_veneer.dart';
import 'package:get_it/get_it.dart';
import 'package:permission_handler/permission_handler.dart';
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
  locator.registerLazySingleton<InboxApi>(() => InboxApi());
  locator.registerLazySingleton<PlannerApi>(() => PlannerApi());
  locator.registerLazySingleton<OAuthApi>(() => OAuthApi());
  locator.registerLazySingleton<PageApi>(() => PageApi());

  // DB helpers
  locator.registerLazySingleton<Database>(() => DbUtil.instance);
  locator.registerLazySingleton<ReminderDb>(() => ReminderDb());

  // Interactors
  locator.registerFactory<AlertsInteractor>(() => AlertsInteractor());
  locator.registerFactory<AlertThresholdsInteractor>(() => AlertThresholdsInteractor());
  locator.registerFactory<AnnouncementDetailsInteractor>(() => AnnouncementDetailsInteractor());
  locator.registerFactory<AttachmentFetcherInteractor>(() => AttachmentFetcherInteractor());
  locator.registerFactory<AssignmentDetailsInteractor>(() => AssignmentDetailsInteractor());
  locator.registerFactory<AttachmentPickerInteractor>(() => AttachmentPickerInteractor());
  locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => AudioVideoAttachmentViewerInteractor());
  locator.registerFactory<ConversationDetailsInteractor>(() => ConversationDetailsInteractor());
  locator.registerFactory<ConversationListInteractor>(() => ConversationListInteractor());
  locator.registerFactory<ConversationReplyInteractor>(() => ConversationReplyInteractor());
  locator.registerFactory<CourseDetailsInteractor>(() => CourseDetailsInteractor());
  locator.registerFactory<CoursesInteractor>(() => CoursesInteractor());
  locator.registerFactory<CreateConversationInteractor>(() => CreateConversationInteractor());
  locator.registerFactory<DashboardInteractor>(() => DashboardInteractor());
  locator.registerFactory<DomainSearchInteractor>(() => DomainSearchInteractor());
  locator.registerFactory<ErrorReportInteractor>(() => ErrorReportInteractor());
  locator.registerFactory<EventDetailsInteractor>(() => EventDetailsInteractor());
  locator.registerFactory<ManageStudentsInteractor>(() => ManageStudentsInteractor());
  locator.registerFactory<SettingsInteractor>(() => SettingsInteractor());
  locator.registerFactory<ViewAttachmentInteractor>(() => ViewAttachmentInteractor());
  locator.registerFactory<WebLoginInteractor>(() => WebLoginInteractor());
  locator.registerFactory<WebViewInteractor>(() => WebViewInteractor());

  // Veneers and mockable dependencies
  locator.registerLazySingleton<AndroidIntentVeneer>(() => AndroidIntentVeneer());
  locator.registerLazySingleton<FlutterDownloaderVeneer>(() => FlutterDownloaderVeneer());
  locator.registerLazySingleton<PathProviderVeneer>(() => PathProviderVeneer());
  locator.registerLazySingleton<PermissionHandler>(() => PermissionHandler());

  // Other
  locator.registerLazySingleton<AlertCountNotifier>(() => AlertCountNotifier());
  locator.registerLazySingleton<NotificationUtil>(() => NotificationUtil());
  locator.registerLazySingleton<InboxCountNotifier>(() => InboxCountNotifier());
  locator.registerLazySingleton<QuickNav>(() => QuickNav());
}
