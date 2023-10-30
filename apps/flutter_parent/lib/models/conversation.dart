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
import 'package:built_value/serializer.dart';

import 'basic_user.dart';
import 'message.dart';

part 'conversation.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter pub run build_runner build
abstract class Conversation implements Built<Conversation, ConversationBuilder> {
  @BuiltValueSerializer(serializeNulls: false) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<Conversation> get serializer => _$conversationSerializer;

  /// The unique id for the conversation
  String get id;

  /// Message Subject
  String get subject;

  /// The workflowState of the conversation (unread, read, archived)
  @BuiltValueField(wireName: 'workflow_state')
  ConversationWorkflowState get workflowState;

  /// 100 character preview of the last message
  @BuiltValueField(wireName: 'last_message')
  String? get lastMessage;

  /// 100 character preview of the last authored message
  @BuiltValueField(wireName: 'last_authored_message')
  String? get lastAuthoredMessage;

  /// Date of the last message sent
  @BuiltValueField(wireName: 'last_message_at')
  DateTime? get lastMessageAt;

  @BuiltValueField(wireName: 'last_authored_message_at')
  DateTime? get lastAuthoredMessageAt;

  // Number of messages in the conversation.
  @BuiltValueField(wireName: 'message_count')
  int get messageCount;

  /// Whether or not the user is subscribed to the current message
  @BuiltValueField(wireName: 'subscribed')
  bool get isSubscribed;

  /// Whether or not the message is starred
  @BuiltValueField(wireName: 'starred')
  bool get isStarred;

  /// The avatar to display. Knows if group, user, etc.
  @BuiltValueField(wireName: 'avatar_url')
  String? get avatarUrl;

  /// Whether this conversation is visible in the current context. Not 100% what that means
  @BuiltValueField(wireName: 'visible')
  bool get isVisible;

  /// The IDs of all people in the conversation. EXCLUDING the current user unless it's a monologue
  BuiltList<String>? get audience;

  /// The name and IDs of all participants in the conversation
  BuiltList<BasicUser>? get participants;

  /// Messages attached to the conversation
  BuiltList<Message>? get messages;

  @BuiltValueField(wireName: 'context_name')
  String? get contextName;

  @BuiltValueField(wireName: 'context_code')
  String? get contextCode;

  bool isUnread() => workflowState == ConversationWorkflowState.unread;

  Conversation._();
  factory Conversation([void Function(ConversationBuilder) updates]) = _$Conversation;

  static void _initializeBuilder(ConversationBuilder b) => b
    ..id = ''
    ..subject = ''
    ..messageCount = 0
    ..isSubscribed = false
    ..workflowState = ConversationWorkflowState.unread
    ..isStarred = false
    ..isVisible = false;

  String? getContextId() {
    if (contextCode == null) {
      return null;
    }
    final index = contextCode!.indexOf('_');
    return contextCode!.substring(index + 1, contextCode!.length);
  }
}

@BuiltValueEnum(wireName: 'workflow_state')
class ConversationWorkflowState extends EnumClass {
  const ConversationWorkflowState._(String name) : super(name);

  static BuiltSet<ConversationWorkflowState> get values => _$conversationWorkflowStateValues;

  static ConversationWorkflowState valueOf(String name) => _$conversationWorkflowStateValueOf(name);

  static Serializer<ConversationWorkflowState> get serializer => _$conversationWorkflowStateSerializer;

  static const ConversationWorkflowState read = _$conversationWorkflowStateRead;

  static const ConversationWorkflowState unread = _$conversationWorkflowStateUnread;

  static const ConversationWorkflowState archived = _$conversationWorkflowStateArchived;

  @BuiltValueEnumConst(fallback: true)
  static const ConversationWorkflowState unknown = _$conversationWorkflowStateUnknown;
}
