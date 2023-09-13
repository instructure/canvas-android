// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'conversation.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

const ConversationWorkflowState _$conversationWorkflowStateRead =
    const ConversationWorkflowState._('read');
const ConversationWorkflowState _$conversationWorkflowStateUnread =
    const ConversationWorkflowState._('unread');
const ConversationWorkflowState _$conversationWorkflowStateArchived =
    const ConversationWorkflowState._('archived');
const ConversationWorkflowState _$conversationWorkflowStateUnknown =
    const ConversationWorkflowState._('unknown');

ConversationWorkflowState _$conversationWorkflowStateValueOf(String name) {
  switch (name) {
    case 'read':
      return _$conversationWorkflowStateRead;
    case 'unread':
      return _$conversationWorkflowStateUnread;
    case 'archived':
      return _$conversationWorkflowStateArchived;
    case 'unknown':
      return _$conversationWorkflowStateUnknown;
    default:
      return _$conversationWorkflowStateUnknown;
  }
}

final BuiltSet<ConversationWorkflowState> _$conversationWorkflowStateValues =
    new BuiltSet<ConversationWorkflowState>(const <ConversationWorkflowState>[
  _$conversationWorkflowStateRead,
  _$conversationWorkflowStateUnread,
  _$conversationWorkflowStateArchived,
  _$conversationWorkflowStateUnknown,
]);

Serializer<Conversation> _$conversationSerializer =
    new _$ConversationSerializer();
Serializer<ConversationWorkflowState> _$conversationWorkflowStateSerializer =
    new _$ConversationWorkflowStateSerializer();

class _$ConversationSerializer implements StructuredSerializer<Conversation> {
  @override
  final Iterable<Type> types = const [Conversation, _$Conversation];
  @override
  final String wireName = 'Conversation';

  @override
  Iterable<Object?> serialize(Serializers serializers, Conversation object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'subject',
      serializers.serialize(object.subject,
          specifiedType: const FullType(String)),
      'workflow_state',
      serializers.serialize(object.workflowState,
          specifiedType: const FullType(ConversationWorkflowState)),
      'message_count',
      serializers.serialize(object.messageCount,
          specifiedType: const FullType(int)),
      'subscribed',
      serializers.serialize(object.isSubscribed,
          specifiedType: const FullType(bool)),
      'starred',
      serializers.serialize(object.isStarred,
          specifiedType: const FullType(bool)),
      'visible',
      serializers.serialize(object.isVisible,
          specifiedType: const FullType(bool)),
    ];
    Object? value;
    value = object.lastMessage;
    if (value != null) {
      result
        ..add('last_message')
        ..add(serializers.serialize(value,
            specifiedType: const FullType(String)));
    }
    value = object.lastAuthoredMessage;
    if (value != null) {
      result
        ..add('last_authored_message')
        ..add(serializers.serialize(value,
            specifiedType: const FullType(String)));
    }
    value = object.lastMessageAt;
    if (value != null) {
      result
        ..add('last_message_at')
        ..add(serializers.serialize(value,
            specifiedType: const FullType(DateTime)));
    }
    value = object.lastAuthoredMessageAt;
    if (value != null) {
      result
        ..add('last_authored_message_at')
        ..add(serializers.serialize(value,
            specifiedType: const FullType(DateTime)));
    }
    value = object.avatarUrl;
    if (value != null) {
      result
        ..add('avatar_url')
        ..add(serializers.serialize(value,
            specifiedType: const FullType(String)));
    }
    value = object.audience;
    if (value != null) {
      result
        ..add('audience')
        ..add(serializers.serialize(value,
            specifiedType:
                const FullType(BuiltList, const [const FullType(String)])));
    }
    value = object.participants;
    if (value != null) {
      result
        ..add('participants')
        ..add(serializers.serialize(value,
            specifiedType:
                const FullType(BuiltList, const [const FullType(BasicUser)])));
    }
    value = object.messages;
    if (value != null) {
      result
        ..add('messages')
        ..add(serializers.serialize(value,
            specifiedType:
                const FullType(BuiltList, const [const FullType(Message)])));
    }
    value = object.contextName;
    if (value != null) {
      result
        ..add('context_name')
        ..add(serializers.serialize(value,
            specifiedType: const FullType(String)));
    }
    value = object.contextCode;
    if (value != null) {
      result
        ..add('context_code')
        ..add(serializers.serialize(value,
            specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  Conversation deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new ConversationBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'subject':
          result.subject = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'workflow_state':
          result.workflowState = serializers.deserialize(value,
                  specifiedType: const FullType(ConversationWorkflowState))!
              as ConversationWorkflowState;
          break;
        case 'last_message':
          result.lastMessage = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'last_authored_message':
          result.lastAuthoredMessage = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'last_message_at':
          result.lastMessageAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'last_authored_message_at':
          result.lastAuthoredMessageAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'message_count':
          result.messageCount = serializers.deserialize(value,
              specifiedType: const FullType(int))! as int;
          break;
        case 'subscribed':
          result.isSubscribed = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'starred':
          result.isStarred = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'avatar_url':
          result.avatarUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'visible':
          result.isVisible = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'audience':
          result.audience.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(String)]))!
              as BuiltList<Object?>);
          break;
        case 'participants':
          result.participants.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(BasicUser)]))!
              as BuiltList<Object?>);
          break;
        case 'messages':
          result.messages.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Message)]))!
              as BuiltList<Object?>);
          break;
        case 'context_name':
          result.contextName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'context_code':
          result.contextCode = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$ConversationWorkflowStateSerializer
    implements PrimitiveSerializer<ConversationWorkflowState> {
  @override
  final Iterable<Type> types = const <Type>[ConversationWorkflowState];
  @override
  final String wireName = 'workflow_state';

  @override
  Object serialize(Serializers serializers, ConversationWorkflowState object,
          {FullType specifiedType = FullType.unspecified}) =>
      object.name;

  @override
  ConversationWorkflowState deserialize(
          Serializers serializers, Object serialized,
          {FullType specifiedType = FullType.unspecified}) =>
      ConversationWorkflowState.valueOf(serialized as String);
}

