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
library user;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'user.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class User implements Built<User, UserBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<User> get serializer => _$userSerializer;

  User._();
  factory User([void Function(UserBuilder) updates]) = _$User;

  String get id;

  String get name;

  @nullable
  @BuiltValueField(wireName: 'sortable_name')
  String get sortableName;

  @nullable
  @BuiltValueField(wireName: 'short_name')
  String get shortName;

  @nullable
  String get pronouns;

  @nullable
  @BuiltValueField(wireName: 'avatar_url')
  String get avatarUrl;

  @nullable
  @BuiltValueField(wireName: 'primary_email')
  String get primaryEmail;

  @nullable
  String get locale;

  @nullable
  @BuiltValueField(wireName: 'effective_locale')
  String get effectiveLocale;

  static void _initializeBuilder(UserBuilder b) => b
    ..id = ''
    ..name = '';
}
