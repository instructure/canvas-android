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
import 'package:flutter_parent/utils/crash_utils.dart';
import 'package:flutter_parent/utils/design/theme_prefs.dart';
import 'package:flutter_parent/utils/service_locator.dart';

void main() {
  runZoned<Future<void>>(() async {
    WidgetsFlutterBinding.ensureInitialized();

    await Future.wait([
      ApiPrefs.init(),
      ThemePrefs.init(),
      CrashUtils.init(),
      FlutterDownloader.initialize(),
    ]);
    setupLocator();

    runApp(ParentApp());
  }, onError: (error, stacktrace) => CrashUtils.reportCrash(error, stacktrace));
}
