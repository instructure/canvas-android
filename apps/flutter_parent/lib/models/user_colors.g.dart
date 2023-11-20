// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_colors.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<UserColors> _$userColorsSerializer = new _$UserColorsSerializer();

class _$UserColorsSerializer implements StructuredSerializer<UserColors> {
  @override
  final Iterable<Type> types = const [UserColors, _$UserColors];
  @override
  final String wireName = 'UserColors';

  @override
  Iterable<Object?> serialize(Serializers serializers, UserColors object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'custom_colors',
      serializers.serialize(object.customColors,
          specifiedType: const FullType(BuiltMap,
              const [const FullType(String), const FullType(String)])),
    ];

    return result;
  }

  @override
  UserColors deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new UserColorsBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'custom_colors':
          result.customColors.replace(serializers.deserialize(value,
              specifiedType: const FullType(BuiltMap,
                  const [const FullType(String), const FullType(String)]))!);
          break;
      }
    }

    return result.build();
  }
}

class _$UserColors extends UserColors {
  @override
  final BuiltMap<String, String> customColors;

  factory _$UserColors([void Function(UserColorsBuilder)? updates]) =>
      (new UserColorsBuilder()..update(updates))._build();

  _$UserColors._({required this.customColors}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        customColors, r'UserColors', 'customColors');
  }

  @override
  UserColors rebuild(void Function(UserColorsBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  UserColorsBuilder toBuilder() => new UserColorsBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is UserColors && customColors == other.customColors;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, customColors.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'UserColors')
          ..add('customColors', customColors))
        .toString();
  }
}

class UserColorsBuilder implements Builder<UserColors, UserColorsBuilder> {
  _$UserColors? _$v;

  MapBuilder<String, String>? _customColors;
  MapBuilder<String, String> get customColors =>
      _$this._customColors ??= new MapBuilder<String, String>();
  set customColors(MapBuilder<String, String>? customColors) =>
      _$this._customColors = customColors;

  UserColorsBuilder() {
    UserColors._initializeBuilder(this);
  }

  UserColorsBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _customColors = $v.customColors.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(UserColors other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$UserColors;
  }

  @override
  void update(void Function(UserColorsBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  UserColors build() => _build();

  _$UserColors _build() {
    _$UserColors _$result;
    try {
      _$result = _$v ?? new _$UserColors._(customColors: customColors.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'customColors';
        customColors.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'UserColors', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
