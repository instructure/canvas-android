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
import 'package:flutter_parent/models/submission.dart';
import 'package:test/test.dart';

void main() {
  const String studentId = '1337';

  group('isGraded', () {
    test('returns false if there is not a grade', () {
      final submission = Submission((b) => b
        ..assignmentId = ''
        ..grade = null
        ..userId = studentId);
      expect(submission.isGraded(), false);
    });

    test('returns false if the workflow is pending_review', () {
      final submission = Submission((b) => b
        ..assignmentId = ''
        ..grade = 'a'
        ..workflowState = 'pending_review'
        ..userId = studentId);
      expect(submission.isGraded(), false);
    });

    test('returns false if there is not a postedAt', () {
      final submission = Submission((b) => b
        ..assignmentId = ''
        ..grade = 'a'
        ..workflowState = 'graded'
        ..postedAt = null
        ..userId = studentId);
      expect(submission.isGraded(), false);
    });

    test('returns true if all reqiured fields are set', () {
      final submission = Submission((b) => b
        ..assignmentId = ''
        ..grade = 'a'
        ..workflowState = 'graded'
        ..postedAt = DateTime.now()
        ..userId = studentId);
      expect(submission.isGraded(), true);
    });
  });
}
