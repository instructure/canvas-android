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
import 'package:built_value/json_object.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/plannable.dart';
import 'package:flutter_parent/models/planner_submission.dart';

part 'planner_item.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class PlannerItem implements Built<PlannerItem, PlannerItemBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<PlannerItem> get serializer => _$plannerItemSerializer;

  PlannerItem._();

  @nullable
  @BuiltValueField(wireName: 'course_id')
  String get courseId;

  @nullable
  @BuiltValueField(wireName: 'context_type')
  String get contextType;

  @nullable
  @BuiltValueField(wireName: 'context_name')
  String get contextName;

  @BuiltValueField(wireName: 'plannable_type')
  String get plannableType;

  Plannable get plannable;

  @nullable
  @BuiltValueField(wireName: 'plannable_date')
  DateTime get plannableDate;

  @nullable
  @BuiltValueField(wireName: 'submissions')
  JsonObject get submissionStatusRaw;

  @nullable
  @BuiltValueField(wireName: 'html_url')
  String get htmlUrl;

  @nullable
  PlannerSubmission get submissionStatus;

//  @nullable TODO - keep in place for potentially moving back to planner api
//  PlannerSubmission get submissionStatus {
//    if (submissionStatusRaw == null || submissionStatusRaw.isBool) return null;
//    return deserialize<PlannerSubmission>(submissionStatusRaw.value);
//  }

  factory PlannerItem([void Function(PlannerItemBuilder) updates]) = _$PlannerItem;
}
