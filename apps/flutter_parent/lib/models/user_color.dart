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

import 'dart:ui';

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';

part 'user_color.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class UserColor implements Built<UserColor, UserColorBuilder> {
  int? get id;

  String get userDomain;

  String get userId;

  String get canvasContext;

  Color get color;

  UserColor._();
  factory UserColor([void Function(UserColorBuilder) updates]) = _$UserColor;

  static void _initializeBuilder(UserColorBuilder b) => b
    ..userDomain = ''
    ..userId = ''
    ..canvasContext = ''
    ..color = Color(0);
}
