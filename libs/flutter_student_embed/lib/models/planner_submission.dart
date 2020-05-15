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

part 'planner_submission.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class PlannerSubmission implements Built<PlannerSubmission, PlannerSubmissionBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<PlannerSubmission> get serializer => _$plannerSubmissionSerializer;

  PlannerSubmission._();

  bool get submitted;
  bool get excused;
  bool get graded;
  bool get late;
  bool get missing;

  @BuiltValueField(wireName: 'needs_grading')
  bool get needsGrading;

  static void _initializeBuilder(PlannerSubmissionBuilder b) => b
    ..submitted = false
    ..excused = false
    ..graded = false
    ..late = false
    ..missing = false
    ..needsGrading = false;

  factory PlannerSubmission([void Function(PlannerSubmissionBuilder) updates]) = _$PlannerSubmission;
}
