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

library serializers;

import 'package:built_collection/built_collection.dart';
import 'package:built_value/iso_8601_date_time_serializer.dart';
import 'package:built_value/serializer.dart';
import 'package:built_value/standard_json_plugin.dart';
import 'package:flutter_student_embed/models/course.dart';
import 'package:flutter_student_embed/models/enrollment.dart';
import 'package:flutter_student_embed/models/login.dart';
import 'package:flutter_student_embed/models/plannable.dart';
import 'package:flutter_student_embed/models/planner_item.dart';
import 'package:flutter_student_embed/models/planner_submission.dart';
import 'package:flutter_student_embed/models/user.dart';

part 'serializers.g.dart';

/// If changes are made, run `flutter pub run build_runner build` from the project root. Alternatively, you can
/// have it watch for changes and automatically build if you run `flutter pub run build_runner watch`.
@SerializersFor([
  Course,
  Enrollment,
  Login,
  Plannable,
  PlannerItem,
  PlannerSubmission,
  User,
])
final Serializers _serializers = _$_serializers;

Serializers jsonSerializers = (_serializers.toBuilder()
      ..addPlugin(StandardJsonPlugin())
      ..addPlugin(RemoveNullInMapConvertedListPlugin())
      ..add(Iso8601DateTimeSerializer())
      ..addBuilderFactory(FullType(BuiltList, [FullType(String)]), () => ListBuilder<String>())
      ..addBuilderFactory(
          FullType(BuiltMap, [
            FullType(String),
            FullType(BuiltList, [FullType(String)])
          ]),
          () => MapBuilder<String, BuiltList<String>>())
      ..addBuilderFactory(FullType(BuiltMap, [FullType(String), FullType(String)]), () => MapBuilder<String, String>()))
    .build();

T deserialize<T>(dynamic value) => jsonSerializers.deserializeWith<T>(jsonSerializers.serializerForType(T), value);

dynamic serialize<T>(T value) => jsonSerializers.serializeWith(jsonSerializers.serializerForType(T), value);

List<T> deserializeList<T>(dynamic value) => List.from(value?.map((value) => deserialize<T>(value))?.toList() ?? []);

/// Plugin that works around an issue where deserialization breaks if a map key is null
/// Sourced from https://github.com/google/built_value.dart/issues/653#issuecomment-495964030
class RemoveNullInMapConvertedListPlugin implements SerializerPlugin {
  Object beforeSerialize(Object object, FullType specifiedType) => object;

  Object afterSerialize(Object object, FullType specifiedType) => object;

  Object beforeDeserialize(Object object, FullType specifiedType) {
    if (specifiedType.root == BuiltMap && object is List) {
      return object.where((v) => v != null).toList();
    }
    return object;
  }

  Object afterDeserialize(Object object, FullType specifiedType) => object;
}
