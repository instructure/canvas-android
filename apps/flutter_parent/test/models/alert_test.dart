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

import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('Alert type is converted to correct api string', () {
    expect('course_announcement', AlertType.courseAnnouncement.toApiString());
    expect('institution_announcement', AlertType.institutionAnnouncement.toApiString());
    expect('assignment_grade_high', AlertType.assignmentGradeHigh.toApiString());
    expect('assignment_grade_low', AlertType.assignmentGradeLow.toApiString());
    expect('assignment_missing', AlertType.assignmentMissing.toApiString());
    expect('course_grade_high', AlertType.courseGradeHigh.toApiString());
    expect('course_grade_low', AlertType.courseGradeLow.toApiString());
  });

  test('isSwitch returns true when switch, false when not', () {
    expect(true, AlertType.assignmentMissing.isSwitch());
    expect(true, AlertType.courseAnnouncement.isSwitch());
    expect(true, AlertType.institutionAnnouncement.isSwitch());
    expect(true, AlertType.assignmentMissing.isSwitch());
    expect(false, AlertType.courseGradeLow.isSwitch());
    expect(false, AlertType.courseGradeHigh.isSwitch());
    expect(false, AlertType.assignmentGradeLow.isSwitch());
    expect(false, AlertType.assignmentGradeHigh.isSwitch());
  });
}
