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
import 'package:flutter_parent/models/lock_info.dart';
import 'package:flutter_parent/models/locked_module.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/models/submission_wrapper.dart';
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
      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.NONE);
    });

    test('returns NONE for on paper submission type', () {
      final assignment = _mockAssignment(types: [SubmissionTypes.onPaper]);
      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.NONE);
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

      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.MISSING);
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

      expect(assignment.getStatus(studentId: studentId), SubmissionStatus.NOT_SUBMITTED);
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

  group('isFullyLocked', () {
    test('returns false when there is no lock info', () {
      final assignment = _mockAssignment();

      expect(assignment.isFullyLocked, false);
    });

    test('returns true when there is lock info with module name', () {
      final lockInfo = LockInfo((b) => b
        ..contextModule = LockedModule((m) => m
          ..id = ''
          ..contextId = ''
          ..isRequireSequentialProgress = false
          ..name = 'name').toBuilder());
      final assignment = _mockAssignment().rebuild((b) => b..lockInfo = lockInfo.toBuilder());

      expect(assignment.isFullyLocked, true);
    });

    test('returns true when there is lock info with unlock at', () {
      final unlockDate = DateTime(2100);
      final lockInfo = LockInfo((b) => b..unlockAt = unlockDate);
      final assignment = _mockAssignment().rebuild((b) => b..lockInfo = lockInfo.toBuilder());

      expect(assignment.isFullyLocked, true);
    });

    test('returns false when there is lock info with unlock at in the past', () {
      final unlockDate = DateTime(2000);
      final lockInfo = LockInfo((b) => b..unlockAt = unlockDate);
      final assignment = _mockAssignment().rebuild((b) => b..lockInfo = lockInfo.toBuilder());

      expect(assignment.isFullyLocked, false);
    });
  });

  group('isDiscussion', () {
    test('isDiscussion returns true when submission types contains discussion_topic', () {
      final assignment = _mockAssignment().rebuild(
        (b) => b..submissionTypes = ListBuilder([SubmissionTypes.discussionTopic]),
      );

      expect(assignment.isDiscussion, isTrue);
    });

    test('isDiscussion returns false when submission types does not contain discussion_topic', () {
      final assignment = _mockAssignment().rebuild((b) => b..submissionTypes = ListBuilder([]));

      expect(assignment.isDiscussion, isFalse);
    });
  });

  group('isQuiz', () {
    test('isQuiz returns true when submission types contains online_quiz', () {
      final assignment = _mockAssignment().rebuild(
        (b) => b..submissionTypes = ListBuilder([SubmissionTypes.onlineQuiz]),
      );

      expect(assignment.isQuiz, isTrue);
    });

    test('isQuiz returns false when submission types does not contain online_quiz', () {
      final assignment = _mockAssignment().rebuild((b) => b..submissionTypes = ListBuilder([]));

      expect(assignment.isQuiz, isFalse);
    });
  });
}

Assignment _mockAssignment({
  DateTime? dueAt,
  SubmissionBuilder? submission,
  List<SubmissionTypes> types = const [SubmissionTypes.onlineTextEntry],
}) {
  List<Submission> submissionList = submission != null ? [submission.build()] : [];

  SubmissionWrapper submissionWrapper =
      SubmissionWrapper((b) => b..submissionList = BuiltList<Submission>.from(submissionList).toBuilder());

  return Assignment((b) => b
    ..id = ''
    ..courseId = ''
    ..assignmentGroupId = ''
    ..position = 0
    ..dueAt = dueAt
    ..submissionWrapper = submissionWrapper.toBuilder()
    ..submissionTypes = BuiltList<SubmissionTypes>(types).toBuilder());
}

Submission _mockSubmission(String studentId) => Submission((b) => b
  ..assignmentId = ''
  ..userId = studentId);
