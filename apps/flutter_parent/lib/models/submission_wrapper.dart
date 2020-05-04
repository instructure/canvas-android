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
import 'package:flutter_parent/models/submission.dart';

part 'submission_wrapper.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build --delete-conflicting-outputs
abstract class SubmissionWrapper implements Built<SubmissionWrapper, SubmissionWrapperBuilder> {
  @BuiltValueSerializer(custom: true)
  static Serializer<SubmissionWrapper> get serializer => SubmissionWrapperSerializer();

  @nullable
  Submission get submission;

  @nullable
  BuiltList<Submission> get submissionList;

  SubmissionWrapper._();

  factory SubmissionWrapper([updates(SubmissionWrapperBuilder b)]) = _$SubmissionWrapper;
}

class SubmissionWrapperSerializer implements StructuredSerializer<SubmissionWrapper> {
  @override
  SubmissionWrapper deserialize(Serializers serializers, Iterable serialized,
      {FullType specifiedType = FullType.unspecified}) {
    // Initialize an empty builder
    final result = new SubmissionWrapperBuilder();

    // Create an `Iterator` from the serialized data received
    final iterator = serialized.iterator;
    // Loop the iterator for each key
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      // for each key, assign the correct value to the builder
      switch (key) {
        case 'submission':
          // If the value is of type List<dynamic>, assign it to `values`
          if (value is List<dynamic>) {
            result.submissionList.replace(serializers.deserialize(value,
                specifiedType: const FullType(BuiltList, const [const FullType(Submission)])) as BuiltList);
            // else, the value is of type `String`
          } else {
            result.submission = serializers.deserialize(value.toString(), specifiedType: const FullType(Submission))
                as SubmissionBuilder;
          }
          break;
      }
    }

    return result.build();
  }

  @override
  Iterable serialize(Serializers serializers, SubmissionWrapper object,
      {FullType specifiedType = FullType.unspecified}) {
    // Create an empty object array
    final result = <Object>[];
    // if the value of the `CustomValue` is not null, then assign it to a String
    if (object.submission != null) {
      result..add('submission')..add(serializers.serialize(object.submission, specifiedType: const FullType(String)));
    }
    // Else, it means that we have a list. In this case the list will always override
    // the defined String value
    if (object.submissionList != null) {
      result
        ..add('submissionList')
        ..add(serializers.serialize(object.submissionList,
            specifiedType: const FullType(BuiltList, const [const FullType(String)])));
    }

    return result;
  }

  @override
  Iterable<Type> get types => [SubmissionWrapper, _$SubmissionWrapper]; //[SubmissionWrapper, _$SubmissionWrapper];

  @override
  String get wireName => 'SubmissionWrapper';
}
