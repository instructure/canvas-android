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
  Iterable<Object> serialize(Serializers serializers, FileUploadConfig object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[];
    result.add('upload_url');
    if (object.url == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.url,
          specifiedType: const FullType(String)));
    }
    result.add('upload_params');
    if (object.params == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.params,
          specifiedType: const FullType(BuiltMap,
              const [const FullType(String), const FullType(String)])));
    }
    return result;
  }

  @override
  FileUploadConfig deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new FileUploadConfigBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'upload_url':
          result.url = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'upload_params':
          result.params.replace(serializers.deserialize(value,
              specifiedType: const FullType(BuiltMap,
                  const [const FullType(String), const FullType(String)])));
          break;
      }
    }

    return result.build();
  }
}

class _$FileUploadConfig extends FileUploadConfig {
  @override
  final String url;
  @override
  final BuiltMap<String, String> params;

  factory _$FileUploadConfig(
          [void Function(FileUploadConfigBuilder) updates]) =>
      (new FileUploadConfigBuilder()..update(updates)).build();

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
    return $jf($jc($jc(0, url.hashCode), params.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('FileUploadConfig')
          ..add('url', url)
          ..add('params', params))
        .toString();
  }
}

class FileUploadConfigBuilder
    implements Builder<FileUploadConfig, FileUploadConfigBuilder> {
  _$FileUploadConfig _$v;

  String _url;
  String get url => _$this._url;
  set url(String url) => _$this._url = url;

  MapBuilder<String, String> _params;
  MapBuilder<String, String> get params =>
      _$this._params ??= new MapBuilder<String, String>();
  set params(MapBuilder<String, String> params) => _$this._params = params;

  FileUploadConfigBuilder();

  FileUploadConfigBuilder get _$this {
    if (_$v != null) {
      _url = _$v.url;
      _params = _$v.params?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(FileUploadConfig other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$FileUploadConfig;
  }

  @override
  void update(void Function(FileUploadConfigBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$FileUploadConfig build() {
    _$FileUploadConfig _$result;
    try {
      _$result =
          _$v ?? new _$FileUploadConfig._(url: url, params: _params?.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'params';
        _params?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'FileUploadConfig', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
