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

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_student_embed/student_flutter_app.dart';
import 'package:flutter_student_embed/utils/crash_utils.dart';
import 'package:flutter_student_embed/utils/db/db_util.dart';
import 'package:flutter_student_embed/utils/native_comm.dart';
import 'package:flutter_student_embed/utils/service_locator.dart';

import 'network/utils/api_prefs.dart';

void main() async {
  runZoned<Future<void>>(() async {
    WidgetsFlutterBinding.ensureInitialized();
    NativeComm.init();

    await Future.wait([
      ApiPrefs.init(),
      CrashUtils.init(),
      DbUtil.init(),
    ]);
    setupLocator();

    runApp(StudentFlutterApp());
  }, onError: (error, stacktrace) => CrashUtils.reportCrash(error, stacktrace));
}
