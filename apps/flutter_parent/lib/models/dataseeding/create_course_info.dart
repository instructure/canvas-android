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

part 'create_course_info.g.dart';

abstract class CreateCourseInfo implements Built<CreateCourseInfo, CreateCourseInfoBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CreateCourseInfo> get serializer => _$createCourseInfoSerializer;

  CreateCourseInfo._();
  factory CreateCourseInfo([void Function(CreateCourseInfoBuilder) updates]) = _$CreateCourseInfo;

  String get name;

  @BuiltValueField(wireName: "course_code")
  String get courseCode;

  @BuiltValueField(wireName: "enrollment_term_id")
  int? get enrollmentTermId;

  String get role;

  @BuiltValueField(wireName: "syllabus_body")
  String? get syllabusBody;

  static void _initializeBuilder(CreateCourseInfoBuilder b) => b..role = "student";
}
