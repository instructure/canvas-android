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

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'plannable.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class Plannable implements Built<Plannable, PlannableBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<Plannable> get serializer => _$plannableSerializer;

  Plannable._();

  String get id;

  String get title;

  @nullable
  @BuiltValueField(wireName: 'course_id')
  String get courseId;

  @nullable
  @BuiltValueField(wireName: 'group_id')
  String get groupId;

  @nullable
  @BuiltValueField(wireName: 'user_id')
  String get userId;

  @nullable
  @BuiltValueField(wireName: 'points_possible')
  double get pointsPossible;

  @nullable
  @BuiltValueField(wireName: 'todo_date')
  DateTime get toDoDate;

  @nullable
  @BuiltValueField(wireName: 'due_at')
  DateTime get dueAt;

  // Used to determine if a quiz is an assignment or not
  @nullable
  @BuiltValueField(wireName: 'assignment_id')
  String get assignmentId;

  @nullable
  String get details;

  String contextCode() {
    var code = '';
    if (courseId != null) {
      code = 'course_$courseId';
    } else if (groupId != null) {
      code = 'group_$groupId';
    } else if (userId != null) {
      code = 'user_$userId';
    }
    return code;
  }

  factory Plannable([void Function(PlannableBuilder) updates]) = _$Plannable;
}
