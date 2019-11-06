library enrollment;

import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'grade.dart';
import 'serializers.dart';
import 'user.dart';

part 'enrollment.g.dart';

abstract class Enrollment implements Built<Enrollment, EnrollmentBuilder> {
  Enrollment._();

  factory Enrollment([updates(EnrollmentBuilder b)]) = _$Enrollment;

  // The enrollment role, for course-level permissions - this field will match `type` if the enrollment role has not been customized
  String get role;
  String get type;

  int get id;

  // Only included when we get enrollments using the user's url: /users/self/enrollments
  @BuiltValueField(wireName: 'course_id')
  int get courseId;
  @BuiltValueField(wireName: 'course_section_id')
  int get courseSectionId;
  @BuiltValueField(wireName: 'enrollment_state')
  String get enrollmentState;
  @BuiltValueField(wireName: 'user_id')
  int get userId;
  Grade get grade;

  // Only included when we get the enrollment with a course object
  @BuiltValueField(wireName: 'computed_current_score')
  double get computedCurrentScore;
  @BuiltValueField(wireName: 'computed_final_score')
  double get computedFinalScore;
  @BuiltValueField(wireName: 'computed_current_grade')
  String get computedCurrentGrade;
  @BuiltValueField(wireName: 'computed_final_grade')
  String get computedFinalGrade;
  @BuiltValueField(wireName: 'multiple_grading_periods_enabled')
  bool get multipleGradingPeriodsEnabled;
  @BuiltValueField(wireName: 'totals_for_all_grading_periods_option')
  bool get totalsForAllGradingPeriodsOption;
  @BuiltValueField(wireName: 'current_period_computed_current_score')
  double get currentPeriodComputedCurrentScore;
  @BuiltValueField(wireName: 'current_period_computed_final_score')
  double get currentPeriodComputedFinalScore;
  @BuiltValueField(wireName: 'current_period_computed_current_grade')
  String get currentPeriodComputedCurrentGrade;
  @BuiltValueField(wireName: 'current_period_computed_final_grade')
  String get currentPeriodComputedFinalGrade;
  @BuiltValueField(wireName: 'current_grading_period_id')
  int get currentGradingPeriodId;
  @BuiltValueField(wireName: 'current_grading_period_title')
  String get currentGradingPeriodTitle;
  @BuiltValueField(wireName: 'associated_user_id')
  int get associatedUserId; // The unique id of the associated user. Will be null unless type is ObserverEnrollment.
  @BuiltValueField(wireName: 'last_activity_at')
  DateTime get lastActivityAt;
  @BuiltValueField(wireName: 'limit_privileges_to_course_section')
  bool get limitPrivilegesToCourseSection;
  @BuiltValueField(wireName: 'observed_user')
  User get observedUser;
  User get user;

  String toJson() {
    return json.encode(jsonSerializers.serializeWith(Enrollment.serializer, this));
  }

  static Enrollment fromJson(String jsonString) {
    return jsonSerializers.deserializeWith(
        Enrollment.serializer, json.decode(jsonString));
  }

  static Serializer<Enrollment> get serializer => _$enrollmentSerializer;

  // Helper functions
  bool _matchesEnrollment(value) => value == type || value == role;

  bool isTa() => ['ta', 'TaEnrollment'].any(_matchesEnrollment);

  bool isStudent() => ['student', 'StudentEnrollment'].any(_matchesEnrollment);

  bool isTeacher() => ['teacher', 'TeacherEnrollment'].any(_matchesEnrollment);

  bool isObserver() =>
      ['observer', 'ObserverEnrollment'].any(_matchesEnrollment);

  bool isDesigner() =>
      ['designer', 'DesignerEnrollment'].any(_matchesEnrollment);

// NOTE: There is also a StudentViewEnrollment that allows Teachers to view the course as a student - we don't handle that right now, and we probably don't have to worry about it
}
