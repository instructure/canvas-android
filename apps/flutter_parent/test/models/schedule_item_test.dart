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

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/plannable.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/models/planner_submission.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/models/submission_wrapper.dart';
import 'package:test/test.dart';

void main() {
  final contextId = '1234';
  final contextType = 'course';
  final courseName = 'hodor';
  final date = DateTime.now();

  final scheduleItem = ScheduleItem((b) => b
    ..id = ''
    ..type = ScheduleItem.apiTypeCalendar
    ..isAllDay = false
    ..isHidden = false
    ..effectiveContextCode = '${contextType}_$contextId');

  final assignment = Assignment((b) => b
    ..courseId = ''
    ..assignmentGroupId = ''
    ..submissionTypes = ListBuilder([SubmissionTypes.onlineUrl])
    ..position = 0
    ..id = '123');

  final submission = Submission((b) => b
    ..assignmentId = '123'
    ..userId = '123');

  group('context helpers', () {
    test('returns a valid id for getContextId', () {
      final result = scheduleItem.getContextId();

      expect(result, contextId);
    });

    test('returns a valid context type for getContextType', () {
      final result = scheduleItem.getContextType();

      expect(result, contextType);
    });

    test('returns a valid id for getContextId when effectiveContextCode is null', () {
      final newContextId = 'hodor';
      final item = scheduleItem.rebuild((b) => b
        ..contextCode = '${contextType}_$newContextId'
        ..effectiveContextCode = null);

      final result = item.getContextId();

      expect(result, newContextId);
    });

    test('returns a valid context type for getContextType when effectiveContextCode is null', () {
      final newContextType = 'group';
      final item = scheduleItem.rebuild((b) => b
        ..contextCode = '${newContextType}_$contextId'
        ..effectiveContextCode = null);

      final result = item.getContextType();

      expect(result, newContextType);
    });
  });

  group('Item type helpers', () {
    test('type of ScheduleItem.apiTypeCalendar results in event type', () {
      final result = scheduleItem.getItemType();

      expect(result, ScheduleItemType.event);
    });

    test('assignment.isQuiz results in quiz type', () {
      final newAssignment = assignment.rebuild((b) => b..submissionTypes = ListBuilder([SubmissionTypes.onlineQuiz]));
      final item = scheduleItem.rebuild((b) => b
        ..type = ScheduleItem.apiTypeAssignment
        ..assignment = newAssignment.toBuilder());
      final result = item.getItemType();

      expect(result, ScheduleItemType.quiz);
    });

    test('assignment.isDiscussion results in discussion type', () {
      final newAssignment =
          assignment.rebuild((b) => b..submissionTypes = ListBuilder([SubmissionTypes.discussionTopic]));
      final item = scheduleItem.rebuild((b) => b
        ..type = ScheduleItem.apiTypeAssignment
        ..assignment = newAssignment.toBuilder());
      final result = item.getItemType();

      expect(result, ScheduleItemType.discussion);
    });

    test('else, results in an assignment type', () {
      final item = scheduleItem.rebuild((b) => b
        ..type = ScheduleItem.apiTypeAssignment
        ..assignment = assignment.toBuilder());
      final result = item.getItemType();

      expect(result, ScheduleItemType.assignment);
    });

    test('type of ScheduleItem.apiTypeCalendar results in event type string', () {
      final result = scheduleItem.getItemTypeAsString();

      expect(result, ScheduleItem.scheduleTypeEvent);
    });

    test('assignment.isQuiz results in quiz type string', () {
      final newAssignment = assignment.rebuild((b) => b..submissionTypes = ListBuilder([SubmissionTypes.onlineQuiz]));
      final item = scheduleItem.rebuild((b) => b
        ..type = ScheduleItem.apiTypeAssignment
        ..assignment = newAssignment.toBuilder());
      final result = item.getItemTypeAsString();

      expect(result, ScheduleItem.scheduleTypeQuiz);
    });

    test('assignment.isDiscussion results in discussion type string', () {
      final newAssignment =
          assignment.rebuild((b) => b..submissionTypes = ListBuilder([SubmissionTypes.discussionTopic]));
      final item = scheduleItem.rebuild((b) => b
        ..type = ScheduleItem.apiTypeAssignment
        ..assignment = newAssignment.toBuilder());
      final result = item.getItemTypeAsString();

      expect(result, ScheduleItem.scheduleTypeDiscussion);
    });

    test('else, results in an assignment type string', () {
      final item = scheduleItem.rebuild((b) => b
        ..type = ScheduleItem.apiTypeAssignment
        ..assignment = assignment.toBuilder());
      final result = item.getItemTypeAsString();

      expect(result, ScheduleItem.apiTypeAssignment);
    });
  });

  group('Planner Item Conversion', () {
    test('PlannerSubmission returns null for null assignment', () {
      final item = scheduleItem.rebuild((b) => b..assignment = null);

      final result = item.getPlannerSubmission();

      expect(result, isNull);
    });

    test('PlannerSubmission returns submitted false for null submission', () {
      final newAssignment =
          assignment.rebuild((b) => b..submissionWrapper = SubmissionWrapper((b) => b..submission = null).toBuilder());

      final item = scheduleItem.rebuild((b) => b..assignment = newAssignment.toBuilder());

      final result = item.getPlannerSubmission();

      expect(result, isNotNull);
      expect(result!.submitted, isFalse);
    });

    test('returns valid PlannerSubmission for valid submission', () {
      final newAssignment = assignment.rebuild(
          (b) => b..submissionWrapper = SubmissionWrapper((b) => b..submission = submission.toBuilder()).toBuilder());

      final item = scheduleItem.rebuild((b) => b..assignment = newAssignment.toBuilder());

      final result = item.getPlannerSubmission();
      final expectedPlannerSubmission = PlannerSubmission((b) => b
        ..submitted = submission.submittedAt != null
        ..excused = submission.excused
        ..graded = submission.isGraded()
        ..late = submission.isLate
        ..missing = submission.missing);

      expect(result, isNotNull);
      expect(result, expectedPlannerSubmission);
    });

    test('returns valid PlannerItem for valid scheduleItem', () {
      final newAssignment = assignment.rebuild((b) => b
        ..pointsPossible = 20.0
        ..dueAt = date
        ..submissionWrapper = SubmissionWrapper((b) => b..submission = submission.toBuilder()).toBuilder());

      final item = scheduleItem.rebuild((b) => b
        ..effectiveContextCode = '${contextType}_$contextId'
        ..startAt = date
        ..htmlUrl = ''
        ..title = 'hodor'
        ..assignment = newAssignment.toBuilder());

      final result = item.toPlannerItem(courseName);
      final plannable = Plannable((b) => b
        ..id = item.id
        ..title = item.title
        ..pointsPossible = newAssignment.pointsPossible
        ..dueAt = newAssignment.dueAt
        ..assignmentId = newAssignment.id);
      final expectedResult = PlannerItem((b) => b
        ..courseId = contextId
        ..contextType = contextType
        ..contextName = courseName
        ..plannableType = ScheduleItem.scheduleTypeEvent
        ..plannable = plannable.toBuilder()
        ..plannableDate = item.startAt
        ..htmlUrl = item.htmlUrl
        ..submissionStatus = item.getPlannerSubmission()?.toBuilder());

      expect(result, isNotNull);
      expect(result, expectedResult);
    });

    test('returns valid PlannerItem for valid scheduleItem with null assignment', () {
      final item = scheduleItem.rebuild((b) => b
        ..effectiveContextCode = '${contextType}_$contextId'
        ..startAt = date
        ..htmlUrl = ''
        ..title = 'hodor');

      final result = item.toPlannerItem(courseName);
      final plannable = Plannable((b) => b
        ..id = item.id
        ..title = item.title);
      final expectedResult = PlannerItem((b) => b
        ..courseId = contextId
        ..contextType = contextType
        ..contextName = courseName
        ..plannableType = ScheduleItem.scheduleTypeEvent
        ..plannable = plannable.toBuilder()
        ..plannableDate = item.startAt
        ..htmlUrl = item.htmlUrl
        ..submissionStatus = item.getPlannerSubmission()?.toBuilder());

      expect(result, isNotNull);
      expect(result, expectedResult);
    });
  });
}
