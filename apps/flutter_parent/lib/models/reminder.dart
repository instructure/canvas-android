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

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/notification_payload.dart';
import 'package:flutter_parent/models/serializers.dart';

part 'reminder.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class Reminder implements Built<Reminder, ReminderBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<Reminder> get serializer => _$reminderSerializer;

  static const TYPE_ASSIGNMENT = 'assignment';
  static const TYPE_EVENT = 'event';

  int? get id;

  String get userDomain;

  String get userId;

  String get type;

  String get itemId;

  String get courseId;

  DateTime? get date;

  Reminder._();
  factory Reminder([void Function(ReminderBuilder) updates]) = _$Reminder;

  static Reminder? fromNotification(NotificationPayload? payload) => deserialize(json.decode(payload?.data ?? ''));

  static void _initializeBuilder(ReminderBuilder b) => b
    ..userDomain = ''
    ..userId = ''
    ..type = TYPE_EVENT
    ..itemId = ''
    ..courseId = ''
    ..date = DateTime.now();
}
