/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter_parent/api/alert_api.dart';
import 'package:flutter_parent/api/assignment_api.dart';
import 'package:flutter_parent/api/auth_api.dart';
import 'package:flutter_parent/api/course_api.dart';
import 'package:flutter_parent/screens/alerts/alerts_interactor.dart';
import 'package:flutter_parent/screens/courses/courses_interactor.dart';
import 'package:flutter_parent/screens/courses/details/course_details_interactor.dart';
import 'package:flutter_parent/screens/domain_search/domain_search_interactor.dart';
import 'package:flutter_parent/screens/web_login/web_login_interactor.dart';
import 'package:get_it/get_it.dart';

GetIt locator = GetIt.instance;

void setupLocator() {
  locator.registerLazySingleton<AlertsApi>(() => AlertsApi());
  locator.registerLazySingleton<AssignmentApi>(() => AssignmentApi());
  locator.registerLazySingleton<AuthApi>(() => AuthApi());
  locator.registerLazySingleton<CourseApi>(() => CourseApi());

  locator.registerFactory<AlertsInteractor>(() => AlertsInteractor());
  locator.registerFactory<CourseDetailsInteractor>(() => CourseDetailsInteractor());
  locator.registerFactory<CoursesInteractor>(() => CoursesInteractor());
  locator.registerFactory<DomainSearchInteractor>(() => DomainSearchInteractor());
  locator.registerFactory<WebLoginInteractor>(() => WebLoginInteractor());
}
