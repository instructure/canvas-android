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
import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/screens/crash_screen.dart';

class CrashUtils {
  static Future<void> init() async {
    // Set up error handling
    FlutterError.onError = (error) async {
      await FirebaseCrashlytics.instance
          .setUserIdentifier('domain: ${ApiPrefs.getDomain() ?? 'null'} user_id: ${ApiPrefs.getUser()?.id ?? 'null'}');
      FirebaseCrashlytics.instance.recordFlutterError(error);
    };

    if (kReleaseMode) {
      await FirebaseCrashlytics.instance.setCrashlyticsCollectionEnabled(true);
    } else {
      await FirebaseCrashlytics.instance.setCrashlyticsCollectionEnabled(false);
    }

    // Set up custom crash screen
    ErrorWidget.builder = (error) {
      // Only need to dump errors in debug, release builds call onError
      if (kReleaseMode) {
        FirebaseCrashlytics.instance.recordFlutterError(error);
        FirebaseCrashlytics.instance.log('Widget Crash');
      } else {
        FlutterError.dumpErrorToConsole(error);
      }
      return CrashScreen(error);
    };
  }
}
