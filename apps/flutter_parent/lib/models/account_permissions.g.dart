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
  Iterable<Object?> serialize(
      Serializers serializers, AccountPermissions object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'become_user',
      serializers.serialize(object.becomeUser,
          specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  AccountPermissions deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AccountPermissionsBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'become_user':
          result.becomeUser = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
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
          [void Function(AccountPermissionsBuilder)? updates]) =>
      (new AccountPermissionsBuilder()..update(updates))._build();

  _$AccountPermissions._({required this.becomeUser}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        becomeUser, r'AccountPermissions', 'becomeUser');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, becomeUser.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'AccountPermissions')
          ..add('becomeUser', becomeUser))
        .toString();
  }
}

class AccountPermissionsBuilder
    implements Builder<AccountPermissions, AccountPermissionsBuilder> {
  _$AccountPermissions? _$v;

  bool? _becomeUser;
  bool? get becomeUser => _$this._becomeUser;
  set becomeUser(bool? becomeUser) => _$this._becomeUser = becomeUser;

  AccountPermissionsBuilder() {
    AccountPermissions._initializeBuilder(this);
  }

  AccountPermissionsBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _becomeUser = $v.becomeUser;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AccountPermissions other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$AccountPermissions;
  }

  @override
  void update(void Function(AccountPermissionsBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  AccountPermissions build() => _build();

  _$AccountPermissions _build() {
    final _$result = _$v ??
        new _$AccountPermissions._(
            becomeUser: BuiltValueNullFieldError.checkNotNull(
                becomeUser, r'AccountPermissions', 'becomeUser'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
