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
  Iterable<Object> serialize(Serializers serializers, RemoteFile object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'url',
      serializers.serialize(object.url, specifiedType: const FullType(String)),
      'filename',
      serializers.serialize(object.filename,
          specifiedType: const FullType(String)),
      'preview_url',
      serializers.serialize(object.previewUrl,
          specifiedType: const FullType(String)),
      'thumbnail_url',
      serializers.serialize(object.thumbnailUrl,
          specifiedType: const FullType(String)),
      'content-type',
      serializers.serialize(object.contentType,
          specifiedType: const FullType(String)),
      'display_name',
      serializers.serialize(object.displayName,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  RemoteFile deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new RemoteFileBuilder();

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
        case 'url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'filename':
          result.filename = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'preview_url':
          result.previewUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'thumbnail_url':
          result.thumbnailUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'content-type':
          result.contentType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'display_name':
          result.displayName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
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
  final String filename;
  @override
  final String previewUrl;
  @override
  final String thumbnailUrl;
  @override
  final String contentType;
  @override
  final String displayName;

  factory _$RemoteFile([void Function(RemoteFileBuilder) updates]) =>
      (new RemoteFileBuilder()..update(updates)).build();

  _$RemoteFile._(
      {this.id,
      this.url,
      this.filename,
      this.previewUrl,
      this.thumbnailUrl,
      this.contentType,
      this.displayName})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('RemoteFile', 'id');
    }
    if (url == null) {
      throw new BuiltValueNullFieldError('RemoteFile', 'url');
    }
    if (filename == null) {
      throw new BuiltValueNullFieldError('RemoteFile', 'filename');
    }
    if (previewUrl == null) {
      throw new BuiltValueNullFieldError('RemoteFile', 'previewUrl');
    }
    if (thumbnailUrl == null) {
      throw new BuiltValueNullFieldError('RemoteFile', 'thumbnailUrl');
    }
    if (contentType == null) {
      throw new BuiltValueNullFieldError('RemoteFile', 'contentType');
    }
    if (displayName == null) {
      throw new BuiltValueNullFieldError('RemoteFile', 'displayName');
    }
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
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc($jc($jc(0, id.hashCode), url.hashCode),
                        filename.hashCode),
                    previewUrl.hashCode),
                thumbnailUrl.hashCode),
            contentType.hashCode),
        displayName.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('RemoteFile')
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
  _$RemoteFile _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _url;
  String get url => _$this._url;
  set url(String url) => _$this._url = url;

  String _filename;
  String get filename => _$this._filename;
  set filename(String filename) => _$this._filename = filename;

  String _previewUrl;
  String get previewUrl => _$this._previewUrl;
  set previewUrl(String previewUrl) => _$this._previewUrl = previewUrl;

  String _thumbnailUrl;
  String get thumbnailUrl => _$this._thumbnailUrl;
  set thumbnailUrl(String thumbnailUrl) => _$this._thumbnailUrl = thumbnailUrl;

  String _contentType;
  String get contentType => _$this._contentType;
  set contentType(String contentType) => _$this._contentType = contentType;

  String _displayName;
  String get displayName => _$this._displayName;
  set displayName(String displayName) => _$this._displayName = displayName;

  RemoteFileBuilder() {
    RemoteFile._initializeBuilder(this);
  }

  RemoteFileBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _url = _$v.url;
      _filename = _$v.filename;
      _previewUrl = _$v.previewUrl;
      _thumbnailUrl = _$v.thumbnailUrl;
      _contentType = _$v.contentType;
      _displayName = _$v.displayName;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(RemoteFile other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$RemoteFile;
  }

  @override
  void update(void Function(RemoteFileBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$RemoteFile build() {
    final _$result = _$v ??
        new _$RemoteFile._(
            id: id,
            url: url,
            filename: filename,
            previewUrl: previewUrl,
            thumbnailUrl: thumbnailUrl,
            contentType: contentType,
            displayName: displayName);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
