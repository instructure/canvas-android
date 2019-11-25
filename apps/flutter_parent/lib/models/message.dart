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

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import 'attachment.dart';
import 'media_comment.dart';

part 'message.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build
abstract class Message implements Built<Message, MessageBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<Message> get serializer => _$messageSerializer;

  int get id;

  @BuiltValueField(wireName: "created_at")
  @nullable
  DateTime get createdAt;

  @nullable
  String get body;

  @BuiltValueField(wireName: "author_id")
  int get authorId;

  @BuiltValueField(wireName: "generated")
  bool get isGenerated;

  @nullable
  BuiltList<Attachment> get attachments;

  @BuiltValueField(wireName: "media_comment")
  @nullable
  MediaComment get mediaComment;

  @BuiltValueField(wireName: "forwarded_messages")
  @nullable
  BuiltList<Message> get forwardedMessages;

  @BuiltValueField(wireName: "participating_user_ids")
  @nullable
  BuiltList<int> get participatingUserIds;

  Message._();
  factory Message([void Function(MessageBuilder) updates]) = _$Message;
}
