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

library serializers;

import 'package:built_collection/built_collection.dart';
import 'package:built_value/iso_8601_date_time_serializer.dart';
import 'package:built_value/serializer.dart';
import 'package:built_value/standard_json_plugin.dart';
import 'package:flutter_parent/models/alert.dart';
import 'package:flutter_parent/models/announcement.dart';
import 'package:flutter_parent/models/assignment.dart';
import 'package:flutter_parent/models/assignment_group.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/canvas_token.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/grade.dart';
import 'package:flutter_parent/models/media_comment.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/models/mobile_verify_result.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/models/remote_file.dart';
import 'package:flutter_parent/models/school_domain.dart';
import 'package:flutter_parent/models/submission.dart';
import 'package:flutter_parent/models/unread_count.dart';
import 'package:flutter_parent/models/user.dart';

import 'file_upload_config.dart';

part 'serializers.g.dart';

/// If changes are made, run `flutter pub run build_runner build` from the project root. Alternatively, you can
/// have it watch for changes and automatically build if you run `flutter pub run build_runner watch`.
@SerializersFor([
  Alert,
  Announcement,
  Assignment,
  AssignmentGroup,
  Attachment,
  BasicUser,
  CanvasToken,
  Conversation,
  Course,
  Enrollment,
  FileUploadConfig,
  Grade,
  MediaComment,
  Message,
  MobileVerifyResult,
  Recipient,
  RemoteFile,
  SchoolDomain,
  Submission,
  UnreadCount,
  User,
])
final Serializers _serializers = _$_serializers;

Serializers jsonSerializers = (_serializers.toBuilder()
      ..addPlugin(StandardJsonPlugin())
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

T deserialize<T>(dynamic value) => jsonSerializers.deserializeWith<T>(jsonSerializers.serializerForType(T), value);

dynamic serialize<T>(T value) => jsonSerializers.serializeWith(jsonSerializers.serializerForType(T), value);

List<T> deserializeList<T>(dynamic value) => List.from(value?.map((value) => deserialize<T>(value))?.toList() ?? []);
