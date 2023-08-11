// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'file_upload_config.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<FileUploadConfig> _$fileUploadConfigSerializer =
    new _$FileUploadConfigSerializer();

class _$FileUploadConfigSerializer
    implements StructuredSerializer<FileUploadConfig> {
  @override
  final Iterable<Type> types = const [FileUploadConfig, _$FileUploadConfig];
  @override
  final String wireName = 'FileUploadConfig';

  @override
  Iterable<Object?> serialize(Serializers serializers, FileUploadConfig object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[];
    Object? value;
    value = object.url;

    result
      ..add('upload_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.params;

    result
      ..add('upload_params')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(BuiltMap,
              const [const FullType(String), const FullType(String)])));

    return result;
  }

  @override
  FileUploadConfig deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new FileUploadConfigBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'upload_url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'upload_params':
          result.params.replace(serializers.deserialize(value,
              specifiedType: const FullType(BuiltMap,
                  const [const FullType(String), const FullType(String)]))!);
          break;
      }
    }

    return result.build();
  }
}

class _$FileUploadConfig extends FileUploadConfig {
  @override
  final String? url;
  @override
  final BuiltMap<String, String>? params;

  factory _$FileUploadConfig(
          [void Function(FileUploadConfigBuilder)? updates]) =>
      (new FileUploadConfigBuilder()..update(updates))._build();

  _$FileUploadConfig._({this.url, this.params}) : super._();

  @override
  FileUploadConfig rebuild(void Function(FileUploadConfigBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  FileUploadConfigBuilder toBuilder() =>
      new FileUploadConfigBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is FileUploadConfig &&
        url == other.url &&
        params == other.params;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, url.hashCode);
    _$hash = $jc(_$hash, params.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'FileUploadConfig')
          ..add('url', url)
          ..add('params', params))
        .toString();
  }
}

class FileUploadConfigBuilder
    implements Builder<FileUploadConfig, FileUploadConfigBuilder> {
  _$FileUploadConfig? _$v;

  String? _url;
  String? get url => _$this._url;
  set url(String? url) => _$this._url = url;

  MapBuilder<String, String>? _params;
  MapBuilder<String, String> get params =>
      _$this._params ??= new MapBuilder<String, String>();
  set params(MapBuilder<String, String>? params) => _$this._params = params;

  FileUploadConfigBuilder();

  FileUploadConfigBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _url = $v.url;
      _params = $v.params?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(FileUploadConfig other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$FileUploadConfig;
  }

  @override
  void update(void Function(FileUploadConfigBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  FileUploadConfig build() => _build();

  _$FileUploadConfig _build() {
    _$FileUploadConfig _$result;
    try {
      _$result =
          _$v ?? new _$FileUploadConfig._(url: url, params: _params?.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'params';
        _params?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'FileUploadConfig', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
