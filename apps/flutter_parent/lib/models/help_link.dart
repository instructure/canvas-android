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

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'help_link.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class HelpLink implements Built<HelpLink, HelpLinkBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<HelpLink> get serializer => _$helpLinkSerializer;

  HelpLink._();

  factory HelpLink([void Function(HelpLinkBuilder) updates]) = _$HelpLink;

  String? get id;

  String get type;

  @BuiltValueField(wireName: 'available_to')
  BuiltList<AvailableTo> get availableTo;

  String? get url;

  String? get text;

  String? get subtext;
}

class AvailableTo extends EnumClass {
  static Serializer<AvailableTo> get serializer => _$availableToSerializer;

  static const AvailableTo user = _$user;
  static const AvailableTo student = _$student;
  static const AvailableTo teacher = _$teacher;
  static const AvailableTo admin = _$admin;
  static const AvailableTo observer = _$observer;
  static const AvailableTo unenrolled = _$unenrolled;

  const AvailableTo._(String name) : super(name);

  static BuiltSet<AvailableTo> get values => _$atValues;
  static AvailableTo valueOf(String name) => _$atValueOf(name);
}
