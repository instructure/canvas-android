// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'color_change_response.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<ColorChangeResponse> _$colorChangeResponseSerializer =
    new _$ColorChangeResponseSerializer();

class _$ColorChangeResponseSerializer
    implements StructuredSerializer<ColorChangeResponse> {
  @override
  final Iterable<Type> types = const [
    ColorChangeResponse,
    _$ColorChangeResponse
  ];
  @override
  final String wireName = 'ColorChangeResponse';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, ColorChangeResponse object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[];
    Object? value;
    value = object.hexCode;

    result
      ..add('hexcode')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  ColorChangeResponse deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new ColorChangeResponseBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'hexcode':
          result.hexCode = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
      }
    }

    return result.build();
  }
}

class _$ColorChangeResponse extends ColorChangeResponse {
  @override
  final String? hexCode;

  factory _$ColorChangeResponse(
          [void Function(ColorChangeResponseBuilder)? updates]) =>
      (new ColorChangeResponseBuilder()..update(updates))._build();

  _$ColorChangeResponse._({this.hexCode}) : super._();

  @override
  ColorChangeResponse rebuild(
          void Function(ColorChangeResponseBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  ColorChangeResponseBuilder toBuilder() =>
      new ColorChangeResponseBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is ColorChangeResponse && hexCode == other.hexCode;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, hexCode.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'ColorChangeResponse')
          ..add('hexCode', hexCode))
        .toString();
  }
}

class ColorChangeResponseBuilder
    implements Builder<ColorChangeResponse, ColorChangeResponseBuilder> {
  _$ColorChangeResponse? _$v;

  String? _hexCode;
  String? get hexCode => _$this._hexCode;
  set hexCode(String? hexCode) => _$this._hexCode = hexCode;

  ColorChangeResponseBuilder() {
    ColorChangeResponse._initializeBuilder(this);
  }

  ColorChangeResponseBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _hexCode = $v.hexCode;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(ColorChangeResponse other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$ColorChangeResponse;
  }

  @override
  void update(void Function(ColorChangeResponseBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  ColorChangeResponse build() => _build();

  _$ColorChangeResponse _build() {
    final _$result = _$v ?? new _$ColorChangeResponse._(hexCode: hexCode);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
