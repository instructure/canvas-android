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

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_crashlytics/flutter_crashlytics.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/screens/crash_screen.dart';

class CrashUtils {
  static Future<void> init() async {
    if (kReleaseMode) await FlutterCrashlytics().initialize();

    // Set up custom crash screen
    ErrorWidget.builder = (error) {
      // Only need to dump errors in debug, release builds call onError
      if (kReleaseMode) {
        FlutterCrashlytics().log('Widget Crash');
      } else {
        FlutterError.dumpErrorToConsole(error);
      }
      return CrashScreen(error);
    };

    // Set up error handling
    FlutterError.onError = (error) {
      if (kReleaseMode) {
        reportCrash(error.exception, error.stack);
      } else {
        // Manually handle debug reporting here, as this console formatting is nicer than simple print(stacktrace)
        FlutterError.dumpErrorToConsole(error);
      }
    };
  }

  /// Report a crash to crashlytics in release mode, otherwise only a stack trace is printed
  static reportCrash(dynamic exception, StackTrace stacktrace) async {
    print('Caught exception: $exception');
    debugPrintStack(stackTrace: stacktrace);

    // Report to Crashlytics, only in release mode
    if (!kReleaseMode) return;

    // Set any user info that will help to debug the issue
    await Future.wait([
      FlutterCrashlytics().setInfo('domain', ApiPrefs.getDomain() ?? 'null'),
      FlutterCrashlytics().setInfo('user_id', ApiPrefs.getUser()?.id ?? 'null'),
    ]);

    await FlutterCrashlytics().reportCrash(exception, stacktrace, forceCrash: false);
  }
}
