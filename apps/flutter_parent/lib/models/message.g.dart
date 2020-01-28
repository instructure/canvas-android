// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'message.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Message> _$messageSerializer = new _$MessageSerializer();

class _$MessageSerializer implements StructuredSerializer<Message> {
  @override
  final Iterable<Type> types = const [Message, _$Message];
  @override
  final String wireName = 'Message';

  @override
  Iterable<Object> serialize(Serializers serializers, Message object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'author_id',
      serializers.serialize(object.authorId,
          specifiedType: const FullType(String)),
      'generated',
      serializers.serialize(object.isGenerated,
          specifiedType: const FullType(bool)),
    ];
    result.add('created_at');
    if (object.createdAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.createdAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('body');
    if (object.body == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.body,
          specifiedType: const FullType(String)));
    }
    result.add('attachments');
    if (object.attachments == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.attachments,
          specifiedType:
              const FullType(BuiltList, const [const FullType(Attachment)])));
    }
    result.add('media_comment');
    if (object.mediaComment == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.mediaComment,
          specifiedType: const FullType(MediaComment)));
    }
    result.add('forwarded_messages');
    if (object.forwardedMessages == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.forwardedMessages,
          specifiedType:
              const FullType(BuiltList, const [const FullType(Message)])));
    }
    result.add('participating_user_ids');
    if (object.participatingUserIds == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.participatingUserIds,
          specifiedType:
              const FullType(BuiltList, const [const FullType(String)])));
    }
    return result;
  }

  @override
  Message deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new MessageBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'created_at':
          result.createdAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'body':
          result.body = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'author_id':
          result.authorId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'generated':
          result.isGenerated = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'attachments':
          result.attachments.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Attachment)]))
              as BuiltList<Object>);
          break;
        case 'media_comment':
          result.mediaComment.replace(serializers.deserialize(value,
              specifiedType: const FullType(MediaComment)) as MediaComment);
          break;
        case 'forwarded_messages':
          result.forwardedMessages.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Message)]))
              as BuiltList<Object>);
          break;
        case 'participating_user_ids':
          result.participatingUserIds.replace(serializers.deserialize(value,
                  specifiedType:
                      const FullType(BuiltList, const [const FullType(String)]))
              as BuiltList<Object>);
          break;
      }
    }

    return result.build();
  }
}

class _$Message extends Message {
  @override
  final String id;
  @override
  final DateTime createdAt;
  @override
  final String body;
  @override
  final String authorId;
  @override
  final bool isGenerated;
  @override
  final BuiltList<Attachment> attachments;
  @override
  final MediaComment mediaComment;
  @override
  final BuiltList<Message> forwardedMessages;
  @override
  final BuiltList<String> participatingUserIds;

  factory _$Message([void Function(MessageBuilder) updates]) =>
      (new MessageBuilder()..update(updates)).build();

  _$Message._(
      {this.id,
      this.createdAt,
      this.body,
      this.authorId,
      this.isGenerated,
      this.attachments,
      this.mediaComment,
      this.forwardedMessages,
      this.participatingUserIds})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Message', 'id');
    }
    if (authorId == null) {
      throw new BuiltValueNullFieldError('Message', 'authorId');
    }
    if (isGenerated == null) {
      throw new BuiltValueNullFieldError('Message', 'isGenerated');
    }
  }

  @override
  Message rebuild(void Function(MessageBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  MessageBuilder toBuilder() => new MessageBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Message &&
        id == other.id &&
        createdAt == other.createdAt &&
        body == other.body &&
        authorId == other.authorId &&
        isGenerated == other.isGenerated &&
        attachments == other.attachments &&
        mediaComment == other.mediaComment &&
        forwardedMessages == other.forwardedMessages &&
        participatingUserIds == other.participatingUserIds;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc($jc($jc(0, id.hashCode), createdAt.hashCode),
                                body.hashCode),
                            authorId.hashCode),
                        isGenerated.hashCode),
                    attachments.hashCode),
                mediaComment.hashCode),
            forwardedMessages.hashCode),
        participatingUserIds.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Message')
          ..add('id', id)
          ..add('createdAt', createdAt)
          ..add('body', body)
          ..add('authorId', authorId)
          ..add('isGenerated', isGenerated)
          ..add('attachments', attachments)
          ..add('mediaComment', mediaComment)
          ..add('forwardedMessages', forwardedMessages)
          ..add('participatingUserIds', participatingUserIds))
        .toString();
  }
}

class MessageBuilder implements Builder<Message, MessageBuilder> {
  _$Message _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  DateTime _createdAt;
  DateTime get createdAt => _$this._createdAt;
  set createdAt(DateTime createdAt) => _$this._createdAt = createdAt;

  String _body;
  String get body => _$this._body;
  set body(String body) => _$this._body = body;

  String _authorId;
  String get authorId => _$this._authorId;
  set authorId(String authorId) => _$this._authorId = authorId;

  bool _isGenerated;
  bool get isGenerated => _$this._isGenerated;
  set isGenerated(bool isGenerated) => _$this._isGenerated = isGenerated;

  ListBuilder<Attachment> _attachments;
  ListBuilder<Attachment> get attachments =>
      _$this._attachments ??= new ListBuilder<Attachment>();
  set attachments(ListBuilder<Attachment> attachments) =>
      _$this._attachments = attachments;

  MediaCommentBuilder _mediaComment;
  MediaCommentBuilder get mediaComment =>
      _$this._mediaComment ??= new MediaCommentBuilder();
  set mediaComment(MediaCommentBuilder mediaComment) =>
      _$this._mediaComment = mediaComment;

  ListBuilder<Message> _forwardedMessages;
  ListBuilder<Message> get forwardedMessages =>
      _$this._forwardedMessages ??= new ListBuilder<Message>();
  set forwardedMessages(ListBuilder<Message> forwardedMessages) =>
      _$this._forwardedMessages = forwardedMessages;

  ListBuilder<String> _participatingUserIds;
  ListBuilder<String> get participatingUserIds =>
      _$this._participatingUserIds ??= new ListBuilder<String>();
  set participatingUserIds(ListBuilder<String> participatingUserIds) =>
      _$this._participatingUserIds = participatingUserIds;

  MessageBuilder() {
    Message._initializeBuilder(this);
  }

  MessageBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _createdAt = _$v.createdAt;
      _body = _$v.body;
      _authorId = _$v.authorId;
      _isGenerated = _$v.isGenerated;
      _attachments = _$v.attachments?.toBuilder();
      _mediaComment = _$v.mediaComment?.toBuilder();
      _forwardedMessages = _$v.forwardedMessages?.toBuilder();
      _participatingUserIds = _$v.participatingUserIds?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Message other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Message;
  }

  @override
  void update(void Function(MessageBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Message build() {
    _$Message _$result;
    try {
      _$result = _$v ??
          new _$Message._(
              id: id,
              createdAt: createdAt,
              body: body,
              authorId: authorId,
              isGenerated: isGenerated,
              attachments: _attachments?.build(),
              mediaComment: _mediaComment?.build(),
              forwardedMessages: _forwardedMessages?.build(),
              participatingUserIds: _participatingUserIds?.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'attachments';
        _attachments?.build();
        _$failedField = 'mediaComment';
        _mediaComment?.build();
        _$failedField = 'forwardedMessages';
        _forwardedMessages?.build();
        _$failedField = 'participatingUserIds';
        _participatingUserIds?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'Message', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
