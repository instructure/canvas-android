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
  Iterable<Object> serialize(Serializers serializers, Recipient object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
    ];
    result.add('pronouns');
    if (object.pronouns == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.pronouns,
          specifiedType: const FullType(String)));
    }
    result.add('avatar_url');
    if (object.avatarUrl == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.avatarUrl,
          specifiedType: const FullType(String)));
    }
    result.add('common_courses');
    if (object.commonCourses == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.commonCourses,
          specifiedType: const FullType(BuiltMap, const [
            const FullType(String),
            const FullType(BuiltList, const [const FullType(String)])
          ])));
    }
    return result;
  }

  @override
  Recipient deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new RecipientBuilder();

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
        case 'name':
          result.name = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'pronouns':
          result.pronouns = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'avatar_url':
          result.avatarUrl = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'common_courses':
          result.commonCourses.replace(serializers.deserialize(value,
              specifiedType: const FullType(BuiltMap, const [
                const FullType(String),
                const FullType(BuiltList, const [const FullType(String)])
              ])));
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
  final String pronouns;
  @override
  final String avatarUrl;
  @override
  final BuiltMap<String, BuiltList<String>> commonCourses;

  factory _$Recipient([void Function(RecipientBuilder) updates]) =>
      (new RecipientBuilder()..update(updates)).build();

  _$Recipient._(
      {this.id, this.name, this.pronouns, this.avatarUrl, this.commonCourses})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Recipient', 'id');
    }
    if (name == null) {
      throw new BuiltValueNullFieldError('Recipient', 'name');
    }
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
    return $jf($jc(
        $jc($jc($jc($jc(0, id.hashCode), name.hashCode), pronouns.hashCode),
            avatarUrl.hashCode),
        commonCourses.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Recipient')
          ..add('id', id)
          ..add('name', name)
          ..add('pronouns', pronouns)
          ..add('avatarUrl', avatarUrl)
          ..add('commonCourses', commonCourses))
        .toString();
  }
}

class RecipientBuilder implements Builder<Recipient, RecipientBuilder> {
  _$Recipient _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  String _pronouns;
  String get pronouns => _$this._pronouns;
  set pronouns(String pronouns) => _$this._pronouns = pronouns;

  String _avatarUrl;
  String get avatarUrl => _$this._avatarUrl;
  set avatarUrl(String avatarUrl) => _$this._avatarUrl = avatarUrl;

  MapBuilder<String, BuiltList<String>> _commonCourses;
  MapBuilder<String, BuiltList<String>> get commonCourses =>
      _$this._commonCourses ??= new MapBuilder<String, BuiltList<String>>();
  set commonCourses(MapBuilder<String, BuiltList<String>> commonCourses) =>
      _$this._commonCourses = commonCourses;

  RecipientBuilder();

  RecipientBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _name = _$v.name;
      _pronouns = _$v.pronouns;
      _avatarUrl = _$v.avatarUrl;
      _commonCourses = _$v.commonCourses?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Recipient other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Recipient;
  }

  @override
  void update(void Function(RecipientBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Recipient build() {
    _$Recipient _$result;
    try {
      _$result = _$v ??
          new _$Recipient._(
              id: id,
              name: name,
              pronouns: pronouns,
              avatarUrl: avatarUrl,
              commonCourses: _commonCourses?.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'commonCourses';
        _commonCourses?.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'Recipient', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
