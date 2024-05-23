// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attachment.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Attachment> _$attachmentSerializer = new _$AttachmentSerializer();

class _$AttachmentSerializer implements StructuredSerializer<Attachment> {
  @override
  final Iterable<Type> types = const [Attachment, _$Attachment];
  @override
  final String wireName = 'Attachment';

  @override
  Iterable<Object?> serialize(Serializers serializers, Attachment object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.jsonId,
          specifiedType: const FullType(JsonObject)),
      'size',
      serializers.serialize(object.size, specifiedType: const FullType(int)),
    ];
    Object? value;
    value = object.contentType;

    result
      ..add('content-type')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.filename;

    result
      ..add('filename')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.displayName;

    result
      ..add('display_name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.url;

    result
      ..add('url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.thumbnailUrl;

    result
      ..add('thumbnail_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.previewUrl;

    result
      ..add('preview_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.createdAt;

    result
      ..add('created_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));

    return result;
  }

  @override
  Attachment deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AttachmentBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'id':
          result.jsonId = serializers.deserialize(value,
              specifiedType: const FullType(JsonObject))! as JsonObject;
          break;
        case 'content-type':
          result.contentType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'filename':
          result.filename = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'display_name':
          result.displayName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'thumbnail_url':
          result.thumbnailUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'preview_url':
          result.previewUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'created_at':
          result.createdAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'size':
          result.size = serializers.deserialize(value,
              specifiedType: const FullType(int))! as int;
          break;
      }
    }

    return result.build();
  }
}

class _$Attachment extends Attachment {
  @override
  final JsonObject jsonId;
  @override
  final String? contentType;
  @override
  final String? filename;
  @override
  final String? displayName;
  @override
  final String? url;
  @override
  final String? thumbnailUrl;
  @override
  final String? previewUrl;
  @override
  final DateTime? createdAt;
  @override
  final int size;

  factory _$Attachment([void Function(AttachmentBuilder)? updates]) =>
      (new AttachmentBuilder()..update(updates))._build();

  _$Attachment._(
      {required this.jsonId,
      this.contentType,
      this.filename,
      this.displayName,
      this.url,
      this.thumbnailUrl,
      this.previewUrl,
      this.createdAt,
      required this.size})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(jsonId, r'Attachment', 'jsonId');
    BuiltValueNullFieldError.checkNotNull(size, r'Attachment', 'size');
  }

  @override
  Attachment rebuild(void Function(AttachmentBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AttachmentBuilder toBuilder() => new AttachmentBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Attachment &&
        jsonId == other.jsonId &&
        contentType == other.contentType &&
        filename == other.filename &&
        displayName == other.displayName &&
        url == other.url &&
        thumbnailUrl == other.thumbnailUrl &&
        previewUrl == other.previewUrl &&
        createdAt == other.createdAt &&
        size == other.size;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, jsonId.hashCode);
    _$hash = $jc(_$hash, contentType.hashCode);
    _$hash = $jc(_$hash, filename.hashCode);
    _$hash = $jc(_$hash, displayName.hashCode);
    _$hash = $jc(_$hash, url.hashCode);
    _$hash = $jc(_$hash, thumbnailUrl.hashCode);
    _$hash = $jc(_$hash, previewUrl.hashCode);
    _$hash = $jc(_$hash, createdAt.hashCode);
    _$hash = $jc(_$hash, size.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Attachment')
          ..add('jsonId', jsonId)
          ..add('contentType', contentType)
          ..add('filename', filename)
          ..add('displayName', displayName)
          ..add('url', url)
          ..add('thumbnailUrl', thumbnailUrl)
          ..add('previewUrl', previewUrl)
          ..add('createdAt', createdAt)
          ..add('size', size))
        .toString();
  }
}

class AttachmentBuilder implements Builder<Attachment, AttachmentBuilder> {
  _$Attachment? _$v;

  JsonObject? _jsonId;
  JsonObject? get jsonId => _$this._jsonId;
  set jsonId(JsonObject? jsonId) => _$this._jsonId = jsonId;

  String? _contentType;
  String? get contentType => _$this._contentType;
  set contentType(String? contentType) => _$this._contentType = contentType;

  String? _filename;
  String? get filename => _$this._filename;
  set filename(String? filename) => _$this._filename = filename;

  String? _displayName;
  String? get displayName => _$this._displayName;
  set displayName(String? displayName) => _$this._displayName = displayName;

  String? _url;
  String? get url => _$this._url;
  set url(String? url) => _$this._url = url;

  String? _thumbnailUrl;
  String? get thumbnailUrl => _$this._thumbnailUrl;
  set thumbnailUrl(String? thumbnailUrl) => _$this._thumbnailUrl = thumbnailUrl;

  String? _previewUrl;
  String? get previewUrl => _$this._previewUrl;
  set previewUrl(String? previewUrl) => _$this._previewUrl = previewUrl;

  DateTime? _createdAt;
  DateTime? get createdAt => _$this._createdAt;
  set createdAt(DateTime? createdAt) => _$this._createdAt = createdAt;

  int? _size;
  int? get size => _$this._size;
  set size(int? size) => _$this._size = size;

  AttachmentBuilder() {
    Attachment._initializeBuilder(this);
  }

  AttachmentBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _jsonId = $v.jsonId;
      _contentType = $v.contentType;
      _filename = $v.filename;
      _displayName = $v.displayName;
      _url = $v.url;
      _thumbnailUrl = $v.thumbnailUrl;
      _previewUrl = $v.previewUrl;
      _createdAt = $v.createdAt;
      _size = $v.size;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Attachment other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Attachment;
  }

  @override
  void update(void Function(AttachmentBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Attachment build() => _build();

  _$Attachment _build() {
    final _$result = _$v ??
        new _$Attachment._(
            jsonId: BuiltValueNullFieldError.checkNotNull(
                jsonId, r'Attachment', 'jsonId'),
            contentType: contentType,
            filename: filename,
            displayName: displayName,
            url: url,
            thumbnailUrl: thumbnailUrl,
            previewUrl: previewUrl,
            createdAt: createdAt,
            size: BuiltValueNullFieldError.checkNotNull(
                size, r'Attachment', 'size'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
