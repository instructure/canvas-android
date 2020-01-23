//  Copyright (C) 2019 - present Instructure, Inc.
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, version 3 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:test/test.dart';

void main() {
  const String studentId = '1337';

  group('isSubmittable', () {
    test('returns true if submission types contain discussion_topic', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper, SubmissionTypes.discussionTopic]);
      expect(assignment.isSubmittable(), true);
    });

    test('returns true if submission types contain online_quiz', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper, SubmissionTypes.onlineQuiz]);
      expect(assignment.isSubmittable(), true);
    });

    test('returns true if submission types contain external_tool', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper, SubmissionTypes.externalTool]);
      expect(assignment.isSubmittable(), true);
    });

    test('returns true if submission types contain online_text_entry', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper, SubmissionTypes.onlineTextEntry]);
      expect(assignment.isSubmittable(), true);
    });

    test('returns true if submission types contain online_url', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper, SubmissionTypes.onlineUrl]);
      expect(assignment.isSubmittable(), true);
    });

    test('returns true if submission types contain online_upload', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper, SubmissionTypes.onlineUpload]);
      expect(assignment.isSubmittable(), true);
    });

    test('returns true if submission types contain media_recording', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper, SubmissionTypes.mediaRecording]);
      expect(assignment.isSubmittable(), true);
    });

    test('returns false if submission types contain only onPaper', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper]);
      expect(assignment.isSubmittable(), false);
    });

    test('returns false if submission types contain only none', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.none]);
      expect(assignment.isSubmittable(), false);
    });

    test('returns false if submission types contain only non-submittable types', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.none, SubmissionTypes.onPaper]);
      expect(assignment.isSubmittable(), false);
    });
  });

  group('getStatus', () {
    test('returns NONE for none submission type', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.none]);
      expect(assignment.getStatus(), SubmissionStatus.NONE);
    });

    test('returns NONE for on paper submission type', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper]);
      expect(assignment.getStatus(), SubmissionStatus.NONE);
    });

    test('returns LATE for a late submission', () {
      final assignment = _mockAssignment(submission: _mockSubmission(studentId).toBuilder()..isLate = true);
      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.LATE);
    });

    test('returns MISSING for a missing submission', () {
      final assignment = _mockAssignment(submission: _mockSubmission(studentId).toBuilder()..missing = true);
      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.MISSING);
    });

    test('returns MISSING for a pass due assignment with a null submission', () {
      final past = DateTime.now().subtract(Duration(seconds: 1));
      final assignment = _mockAssignment(dueAt: past, submission: null);

      expect(assignment.getStatus(), SubmissionStatus.MISSING);
    });

    test('returns MISSING for a pass due assignment with an empty (server generated) submission', () {
      final past = DateTime.now().subtract(Duration(seconds: 1));
      final submission = _mockSubmission(studentId).toBuilder()
        ..attempt = 0
        ..grade = null;
      final assignment = _mockAssignment(dueAt: past, submission: submission);

      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.MISSING);
    });

    test('returns NOT_SUBMITTED for a missing submission with type external_tool', () {
      final past = DateTime.now().subtract(Duration(seconds: 1));
      final assignment = _mockAssignment(dueAt: past, submission: null)
          .rebuild((b) => b..submissionTypes = BuiltList.of([SubmissionTypes.externalTool]).toBuilder());

      expect(assignment.getStatus(), SubmissionStatus.NOT_SUBMITTED);
    });

    test('returns NOT_SUBMITTED for a submission with no submitted at time', () {
      final assignment = _mockAssignment(submission: _mockSubmission(studentId).toBuilder()..submittedAt = null);
      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.NOT_SUBMITTED);
    });

    test('returns SUBMITTED for a submission with a submitted at time', () {
      final past = DateTime.now().subtract(Duration(seconds: 1));
      final assignment = _mockAssignment(submission: _mockSubmission(studentId).toBuilder()..submittedAt = past);

      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.SUBMITTED);
    });

    test('returns SUBMITTED for a passed due submission with a valid attempt', () {
      final past = DateTime.now().subtract(Duration(seconds: 1));
      final submission = _mockSubmission(studentId).toBuilder()
        ..attempt = 1
        ..submittedAt = DateTime.now();
      final assignment = _mockAssignment(dueAt: past, submission: submission);

      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.SUBMITTED);
    });
  });
}

Assignment _mockAssignment({
  DateTime dueAt,
  SubmissionBuilder submission,
  List<SubmissionTypes> types = const [SubmissionTypes.onlineTextEntry],
}) =>
    Assignment((b) => b
      ..id = ''
      ..courseId = ''
      ..assignmentGroupId = ''
      ..position = 0
      ..dueAt = dueAt
      ..submissionList = BuiltList<Submission>(submission != null ? [submission.build()] : []).toBuilder()
      ..submissionTypes = BuiltList<SubmissionTypes>(types).toBuilder());

Submission _mockSubmission(String studentId) => Submission((b) => b
  ..assignmentId = ''
  ..userId = studentId);
