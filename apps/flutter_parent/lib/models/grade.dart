/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.
library grade;

import 'dart:convert';

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'serializers.dart';

part 'grade.g.dart';

abstract class Grade implements Built<Grade, GradeBuilder> {
  Grade._();

  factory Grade([updates(GradeBuilder b)]) = _$Grade;

  @BuiltValueField(wireName: 'html_url')
  String get htmlUrl;
  @BuiltValueField(wireName: 'current_score')
  double get currentScore;
  @BuiltValueField(wireName: 'final_score')
  double get finalScore;
  @BuiltValueField(wireName: 'current_grade')
  String get currentGrade;
  @BuiltValueField(wireName: 'final_grade')
  String get finalGrade;
  String toJson() {
    return json.encode(jsonSerializers.serializeWith(Grade.serializer, this));
  }

  static Grade fromJson(String jsonString) {
    return jsonSerializers.deserializeWith(
        Grade.serializer, json.decode(jsonString));
  }

  static Serializer<Grade> get serializer => _$gradeSerializer;
}
