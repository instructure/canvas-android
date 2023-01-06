// Copyright (C) 2023 - present Instructure, Inc.
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

part 'feature_flags.g.dart';

abstract class FeatureFlags implements Built<FeatureFlags, FeatureFlagsBuilder> {

  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<FeatureFlags> get serializer => _$featureFlagsSerializer;

  @BuiltValueField(wireName: 'send_usage_metrics')
  bool get sendUsageMetrics;

  FeatureFlags._();
  factory FeatureFlags([void Function(FeatureFlagsBuilder) updates]) = _$FeatureFlags;

  static void _initializeBuilder(FeatureFlagsBuilder b) => b..sendUsageMetrics = false;
}