// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'terms_of_service.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<TermsOfService> _$termsOfServiceSerializer =
    new _$TermsOfServiceSerializer();

class _$TermsOfServiceSerializer
    implements StructuredSerializer<TermsOfService> {
  @override
  final Iterable<Type> types = const [TermsOfService, _$TermsOfService];
  @override
  final String wireName = 'TermsOfService';

  @override
  Iterable<Object> serialize(Serializers serializers, TermsOfService object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'passive',
      serializers.serialize(object.passive,
          specifiedType: const FullType(bool)),
      'account_id',
      serializers.serialize(object.accountId,
          specifiedType: const FullType(String)),
    ];
    result.add('terms_type');
    if (object.termsType == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.termsType,
          specifiedType: const FullType(String)));
    }
    result.add('content');
    if (object.content == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.content,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  TermsOfService deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new TermsOfServiceBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'terms_type':
          result.termsType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'passive':
          result.passive = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
          break;
        case 'account_id':
          result.accountId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'content':
          result.content = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$TermsOfService extends TermsOfService {
  @override
  final String id;
  @override
  final String termsType;
  @override
  final bool passive;
  @override
  final String accountId;
  @override
  final String content;

  factory _$TermsOfService([void Function(TermsOfServiceBuilder) updates]) =>
      (new TermsOfServiceBuilder()..update(updates)).build();

  _$TermsOfService._(
      {this.id, this.termsType, this.passive, this.accountId, this.content})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('TermsOfService', 'id');
    }
    if (passive == null) {
      throw new BuiltValueNullFieldError('TermsOfService', 'passive');
    }
    if (accountId == null) {
      throw new BuiltValueNullFieldError('TermsOfService', 'accountId');
    }
  }

  @override
  TermsOfService rebuild(void Function(TermsOfServiceBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  TermsOfServiceBuilder toBuilder() =>
      new TermsOfServiceBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is TermsOfService &&
        id == other.id &&
        termsType == other.termsType &&
        passive == other.passive &&
        accountId == other.accountId &&
        content == other.content;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc($jc($jc($jc(0, id.hashCode), termsType.hashCode), passive.hashCode),
            accountId.hashCode),
        content.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('TermsOfService')
          ..add('id', id)
          ..add('termsType', termsType)
          ..add('passive', passive)
          ..add('accountId', accountId)
          ..add('content', content))
        .toString();
  }
}

class TermsOfServiceBuilder
    implements Builder<TermsOfService, TermsOfServiceBuilder> {
  _$TermsOfService _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _termsType;
  String get termsType => _$this._termsType;
  set termsType(String termsType) => _$this._termsType = termsType;

  bool _passive;
  bool get passive => _$this._passive;
  set passive(bool passive) => _$this._passive = passive;

  String _accountId;
  String get accountId => _$this._accountId;
  set accountId(String accountId) => _$this._accountId = accountId;

  String _content;
  String get content => _$this._content;
  set content(String content) => _$this._content = content;

  TermsOfServiceBuilder();

  TermsOfServiceBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _termsType = _$v.termsType;
      _passive = _$v.passive;
      _accountId = _$v.accountId;
      _content = _$v.content;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(TermsOfService other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$TermsOfService;
  }

  @override
  void update(void Function(TermsOfServiceBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$TermsOfService build() {
    final _$result = _$v ??
        new _$TermsOfService._(
            id: id,
            termsType: termsType,
            passive: passive,
            accountId: accountId,
            content: content);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
