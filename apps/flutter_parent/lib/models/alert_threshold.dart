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

import 'alert.dart';

part 'alert_threshold.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class AlertThreshold implements Built<AlertThreshold, AlertThresholdBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<AlertThreshold> get serializer => _$alertThresholdSerializer;

  AlertThreshold._();

  factory AlertThreshold([void Function(AlertThresholdBuilder) updates]) = _$AlertThreshold;

  String get id;

  @BuiltValueField(wireName: 'alert_type')
  AlertType get alertType;

  String? get threshold;

  @BuiltValueField(wireName: 'user_id')
  String get userId;

  @BuiltValueField(wireName: 'observer_id')
  String get observerId;

  static void _initializeBuilder(AlertThresholdBuilder b) => b
    ..id = ''
    ..alertType = AlertType.institutionAnnouncement
    ..userId = ''
    ..observerId = '';
}
