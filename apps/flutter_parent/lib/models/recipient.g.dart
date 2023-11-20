// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'recipient.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Recipient> _$recipientSerializer = new _$RecipientSerializer();

class _$RecipientSerializer implements StructuredSerializer<Recipient> {
  @override
  final Iterable<Type> types = const [Recipient, _$Recipient];
  @override
  final String wireName = 'Recipient';

  @override
  Iterable<Object?> serialize(Serializers serializers, Recipient object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.pronouns;

    result
      ..add('pronouns')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.avatarUrl;

    result
      ..add('avatar_url')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.commonCourses;

    result
      ..add('common_courses')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(BuiltMap, const [
            const FullType(String),
            const FullType(BuiltList, const [const FullType(String)])
          ])));

    return result;
  }

  @override
  Recipient deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new RecipientBuilder();

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
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'pronouns':
          result.pronouns = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'avatar_url':
          result.avatarUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'common_courses':
          result.commonCourses.replace(serializers.deserialize(value,
              specifiedType: const FullType(BuiltMap, const [
                const FullType(String),
                const FullType(BuiltList, const [const FullType(String)])
              ]))!);
          break;
      }
    }

    return result.build();
  }
}

class _$Recipient extends Recipient {
  @override
  final String id;
  @override
  final String name;
  @override
  final String? pronouns;
  @override
  final String? avatarUrl;
  @override
  final BuiltMap<String, BuiltList<String>>? commonCourses;

  factory _$Recipient([void Function(RecipientBuilder)? updates]) =>
      (new RecipientBuilder()..update(updates))._build();

  _$Recipient._(
      {required this.id,
      required this.name,
      this.pronouns,
      this.avatarUrl,
      this.commonCourses})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Recipient', 'id');
    BuiltValueNullFieldError.checkNotNull(name, r'Recipient', 'name');
  }

  @override
  Recipient rebuild(void Function(RecipientBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  RecipientBuilder toBuilder() => new RecipientBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Recipient &&
        id == other.id &&
        name == other.name &&
        pronouns == other.pronouns &&
        avatarUrl == other.avatarUrl &&
        commonCourses == other.commonCourses;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, pronouns.hashCode);
    _$hash = $jc(_$hash, avatarUrl.hashCode);
    _$hash = $jc(_$hash, commonCourses.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Recipient')
          ..add('id', id)
          ..add('name', name)
          ..add('pronouns', pronouns)
          ..add('avatarUrl', avatarUrl)
          ..add('commonCourses', commonCourses))
        .toString();
  }
}

class RecipientBuilder implements Builder<Recipient, RecipientBuilder> {
  _$Recipient? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  String? _pronouns;
  String? get pronouns => _$this._pronouns;
  set pronouns(String? pronouns) => _$this._pronouns = pronouns;

  String? _avatarUrl;
  String? get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String? avatarUrl) => _$this._avatarUrl = avatarUrl;

  MapBuilder<String, BuiltList<String>>? _commonCourses;
  MapBuilder<String, BuiltList<String>> get commonCourses =>
      _$this._commonCourses ??= new MapBuilder<String, BuiltList<String>>();
  set commonCourses(MapBuilder<String, BuiltList<String>>? commonCourses) =>
      _$this._commonCourses = commonCourses;

  RecipientBuilder();

  RecipientBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _name = $v.name;
      _pronouns = $v.pronouns;
      _avatarUrl = $v.avatarUrl;
      _commonCourses = $v.commonCourses?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Recipient other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Recipient;
  }

  @override
  void update(void Function(RecipientBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Recipient build() => _build();

  _$Recipient _build() {
    _$Recipient _$result;
    try {
      _$result = _$v ??
          new _$Recipient._(
              id: BuiltValueNullFieldError.checkNotNull(id, r'Recipient', 'id'),
              name: BuiltValueNullFieldError.checkNotNull(
                  name, r'Recipient', 'name'),
              pronouns: pronouns,
              avatarUrl: avatarUrl,
              commonCourses: _commonCourses?.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'commonCourses';
        _commonCourses?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'Recipient', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
