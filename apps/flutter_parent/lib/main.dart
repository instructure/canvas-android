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

import 'dart:async';
import 'dart:io';

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_downloader/flutter_downloader.dart';
import 'package:flutter_parent/network/utils/analytics.dart';
import 'package:flutter_parent/network/utils/api_prefs.dart';
import 'package:flutter_parent/parent_app.dart';
import 'package:flutter_parent/router/panda_router.dart';
import 'package:flutter_parent/utils/crash_utils.dart';
import 'package:flutter_parent/utils/db/db_util.dart';
import 'package:flutter_parent/utils/design/theme_prefs.dart';
import 'package:flutter_parent/utils/notification_util.dart';
import 'package:flutter_parent/utils/old_app_migration.dart';
import 'package:flutter_parent/utils/remote_config_utils.dart';
import 'package:flutter_parent/utils/service_locator.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();

  setupLocator();
  runZonedGuarded<Future<void>>(() async {
    await Future.wait([
      ApiPrefs.init(),
      ThemePrefs.init(),
      RemoteConfigUtils.initialize(),
      CrashUtils.init(),
      FlutterDownloader.initialize(),
      DbUtil.init()
    ]);
    PandaRouter.init();

    // This completer waits for the app to be built before allowing the notificationUtil to handle notifications
    final Completer<void> _appCompleter = Completer<void>();
    NotificationUtil.init(_appCompleter);

    // TODO Only migrate ol logins from the Android app. If we want to do this for iOS we need to channel method calls.
    if (Platform.isAndroid) {
      await locator<OldAppMigration>().performMigrationIfNecessary(); // ApiPrefs must be initialized before calling this
    }

    // Set environment properties for analytics. No need to await this.
    locator<Analytics>().setEnvironmentProperties();

    runApp(ParentApp(_appCompleter));
  }, recordError);
}

Future<void> recordError(dynamic exception, StackTrace stack,
    {dynamic reason,
      Iterable<DiagnosticsNode> information,
      bool printDetails}) async {

}
