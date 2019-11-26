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
  Iterable<Object> serialize(Serializers serializers, Attachment object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(int)),
      'size',
      serializers.serialize(object.size, specifiedType: const FullType(int)),
    ];
    result.add('content-type');
    if (object.contentType == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.contentType,
          specifiedType: const FullType(String)));
    }
    result.add('filename');
    if (object.filename == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.filename,
          specifiedType: const FullType(String)));
    }
    result.add('display_name');
    if (object.displayName == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.displayName,
          specifiedType: const FullType(String)));
    }
    result.add('url');
    if (object.url == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.url,
          specifiedType: const FullType(String)));
    }
    result.add('thumbnail_url');
    if (object.thumbnailUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.thumbnailUrl,
          specifiedType: const FullType(String)));
    }
    result.add('preview_url');
    if (object.previewUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.previewUrl,
          specifiedType: const FullType(String)));
    }
    result.add('created_at');
    if (object.createdAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.createdAt,
          specifiedType: const FullType(DateTime)));
    }
    return result;
  }

  @override
  Attachment deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AttachmentBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'content-type':
          result.contentType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'filename':
          result.filename = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'display_name':
          result.displayName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'thumbnail_url':
          result.thumbnailUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'preview_url':
          result.previewUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'created_at':
          result.createdAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'size':
          result.size = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
      }
    }

    return result.build();
  }
}

class _$Attachment extends Attachment {
  @override
  final int id;
  @override
  final String contentType;
  @override
  final String filename;
  @override
  final String displayName;
  @override
  final String url;
  @override
  final String thumbnailUrl;
  @override
  final String previewUrl;
  @override
  final DateTime createdAt;
  @override
  final int size;

  factory _$Attachment([void Function(AttachmentBuilder) updates]) =>
      (new AttachmentBuilder()..update(updates)).build();

  _$Attachment._(
      {this.id,
      this.contentType,
      this.filename,
      this.displayName,
      this.url,
      this.thumbnailUrl,
      this.previewUrl,
      this.createdAt,
      this.size})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Attachment', 'id');
    }
    if (size == null) {
      throw new BuiltValueNullFieldError('Attachment', 'size');
    }
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
        id == other.id &&
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
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc($jc($jc(0, id.hashCode), contentType.hashCode),
                                filename.hashCode),
                            displayName.hashCode),
                        url.hashCode),
                    thumbnailUrl.hashCode),
                previewUrl.hashCode),
            createdAt.hashCode),
        size.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Attachment')
          ..add('id', id)
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
  _$Attachment _$v;

  int _id;
  int get id => _$this._id;
  set id(int id) => _$this._id = id;

  String _contentType;
  String get contentType => _$this._contentType;
  set contentType(String contentType) => _$this._contentType = contentType;

  String _filename;
  String get filename => _$this._filename;
  set filename(String filename) => _$this._filename = filename;

  String _displayName;
  String get displayName => _$this._displayName;
  set displayName(String displayName) => _$this._displayName = displayName;

  String _url;
  String get url => _$this._url;
  set url(String url) => _$this._url = url;

  String _thumbnailUrl;
  String get thumbnailUrl => _$this._thumbnailUrl;
  set thumbnailUrl(String thumbnailUrl) => _$this._thumbnailUrl = thumbnailUrl;

  String _previewUrl;
  String get previewUrl => _$this._previewUrl;
  set previewUrl(String previewUrl) => _$this._previewUrl = previewUrl;

  DateTime _createdAt;
  DateTime get createdAt => _$this._createdAt;
  set createdAt(DateTime createdAt) => _$this._createdAt = createdAt;

  int _size;
  int get size => _$this._size;
  set size(int size) => _$this._size = size;

  AttachmentBuilder() {
    Attachment._initializeBuilder(this);
  }

  AttachmentBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _contentType = _$v.contentType;
      _filename = _$v.filename;
      _displayName = _$v.displayName;
      _url = _$v.url;
      _thumbnailUrl = _$v.thumbnailUrl;
      _previewUrl = _$v.previewUrl;
      _createdAt = _$v.createdAt;
      _size = _$v.size;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Attachment other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Attachment;
  }

  @override
  void update(void Function(AttachmentBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Attachment build() {
    final _$result = _$v ??
        new _$Attachment._(
            id: id,
            contentType: contentType,
            filename: filename,
            displayName: displayName,
            url: url,
            thumbnailUrl: thumbnailUrl,
            previewUrl: previewUrl,
            createdAt: createdAt,
            size: size);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
