// GENERATED CODE - DO NOT MODIFY BY HAND

part of pseudonym;

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
  Iterable<Object> serialize(Serializers serializers, Pseudonym object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
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
  Pseudonym deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PseudonymBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'unique_id':
          result.uniqueId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'password':
          result.password = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
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

  factory _$Pseudonym([void Function(PseudonymBuilder) updates]) =>
      (new PseudonymBuilder()..update(updates)).build();

  _$Pseudonym._({this.uniqueId, this.password}) : super._() {
    if (uniqueId == null) {
      throw new BuiltValueNullFieldError('Pseudonym', 'uniqueId');
    }
    if (password == null) {
      throw new BuiltValueNullFieldError('Pseudonym', 'password');
    }
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
    return $jf($jc($jc(0, uniqueId.hashCode), password.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Pseudonym')
          ..add('uniqueId', uniqueId)
          ..add('password', password))
        .toString();
  }
}

class PseudonymBuilder implements Builder<Pseudonym, PseudonymBuilder> {
  _$Pseudonym _$v;

  String _uniqueId;
  String get uniqueId => _$this._uniqueId;
  set uniqueId(String uniqueId) => _$this._uniqueId = uniqueId;

  String _password;
  String get password => _$this._password;
  set password(String password) => _$this._password = password;

  PseudonymBuilder() {
    Pseudonym._initializeBuilder(this);
  }

  PseudonymBuilder get _$this {
    if (_$v != null) {
      _uniqueId = _$v.uniqueId;
      _password = _$v.password;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Pseudonym other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Pseudonym;
  }

  @override
  void update(void Function(PseudonymBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Pseudonym build() {
    final _$result =
        _$v ?? new _$Pseudonym._(uniqueId: uniqueId, password: password);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
