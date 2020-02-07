// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reminder.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Reminder> _$reminderSerializer = new _$ReminderSerializer();

class _$ReminderSerializer implements StructuredSerializer<Reminder> {
  @override
  final Iterable<Type> types = const [Reminder, _$Reminder];
  @override
  final String wireName = 'Reminder';

  @override
  Iterable<Object> serialize(Serializers serializers, Reminder object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'userDomain',
      serializers.serialize(object.userDomain,
          specifiedType: const FullType(String)),
      'userId',
      serializers.serialize(object.userId,
          specifiedType: const FullType(String)),
      'type',
      serializers.serialize(object.type, specifiedType: const FullType(String)),
      'itemId',
      serializers.serialize(object.itemId,
          specifiedType: const FullType(String)),
      'date',
      serializers.serialize(object.date,
          specifiedType: const FullType(DateTime)),
    ];
    result.add('id');
    if (object.id == null) {
      result.add(null);
    } else {
      result.add(
          serializers.serialize(object.id, specifiedType: const FullType(int)));
    }
    return result;
  }

  @override
  Reminder deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new ReminderBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'userDomain':
          result.userDomain = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'userId':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'type':
          result.type = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'itemId':
          result.itemId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'date':
          result.date = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
      }
    }

    return result.build();
  }
}

class _$Reminder extends Reminder {
  @override
  final int id;
  @override
  final String userDomain;
  @override
  final String userId;
  @override
  final String type;
  @override
  final String itemId;
  @override
  final DateTime date;

  factory _$Reminder([void Function(ReminderBuilder) updates]) =>
      (new ReminderBuilder()..update(updates)).build();

  _$Reminder._(
      {this.id,
      this.userDomain,
      this.userId,
      this.type,
      this.itemId,
      this.date})
      : super._() {
    if (userDomain == null) {
      throw new BuiltValueNullFieldError('Reminder', 'userDomain');
    }
    if (userId == null) {
      throw new BuiltValueNullFieldError('Reminder', 'userId');
    }
    if (type == null) {
      throw new BuiltValueNullFieldError('Reminder', 'type');
    }
    if (itemId == null) {
      throw new BuiltValueNullFieldError('Reminder', 'itemId');
    }
    if (date == null) {
      throw new BuiltValueNullFieldError('Reminder', 'date');
    }
  }

  @override
  Reminder rebuild(void Function(ReminderBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  ReminderBuilder toBuilder() => new ReminderBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Reminder &&
        id == other.id &&
        userDomain == other.userDomain &&
        userId == other.userId &&
        type == other.type &&
        itemId == other.itemId &&
        date == other.date;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc($jc($jc(0, id.hashCode), userDomain.hashCode),
                    userId.hashCode),
                type.hashCode),
            itemId.hashCode),
        date.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Reminder')
          ..add('id', id)
          ..add('userDomain', userDomain)
          ..add('userId', userId)
          ..add('type', type)
          ..add('itemId', itemId)
          ..add('date', date))
        .toString();
  }
}

class ReminderBuilder implements Builder<Reminder, ReminderBuilder> {
  _$Reminder _$v;

  int _id;
  int get id => _$this._id;
  set id(int id) => _$this._id = id;

  String _userDomain;
  String get userDomain => _$this._userDomain;
  set userDomain(String userDomain) => _$this._userDomain = userDomain;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  String _type;
  String get type => _$this._type;
  set type(String type) => _$this._type = type;

  String _itemId;
  String get itemId => _$this._itemId;
  set itemId(String itemId) => _$this._itemId = itemId;

  DateTime _date;
  DateTime get date => _$this._date;
  set date(DateTime date) => _$this._date = date;

  ReminderBuilder() {
    Reminder._initializeBuilder(this);
  }

  ReminderBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _userDomain = _$v.userDomain;
      _userId = _$v.userId;
      _type = _$v.type;
      _itemId = _$v.itemId;
      _date = _$v.date;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Reminder other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Reminder;
  }

  @override
  void update(void Function(ReminderBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Reminder build() {
    final _$result = _$v ??
        new _$Reminder._(
            id: id,
            userDomain: userDomain,
            userId: userId,
            type: type,
            itemId: itemId,
            date: date);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
