// GENERATED CODE - DO NOT MODIFY BY HAND

part of user_name_data;

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<UserNameData> _$userNameDataSerializer =
    new _$UserNameDataSerializer();

class _$UserNameDataSerializer implements StructuredSerializer<UserNameData> {
  @override
  final Iterable<Type> types = const [UserNameData, _$UserNameData];
  @override
  final String wireName = 'UserNameData';

  @override
  Iterable<Object> serialize(Serializers serializers, UserNameData object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'short_name',
      serializers.serialize(object.shortName,
          specifiedType: const FullType(String)),
      'sortable_name',
      serializers.serialize(object.sortableName,
          specifiedType: const FullType(String)),
    ];
    result.add('terms_of_use');
    if (object.termsOfUse == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.termsOfUse,
          specifiedType: const FullType(bool)));
    }
    return result;
  }

  @override
  UserNameData deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new UserNameDataBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'short_name':
          result.shortName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'sortable_name':
          result.sortableName = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'terms_of_use':
          result.termsOfUse = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$UserNameData extends UserNameData {
  @override
  final String name;
  @override
  final String shortName;
  @override
  final String sortableName;
  @override
  final bool termsOfUse;

  factory _$UserNameData([void Function(UserNameDataBuilder) updates]) =>
      (new UserNameDataBuilder()..update(updates)).build();

  _$UserNameData._(
      {this.name, this.shortName, this.sortableName, this.termsOfUse})
      : super._() {
    if (name == null) {
      throw new BuiltValueNullFieldError('UserNameData', 'name');
    }
    if (shortName == null) {
      throw new BuiltValueNullFieldError('UserNameData', 'shortName');
    }
    if (sortableName == null) {
      throw new BuiltValueNullFieldError('UserNameData', 'sortableName');
    }
  }

  @override
  UserNameData rebuild(void Function(UserNameDataBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  UserNameDataBuilder toBuilder() => new UserNameDataBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is UserNameData &&
        name == other.name &&
        shortName == other.shortName &&
        sortableName == other.sortableName &&
        termsOfUse == other.termsOfUse;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc($jc($jc(0, name.hashCode), shortName.hashCode),
            sortableName.hashCode),
        termsOfUse.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('UserNameData')
          ..add('name', name)
          ..add('shortName', shortName)
          ..add('sortableName', sortableName)
          ..add('termsOfUse', termsOfUse))
        .toString();
  }
}

class UserNameDataBuilder
    implements Builder<UserNameData, UserNameDataBuilder> {
  _$UserNameData _$v;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _shortName;
  String get shortName => _$this._shortName;
  set shortName(String shortName) => _$this._shortName = shortName;

  String _sortableName;
  String get sortableName => _$this._sortableName;
  set sortableName(String sortableName) => _$this._sortableName = sortableName;

  bool _termsOfUse;
  bool get termsOfUse => _$this._termsOfUse;
  set termsOfUse(bool termsOfUse) => _$this._termsOfUse = termsOfUse;

  UserNameDataBuilder() {
    UserNameData._initializeBuilder(this);
  }

  UserNameDataBuilder get _$this {
    if (_$v != null) {
      _name = _$v.name;
      _shortName = _$v.shortName;
      _sortableName = _$v.sortableName;
      _termsOfUse = _$v.termsOfUse;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(UserNameData other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$UserNameData;
  }

  @override
  void update(void Function(UserNameDataBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$UserNameData build() {
    final _$result = _$v ??
        new _$UserNameData._(
            name: name,
            shortName: shortName,
            sortableName: sortableName,
            termsOfUse: termsOfUse);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
