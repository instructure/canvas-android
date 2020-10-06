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
import 'package:flutter_student_embed/network/api/course_api.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_student_embed/screens/to_do/create_update_to_do_screen_interactor.dart';
import 'package:flutter_student_embed/utils/db/calendar_filter_db.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:get_it/get_it.dart';
import 'package:sqflite/sqflite.dart';

import 'db/db_util.dart';

GetIt locator = GetIt.instance;

void setupLocator() {
  // APIs
  locator.registerLazySingleton<CourseApi>(() => CourseApi());
  locator.registerLazySingleton<PlannerApi>(() => PlannerApi());

  // DB helpers
  locator.registerLazySingleton<CalendarFilterDb>(() => CalendarFilterDb());
  locator.registerLazySingleton<Database>(() => DbUtil.instance);

  // Interactors
  locator.registerFactory<CalendarFilterListInteractor>(() => CalendarFilterListInteractor());
  locator.registerFactory<CreateUpdateToDoScreenInteractor>(() => CreateUpdateToDoScreenInteractor());

  // Other
  locator.registerLazySingleton<QuickNav>(() => QuickNav());
  locator.registerLazySingleton<FirebaseCrashlytics>(() => FirebaseCrashlytics.instance);
}
