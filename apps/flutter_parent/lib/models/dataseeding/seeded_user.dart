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

import '../login.dart';
import '../user.dart';

part 'seeded_user.g.dart';

abstract class SeededUser implements Built<SeededUser, SeededUserBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<SeededUser> get serializer => _$seededUserSerializer;

  SeededUser._();
  factory SeededUser([void Function(SeededUserBuilder) updates]) = _$SeededUser;

  String get id;

  String get name;

  @BuiltValueField(wireName: "short_name")
  String get shortName;

  @BuiltValueField(wireName: "sortable_name")
  String get sortableName;

  @BuiltValueField(wireName: "terms_of_use")
  bool? get termsOfUse;

  @BuiltValueField(wireName: "login_id")
  String? get loginId;

  String? get password;

  @BuiltValueField(wireName: "avatar_url")
  String? get avatarUrl;

  String? get token;

  String? get domain;

  static void _initializeBuilder(SeededUserBuilder b) => b..name = '';

  User toUser() {
    return User((b) => b
      ..id = id
      ..name = name
      ..shortName = shortName
      ..sortableName = sortableName
      ..build());
  }

  Login toLogin() {
    return Login((b) => b
      ..domain = "https://$domain/"
      ..clientSecret = token
      ..accessToken = token
      ..user.name = name
      ..user.id = id
      ..user.shortName = shortName
      ..user.sortableName = sortableName
      ..build());
  }
}
