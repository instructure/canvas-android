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
import 'package:flutter_parent/models/grading_period.dart';

part 'grading_period_response.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class GradingPeriodResponse implements Built<GradingPeriodResponse, GradingPeriodResponseBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<GradingPeriodResponse> get serializer => _$gradingPeriodResponseSerializer;

  GradingPeriodResponse._();

  factory GradingPeriodResponse([void Function(GradingPeriodResponseBuilder) updates]) = _$GradingPeriodResponse;

  @BuiltValueField(wireName: 'grading_periods')
  BuiltList<GradingPeriod> get gradingPeriods;
}
