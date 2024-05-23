// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'announcement.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Announcement> _$announcementSerializer =
    new _$AnnouncementSerializer();

class _$AnnouncementSerializer implements StructuredSerializer<Announcement> {
  @override
  final Iterable<Type> types = const [Announcement, _$Announcement];
  @override
  final String wireName = 'Announcement';

  @override
  Iterable<Object?> serialize(Serializers serializers, Announcement object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'title',
      serializers.serialize(object.title,
          specifiedType: const FullType(String)),
      'message',
      serializers.serialize(object.message,
          specifiedType: const FullType(String)),
      'posted_at',
      serializers.serialize(object.postedAt,
          specifiedType: const FullType(DateTime)),
      'html_url',
      serializers.serialize(object.htmlUrl,
          specifiedType: const FullType(String)),
      'attachments',
      serializers.serialize(object.attachments,
          specifiedType:
              const FullType(BuiltList, const [const FullType(RemoteFile)])),
    ];

    return result;
  }

  @override
  Announcement deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AnnouncementBuilder();

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
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'message':
          result.message = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'posted_at':
          result.postedAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime))! as DateTime;
          break;
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'attachments':
          result.attachments.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(RemoteFile)]))!
              as BuiltList<Object?>);
          break;
      }
    }

    return result.build();
  }
}

class _$Announcement extends Announcement {
  @override
  final String id;
  @override
  final String title;
  @override
  final String message;
  @override
  final DateTime postedAt;
  @override
  final String htmlUrl;
  @override
  final BuiltList<RemoteFile> attachments;

  factory _$Announcement([void Function(AnnouncementBuilder)? updates]) =>
      (new AnnouncementBuilder()..update(updates))._build();

  _$Announcement._(
      {required this.id,
      required this.title,
      required this.message,
      required this.postedAt,
      required this.htmlUrl,
      required this.attachments})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Announcement', 'id');
    BuiltValueNullFieldError.checkNotNull(title, r'Announcement', 'title');
    BuiltValueNullFieldError.checkNotNull(message, r'Announcement', 'message');
    BuiltValueNullFieldError.checkNotNull(
        postedAt, r'Announcement', 'postedAt');
    BuiltValueNullFieldError.checkNotNull(htmlUrl, r'Announcement', 'htmlUrl');
    BuiltValueNullFieldError.checkNotNull(
        attachments, r'Announcement', 'attachments');
  }

  @override
  Announcement rebuild(void Function(AnnouncementBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AnnouncementBuilder toBuilder() => new AnnouncementBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Announcement &&
        id == other.id &&
        title == other.title &&
        message == other.message &&
        postedAt == other.postedAt &&
        htmlUrl == other.htmlUrl &&
        attachments == other.attachments;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, title.hashCode);
    _$hash = $jc(_$hash, message.hashCode);
    _$hash = $jc(_$hash, postedAt.hashCode);
    _$hash = $jc(_$hash, htmlUrl.hashCode);
    _$hash = $jc(_$hash, attachments.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Announcement')
          ..add('id', id)
          ..add('title', title)
          ..add('message', message)
          ..add('postedAt', postedAt)
          ..add('htmlUrl', htmlUrl)
          ..add('attachments', attachments))
        .toString();
  }
}

class AnnouncementBuilder
    implements Builder<Announcement, AnnouncementBuilder> {
  _$Announcement? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _title;
  String? get title => _$this._title;
  set title(String? title) => _$this._title = title;

  String? _message;
  String? get message => _$this._message;
  set message(String? message) => _$this._message = message;

  DateTime? _postedAt;
  DateTime? get postedAt => _$this._postedAt;
  set postedAt(DateTime? postedAt) => _$this._postedAt = postedAt;

  String? _htmlUrl;
  String? get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String? htmlUrl) => _$this._htmlUrl = htmlUrl;

  ListBuilder<RemoteFile>? _attachments;
  ListBuilder<RemoteFile> get attachments =>
      _$this._attachments ??= new ListBuilder<RemoteFile>();
  set attachments(ListBuilder<RemoteFile>? attachments) =>
      _$this._attachments = attachments;

  AnnouncementBuilder() {
    Announcement._initializeBuilder(this);
  }

  AnnouncementBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _title = $v.title;
      _message = $v.message;
      _postedAt = $v.postedAt;
      _htmlUrl = $v.htmlUrl;
      _attachments = $v.attachments.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Announcement other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Announcement;
  }

  @override
  void update(void Function(AnnouncementBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Announcement build() => _build();

  _$Announcement _build() {
    _$Announcement _$result;
    try {
      _$result = _$v ??
          new _$Announcement._(
              id: BuiltValueNullFieldError.checkNotNull(
                  id, r'Announcement', 'id'),
              title: BuiltValueNullFieldError.checkNotNull(
                  title, r'Announcement', 'title'),
              message: BuiltValueNullFieldError.checkNotNull(
                  message, r'Announcement', 'message'),
              postedAt: BuiltValueNullFieldError.checkNotNull(
                  postedAt, r'Announcement', 'postedAt'),
              htmlUrl: BuiltValueNullFieldError.checkNotNull(
                  htmlUrl, r'Announcement', 'htmlUrl'),
              attachments: attachments.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'attachments';
        attachments.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'Announcement', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