class _$Conversation extends Conversation {
  @override
  final String id;
  @override
  final String subject;
  @override
  final ConversationWorkflowState workflowState;
  @override
  final String? lastMessage;
  @override
  final String? lastAuthoredMessage;
  @override
  final DateTime? lastMessageAt;
  @override
  final DateTime? lastAuthoredMessageAt;
  @override
  final int messageCount;
  @override
  final bool isSubscribed;
  @override
  final bool isStarred;
  @override
  final String? avatarUrl;
  @override
  final bool isVisible;
  @override
  final BuiltList<String>? audience;
  @override
  final BuiltList<BasicUser>? participants;
  @override
  final BuiltList<Message>? messages;
  @override
  final String? contextName;
  @override
  final String? contextCode;

  factory _$Conversation([void Function(ConversationBuilder)? updates]) =>
      (new ConversationBuilder()..update(updates))._build();

  _$Conversation._(
      {required this.id,
      required this.subject,
      required this.workflowState,
      this.lastMessage,
      this.lastAuthoredMessage,
      this.lastMessageAt,
      this.lastAuthoredMessageAt,
      required this.messageCount,
      required this.isSubscribed,
      required this.isStarred,
      this.avatarUrl,
      required this.isVisible,
      this.audience,
      this.participants,
      this.messages,
      this.contextName,
      this.contextCode})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Conversation', 'id');
    BuiltValueNullFieldError.checkNotNull(subject, r'Conversation', 'subject');
    BuiltValueNullFieldError.checkNotNull(
        workflowState, r'Conversation', 'workflowState');
    BuiltValueNullFieldError.checkNotNull(
        messageCount, r'Conversation', 'messageCount');
    BuiltValueNullFieldError.checkNotNull(
        isSubscribed, r'Conversation', 'isSubscribed');
    BuiltValueNullFieldError.checkNotNull(
        isStarred, r'Conversation', 'isStarred');
    BuiltValueNullFieldError.checkNotNull(
        isVisible, r'Conversation', 'isVisible');
  }

  @override
  Conversation rebuild(void Function(ConversationBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  ConversationBuilder toBuilder() => new ConversationBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Conversation &&
        id == other.id &&
        subject == other.subject &&
        workflowState == other.workflowState &&
        lastMessage == other.lastMessage &&
        lastAuthoredMessage == other.lastAuthoredMessage &&
        lastMessageAt == other.lastMessageAt &&
        lastAuthoredMessageAt == other.lastAuthoredMessageAt &&
        messageCount == other.messageCount &&
        isSubscribed == other.isSubscribed &&
        isStarred == other.isStarred &&
        avatarUrl == other.avatarUrl &&
        isVisible == other.isVisible &&
        audience == other.audience &&
        participants == other.participants &&
        messages == other.messages &&
        contextName == other.contextName &&
        contextCode == other.contextCode;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, subject.hashCode);
    _$hash = $jc(_$hash, workflowState.hashCode);
    _$hash = $jc(_$hash, lastMessage.hashCode);
    _$hash = $jc(_$hash, lastAuthoredMessage.hashCode);
    _$hash = $jc(_$hash, lastMessageAt.hashCode);
    _$hash = $jc(_$hash, lastAuthoredMessageAt.hashCode);
    _$hash = $jc(_$hash, messageCount.hashCode);
    _$hash = $jc(_$hash, isSubscribed.hashCode);
    _$hash = $jc(_$hash, isStarred.hashCode);
    _$hash = $jc(_$hash, avatarUrl.hashCode);
    _$hash = $jc(_$hash, isVisible.hashCode);
    _$hash = $jc(_$hash, audience.hashCode);
    _$hash = $jc(_$hash, participants.hashCode);
    _$hash = $jc(_$hash, messages.hashCode);
    _$hash = $jc(_$hash, contextName.hashCode);
    _$hash = $jc(_$hash, contextCode.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Conversation')
          ..add('id', id)
          ..add('subject', subject)
          ..add('workflowState', workflowState)
          ..add('lastMessage', lastMessage)
          ..add('lastAuthoredMessage', lastAuthoredMessage)
          ..add('lastMessageAt', lastMessageAt)
          ..add('lastAuthoredMessageAt', lastAuthoredMessageAt)
          ..add('messageCount', messageCount)
          ..add('isSubscribed', isSubscribed)
          ..add('isStarred', isStarred)
          ..add('avatarUrl', avatarUrl)
          ..add('isVisible', isVisible)
          ..add('audience', audience)
          ..add('participants', participants)
          ..add('messages', messages)
          ..add('contextName', contextName)
          ..add('contextCode', contextCode))
        .toString();
  }
}

