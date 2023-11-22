// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_name_data.dart';

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
  Iterable<Object?> serialize(Serializers serializers, UserNameData object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'short_name',
      serializers.serialize(object.shortName,
          specifiedType: const FullType(String)),
      'sortable_name',
      serializers.serialize(object.sortableName,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.termsOfUse;

    result
      ..add('terms_of_use')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));

    return result;
  }

  @override
  UserNameData deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new UserNameDataBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'short_name':
          result.shortName = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'sortable_name':
          result.sortableName = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'terms_of_use':
          result.termsOfUse = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
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
  final bool? termsOfUse;

  factory _$UserNameData([void Function(UserNameDataBuilder)? updates]) =>
      (new UserNameDataBuilder()..update(updates))._build();

  _$UserNameData._(
      {required this.name,
      required this.shortName,
      required this.sortableName,
      this.termsOfUse})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(name, r'UserNameData', 'name');
    BuiltValueNullFieldError.checkNotNull(
        shortName, r'UserNameData', 'shortName');
    BuiltValueNullFieldError.checkNotNull(
        sortableName, r'UserNameData', 'sortableName');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, shortName.hashCode);
    _$hash = $jc(_$hash, sortableName.hashCode);
    _$hash = $jc(_$hash, termsOfUse.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'UserNameData')
          ..add('name', name)
          ..add('shortName', shortName)
          ..add('sortableName', sortableName)
          ..add('termsOfUse', termsOfUse))
        .toString();
  }
}

class UserNameDataBuilder
    implements Builder<UserNameData, UserNameDataBuilder> {
  _$UserNameData? _$v;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _shortName;
  String? get shortName => _$this._shortName;
  set shortName(String? shortName) => _$this._shortName = shortName;

  String? _sortableName;
  String? get sortableName => _$this._sortableName;
  set sortableName(String? sortableName) => _$this._sortableName = sortableName;

  bool? _termsOfUse;
  bool? get termsOfUse => _$this._termsOfUse;
  set termsOfUse(bool? termsOfUse) => _$this._termsOfUse = termsOfUse;

  UserNameDataBuilder() {
    UserNameData._initializeBuilder(this);
  }

  UserNameDataBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _name = $v.name;
      _shortName = $v.shortName;
      _sortableName = $v.sortableName;
      _termsOfUse = $v.termsOfUse;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(UserNameData other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$UserNameData;
  }

  @override
  void update(void Function(UserNameDataBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  UserNameData build() => _build();

  _$UserNameData _build() {
    final _$result = _$v ??
        new _$UserNameData._(
            name: BuiltValueNullFieldError.checkNotNull(
                name, r'UserNameData', 'name'),
            shortName: BuiltValueNullFieldError.checkNotNull(
                shortName, r'UserNameData', 'shortName'),
            sortableName: BuiltValueNullFieldError.checkNotNull(
                sortableName, r'UserNameData', 'sortableName'),
            termsOfUse: termsOfUse);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
