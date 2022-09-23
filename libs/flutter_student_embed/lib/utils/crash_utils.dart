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
import 'package:flutter/widgets.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/screens/crash_screen.dart';
import 'package:flutter_student_embed/utils/service_locator.dart';

class CrashUtils {
  static Future<void> init() async {
    FirebaseCrashlytics firebase = locator<FirebaseCrashlytics>();

    // Set up error handling
    FlutterError.onError = (error) async {
      // We don't know how the crashlytics stores the userId so we just set it to empty to make sure we don't log it.
      await firebase.setUserIdentifier('');
      firebase.recordFlutterError(error);
    };

    if (kReleaseMode) {
      await firebase.setCrashlyticsCollectionEnabled(true);
    } else {
      await firebase.setCrashlyticsCollectionEnabled(false);
    }

    // Set up custom crash screen
    ErrorWidget.builder = (error) {
      // Only need to dump errors in debug, release builds call onError
      if (kReleaseMode) {
        firebase.recordFlutterError(error);
        firebase.log('Widget Crash');
      } else {
        FlutterError.dumpErrorToConsole(error);
      }
      return CrashScreen(error);
    };
  }
}
