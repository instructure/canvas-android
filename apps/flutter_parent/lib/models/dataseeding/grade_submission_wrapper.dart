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

import '../assignment.dart';
import 'create_assignment_info.dart';
import 'create_submission_info.dart';
import 'grade_submission_info.dart';

part 'grade_submission_wrapper.g.dart';

abstract class GradeSubmissionWrapper implements Built<GradeSubmissionWrapper, GradeSubmissionWrapperBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<GradeSubmissionWrapper> get serializer => _$gradeSubmissionWrapperSerializer;

  GradeSubmissionWrapper._();

  factory GradeSubmissionWrapper([void Function(GradeSubmissionWrapperBuilder) updates]) = _$GradeSubmissionWrapper;

  GradeSubmissionInfo get submission;

  static void _initializeBuilder(GradeSubmissionWrapperBuilder b) => b;
}
