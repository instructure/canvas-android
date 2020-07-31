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
  Iterable<Object> serialize(Serializers serializers, UserColors object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'custom_colors',
      serializers.serialize(object.customColors,
          specifiedType: const FullType(BuiltMap,
              const [const FullType(String), const FullType(String)])),
    ];

    return result;
  }

  @override
  UserColors deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new UserColorsBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      switch (key) {
        case 'custom_colors':
          result.customColors.replace(serializers.deserialize(value,
              specifiedType: const FullType(BuiltMap,
                  const [const FullType(String), const FullType(String)])));
          break;
      }
    }

    return result.build();
  }
}

class _$UserColors extends UserColors {
  @override
  final BuiltMap<String, String> customColors;

  factory _$UserColors([void Function(UserColorsBuilder) updates]) =>
      (new UserColorsBuilder()..update(updates)).build();

  _$UserColors._({this.customColors}) : super._() {
    if (customColors == null) {
      throw new BuiltValueNullFieldError('UserColors', 'customColors');
    }
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
    return $jf($jc(0, customColors.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('UserColors')
          ..add('customColors', customColors))
        .toString();
  }
}

class UserColorsBuilder implements Builder<UserColors, UserColorsBuilder> {
  _$UserColors _$v;

  MapBuilder<String, String> _customColors;
  MapBuilder<String, String> get customColors =>
      _$this._customColors ??= new MapBuilder<String, String>();
  set customColors(MapBuilder<String, String> customColors) =>
      _$this._customColors = customColors;

  UserColorsBuilder() {
    UserColors._initializeBuilder(this);
  }

  UserColorsBuilder get _$this {
    if (_$v != null) {
      _customColors = _$v.customColors?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(UserColors other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$UserColors;
  }

  @override
  void update(void Function(UserColorsBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$UserColors build() {
    _$UserColors _$result;
    try {
      _$result = _$v ?? new _$UserColors._(customColors: customColors.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'customColors';
        customColors.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'UserColors', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
