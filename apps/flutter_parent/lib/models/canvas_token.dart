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
import 'package:flutter_parent/models/user.dart';

part 'canvas_token.g.dart';

abstract class CanvasToken implements Built<CanvasToken, CanvasTokenBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CanvasToken> get serializer => _$canvasTokenSerializer;

  CanvasToken._();

  factory CanvasToken([void Function(CanvasTokenBuilder) updates]) = _$CanvasToken;

  @BuiltValueField(wireName: 'access_token')
  String get accessToken;

  @BuiltValueField(wireName: 'refresh_token')
  String? get refreshToken;

  User? get user;

  @BuiltValueField(wireName: 'real_user')
  User? get realUser;
}
