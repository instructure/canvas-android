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
  Iterable<Object?> serialize(Serializers serializers, Message object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'author_id',
      serializers.serialize(object.authorId,
          specifiedType: const FullType(String)),
      'generated',
      serializers.serialize(object.isGenerated,
          specifiedType: const FullType(bool)),
    ];
    Object? value;
    value = object.createdAt;

    result
      ..add('created_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.body;

    result
      ..add('body')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.attachments;

    result
      ..add('attachments')
      ..add(serializers.serialize(value,
          specifiedType:
              const FullType(BuiltList, const [const FullType(Attachment)])));
    value = object.mediaComment;

    result
      ..add('media_comment')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(MediaComment)));
    value = object.forwardedMessages;

    result
      ..add('forwarded_messages')
      ..add(serializers.serialize(value,
          specifiedType:
              const FullType(BuiltList, const [const FullType(Message)])));
    value = object.participatingUserIds;

    result
      ..add('participating_user_ids')
      ..add(serializers.serialize(value,
          specifiedType:
              const FullType(BuiltList, const [const FullType(String)])));

    return result;
  }

  @override
  Message deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new MessageBuilder();

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
        case 'created_at':
          result.createdAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'body':
          result.body = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'author_id':
          result.authorId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'generated':
          result.isGenerated = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'attachments':
          result.attachments.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Attachment)]))!
              as BuiltList<Object?>);
          break;
        case 'media_comment':
          result.mediaComment.replace(serializers.deserialize(value,
              specifiedType: const FullType(MediaComment))! as MediaComment);
          break;
        case 'forwarded_messages':
          result.forwardedMessages.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Message)]))!
              as BuiltList<Object?>);
          break;
        case 'participating_user_ids':
          result.participatingUserIds.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(String)]))!
              as BuiltList<Object?>);
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
  final DateTime? createdAt;
  @override
  final String? body;
  @override
  final String authorId;
  @override
  final bool isGenerated;
  @override
  final BuiltList<Attachment>? attachments;
  @override
  final MediaComment? mediaComment;
  @override
  final BuiltList<Message>? forwardedMessages;
  @override
  final BuiltList<String>? participatingUserIds;

  factory _$Message([void Function(MessageBuilder)? updates]) =>
      (new MessageBuilder()..update(updates))._build();

  _$Message._(
      {required this.id,
      this.createdAt,
      this.body,
      required this.authorId,
      required this.isGenerated,
      this.attachments,
      this.mediaComment,
      this.forwardedMessages,
      this.participatingUserIds})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Message', 'id');
    BuiltValueNullFieldError.checkNotNull(authorId, r'Message', 'authorId');
    BuiltValueNullFieldError.checkNotNull(
        isGenerated, r'Message', 'isGenerated');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, createdAt.hashCode);
    _$hash = $jc(_$hash, body.hashCode);
    _$hash = $jc(_$hash, authorId.hashCode);
    _$hash = $jc(_$hash, isGenerated.hashCode);
    _$hash = $jc(_$hash, attachments.hashCode);
    _$hash = $jc(_$hash, mediaComment.hashCode);
    _$hash = $jc(_$hash, forwardedMessages.hashCode);
    _$hash = $jc(_$hash, participatingUserIds.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Message')
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
  _$Message? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  DateTime? _createdAt;
  DateTime? get createdAt => _$this._createdAt;
  set createdAt(DateTime? createdAt) => _$this._createdAt = createdAt;

  String? _body;
  String? get body => _$this._body;
  set body(String? body) => _$this._body = body;

  String? _authorId;
  String? get authorId => _$this._authorId;
  set authorId(String? authorId) => _$this._authorId = authorId;

  bool? _isGenerated;
  bool? get isGenerated => _$this._isGenerated;
  set isGenerated(bool? isGenerated) => _$this._isGenerated = isGenerated;

  ListBuilder<Attachment>? _attachments;
  ListBuilder<Attachment> get attachments =>
      _$this._attachments ??= new ListBuilder<Attachment>();
  set attachments(ListBuilder<Attachment>? attachments) =>
      _$this._attachments = attachments;

  MediaCommentBuilder? _mediaComment;
  MediaCommentBuilder get mediaComment =>
      _$this._mediaComment ??= new MediaCommentBuilder();
  set mediaComment(MediaCommentBuilder? mediaComment) =>
      _$this._mediaComment = mediaComment;

  ListBuilder<Message>? _forwardedMessages;
  ListBuilder<Message> get forwardedMessages =>
      _$this._forwardedMessages ??= new ListBuilder<Message>();
  set forwardedMessages(ListBuilder<Message>? forwardedMessages) =>
      _$this._forwardedMessages = forwardedMessages;

  ListBuilder<String>? _participatingUserIds;
  ListBuilder<String> get participatingUserIds =>
      _$this._participatingUserIds ??= new ListBuilder<String>();
  set participatingUserIds(ListBuilder<String>? participatingUserIds) =>
      _$this._participatingUserIds = participatingUserIds;

  MessageBuilder() {
    Message._initializeBuilder(this);
  }

  MessageBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _createdAt = $v.createdAt;
      _body = $v.body;
      _authorId = $v.authorId;
      _isGenerated = $v.isGenerated;
      _attachments = $v.attachments?.toBuilder();
      _mediaComment = $v.mediaComment?.toBuilder();
      _forwardedMessages = $v.forwardedMessages?.toBuilder();
      _participatingUserIds = $v.participatingUserIds?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Message other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Message;
  }

  @override
  void update(void Function(MessageBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Message build() => _build();

  _$Message _build() {
    _$Message _$result;
    try {
      _$result = _$v ??
          new _$Message._(
              id: BuiltValueNullFieldError.checkNotNull(id, r'Message', 'id'),
              createdAt: createdAt,
              body: body,
              authorId: BuiltValueNullFieldError.checkNotNull(
                  authorId, r'Message', 'authorId'),
              isGenerated: BuiltValueNullFieldError.checkNotNull(
                  isGenerated, r'Message', 'isGenerated'),
              attachments: _attachments?.build(),
              mediaComment: _mediaComment?.build(),
              forwardedMessages: _forwardedMessages?.build(),
              participatingUserIds: _participatingUserIds?.build());
    } catch (_) {
      late String _$failedField;
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
            r'Message', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
