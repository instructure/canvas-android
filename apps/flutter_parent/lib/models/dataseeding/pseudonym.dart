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

library pseudonym;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
//import 'package:flutter/cupertino.dart';

part 'pseudonym.g.dart';

abstract class Pseudonym implements Built<Pseudonym, PseudonymBuilder>{
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Pseudonym> get serializer => _$pseudonymSerializer;

  Pseudonym._();
  factory Pseudonym([void Function(PseudonymBuilder) updates]) = _$Pseudonym;

//  Pseudonym({
//    this.uniqueId,
//    this.password
//  });

  @BuiltValueField(wireName: "unique_id")
  String get uniqueId;
  String get password;

  static void _initializeBuilder(PseudonymBuilder b) => b
    ..uniqueId = ''
    ..password = '';
}