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

library serializers;

import 'package:built_collection/built_collection.dart';
import 'package:built_value/iso_8601_date_time_serializer.dart';
import 'package:built_value/json_object.dart';
import 'package:built_value/serializer.dart';
import 'package:built_value/standard_json_plugin.dart';
import 'package:flutter_parent/models/account_creation_models/create_account_post_body.dart';
import 'package:flutter_parent/models/account_creation_models/post_pairing_code.dart';
import 'package:flutter_parent/models/account_creation_models/post_pseudonym.dart';
import 'package:flutter_parent/models/account_creation_models/post_user.dart';
import 'package:flutter_parent/models/account_notification.dart';
import 'package:flutter_parent/models/account_permissions.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/alert_threshold.dart';
import 'package:flutter_parent/models/announcement.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/authenticated_url.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/canvas_page.dart';
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/course_permissions.dart';
import 'package:flutter_parent/models/course_tab.dart';
import 'package:flutter_parent/models/dataseeding/communication_channel.dart';
import 'package:flutter_parent/models/dataseeding/create_assignment_info.dart';
import 'package:flutter_parent/models/dataseeding/oauth_token.dart';
import 'package:flutter_parent/models/dataseeding/pseudonym.dart';
import 'package:flutter_parent/models/dataseeding/quiz.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grade.dart';
import 'package:flutter_parent/models/grading_period.dart';
import 'package:flutter_parent/models/grading_period_response.dart';
import 'package:flutter_parent/models/grading_scheme_item.dart';
import 'package:flutter_parent/models/help_link.dart';
import 'package:flutter_parent/models/help_links.dart';
import 'package:flutter_parent/models/lock_info.dart';
import 'package:flutter_parent/models/locked_module.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/media_comment.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/notification_payload.dart';
import 'package:flutter_parent/models/plannable.dart';
import 'package:flutter_parent/models/planner_item.dart';
import 'package:flutter_parent/models/planner_submission.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/models/reminder.dart';
import 'package:flutter_parent/models/remote_file.dart';
import 'package:flutter_parent/models/schedule_item.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/models/section.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/models/submission_wrapper.dart';
import 'package:flutter_parent/models/term.dart';
import 'package:flutter_parent/models/terms_of_service.dart';
import 'package:flutter_parent/models/unread_count.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/models/user_colors.dart';

import 'assignment_override.dart';
import 'color_change_response.dart';
import 'course_settings.dart';
import 'dataseeding/create_assignment_wrapper.dart';
import 'dataseeding/create_course_info.dart';
import 'dataseeding/create_course_wrapper.dart';
import 'dataseeding/create_enrollment_info.dart';
import 'dataseeding/create_enrollment_wrapper.dart';
import 'dataseeding/create_submission_info.dart';
import 'dataseeding/create_submission_wrapper.dart';
import 'dataseeding/create_user_info.dart';
import 'dataseeding/grade_submission_info.dart';
import 'dataseeding/grade_submission_wrapper.dart';
import 'dataseeding/pairing_code.dart';
import 'dataseeding/seed_context.dart';
import 'dataseeding/seeded_user.dart';
import 'dataseeding/user_name_data.dart';
import 'feature_flags.dart';
import 'file_upload_config.dart';

part 'serializers.g.dart';

/// If changes are made, run `flutter pub run build_runner build` from the project root. Alternatively, you can
/// have it watch for changes and automatically build if you run `flutter pub run build_runner watch`.
@SerializersFor([
  AccountNotification,
  AccountPermissions,
  Alert,
  AlertThreshold,
  Announcement,
  Assignment,
  AssignmentGroup,
  AssignmentOverride,
  Attachment,
  AuthenticatedUrl,
  BasicUser,
  CanvasPage,
  CanvasToken,
  CommunicationChannel,
  ColorChangeResponse,
  Conversation,
  Course,
  CoursePermissions,
  CourseSettings,
  CourseTab,
  CreateAssignmentInfo,
  CreateAssignmentWrapper,
  CreateAccountPostBody,
  CreateCourseInfo,
  CreateCourseWrapper,
  CreateEnrollmentInfo,
  CreateEnrollmentWrapper,
  CreateSubmissionInfo,
  CreateSubmissionWrapper,
  CreateUserInfo,
  Enrollment,
  FeatureFlags,
  FileUploadConfig,
  Grade,
  GradeSubmissionInfo,
  GradeSubmissionWrapper,
  GradingPeriod,
  GradingPeriodResponse,
  HelpLink,
  HelpLinks,
  LockInfo,
  LockedModule,
  Login,
  MediaComment,
  Message,
  MobileVerifyResult,
  NotificationPayload,
  OAuthToken,
  PairingCode,
  Plannable,
  PlannerItem,
  PlannerSubmission,
  PostPairingCode,
  PostPseudonym,
  PostUser,
  Pseudonym,
  Quiz,
  Recipient,
  Reminder,
  RemoteFile,
  Section,
  SchoolDomain,
  ScheduleItem,
  Section,
  SeedContext,
  SeededUser,
  Submission,
  SubmissionWrapper,
  Term,
  TermsOfService,
  UnreadCount,
  User,
  UserColors,
  UserNameData,
  GradingSchemeItem,
])
final Serializers _serializers = _$_serializers;

Serializers jsonSerializers = (_serializers.toBuilder()
      ..addPlugin(StandardJsonPlugin())
      ..addPlugin(RemoveNullInMapConvertedListPlugin())
      ..add(Iso8601DateTimeSerializer())
      ..add(ResultEnumSerializer())
      ..addBuilderFactory(FullType(BuiltList, [FullType(String)]), () => ListBuilder<String>())
      ..addBuilderFactory(
          FullType(BuiltMap, [
            FullType(String),
            FullType(BuiltList, [FullType(String)])
          ]),
          () => MapBuilder<String, BuiltList<String>>())
      ..addBuilderFactory(FullType(BuiltMap, [FullType(String), FullType(String)]), () => MapBuilder<String, String>()))
    .build();

T? deserialize<T>(dynamic value) {
  var serializer = jsonSerializers.serializerForType(T);
  if (serializer == null || !(serializer is Serializer<T>)) return null;
  return jsonSerializers.deserializeWith<T>(serializer, value);
}

dynamic serialize<T>(T value) {
  var serializer = jsonSerializers.serializerForType(T);
  if (serializer == null) return null;
  return jsonSerializers.serializeWith(serializer, value);
}

List<T> deserializeList<T>(dynamic value) => List.from(value?.map((value) => deserialize<T>(value))?.toList() ?? []);

/// Plugin that works around an issue where deserialization breaks if a map key is null
/// Sourced from https://github.com/google/built_value.dart/issues/653#issuecomment-495964030
class RemoveNullInMapConvertedListPlugin implements SerializerPlugin {
  Object? beforeSerialize(Object? object, FullType specifiedType) => object;

  Object? afterSerialize(Object? object, FullType specifiedType) => object;

  Object? beforeDeserialize(Object? object, FullType specifiedType) {
    if (specifiedType.root == BuiltMap && object is List) {
      return object.where((v) => v != null).toList();
    }
    return object;
  }

  Object? afterDeserialize(Object? object, FullType specifiedType) => object;
}
