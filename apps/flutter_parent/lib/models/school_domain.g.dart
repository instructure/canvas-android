// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'school_domain.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<SchoolDomain> _$schoolDomainSerializer =
    new _$SchoolDomainSerializer();

class _$SchoolDomainSerializer implements StructuredSerializer<SchoolDomain> {
  @override
  final Iterable<Type> types = const [SchoolDomain, _$SchoolDomain];
  @override
  final String wireName = 'SchoolDomain';

  @override
  Iterable<Object> serialize(Serializers serializers, SchoolDomain object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'domain',
      serializers.serialize(object.domain,
          specifiedType: const FullType(String)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
    ];
    result.add('authentication_provider');
    if (object.authenticationProvider == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.authenticationProvider,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  SchoolDomain deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new SchoolDomainBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'domain':
          result.domain = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'authentication_provider':
          result.authenticationProvider = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$SchoolDomain extends SchoolDomain {
  @override
  final String domain;
  @override
  final String name;
  @override
  final String authenticationProvider;

  factory _$SchoolDomain([void Function(SchoolDomainBuilder) updates]) =>
      (new SchoolDomainBuilder()..update(updates)).build();

  _$SchoolDomain._({this.domain, this.name, this.authenticationProvider})
      : super._() {
    if (domain == null) {
      throw new BuiltValueNullFieldError('SchoolDomain', 'domain');
    }
    if (name == null) {
      throw new BuiltValueNullFieldError('SchoolDomain', 'name');
    }
  }

  @override
  SchoolDomain rebuild(void Function(SchoolDomainBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  SchoolDomainBuilder toBuilder() => new SchoolDomainBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is SchoolDomain &&
        domain == other.domain &&
        name == other.name &&
        authenticationProvider == other.authenticationProvider;
  }

  @override
  int get hashCode {
    return $jf($jc($jc($jc(0, domain.hashCode), name.hashCode),
        authenticationProvider.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('SchoolDomain')
          ..add('domain', domain)
          ..add('name', name)
          ..add('authenticationProvider', authenticationProvider))
        .toString();
  }
}

class SchoolDomainBuilder
    implements Builder<SchoolDomain, SchoolDomainBuilder> {
  _$SchoolDomain _$v;

  String _domain;
  String get domain => _$this._domain;
  set domain(String domain) => _$this._domain = domain;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _authenticationProvider;
  String get authenticationProvider => _$this._authenticationProvider;
  set authenticationProvider(String authenticationProvider) =>
      _$this._authenticationProvider = authenticationProvider;

  SchoolDomainBuilder();

  SchoolDomainBuilder get _$this {
    if (_$v != null) {
      _domain = _$v.domain;
      _name = _$v.name;
      _authenticationProvider = _$v.authenticationProvider;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(SchoolDomain other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$SchoolDomain;
  }

  @override
  void update(void Function(SchoolDomainBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$SchoolDomain build() {
    final _$result = _$v ??
        new _$SchoolDomain._(
            domain: domain,
            name: name,
            authenticationProvider: authenticationProvider);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
