/*
 * Copyright (C) 2020 - present Instructure, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/serializers.dart';
import 'package:flutter_parent/models/submission.dart';

part 'submission_wrapper.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
///
/// This enables us to handle both single submission and lists of submission responses.
/// The parent grades list is an example of where the api will return this list, while most other
/// locations will use a single submission (i.e. student grades list).
abstract class SubmissionWrapper implements Built<SubmissionWrapper, SubmissionWrapperBuilder> {
  @BuiltValueSerializer(custom: true)
  static Serializer<SubmissionWrapper> get serializer => SubmissionWrapperSerializer();

  Submission? get submission;

  BuiltList<Submission>? get submissionList;

  SubmissionWrapper._();

  factory SubmissionWrapper([updates(SubmissionWrapperBuilder b)]) = _$SubmissionWrapper;
}

class SubmissionWrapperSerializer implements StructuredSerializer<SubmissionWrapper> {
  @override
  SubmissionWrapper deserialize(Serializers serializers, Iterable serialized,
      {FullType specifiedType = FullType.unspecified}) {
    // Initialize an empty builder
    final result = new SubmissionWrapperBuilder();

    if (serialized.first == 'id' || serialized.first is String) {
      // This is the single submission case
      Submission submission =
          jsonSerializers.deserializeWith(jsonSerializers.serializerForType(Submission)!, serialized);

      result.submission = submission.toBuilder();
    } else {
      // This is a list of submissions
      List<Submission> submissionList = [];

      serialized.forEach((serializedSubmission) {
        submissionList
            .add(jsonSerializers.deserializeWith(jsonSerializers.serializerForType(Submission)!, serializedSubmission));
      });

      result.submissionList = BuiltList<Submission>.from(submissionList).toBuilder();
    }

    return result.build();
  }

  @override
  Iterable serialize(Serializers serializers, SubmissionWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[];

    // Regardless of how we were storing it, we need to serialize it as "submission" since that's what the api expects
    if (object.submission != null) {
      result
        ..add('submission')
        ..add(serializers.serialize(object.submission, specifiedType: const FullType(Submission)));
    }
    if (object.submissionList != null) {
      result
        ..add('submission')
        ..add(serializers.serialize(object.submissionList,
            specifiedType: const FullType(BuiltList, const [const FullType(Submission)])));
    }

    return result;
  }

  @override
  Iterable<Type> get types => [SubmissionWrapper, _$SubmissionWrapper]; //[SubmissionWrapper, _$SubmissionWrapper];

  @override
  String get wireName => 'SubmissionWrapper';
}
