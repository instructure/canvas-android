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

import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/serializers.dart';

part 'seed_context.g.dart';

// Model class used for conveying seeding info from app to test driver
abstract class SeedContext implements Built<SeedContext, SeedContextBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<SeedContext> get serializer => _$seedContextSerializer;

  SeedContext._();
  factory SeedContext([void Function(SeedContextBuilder) updates]) = _$SeedContext;

  bool get seedingComplete;
  BuiltMap<String, String> get seedObjects;

  static void _initializeBuilder(SeedContextBuilder b) => b..seedingComplete = false;

  // Convenience method for extracting seed objects
  T? getNamedObject<T>(String objectName) {
    return deserialize<T>(json.decode(seedObjects[objectName] ?? ''));
  }
}
