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

import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_student_embed/student_flutter_app.dart';
import 'package:flutter_student_embed/utils/crash_utils.dart';
import 'package:flutter_student_embed/utils/db/db_util.dart';
import 'package:flutter_student_embed/utils/native_comm.dart';
import 'package:flutter_student_embed/utils/service_locator.dart';

import 'network/utils/api_prefs.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();

  runZonedGuarded<Future<void>>(() async {
    NativeComm.init();
    setupLocator();

    await Future.wait([
      ApiPrefs.init(),
      CrashUtils.init(),
      DbUtil.init(),
    ]);

    runApp(StudentFlutterApp());
  }, FirebaseCrashlytics.instance.recordError);
}
