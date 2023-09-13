// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'remote_file.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<RemoteFile> _$remoteFileSerializer = new _$RemoteFileSerializer();

class _$RemoteFileSerializer implements StructuredSerializer<RemoteFile> {
  @override
  final Iterable<Type> types = const [RemoteFile, _$RemoteFile];
  @override
  final String wireName = 'RemoteFile';

  @override
  Iterable<Object?> serialize(Serializers serializers, RemoteFile object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'url',
      serializers.serialize(object.url, specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.filename;

    result
      ..add('filename')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.previewUrl;

    result
      ..add('preview_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.thumbnailUrl;

    result
      ..add('thumbnail_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.contentType;

    result
      ..add('content-type')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.displayName;

    result
      ..add('display_name')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  RemoteFile deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new RemoteFileBuilder();

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
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'filename':
          result.filename = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'preview_url':
          result.previewUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'thumbnail_url':
          result.thumbnailUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'content-type':
          result.contentType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'display_name':
          result.displayName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$RemoteFile extends RemoteFile {
  @override
  final String id;
  @override
  final String url;
  @override
  final String? filename;
  @override
  final String? previewUrl;
  @override
  final String? thumbnailUrl;
  @override
  final String? contentType;
  @override
  final String? displayName;

  factory _$RemoteFile([void Function(RemoteFileBuilder)? updates]) =>
      (new RemoteFileBuilder()..update(updates))._build();

  _$RemoteFile._(
      {required this.id,
      required this.url,
      this.filename,
      this.previewUrl,
      this.thumbnailUrl,
      this.contentType,
      this.displayName})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'RemoteFile', 'id');
    BuiltValueNullFieldError.checkNotNull(url, r'RemoteFile', 'url');
  }

  @override
  RemoteFile rebuild(void Function(RemoteFileBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  RemoteFileBuilder toBuilder() => new RemoteFileBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is RemoteFile &&
        id == other.id &&
        url == other.url &&
        filename == other.filename &&
        previewUrl == other.previewUrl &&
        thumbnailUrl == other.thumbnailUrl &&
        contentType == other.contentType &&
        displayName == other.displayName;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, url.hashCode);
    _$hash = $jc(_$hash, filename.hashCode);
    _$hash = $jc(_$hash, previewUrl.hashCode);
    _$hash = $jc(_$hash, thumbnailUrl.hashCode);
    _$hash = $jc(_$hash, contentType.hashCode);
    _$hash = $jc(_$hash, displayName.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'RemoteFile')
          ..add('id', id)
          ..add('url', url)
          ..add('filename', filename)
          ..add('previewUrl', previewUrl)
          ..add('thumbnailUrl', thumbnailUrl)
          ..add('contentType', contentType)
          ..add('displayName', displayName))
        .toString();
  }
}

class RemoteFileBuilder implements Builder<RemoteFile, RemoteFileBuilder> {
  _$RemoteFile? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _url;
  String? get url => _$this._url;
  set url(String? url) => _$this._url = url;

  String? _filename;
  String? get filename => _$this._filename;
  set filename(String? filename) => _$this._filename = filename;

  String? _previewUrl;
  String? get previewUrl => _$this._previewUrl;
  set previewUrl(String? previewUrl) => _$this._previewUrl = previewUrl;

  String? _thumbnailUrl;
  String? get thumbnailUrl => _$this._thumbnailUrl;
  set thumbnailUrl(String? thumbnailUrl) => _$this._thumbnailUrl = thumbnailUrl;

  String? _contentType;
  String? get contentType => _$this._contentType;
  set contentType(String? contentType) => _$this._contentType = contentType;

  String? _displayName;
  String? get displayName => _$this._displayName;
  set displayName(String? displayName) => _$this._displayName = displayName;

  RemoteFileBuilder() {
    RemoteFile._initializeBuilder(this);
  }

  RemoteFileBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _url = $v.url;
      _filename = $v.filename;
      _previewUrl = $v.previewUrl;
      _thumbnailUrl = $v.thumbnailUrl;
      _contentType = $v.contentType;
      _displayName = $v.displayName;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(RemoteFile other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$RemoteFile;
  }

  @override
  void update(void Function(RemoteFileBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  RemoteFile build() => _build();

  _$RemoteFile _build() {
    final _$result = _$v ??
        new _$RemoteFile._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'RemoteFile', 'id'),
            url: BuiltValueNullFieldError.checkNotNull(
                url, r'RemoteFile', 'url'),
            filename: filename,
            previewUrl: previewUrl,
            thumbnailUrl: thumbnailUrl,
            contentType: contentType,
            displayName: displayName);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
