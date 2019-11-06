/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.
library course;

import 'dart:convert';

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'course_grade.dart';
import 'enrollment.dart';
import 'serializers.dart';

part 'course.g.dart';

abstract class Course implements Built<Course, CourseBuilder> {
  Course._();

  // Helper variables
  @BuiltValueField(serialize: false)
  double get currentScore;
  @BuiltValueField(serialize: false)
  double get finalScore;
  @BuiltValueField(serialize: false)
  String get currentGrade;
  @BuiltValueField(serialize: false)
  String get finalGrade;

  factory Course([updates(CourseBuilder b)]) = _$Course;

  int get id;
  String get name;
  @BuiltValueField(wireName: 'original_name')
  String get originalName;
  @BuiltValueField(wireName: 'course_code')
  String get courseCode;
  @BuiltValueField(wireName: 'start_at')
  String get startAt;
  @BuiltValueField(wireName: 'end_at')
  String get endAt;
  @BuiltValueField(wireName: 'syllabus_body')
  String get syllabusBody;
  @BuiltValueField(wireName: 'hide_final_grades')
  bool get hideFinalGrades;
  @BuiltValueField(wireName: 'is_public')
  bool get isPublic;

  // License license = License.PRIVATE_COPYRIGHTED;
  // Term term;
  BuiltList<Enrollment> get enrollments;
  @BuiltValueField(wireName: 'needs_grading_count')
  int get needsGradingCount;
  @BuiltValueField(wireName: 'apply_assignment_group_weights')
  bool get applyAssignmentGroupWeights;
  @BuiltValueField(wireName: 'is_favorite')
  bool get isFavorite;
  @BuiltValueField(wireName: 'access_restricted_by_date')
  bool get accessRestrictedByDate;
  @BuiltValueField(wireName: 'image_download_url')
  String get imageDownloadUrl;
  @BuiltValueField(wireName: 'has_weighted_grading_periods')
  bool get hasWeightedGradingPeriods;
  @BuiltValueField(wireName: 'has_grading_periods')
  bool get hasGradingPeriods;

  // List<Section> sections;
  // @SerializedName("default_view")
  // HomePage homePage;
  @BuiltValueField(wireName: 'restrict_enrollments_to_course_dates')
  bool get restrictEnrollmentsToCourseDates;
  @BuiltValueField(wireName: 'workflow_state')
  String get workflowState;

  String toJson() {
    return json.encode(jsonSerializers.serializeWith(Course.serializer, this));
  }

  static Course fromJson(String jsonString) {
    return jsonSerializers.deserializeWith(
        Course.serializer, json.decode(jsonString));
  }

  static Serializer<Course> get serializer => _$courseSerializer;

  CourseGrade getCourseGrade(int studentId) => CourseGrade(
      enrollments.firstWhere((enrollment) => enrollment.userId == studentId));
}
