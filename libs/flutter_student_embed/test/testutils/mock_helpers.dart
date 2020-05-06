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

// Create a mocked RemoteConfig object.
// If valueSettings != null, then (1) a mocked settings fetch will occur, and (2) the retrieved
// settings will correspond the specified values.
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_student_embed/network/api/course_api.dart';
import 'package:flutter_student_embed/network/api/planner_api.dart';
import 'package:flutter_student_embed/utils/quick_nav.dart';
import 'package:mockito/mockito.dart';
import 'package:sqflite/sqflite.dart';

class MockCourseApi extends Mock implements CourseApi {}

class MockDatabase extends Mock implements Database {}

class MockDio extends Mock implements Dio {}

class MockHttpClient extends Mock implements HttpClient {}

class MockHttpClientRequest extends Mock implements HttpClientRequest {}

class MockHttpClientResponse extends Mock implements HttpClientResponse {}

class MockHttpHeaders extends Mock implements HttpHeaders {}

class MockNavigatorObserver extends Mock implements NavigatorObserver {}

class MockPlannerApi extends Mock implements PlannerApi {}

class MockQuickNav extends Mock implements QuickNav {}
