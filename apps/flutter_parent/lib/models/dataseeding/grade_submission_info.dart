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
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'grade_submission_info.g.dart';

abstract class GradeSubmissionInfo implements Built<GradeSubmissionInfo, GradeSubmissionInfoBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<GradeSubmissionInfo> get serializer => _$gradeSubmissionInfoSerializer;

  GradeSubmissionInfo._();

  factory GradeSubmissionInfo([void Function(GradeSubmissionInfoBuilder) updates]) = _$GradeSubmissionInfo;

  @BuiltValueField(wireName: "posted_grade")
  String get postedGrade;

  bool? get excuse;

  static void _initializeBuilder(GradeSubmissionInfoBuilder b) => b;
}
