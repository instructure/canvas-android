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

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/json_object.dart';
import 'package:built_value/serializer.dart';

import 'attachment.dart';

part 'media_comment.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build
abstract class MediaComment implements Built<MediaComment, MediaCommentBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<MediaComment> get serializer => _$mediaCommentSerializer;

  @BuiltValueField(wireName: 'media_id')
  String? get mediaId;

  @BuiltValueField(wireName: 'display_name')
  String? get displayName;

  String? get url;

  /// Can be either 'audio' or 'video'
  @BuiltValueField(wireName: 'media_type')
  MediaType get mediaType;

  @BuiltValueField(wireName: 'content-type')
  String? get contentType;

  MediaComment._();
  factory MediaComment([void Function(MediaCommentBuilder) updates]) = _$MediaComment;

  Attachment toAttachment() {
    return Attachment((a) => a
      ..jsonId = JsonObject('media-comment-$mediaId')
      ..contentType = contentType
      ..filename = mediaId
      ..displayName = displayName
      ..url = url);
  }
}

@BuiltValueEnum(wireName: 'media_type')
class MediaType extends EnumClass {
  const MediaType._(String name) : super(name);

  static BuiltSet<MediaType> get values => _$mediaTypeValues;

  static MediaType valueOf(String name) => _$mediaTypeValueOf(name);

  static Serializer<MediaType> get serializer => _$mediaTypeSerializer;

  static const MediaType audio = _$mediaTypeAudio;

  static const MediaType video = _$mediaTypeVideo;

  @BuiltValueEnumConst(fallback: true)
  static const MediaType unknown = _$mediaTypeUnknown;
}
