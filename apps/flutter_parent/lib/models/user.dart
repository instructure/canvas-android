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
library user;

import 'dart:convert';

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'serializers.dart';

part 'user.g.dart';

abstract class User implements Built<User, UserBuilder> {
  User._();

  factory User([updates(UserBuilder b)]) = _$User;

  int get id;
  String get name;
  @BuiltValueField(wireName: 'sortable_name')
  String get sortableName;
  @BuiltValueField(wireName: 'avatar_url')
  String get avatarUrl;
  @BuiltValueField(wireName: 'primary_email')
  String get primaryEmail;
  String get locale;
  @BuiltValueField(wireName: 'effective_locale')
  String get effectiveLocale;

  String toJson() {
    return json.encode(jsonSerializers.serializeWith(User.serializer, this));
  }

  static User fromJson(String jsonString) {
    return jsonSerializers.deserializeWith(
        User.serializer, json.decode(jsonString));
  }

  static Serializer<User> get serializer => _$userSerializer;

  @override
  String toString() => name;

//  @override
//  int get hashCode => id;
//
//  @override
//  bool operator ==(other) => this.id == other.id;
}
