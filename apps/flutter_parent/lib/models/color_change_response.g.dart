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
  Iterable<Object> serialize(
      Serializers serializers, ColorChangeResponse object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[];
    Object value;
    value = object.hexCode;

    result
      ..add('hexcode')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  ColorChangeResponse deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new ColorChangeResponseBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final Object value = iterator.current;
      switch (key) {
        case 'hexcode':
          result.hexCode = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$ColorChangeResponse extends ColorChangeResponse {
  @override
  final String hexCode;

  factory _$ColorChangeResponse(
          [void Function(ColorChangeResponseBuilder) updates]) =>
      (new ColorChangeResponseBuilder()..update(updates)).build();

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
    return $jf($jc(0, hexCode.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('ColorChangeResponse')
          ..add('hexCode', hexCode))
        .toString();
  }
}

class ColorChangeResponseBuilder
    implements Builder<ColorChangeResponse, ColorChangeResponseBuilder> {
  _$ColorChangeResponse _$v;

  String _hexCode;
  String get hexCode => _$this._hexCode;
  set hexCode(String hexCode) => _$this._hexCode = hexCode;

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
  void update(void Function(ColorChangeResponseBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$ColorChangeResponse build() {
    final _$result = _$v ?? new _$ColorChangeResponse._(hexCode: hexCode);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,deprecated_member_use_from_same_package,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
