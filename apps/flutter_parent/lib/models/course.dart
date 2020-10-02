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
library course;

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/section.dart';
import 'package:flutter_parent/models/term.dart';

import 'course_grade.dart';
import 'enrollment.dart';

part 'course.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class Course implements Built<Course, CourseBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Course> get serializer => _$courseSerializer;

  Course._();

  factory Course([void Function(CourseBuilder) updates]) = _$Course;

  // Helper variables
  @nullable
  @BuiltValueField(serialize: false)
  double get currentScore;

  @nullable
  @BuiltValueField(serialize: false)
  double get finalScore;

  @nullable
  @BuiltValueField(serialize: false)
  String get currentGrade;

  @nullable
  @BuiltValueField(serialize: false)
  String get finalGrade;

  String get id;

  String get name;

  @nullable
  @BuiltValueField(wireName: 'original_name')
  String get originalName;

  @nullable
  @BuiltValueField(wireName: 'course_code')
  String get courseCode;

  @nullable
  @BuiltValueField(wireName: 'start_at')
  DateTime get startAt;

  @nullable
  @BuiltValueField(wireName: 'end_at')
  DateTime get endAt;

  @nullable
  @BuiltValueField(wireName: 'syllabus_body')
  String get syllabusBody;

  @BuiltValueField(wireName: 'hide_final_grades')
  bool get hideFinalGrades;

  @BuiltValueField(wireName: 'is_public')
  bool get isPublic;

  BuiltList<Enrollment> get enrollments;

  @BuiltValueField(wireName: 'needs_grading_count')
  int get needsGradingCount;

  @BuiltValueField(wireName: 'apply_assignment_group_weights')
  bool get applyAssignmentGroupWeights;

  @BuiltValueField(wireName: 'is_favorite')
  bool get isFavorite;

  @BuiltValueField(wireName: 'access_restricted_by_date')
  bool get accessRestrictedByDate;

  @nullable
  @BuiltValueField(wireName: 'image_download_url')
  @nullable
  String get imageDownloadUrl;

  @BuiltValueField(wireName: 'has_weighted_grading_periods')
  bool get hasWeightedGradingPeriods;

  @BuiltValueField(wireName: 'has_grading_periods')
  bool get hasGradingPeriods;

  @BuiltValueField(wireName: 'restrict_enrollments_to_course_dates')
  bool get restrictEnrollmentsToCourseDates;

  @nullable
  @BuiltValueField(wireName: 'workflow_state')
  String get workflowState;

  @nullable
  @BuiltValueField(wireName: 'default_view')
  HomePage get homePage;

  @nullable
  Term get term;

  @nullable
  BuiltList<Section> get sections;

  static void _initializeBuilder(CourseBuilder b) => b
    ..id = ''
    ..enrollments = ListBuilder<Enrollment>()
    ..sections = ListBuilder<Section>()
    ..name = ''
    ..needsGradingCount = 0
    ..hideFinalGrades = false
    ..isPublic = false
    ..applyAssignmentGroupWeights = false
    ..isFavorite = false
    ..accessRestrictedByDate = false
    ..hasWeightedGradingPeriods = false
    ..hasGradingPeriods = false
    ..restrictEnrollmentsToCourseDates = false;

  /// Get the course grade.
  /// Optional:
  /// [forceAllPeriods] -> Used to determine if there's an active grading period on the enrollment, true for this will
  ///   always force no active grading period
  /// [enrollment] -> If specified, will use for the grading period grades, unless a non-null value is provided this
  ///   will default to the student's enrollment in the course
  /// [gradingPeriodId] -> Only used when [enrollment] is not provided or is null, when pulling the student's enrollment
  ///   from the course will also match based on [Enrollment.currentGradingPeriodId]
  CourseGrade getCourseGrade(
    String studentId, {
    Enrollment enrollment,
    String gradingPeriodId,
    bool forceAllPeriods = false,
  }) =>
      CourseGrade(
        this,
        enrollment ??
            enrollments.firstWhere(
              (enrollment) =>
                  enrollment.userId == studentId &&
                  (gradingPeriodId == null ||
                      gradingPeriodId.isEmpty ||
                      gradingPeriodId == enrollment.currentGradingPeriodId),
              orElse: () => null,
            ),
        forceAllPeriods: forceAllPeriods,
      );

  String contextFilterId() => 'course_${this.id}';

  /// Filters enrollments by those associated with the currently selected user
  bool isValidForCurrentStudent(String currentStudentId) {
    return enrollments?.any((enrollment) => enrollment.userId == currentStudentId) ?? false;
  }
}

@BuiltValueEnum(wireName: 'default_view')
class HomePage extends EnumClass {
  const HomePage._(String name) : super(name);

  static BuiltSet<HomePage> get values => _$homePageValues;

  static HomePage valueOf(String name) => _$homePageValueOf(name);

  static Serializer<HomePage> get serializer => _$homePageSerializer;

  static const HomePage feed = _$homePageFeed;

  /// Front Page
  static const HomePage wiki = _$homePageWiki;

  static const HomePage modules = _$homePageModules;

  static const HomePage assignments = _$homePageAssignments;

  @BuiltValueEnumConst(fallback: true)
  static const HomePage syllabus = _$homePageSyllabus;
}
