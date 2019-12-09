// Copyright (C) 2019 - present Instructure, Inc.
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
library grade;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'grade.g.dart';

abstract class Grade implements Built<Grade, GradeBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Grade> get serializer => _$gradeSerializer;

  Grade._();
  factory Grade([void Function(GradeBuilder b) updates]) = _$Grade;

  @BuiltValueField(wireName: 'html_url')
  String get htmlUrl;

  @BuiltValueField(wireName: 'current_score')
  @nullable
  double get currentScore;

  @BuiltValueField(wireName: 'final_score')
  @nullable
  double get finalScore;

  @BuiltValueField(wireName: 'current_grade')
  @nullable
  String get currentGrade;

  @BuiltValueField(wireName: 'final_grade')
  @nullable
  String get finalGrade;
}
