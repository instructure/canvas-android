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
  Iterable<Object> serialize(Serializers serializers, Announcement object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
  Announcement deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AnnouncementBuilder();

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
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'message':
          result.message = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'posted_at':
          result.postedAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'html_url':
          result.htmlUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'attachments':
          result.attachments.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(RemoteFile)]))
              as BuiltList<Object>);
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

  factory _$Announcement([void Function(AnnouncementBuilder) updates]) =>
      (new AnnouncementBuilder()..update(updates)).build();

  _$Announcement._(
      {this.id,
      this.title,
      this.message,
      this.postedAt,
      this.htmlUrl,
      this.attachments})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Announcement', 'id');
    }
    if (title == null) {
      throw new BuiltValueNullFieldError('Announcement', 'title');
    }
    if (message == null) {
      throw new BuiltValueNullFieldError('Announcement', 'message');
    }
    if (postedAt == null) {
      throw new BuiltValueNullFieldError('Announcement', 'postedAt');
    }
    if (htmlUrl == null) {
      throw new BuiltValueNullFieldError('Announcement', 'htmlUrl');
    }
    if (attachments == null) {
      throw new BuiltValueNullFieldError('Announcement', 'attachments');
    }
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
    return $jf($jc(
        $jc(
            $jc($jc($jc($jc(0, id.hashCode), title.hashCode), message.hashCode),
                postedAt.hashCode),
            htmlUrl.hashCode),
        attachments.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Announcement')
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
  _$Announcement _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _title;
  String get title => _$this._title;
  set title(String title) => _$this._title = title;

  String _message;
  String get message => _$this._message;
  set message(String message) => _$this._message = message;

  DateTime _postedAt;
  DateTime get postedAt => _$this._postedAt;
  set postedAt(DateTime postedAt) => _$this._postedAt = postedAt;

  String _htmlUrl;
  String get htmlUrl => _$this._htmlUrl;
  set htmlUrl(String htmlUrl) => _$this._htmlUrl = htmlUrl;

  ListBuilder<RemoteFile> _attachments;
  ListBuilder<RemoteFile> get attachments =>
      _$this._attachments ??= new ListBuilder<RemoteFile>();
  set attachments(ListBuilder<RemoteFile> attachments) =>
      _$this._attachments = attachments;

  AnnouncementBuilder();

  AnnouncementBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _title = _$v.title;
      _message = _$v.message;
      _postedAt = _$v.postedAt;
      _htmlUrl = _$v.htmlUrl;
      _attachments = _$v.attachments?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Announcement other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Announcement;
  }

  @override
  void update(void Function(AnnouncementBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Announcement build() {
    _$Announcement _$result;
    try {
      _$result = _$v ??
          new _$Announcement._(
              id: id,
              title: title,
              message: message,
              postedAt: postedAt,
              htmlUrl: htmlUrl,
              attachments: attachments.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'attachments';
        attachments.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'Announcement', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
