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
import 'package:flutter_driver/flutter_driver.dart';

/// This is kind of a "super page" that will be showing at the same time as
/// the course grades page or the syllabus page.  (Grades/syllabus are
/// nested in the course details page.)
class CourseDetailsPage {
  static Future<void> selectSyllabus(FlutterDriver? driver) async {
    await driver?.tap(find.text("SYLLABUS"));
  }

  static Future<void> selectGrades(FlutterDriver? driver) async {
    await driver?.tap(find.text("GRADES"));
  }

  static Future<void> selectSummary(FlutterDriver? driver) async {
    await driver?.tap(find.text("SUMMARY"));
  }

  static Future<void> initiateCreateEmail(FlutterDriver? driver) async {
    await driver?.tap(find.byType('FloatingActionButton'));
  }
}
