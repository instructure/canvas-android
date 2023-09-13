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

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'recipient.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build
abstract class Recipient implements Built<Recipient, RecipientBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<Recipient> get serializer => _$recipientSerializer;

  String get id;

  // The name of the context or short name of the user
  String get name;

  String? get pronouns;

  @BuiltValueField(wireName: 'avatar_url')
  String? get avatarUrl;

  @BuiltValueField(wireName: 'common_courses')
  BuiltMap<String, BuiltList<String>>? get commonCourses;

  Recipient._();
  factory Recipient([void Function(RecipientBuilder) updates]) = _$Recipient;
}
