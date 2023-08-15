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
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/plannable.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/models/planner_submission.dart';

import 'assignment.dart';
import 'assignment_override.dart';

part 'schedule_item.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class ScheduleItem implements Built<ScheduleItem, ScheduleItemBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<ScheduleItem> get serializer => _$scheduleItemSerializer;

  static const apiTypeCalendar = 'event';

  static const apiTypeAssignment = 'assignment';

  String get id;

  String? get title;

  String? get description;

  @BuiltValueField(wireName: 'start_at')
  DateTime? get startAt;

  @BuiltValueField(wireName: 'end_at')
  DateTime? get endAt;

  @BuiltValueField(wireName: 'all_day')
  bool get isAllDay;

  @BuiltValueField(wireName: 'all_day_date')
  DateTime? get allDayDate;

  @BuiltValueField(wireName: 'location_address')
  String? get locationAddress;

  String get type; // Either 'event' or 'assignment'

  @BuiltValueField(wireName: 'location_name')
  String? get locationName;

  @BuiltValueField(wireName: 'html_url')
  String? get htmlUrl;

  @BuiltValueField(wireName: 'context_code')
  String? get contextCode;

  @BuiltValueField(wireName: 'effective_context_code')
  String? get effectiveContextCode;

  @BuiltValueField(wireName: 'hidden')
  bool? get isHidden;

  Assignment? get assignment;

  @BuiltValueField(wireName: 'assignment_overrides')
  BuiltList<AssignmentOverride>? get assignmentOverrides;

  ScheduleItem._();
  factory ScheduleItem([void Function(ScheduleItemBuilder) updates]) = _$ScheduleItem;

  static void _initializeBuilder(ScheduleItemBuilder b) => b
    ..id = ''
    ..type = apiTypeCalendar
    ..isAllDay = false
    ..isHidden = false;

  /// These values are used to map to the string event type names used by planner items
  static const scheduleTypeAssignment = 'assignment';
  static const scheduleTypeEvent = 'calendar_event';
  static const scheduleTypeDiscussion = 'discussion_topic';
  static const scheduleTypeQuiz = 'quiz';

  ScheduleItemType getItemType() {
    if (type == ScheduleItem.apiTypeCalendar) return ScheduleItemType.event;
    if (assignment?.isQuiz == true) return ScheduleItemType.quiz;
    if (assignment?.isDiscussion == true) return ScheduleItemType.discussion;
    return ScheduleItemType.assignment;
  }

  String getItemTypeAsString() {
    if (type == ScheduleItem.apiTypeCalendar) return scheduleTypeEvent;
    if (assignment?.isQuiz == true) return scheduleTypeQuiz;
    if (assignment?.isDiscussion == true) return scheduleTypeDiscussion;
    return scheduleTypeAssignment;
  }

  String getContextId() {
    if (effectiveContextCode != null) {
      return _parseContextCode(effectiveContextCode!);
    } else {
      return _parseContextCode(contextCode!);
    }
  }

  String getContextType() {
    if (effectiveContextCode != null) {
      return _parseContextType(effectiveContextCode!);
    } else {
      return _parseContextType(contextCode!);
    }
  }

  String _parseContextCode(String code) {
    final index = code.indexOf('_');
    return code.substring(index + 1, code.length);
  }

  String _parseContextType(String code) {
    final index = code.indexOf('_');
    return code.substring(0, index);
  }

  PlannerSubmission? getPlannerSubmission() {
    if (assignment == null) return null;

    // We are only worried about fetching the single submission here, as the calendar request is
    // for a specific user.
    final submission = assignment?.submissionWrapper?.submission;
    if (submission == null) return PlannerSubmission((b) => b..submitted = false);

    return PlannerSubmission((b) => b
      ..submitted = submission.submittedAt != null
      ..excused = submission.excused
      ..graded = submission.isGraded()
      ..late = submission.isLate
      ..missing = submission.missing);
  }

  PlannerItem toPlannerItem(String? courseName) {
    final plannable = Plannable((b) => b
      ..id = id
      ..title = title
      ..pointsPossible = assignment?.pointsPossible
      ..dueAt = assignment?.dueAt
      ..assignmentId = assignment?.id);
    return PlannerItem((b) => b
      ..courseId = getContextId()
      ..contextType = getContextType()
      ..contextName = courseName
      ..plannableType = getItemTypeAsString()
      ..plannable = plannable.toBuilder()
      ..plannableDate = startAt
      ..htmlUrl = htmlUrl
      ..submissionStatus = getPlannerSubmission()?.toBuilder());
  }
}

enum ScheduleItemType { event, quiz, assignment, discussion }
