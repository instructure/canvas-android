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
import 'package:flutter_parent/models/lock_info.dart';

part 'canvas_page.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class CanvasPage implements Built<CanvasPage, CanvasPageBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<CanvasPage> get serializer => _$canvasPageSerializer;

  CanvasPage._();

  factory CanvasPage([void Function(CanvasPageBuilder) updates]) = _$CanvasPage;

  @BuiltValueField(wireName: 'page_id')
  String get id;

  String? get url;

  String? get title;

  @BuiltValueField(wireName: 'created_at')
  DateTime? get createdAt;

  @BuiltValueField(wireName: 'updated_at')
  DateTime? get updatedAt;

  @BuiltValueField(wireName: 'hide_from_students')
  bool get hideFromStudents;

  String? get status;

  String? get body;

  @BuiltValueField(wireName: 'front_page')
  bool get frontPage;

  @BuiltValueField(wireName: 'lock_info')
  LockInfo get lockInfo;

  @BuiltValueField(wireName: 'published')
  bool get published;

  @BuiltValueField(wireName: 'editing_roles')
  String? get editingRoles;

  @BuiltValueField(wireName: 'lock_explanation')
  String? get lockExplanation;

  static void _initializeBuilder(CanvasPageBuilder b) => b
    ..hideFromStudents = false
    ..frontPage = false
    ..published = false;
}
