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
import 'package:flutter_parent/models/submission.dart';
import 'package:test/test.dart';

void main() {
  final _submission = Submission((b) => b
    ..userId = '123'
    ..assignmentId = '123'
    ..grade = 'A'
    ..submittedAt = DateTime.now()
    ..isLate = false);

  test('Single submission', () {
    print(_submission.toString());
  });

  test('List of submissions', () {});
}
