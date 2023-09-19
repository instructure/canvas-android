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
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_parent/network/api/heap_api.dart';
import 'package:flutter_parent/utils/features_utils.dart';
import 'package:flutter_parent/utils/debug_flags.dart';

/// Event names
/// The naming scheme for the majority of these is found in a google doc so that we can be consistent
/// across the platforms.
class AnalyticsEventConstants {
  static const ADD_STUDENT_DASHBOARD = 'add_student_dashboard';
  static const ADD_STUDENT_FAILURE = 'add_student_failure';
  static const ADD_STUDENT_MANAGE_STUDENTS = 'add_student_manage_students';
  static const ADD_STUDENT_SUCCESS = 'add_student_success';
  static const DARK_MODE_OFF = 'dark_mode_off';
  static const DARK_MODE_ON = 'dark_mode_on';
  static const DARK_WEB_MODE_OFF = 'dark_web_mode_off';
  static const DARK_WEB_MODE_ON = 'dark_web_mode_on';
  static const HC_MODE_OFF = 'hc_mode_off';
  static const HC_MODE_ON = 'hc_mode_on';
  static const HELP_DOMAIN_SEARCH = 'help_domain_search';
  static const HELP_LOGIN = 'help_login';
  static const LOGIN_FAILURE = 'login_failure';
  static const LOGIN_SUCCESS = 'login_success';
  static const LOGOUT = 'logout';
  static const QR_ACCOUNT_CREATION_CLICKED = 'qr_account_creation_clicked';
  static const QR_LOGIN_CLICKED = 'qr_code_login_clicked';
  static const QR_LOGIN_FAILURE = 'qr_code_login_failure';
  static const QR_LOGIN_SUCCESS = 'qr_code_login_success';
  static const QR_ACCOUNT_FAILURE = 'qr_account_creation_failure';
  static const QR_ACCOUNT_SUCCESS = 'qr_account_creation_success';
  static const RATING_DIALOG = 'rating_dialog';
  static const RATING_DIALOG_SHOW = 'rating_dialog_show';
  static const RATING_DIALOG_DONT_SHOW_AGAIN = 'rating_dialog_dont_show_again';
  static const REMINDER_ASSIGNMENT_CREATE = 'reminder_assignment';
  static const REMINDER_EVENT_CREATE = 'reminder_event';
  static const SWITCH_USERS = 'switch_users';
  static const TOKEN_REFRESH_FAILURE = 'token_refresh_failure';
  static const TOKEN_REFRESH_FAILURE_NO_SECRET = 'token_refresh_failure_no_secret';
  static const TOKEN_REFRESH_FAILURE_TOKEN_NOT_VALID = 'token_refresh_failure_token_not_valid';
  static const USER_PROPERTY_BUILD_TYPE = 'build_type';
  static const USER_PROPERTY_OS_VERSION = 'os_version';
  static const VIEWED_OLD_REMINDER_MESSAGE = 'viewed_old_reminder_message';
}

/// (Copied from canvas-api-2, make sure to stay in sync)
///
/// PARAMS
/// Due to the limits on custom params, we will mostly be using a mapping of the pre-defined params,
/// mappings will be recorded below. Make sure we are only using params where the data is relevant.
///
/// [ASSIGNMENT_ID]/DISCUSSION/ETC ID -> ITEM_ID There is also ITEM_CATEGORY if the event is vague regarding the type of item
/// [CANVAS_CONTEXT_ID] -> GROUP_ID
/// [DOMAIN_PARAM] -> AFFILIATION
/// [SCREEN_OF_ORIGIN] -> ORIGIN Used when events can originate from multiple locations
/// [STAR_RATING] -> The star rating a user gave in the rating dialog
/// [USER_CONTEXT_ID] -> CHARACTER
///
class AnalyticsParamConstants {
  static const ASSIGNMENT_ID = 'item_id';
  static const CANVAS_CONTEXT_ID = 'group_id';
  static const DOMAIN_PARAM = 'affiliation';
  static const SCREEN_OF_ORIGIN = 'origin';
  static const STAR_RATING = 'star_rating';
  static const USER_CONTEXT_ID = 'character';
}

class Analytics {

  HeapApi get _heap => HeapApi();
  /// Set the current screen in analytics
  void setCurrentScreen(String screenName) async {
    final usageMetricsEnabled = await FeaturesUtils.getUsageMetricFeatureFlag();
    if (kReleaseMode && usageMetricsEnabled) {
      await _heap.track(screenName);
    }

    if (DebugFlags.isDebug) {
      print('currentScreen: $screenName');
    }
  }

  /// Log an event to analytics (only in release mode).
  /// If isDebug, it will also print to the console
  ///
  /// Params
  /// * [event] should be one of [AnalyticsEventConstants]
  /// * [extras] a map of keys [AnalyticsParamConstants] to values. Use sparingly, we only get 25 unique parameters
  void logEvent(String event, {Map<String, dynamic> extras = const {}}) async {
    final usageMetricsEnabled = await FeaturesUtils.getUsageMetricFeatureFlag();
    if (kReleaseMode && usageMetricsEnabled) {
      await _heap.track(event, extras: extras);
    }

    if (DebugFlags.isDebug) {
      print('logEvent: $event - $extras');
    }
  }

  /// Logs a message to crashlytics to help when looking over crash logs (only in release mode).
  /// If isDebug, it will also print to the console
  void logMessage(String message) {
    if (kReleaseMode) {
      FirebaseCrashlytics.instance.log(message);
    }

    if (DebugFlags.isDebug) {
      print(message);
    }
  }

  /// Sets environment properties such as the build type and SDK int. This only needs to be called once per session.
  void setEnvironmentProperties() async {

  }
}
