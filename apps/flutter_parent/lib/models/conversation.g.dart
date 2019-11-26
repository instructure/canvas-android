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
  Iterable<Object> serialize(Serializers serializers, Conversation object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(int)),
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
    if (object.lastMessage != null) {
      result
        ..add('last_message')
        ..add(serializers.serialize(object.lastMessage,
            specifiedType: const FullType(String)));
    }
    if (object.lastAuthoredMessage != null) {
      result
        ..add('last_authored_message')
        ..add(serializers.serialize(object.lastAuthoredMessage,
            specifiedType: const FullType(String)));
    }
    if (object.lastMessageAt != null) {
      result
        ..add('last_message_at')
        ..add(serializers.serialize(object.lastMessageAt,
            specifiedType: const FullType(DateTime)));
    }
    if (object.lastAuthoredMessageAt != null) {
      result
        ..add('last_authored_message_at')
        ..add(serializers.serialize(object.lastAuthoredMessageAt,
            specifiedType: const FullType(DateTime)));
    }
    if (object.avatarUrl != null) {
      result
        ..add('avatar_url')
        ..add(serializers.serialize(object.avatarUrl,
            specifiedType: const FullType(String)));
    }
    if (object.audience != null) {
      result
        ..add('audience')
        ..add(serializers.serialize(object.audience,
            specifiedType:
                const FullType(BuiltList, const [const FullType(int)])));
    }
    if (object.participants != null) {
      result
        ..add('participants')
        ..add(serializers.serialize(object.participants,
            specifiedType:
                const FullType(BuiltList, const [const FullType(BasicUser)])));
    }
    if (object.messages != null) {
      result
        ..add('messages')
        ..add(serializers.serialize(object.messages,
            specifiedType:
                const FullType(BuiltList, const [const FullType(Message)])));
    }
    if (object.contextName != null) {
      result
        ..add('context_name')
        ..add(serializers.serialize(object.contextName,
            specifiedType: const FullType(String)));
    }
    if (object.contextCode != null) {
      result
        ..add('context_code')
        ..add(serializers.serialize(object.contextCode,
            specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  Conversation deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new ConversationBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'subject':
          result.subject = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'workflow_state':
          result.workflowState = serializers.deserialize(value,
                  specifiedType: const FullType(ConversationWorkflowState))
              as ConversationWorkflowState;
          break;
        case 'last_message':
          result.lastMessage = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'last_authored_message':
          result.lastAuthoredMessage = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'last_message_at':
          result.lastMessageAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'last_authored_message_at':
          result.lastAuthoredMessageAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'message_count':
          result.messageCount = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'subscribed':
          result.isSubscribed = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'starred':
          result.isStarred = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'avatar_url':
          result.avatarUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'visible':
          result.isVisible = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'audience':
          result.audience.replace(serializers.deserialize(value,
                  specifiedType:
                      const FullType(BuiltList, const [const FullType(int)]))
              as BuiltList<dynamic>);
          break;
        case 'participants':
          result.participants.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(BasicUser)]))
              as BuiltList<dynamic>);
          break;
        case 'messages':
          result.messages.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Message)]))
              as BuiltList<dynamic>);
          break;
        case 'context_name':
          result.contextName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'context_code':
          result.contextCode = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
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
  final int id;
  @override
  final String subject;
  @override
  final ConversationWorkflowState workflowState;
  @override
  final String lastMessage;
  @override
  final String lastAuthoredMessage;
  @override
  final DateTime lastMessageAt;
  @override
  final DateTime lastAuthoredMessageAt;
  @override
  final int messageCount;
  @override
  final bool isSubscribed;
  @override
  final bool isStarred;
  @override
  final String avatarUrl;
  @override
  final bool isVisible;
  @override
  final BuiltList<int> audience;
  @override
  final BuiltList<BasicUser> participants;
  @override
  final BuiltList<Message> messages;
  @override
  final String contextName;
  @override
  final String contextCode;

  factory _$Conversation([void Function(ConversationBuilder) updates]) =>
      (new ConversationBuilder()..update(updates)).build();

  _$Conversation._(
      {this.id,
      this.subject,
      this.workflowState,
      this.lastMessage,
      this.lastAuthoredMessage,
      this.lastMessageAt,
      this.lastAuthoredMessageAt,
      this.messageCount,
      this.isSubscribed,
      this.isStarred,
      this.avatarUrl,
      this.isVisible,
      this.audience,
      this.participants,
      this.messages,
      this.contextName,
      this.contextCode})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Conversation', 'id');
    }
    if (subject == null) {
      throw new BuiltValueNullFieldError('Conversation', 'subject');
    }
    if (workflowState == null) {
      throw new BuiltValueNullFieldError('Conversation', 'workflowState');
    }
    if (messageCount == null) {
      throw new BuiltValueNullFieldError('Conversation', 'messageCount');
    }
    if (isSubscribed == null) {
      throw new BuiltValueNullFieldError('Conversation', 'isSubscribed');
    }
    if (isStarred == null) {
      throw new BuiltValueNullFieldError('Conversation', 'isStarred');
    }
    if (isVisible == null) {
      throw new BuiltValueNullFieldError('Conversation', 'isVisible');
    }
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
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc(
                                $jc(
                                    $jc(
                                        $jc(
                                            $jc(
                                                $jc(
                                                    $jc(
                                                        $jc(
                                                            $jc(
                                                                $jc(
                                                                    $jc(
                                                                        0,
                                                                        id
                                                                            .hashCode),
                                                                    subject
                                                                        .hashCode),
                                                                workflowState
                                                                    .hashCode),
                                                            lastMessage
                                                                .hashCode),
                                                        lastAuthoredMessage
                                                            .hashCode),
                                                    lastMessageAt.hashCode),
                                                lastAuthoredMessageAt.hashCode),
                                            messageCount.hashCode),
                                        isSubscribed.hashCode),
                                    isStarred.hashCode),
                                avatarUrl.hashCode),
                            isVisible.hashCode),
                        audience.hashCode),
                    participants.hashCode),
                messages.hashCode),
            contextName.hashCode),
        contextCode.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Conversation')
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
  _$Conversation _$v;

  int _id;
  int get id => _$this._id;
  set id(int id) => _$this._id = id;

  String _subject;
  String get subject => _$this._subject;
  set subject(String subject) => _$this._subject = subject;

  ConversationWorkflowState _workflowState;
  ConversationWorkflowState get workflowState => _$this._workflowState;
  set workflowState(ConversationWorkflowState workflowState) =>
      _$this._workflowState = workflowState;

  String _lastMessage;
  String get lastMessage => _$this._lastMessage;
  set lastMessage(String lastMessage) => _$this._lastMessage = lastMessage;

  String _lastAuthoredMessage;
  String get lastAuthoredMessage => _$this._lastAuthoredMessage;
  set lastAuthoredMessage(String lastAuthoredMessage) =>
      _$this._lastAuthoredMessage = lastAuthoredMessage;

  DateTime _lastMessageAt;
  DateTime get lastMessageAt => _$this._lastMessageAt;
  set lastMessageAt(DateTime lastMessageAt) =>
      _$this._lastMessageAt = lastMessageAt;

  DateTime _lastAuthoredMessageAt;
  DateTime get lastAuthoredMessageAt => _$this._lastAuthoredMessageAt;
  set lastAuthoredMessageAt(DateTime lastAuthoredMessageAt) =>
      _$this._lastAuthoredMessageAt = lastAuthoredMessageAt;

  int _messageCount;
  int get messageCount => _$this._messageCount;
  set messageCount(int messageCount) => _$this._messageCount = messageCount;

  bool _isSubscribed;
  bool get isSubscribed => _$this._isSubscribed;
  set isSubscribed(bool isSubscribed) => _$this._isSubscribed = isSubscribed;

  bool _isStarred;
  bool get isStarred => _$this._isStarred;
  set isStarred(bool isStarred) => _$this._isStarred = isStarred;

  String _avatarUrl;
  String get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String avatarUrl) => _$this._avatarUrl = avatarUrl;

  bool _isVisible;
  bool get isVisible => _$this._isVisible;
  set isVisible(bool isVisible) => _$this._isVisible = isVisible;

  ListBuilder<int> _audience;
  ListBuilder<int> get audience => _$this._audience ??= new ListBuilder<int>();
  set audience(ListBuilder<int> audience) => _$this._audience = audience;

  ListBuilder<BasicUser> _participants;
  ListBuilder<BasicUser> get participants =>
      _$this._participants ??= new ListBuilder<BasicUser>();
  set participants(ListBuilder<BasicUser> participants) =>
      _$this._participants = participants;

  ListBuilder<Message> _messages;
  ListBuilder<Message> get messages =>
      _$this._messages ??= new ListBuilder<Message>();
  set messages(ListBuilder<Message> messages) => _$this._messages = messages;

  String _contextName;
  String get contextName => _$this._contextName;
  set contextName(String contextName) => _$this._contextName = contextName;

  String _contextCode;
  String get contextCode => _$this._contextCode;
  set contextCode(String contextCode) => _$this._contextCode = contextCode;

  ConversationBuilder() {
    Conversation._initializeBuilder(this);
  }

  ConversationBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _subject = _$v.subject;
      _workflowState = _$v.workflowState;
      _lastMessage = _$v.lastMessage;
      _lastAuthoredMessage = _$v.lastAuthoredMessage;
      _lastMessageAt = _$v.lastMessageAt;
      _lastAuthoredMessageAt = _$v.lastAuthoredMessageAt;
      _messageCount = _$v.messageCount;
      _isSubscribed = _$v.isSubscribed;
      _isStarred = _$v.isStarred;
      _avatarUrl = _$v.avatarUrl;
      _isVisible = _$v.isVisible;
      _audience = _$v.audience?.toBuilder();
      _participants = _$v.participants?.toBuilder();
      _messages = _$v.messages?.toBuilder();
      _contextName = _$v.contextName;
      _contextCode = _$v.contextCode;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Conversation other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Conversation;
  }

  @override
  void update(void Function(ConversationBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Conversation build() {
    _$Conversation _$result;
    try {
      _$result = _$v ??
          new _$Conversation._(
              id: id,
              subject: subject,
              workflowState: workflowState,
              lastMessage: lastMessage,
              lastAuthoredMessage: lastAuthoredMessage,
              lastMessageAt: lastMessageAt,
              lastAuthoredMessageAt: lastAuthoredMessageAt,
              messageCount: messageCount,
              isSubscribed: isSubscribed,
              isStarred: isStarred,
              avatarUrl: avatarUrl,
              isVisible: isVisible,
              audience: _audience?.build(),
              participants: _participants?.build(),
              messages: _messages?.build(),
              contextName: contextName,
              contextCode: contextCode);
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'audience';
        _audience?.build();
        _$failedField = 'participants';
        _participants?.build();
        _$failedField = 'messages';
        _messages?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'Conversation', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
