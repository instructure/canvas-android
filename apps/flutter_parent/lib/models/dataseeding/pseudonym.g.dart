// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pseudonym.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Pseudonym> _$pseudonymSerializer = new _$PseudonymSerializer();

class _$PseudonymSerializer implements StructuredSerializer<Pseudonym> {
  @override
  final Iterable<Type> types = const [Pseudonym, _$Pseudonym];
  @override
  final String wireName = 'Pseudonym';

  @override
  Iterable<Object?> serialize(Serializers serializers, Pseudonym object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'unique_id',
      serializers.serialize(object.uniqueId,
          specifiedType: const FullType(String)),
      'password',
      serializers.serialize(object.password,
          specifiedType: const FullType(String)),
    ];

    return result;
  }

  @override
  Pseudonym deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PseudonymBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'unique_id':
          result.uniqueId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'password':
          result.password = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
      }
    }

    return result.build();
  }
}

class _$Pseudonym extends Pseudonym {
  @override
  final String uniqueId;
  @override
  final String password;

  factory _$Pseudonym([void Function(PseudonymBuilder)? updates]) =>
      (new PseudonymBuilder()..update(updates))._build();

  _$Pseudonym._({required this.uniqueId, required this.password}) : super._() {
    BuiltValueNullFieldError.checkNotNull(uniqueId, r'Pseudonym', 'uniqueId');
    BuiltValueNullFieldError.checkNotNull(password, r'Pseudonym', 'password');
  }

  @override
  Pseudonym rebuild(void Function(PseudonymBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PseudonymBuilder toBuilder() => new PseudonymBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Pseudonym &&
        uniqueId == other.uniqueId &&
        password == other.password;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, uniqueId.hashCode);
    _$hash = $jc(_$hash, password.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Pseudonym')
          ..add('uniqueId', uniqueId)
          ..add('password', password))
        .toString();
  }
}

class PseudonymBuilder implements Builder<Pseudonym, PseudonymBuilder> {
  _$Pseudonym? _$v;

  String? _uniqueId;
  String? get uniqueId => _$this._uniqueId;
  set uniqueId(String? uniqueId) => _$this._uniqueId = uniqueId;

  String? _password;
  String? get password => _$this._password;
  set password(String? password) => _$this._password = password;

  PseudonymBuilder() {
    Pseudonym._initializeBuilder(this);
  }

  PseudonymBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _uniqueId = $v.uniqueId;
      _password = $v.password;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Pseudonym other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Pseudonym;
  }

  @override
  void update(void Function(PseudonymBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Pseudonym build() => _build();

  _$Pseudonym _build() {
    final _$result = _$v ??
        new _$Pseudonym._(
            uniqueId: BuiltValueNullFieldError.checkNotNull(
                uniqueId, r'Pseudonym', 'uniqueId'),
            password: BuiltValueNullFieldError.checkNotNull(
                password, r'Pseudonym', 'password'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
