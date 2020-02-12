// Copyright (C) 2019 - present Instructure, Inc.
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
library enrollment;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'grade.dart';
import 'user.dart';

part 'enrollment.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class Enrollment implements Built<Enrollment, EnrollmentBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Enrollment> get serializer => _$enrollmentSerializer;

  Enrollment._();

  factory Enrollment([void Function(EnrollmentBuilder) updates]) = _$Enrollment;

  // The enrollment role, for course-level permissions - this field will match `type` if the enrollment role has not been customized
  @nullable
  String get role;

  @nullable
  String get type;

  String get id;

  // Only included when we get enrollments using the user's url: /users/self/enrollments
  @nullable
  @BuiltValueField(wireName: 'course_id')
  @nullable
  String get courseId;

  @nullable
  @BuiltValueField(wireName: 'course_section_id')
  @nullable
  String get courseSectionId;

  @BuiltValueField(wireName: 'enrollment_state')
  String get enrollmentState;

  @BuiltValueField(wireName: 'user_id')
  String get userId;

  @nullable
  Grade get grades;

  // Only included when we get the enrollment with a course object
  @nullable
  @BuiltValueField(wireName: 'computed_current_score')
  double get computedCurrentScore;

  @nullable
  @BuiltValueField(wireName: 'computed_final_score')
  double get computedFinalScore;

  @nullable
  @BuiltValueField(wireName: 'computed_current_grade')
  String get computedCurrentGrade;

  @nullable
  @BuiltValueField(wireName: 'computed_final_grade')
  String get computedFinalGrade;

  @BuiltValueField(wireName: 'multiple_grading_periods_enabled')
  bool get multipleGradingPeriodsEnabled;

  @BuiltValueField(wireName: 'totals_for_all_grading_periods_option')
  bool get totalsForAllGradingPeriodsOption;

  @nullable
  @BuiltValueField(wireName: 'current_period_computed_current_score')
  double get currentPeriodComputedCurrentScore;

  @nullable
  @BuiltValueField(wireName: 'current_period_computed_final_score')
  double get currentPeriodComputedFinalScore;

  @nullable
  @BuiltValueField(wireName: 'current_period_computed_current_grade')
  String get currentPeriodComputedCurrentGrade;

  @nullable
  @BuiltValueField(wireName: 'current_period_computed_final_grade')
  String get currentPeriodComputedFinalGrade;

  @nullable
  @BuiltValueField(wireName: 'current_grading_period_id')
  String get currentGradingPeriodId;

  @nullable
  @BuiltValueField(wireName: 'current_grading_period_title')
  String get currentGradingPeriodTitle;

  @BuiltValueField(wireName: 'associated_user_id')
  String get associatedUserId; // The unique id of the associated user. Will be null unless type is ObserverEnrollment.

  @nullable
  @BuiltValueField(wireName: 'last_activity_at')
  DateTime get lastActivityAt;

  @BuiltValueField(wireName: 'limit_privileges_to_course_section')
  bool get limitPrivilegesToCourseSection;

  @nullable
  @BuiltValueField(wireName: 'observed_user')
  User get observedUser;

  @nullable
  User get user;

  // Helper functions
  bool _matchesEnrollment(value) => value == type || value == role;

  bool isTa() => ['ta', 'TaEnrollment'].any(_matchesEnrollment);

  bool isStudent() => ['student', 'StudentEnrollment'].any(_matchesEnrollment);

  bool isTeacher() => ['teacher', 'TeacherEnrollment'].any(_matchesEnrollment);

  bool isObserver() => ['observer', 'ObserverEnrollment'].any(_matchesEnrollment);

  bool isDesigner() => ['designer', 'DesignerEnrollment'].any(_matchesEnrollment);

  bool hasActiveGradingPeriod() =>
      multipleGradingPeriodsEnabled &&
      currentGradingPeriodId != null &&
      currentGradingPeriodId.isNotEmpty &&
      currentGradingPeriodId != '0';

  // NOTE: Looks like the API will never return multipleGradingPeriodsEnabled for observer enrollments, still checking just in case
  bool isTotalsForAllGradingPeriodsEnabled() =>
      (isStudent() || isObserver()) && multipleGradingPeriodsEnabled && totalsForAllGradingPeriodsOption;

// NOTE: There is also a StudentViewEnrollment that allows Teachers to view the course as a student - we don't handle that right now, and we probably don't have to worry about it

  static void _initializeBuilder(EnrollmentBuilder b) => b
    ..id = ''
    ..userId = ''
    ..courseId = ''
    ..courseSectionId = ''
    ..enrollmentState = ''
    ..multipleGradingPeriodsEnabled = false
    ..totalsForAllGradingPeriodsOption = false
    ..currentGradingPeriodId = ''
    ..associatedUserId = ''
    ..limitPrivilegesToCourseSection = false;
}
