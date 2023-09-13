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
  Iterable<Object?> serialize(Serializers serializers, TermsOfService object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'passive',
      serializers.serialize(object.passive,
          specifiedType: const FullType(bool)),
      'account_id',
      serializers.serialize(object.accountId,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.termsType;

    result
      ..add('terms_type')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.content;

    result
      ..add('content')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  TermsOfService deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new TermsOfServiceBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'terms_type':
          result.termsType = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'passive':
          result.passive = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
        case 'account_id':
          result.accountId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'content':
          result.content = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
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
  final String? termsType;
  @override
  final bool passive;
  @override
  final String accountId;
  @override
  final String? content;

  factory _$TermsOfService([void Function(TermsOfServiceBuilder)? updates]) =>
      (new TermsOfServiceBuilder()..update(updates))._build();

  _$TermsOfService._(
      {required this.id,
      this.termsType,
      required this.passive,
      required this.accountId,
      this.content})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'TermsOfService', 'id');
    BuiltValueNullFieldError.checkNotNull(
        passive, r'TermsOfService', 'passive');
    BuiltValueNullFieldError.checkNotNull(
        accountId, r'TermsOfService', 'accountId');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, termsType.hashCode);
    _$hash = $jc(_$hash, passive.hashCode);
    _$hash = $jc(_$hash, accountId.hashCode);
    _$hash = $jc(_$hash, content.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'TermsOfService')
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
  _$TermsOfService? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _termsType;
  String? get termsType => _$this._termsType;
  set termsType(String? termsType) => _$this._termsType = termsType;

  bool? _passive;
  bool? get passive => _$this._passive;
  set passive(bool? passive) => _$this._passive = passive;

  String? _accountId;
  String? get accountId => _$this._accountId;
  set accountId(String? accountId) => _$this._accountId = accountId;

  String? _content;
  String? get content => _$this._content;
  set content(String? content) => _$this._content = content;

  TermsOfServiceBuilder();

  TermsOfServiceBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _termsType = $v.termsType;
      _passive = $v.passive;
      _accountId = $v.accountId;
      _content = $v.content;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(TermsOfService other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$TermsOfService;
  }

  @override
  void update(void Function(TermsOfServiceBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  TermsOfService build() => _build();

  _$TermsOfService _build() {
    final _$result = _$v ??
        new _$TermsOfService._(
            id: BuiltValueNullFieldError.checkNotNull(
                id, r'TermsOfService', 'id'),
            termsType: termsType,
            passive: BuiltValueNullFieldError.checkNotNull(
                passive, r'TermsOfService', 'passive'),
            accountId: BuiltValueNullFieldError.checkNotNull(
                accountId, r'TermsOfService', 'accountId'),
            content: content);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
