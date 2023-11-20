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

part 'create_enrollment_info.g.dart';

abstract class CreateEnrollmentInfo implements Built<CreateEnrollmentInfo, CreateEnrollmentInfoBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CreateEnrollmentInfo> get serializer => _$createEnrollmentInfoSerializer;

  CreateEnrollmentInfo._();
  factory CreateEnrollmentInfo([void Function(CreateEnrollmentInfoBuilder) updates]) = _$CreateEnrollmentInfo;

  @BuiltValueField(wireName: "user_id")
  String get userId;

  String get type;

  String get role;

  @BuiltValueField(wireName: "enrollment_state")
  String get enrollmentState;

  @BuiltValueField(wireName: "associated_user_id")
  String? get associatedUserId;

  static void _initializeBuilder(CreateEnrollmentInfoBuilder b) => b..role = "";
}
