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

import 'assignment.dart';
import 'assignment_override.dart';

part 'schedule_item.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class ScheduleItem implements Built<ScheduleItem, ScheduleItemBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<ScheduleItem> get serializer => _$scheduleItemSerializer;

  static const typeCalendar = 'event';

  static const typeAssignment = 'assignment';

  String get id;

  @nullable
  String get title;

  @nullable
  String get description;

  @BuiltValueField(wireName: 'start_at')
  @nullable
  DateTime get startAt;

  @BuiltValueField(wireName: 'end_at')
  @nullable
  DateTime get endAt;

  @BuiltValueField(wireName: 'all_day')
  bool get isAllDay;

  @BuiltValueField(wireName: 'all_day_date')
  @nullable
  DateTime get allDayDate;

  @BuiltValueField(wireName: 'location_address')
  @nullable
  String get locationAddress;

  String get type; // Either 'event' or 'calendar'

  @BuiltValueField(wireName: 'location_name')
  @nullable
  String get locationName;

  @BuiltValueField(wireName: 'html_url')
  @nullable
  String get htmlUrl;

  @BuiltValueField(wireName: 'context_code')
  @nullable
  String get contextCode;

  @BuiltValueField(wireName: 'effective_context_code')
  @nullable
  String get effectiveContextCode;

  @BuiltValueField(wireName: 'hidden')
  @nullable
  bool get isHidden;

  @nullable
  Assignment get assignment;

  @BuiltValueField(wireName: 'assignment_overrides')
  @nullable
  BuiltList<AssignmentOverride> get assignmentOverrides;

  ScheduleItem._();
  factory ScheduleItem([void Function(ScheduleItemBuilder) updates]) = _$ScheduleItem;

  static void _initializeBuilder(ScheduleItemBuilder b) => b
    ..id = ''
    ..type = typeCalendar
    ..isAllDay = false
    ..isHidden = false;
}
