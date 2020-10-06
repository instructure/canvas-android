/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import 'dart:io';

import 'package:dio/dio.dart';
import 'package:firebase_crashlytics/firebase_crashlytics.dart';
import 'package:flutter/material.dart';
import 'package:flutter_student_embed/network/api/course_api.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/screens/calendar/calendar_widget/calendar_filter_screen/calendar_filter_list_interactor.dart';
import 'package:flutter_student_embed/utils/db/calendar_filter_db.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:mockito/mockito.dart';
import 'package:sqflite/sqflite.dart';

class MockCourseApi extends Mock implements CourseApi {}

class MockDatabase extends Mock implements Database {}

class MockDio extends Mock implements Dio {}

class MockFirebase extends Mock implements FirebaseCrashlytics {}

class MockHttpClient extends Mock implements HttpClient {}

class MockHttpClientRequest extends Mock implements HttpClientRequest {}

class MockHttpClientResponse extends Mock implements HttpClientResponse {}

class MockHttpHeaders extends Mock implements HttpHeaders {}

class MockNavigatorObserver extends Mock implements NavigatorObserver {}

class MockPlannerApi extends Mock implements PlannerApi {}

class MockQuickNav extends Mock implements QuickNav {}

class MockCalendarFilterDb extends Mock implements CalendarFilterDb {}

class MockCalendarFilterListInteractor extends Mock implements CalendarFilterListInteractor {}
