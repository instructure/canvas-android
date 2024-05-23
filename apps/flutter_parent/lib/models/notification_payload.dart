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

part 'notification_payload.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class NotificationPayload implements Built<NotificationPayload, NotificationPayloadBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<NotificationPayload> get serializer => _$notificationPayloadSerializer;

  NotificationPayloadType get type;

  String? get data;

  NotificationPayload._();
  factory NotificationPayload([void Function(NotificationPayloadBuilder) updates]) = _$NotificationPayload;
}

@BuiltValueEnum(wireName: 'type')
class NotificationPayloadType extends EnumClass {
  const NotificationPayloadType._(String name) : super(name);

  static BuiltSet<NotificationPayloadType> get values => _$notificationPayloadTypeValues;

  static NotificationPayloadType valueOf(String name) => _$notificationPayloadTypeValueOf(name);

  static Serializer<NotificationPayloadType> get serializer => _$notificationPayloadTypeSerializer;

  @BuiltValueEnumConst(wireName: 'reminder')
  static const NotificationPayloadType reminder = _$notificationPayloadTypeReminder;

  @BuiltValueEnumConst(fallback: true)
  static const NotificationPayloadType other = _$notificationPayloadTypeOther;
}
