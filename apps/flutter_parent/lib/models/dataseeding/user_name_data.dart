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

part 'user_name_data.g.dart';

abstract class UserNameData implements Built<UserNameData, UserNameDataBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<UserNameData> get serializer => _$userNameDataSerializer;

  UserNameData._();
  factory UserNameData([void Function(UserNameDataBuilder) updates]) = _$UserNameData;

  String get name;

  @BuiltValueField(wireName: "short_name")
  String get shortName;

  @BuiltValueField(wireName: "sortable_name")
  String get sortableName;

  @BuiltValueField(wireName: "terms_of_use")
  bool? get termsOfUse;

  static void _initializeBuilder(UserNameDataBuilder b) => b..name = '';
}
