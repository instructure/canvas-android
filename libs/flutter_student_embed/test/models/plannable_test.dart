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

import 'package:flutter_student_embed/models/plannable.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('contextCode', () {
    Plannable _plannable = Plannable((b) => b
      ..id = 'plannable_123'
      ..title = "Plannable 123");

    test('returns empty string when no course, group, or user ID is specified', () {
      expect(_plannable.contextCode(), isEmpty);
    });

    test('returns course context when course ID is specified', () {
      Plannable plannable = _plannable.rebuild((b) => b..courseId = '123');
      expect(plannable.contextCode(), 'course_123');
    });

    test('returns group context when group ID is specified', () {
      Plannable plannable = _plannable.rebuild((b) => b..groupId = '123');
      expect(plannable.contextCode(), 'group_123');
    });

    test('returns course context when user ID is specified', () {
      Plannable plannable = _plannable.rebuild((b) => b..userId = '123');
      expect(plannable.contextCode(), 'user_123');
    });
  });
}
