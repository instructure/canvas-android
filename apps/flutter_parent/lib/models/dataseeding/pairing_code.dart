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

part 'pairing_code.g.dart';

abstract class PairingCode implements Built<PairingCode, PairingCodeBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<PairingCode> get serializer => _$pairingCodeSerializer;

  PairingCode._();
  factory PairingCode([void Function(PairingCodeBuilder) updates]) = _$PairingCode;

  @BuiltValueField(wireName: "user_id")
  String get userId;

  String get code;

  @BuiltValueField(wireName: "expires_at")
  String get expiresAt;

  @BuiltValueField(wireName: "workflow_state")
  String get workflowState;

  static void _initializeBuilder(PairingCodeBuilder b) => b;
}
