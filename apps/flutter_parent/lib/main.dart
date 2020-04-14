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

import 'package:flutter/material.dart';
import 'package:flutter_downloader/flutter_downloader.dart';
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

void main() {
  runZoned<Future<void>>(() async {
    WidgetsFlutterBinding.ensureInitialized();

    await Future.wait([
      ApiPrefs.init(),
      ThemePrefs.init(),
      CrashUtils.init(),
      FlutterDownloader.initialize(),
      DbUtil.init(),
      RemoteConfigUtils.initialize()
    ]);
    setupLocator();
    PandaRouter.init();

    // Currently must be initialized after locator has been set up. This may change once routing is implemented.
    await NotificationUtil.init();

    await locator<OldAppMigration>().performMigrationIfNecessary(); // ApiPrefs must be initialized before calling this

    runApp(ParentApp());
  }, onError: (error, stacktrace) => CrashUtils.reportCrash(error, stacktrace));
}