class ConversationBuilder
    implements Builder<Conversation, ConversationBuilder> {
  _$Conversation? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _subject;
  String? get subject => _$this._subject;
  set subject(String? subject) => _$this._subject = subject;

  ConversationWorkflowState? _workflowState;
  ConversationWorkflowState? get workflowState => _$this._workflowState;
  set workflowState(ConversationWorkflowState? workflowState) =>
      _$this._workflowState = workflowState;

  String? _lastMessage;
  String? get lastMessage => _$this._lastMessage;
  set lastMessage(String? lastMessage) => _$this._lastMessage = lastMessage;

  String? _lastAuthoredMessage;
  String? get lastAuthoredMessage => _$this._lastAuthoredMessage;
  set lastAuthoredMessage(String? lastAuthoredMessage) =>
      _$this._lastAuthoredMessage = lastAuthoredMessage;

  DateTime? _lastMessageAt;
  DateTime? get lastMessageAt => _$this._lastMessageAt;
  set lastMessageAt(DateTime? lastMessageAt) =>
      _$this._lastMessageAt = lastMessageAt;

  DateTime? _lastAuthoredMessageAt;
  DateTime? get lastAuthoredMessageAt => _$this._lastAuthoredMessageAt;
  set lastAuthoredMessageAt(DateTime? lastAuthoredMessageAt) =>
      _$this._lastAuthoredMessageAt = lastAuthoredMessageAt;

  int? _messageCount;
  int? get messageCount => _$this._messageCount;
  set messageCount(int? messageCount) => _$this._messageCount = messageCount;

  bool? _isSubscribed;
  bool? get isSubscribed => _$this._isSubscribed;
  set isSubscribed(bool? isSubscribed) => _$this._isSubscribed = isSubscribed;

  bool? _isStarred;
  bool? get isStarred => _$this._isStarred;
  set isStarred(bool? isStarred) => _$this._isStarred = isStarred;

  String? _avatarUrl;
  String? get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String? avatarUrl) => _$this._avatarUrl = avatarUrl;

  bool? _isVisible;
  bool? get isVisible => _$this._isVisible;
  set isVisible(bool? isVisible) => _$this._isVisible = isVisible;

  ListBuilder<String>? _audience;
  ListBuilder<String> get audience =>
      _$this._audience ??= new ListBuilder<String>();
  set audience(ListBuilder<String>? audience) => _$this._audience = audience;

  ListBuilder<BasicUser>? _participants;
  ListBuilder<BasicUser> get participants =>
      _$this._participants ??= new ListBuilder<BasicUser>();
  set participants(ListBuilder<BasicUser>? participants) =>
      _$this._participants = participants;

  ListBuilder<Message>? _messages;
  ListBuilder<Message> get messages =>
      _$this._messages ??= new ListBuilder<Message>();
  set messages(ListBuilder<Message>? messages) => _$this._messages = messages;

  String? _contextName;
  String? get contextName => _$this._contextName;
  set contextName(String? contextName) => _$this._contextName = contextName;

  String? _contextCode;
  String? get contextCode => _$this._contextCode;
  set contextCode(String? contextCode) => _$this._contextCode = contextCode;

  ConversationBuilder() {
    Conversation._initializeBuilder(this);
  }

  ConversationBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _subject = $v.subject;
      _workflowState = $v.workflowState;
      _lastMessage = $v.lastMessage;
      _lastAuthoredMessage = $v.lastAuthoredMessage;
      _lastMessageAt = $v.lastMessageAt;
      _lastAuthoredMessageAt = $v.lastAuthoredMessageAt;
      _messageCount = $v.messageCount;
      _isSubscribed = $v.isSubscribed;
      _isStarred = $v.isStarred;
      _avatarUrl = $v.avatarUrl;
      _isVisible = $v.isVisible;
      _audience = $v.audience?.toBuilder();
      _participants = $v.participants?.toBuilder();
      _messages = $v.messages?.toBuilder();
      _contextName = $v.contextName;
      _contextCode = $v.contextCode;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Conversation other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Conversation;
  }

  @override
  void update(void Function(ConversationBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Conversation build() => _build();

  _$Conversation _build() {
    _$Conversation _$result;
    try {
      _$result = _$v ??
          new _$Conversation._(
              id: BuiltValueNullFieldError.checkNotNull(
                  id, r'Conversation', 'id'),
              subject: BuiltValueNullFieldError.checkNotNull(
                  subject, r'Conversation', 'subject'),
              workflowState: BuiltValueNullFieldError.checkNotNull(
                  workflowState, r'Conversation', 'workflowState'),
              lastMessage: lastMessage,
              lastAuthoredMessage: lastAuthoredMessage,
              lastMessageAt: lastMessageAt,
              lastAuthoredMessageAt: lastAuthoredMessageAt,
              messageCount: BuiltValueNullFieldError.checkNotNull(
                  messageCount, r'Conversation', 'messageCount'),
              isSubscribed: BuiltValueNullFieldError.checkNotNull(
                  isSubscribed, r'Conversation', 'isSubscribed'),
              isStarred: BuiltValueNullFieldError.checkNotNull(
                  isStarred, r'Conversation', 'isStarred'),
              avatarUrl: avatarUrl,
              isVisible: BuiltValueNullFieldError.checkNotNull(
                  isVisible, r'Conversation', 'isVisible'),
              audience: _audience?.build(),
              participants: _participants?.build(),
              messages: _messages?.build(),
              contextName: contextName,
              contextCode: contextCode);
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'audience';
        _audience?.build();
        _$failedField = 'participants';
        _participants?.build();
        _$failedField = 'messages';
        _messages?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'Conversation', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
