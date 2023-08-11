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

part 'assignment_override.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class AssignmentOverride implements Built<AssignmentOverride, AssignmentOverrideBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<AssignmentOverride> get serializer => _$assignmentOverrideSerializer;

  @BuiltValueField(wireName: 'id')
  String get id;

  @BuiltValueField(wireName: 'assignment_id')
  String get assignmentId;

  @BuiltValueField(wireName: 'title')
  String? get title;

  @BuiltValueField(wireName: 'due_at')
  DateTime? get dueAt;

  @BuiltValueField(wireName: 'all_day')
  bool? get allDay;

  @BuiltValueField(wireName: 'all_day_date')
  DateTime? get allDayDate;

  @BuiltValueField(wireName: 'unlock_at')
  DateTime? get unlockAt;

  @BuiltValueField(wireName: 'lock_at')
  DateTime? get lockAt;

  @BuiltValueField(wireName: 'student_ids')
  BuiltList<String> get studentIds;

  AssignmentOverride._();
  factory AssignmentOverride([void Function(AssignmentOverrideBuilder) updates]) = _$AssignmentOverride;

  static void _initializeBuilder(AssignmentOverrideBuilder b) => b
    ..id = ''
    ..assignmentId = ''
    ..studentIds = ListBuilder([]);
}
