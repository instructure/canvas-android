// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'account_permissions.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<AccountPermissions> _$accountPermissionsSerializer =
    new _$AccountPermissionsSerializer();

class _$AccountPermissionsSerializer
    implements StructuredSerializer<AccountPermissions> {
  @override
  final Iterable<Type> types = const [AccountPermissions, _$AccountPermissions];
  @override
  final String wireName = 'AccountPermissions';

  @override
  Iterable<Object> serialize(Serializers serializers, AccountPermissions object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'become_user',
      serializers.serialize(object.becomeUser,
          specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  AccountPermissions deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AccountPermissionsBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'become_user':
          result.becomeUser = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$AccountPermissions extends AccountPermissions {
  @override
  final bool becomeUser;

  factory _$AccountPermissions(
          [void Function(AccountPermissionsBuilder) updates]) =>
      (new AccountPermissionsBuilder()..update(updates)).build();

  _$AccountPermissions._({this.becomeUser}) : super._() {
    if (becomeUser == null) {
      throw new BuiltValueNullFieldError('AccountPermissions', 'becomeUser');
    }
  }

  @override
  AccountPermissions rebuild(
          void Function(AccountPermissionsBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AccountPermissionsBuilder toBuilder() =>
      new AccountPermissionsBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is AccountPermissions && becomeUser == other.becomeUser;
  }

  @override
  int get hashCode {
    return $jf($jc(0, becomeUser.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('AccountPermissions')
          ..add('becomeUser', becomeUser))
        .toString();
  }
}

class AccountPermissionsBuilder
    implements Builder<AccountPermissions, AccountPermissionsBuilder> {
  _$AccountPermissions _$v;

  bool _becomeUser;
  bool get becomeUser => _$this._becomeUser;
  set becomeUser(bool becomeUser) => _$this._becomeUser = becomeUser;

  AccountPermissionsBuilder() {
    AccountPermissions._initializeBuilder(this);
  }

  AccountPermissionsBuilder get _$this {
    if (_$v != null) {
      _becomeUser = _$v.becomeUser;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AccountPermissions other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$AccountPermissions;
  }

  @override
  void update(void Function(AccountPermissionsBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$AccountPermissions build() {
    final _$result = _$v ?? new _$AccountPermissions._(becomeUser: becomeUser);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
