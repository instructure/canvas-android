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

part 'create_submission_info.g.dart';

abstract class CreateSubmissionInfo implements Built<CreateSubmissionInfo, CreateSubmissionInfoBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CreateSubmissionInfo> get serializer => _$createSubmissionInfoSerializer;

  CreateSubmissionInfo._();

  factory CreateSubmissionInfo([void Function(CreateSubmissionInfoBuilder) updates]) = _$CreateSubmissionInfo;

  // Shouldn't be null for submissions, can be null for grades
  @BuiltValueField(wireName: "submission_type")
  String? get submissionType;

  String? get body;

  String? get url;

  // TODO: Support file submissions
  // TODO: Support media comments

  @BuiltValueField(wireName: "user_id")
  int? get userId;

  @BuiltValueField(wireName: "submitted_at")
  DateTime? get submittedAt;

  static void _initializeBuilder(CreateSubmissionInfoBuilder b) => b;
}
