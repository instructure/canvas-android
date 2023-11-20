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

part 'locked_module.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class LockedModule implements Built<LockedModule, LockedModuleBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<LockedModule> get serializer => _$lockedModuleSerializer;

  LockedModule._();

  factory LockedModule([void Function(LockedModuleBuilder) updates]) = _$LockedModule;

  String get id;

  @BuiltValueField(wireName: 'context_id')
  String get contextId;

  @BuiltValueField(wireName: 'context_type')
  String? get contextType;

  String? get name;

  @BuiltValueField(wireName: 'unlock_at')
  DateTime? get unlockAt;

  @BuiltValueField(wireName: 'require_sequential_progress')
  bool? get isRequireSequentialProgress;
}
