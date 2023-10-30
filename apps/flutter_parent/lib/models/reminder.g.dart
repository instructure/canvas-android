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
  Iterable<Object?> serialize(Serializers serializers, Reminder object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
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
      'courseId',
      serializers.serialize(object.courseId,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.id;

    result
      ..add('id')
      ..add(serializers.serialize(value, specifiedType: const FullType(int)));
    value = object.date;

    result
      ..add('date')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));

    return result;
  }

  @override
  Reminder deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new ReminderBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int?;
          break;
        case 'userDomain':
          result.userDomain = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'userId':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'type':
          result.type = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'itemId':
          result.itemId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'courseId':
          result.courseId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'date':
          result.date = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
      }
    }

    return result.build();
  }
}

class _$Reminder extends Reminder {
  @override
  final int? id;
  @override
  final String userDomain;
  @override
  final String userId;
  @override
  final String type;
  @override
  final String itemId;
  @override
  final String courseId;
  @override
  final DateTime? date;

  factory _$Reminder([void Function(ReminderBuilder)? updates]) =>
      (new ReminderBuilder()..update(updates))._build();

  _$Reminder._(
      {this.id,
      required this.userDomain,
      required this.userId,
      required this.type,
      required this.itemId,
      required this.courseId,
      this.date})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(
        userDomain, r'Reminder', 'userDomain');
    BuiltValueNullFieldError.checkNotNull(userId, r'Reminder', 'userId');
    BuiltValueNullFieldError.checkNotNull(type, r'Reminder', 'type');
    BuiltValueNullFieldError.checkNotNull(itemId, r'Reminder', 'itemId');
    BuiltValueNullFieldError.checkNotNull(courseId, r'Reminder', 'courseId');
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
        courseId == other.courseId &&
        date == other.date;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, userDomain.hashCode);
    _$hash = $jc(_$hash, userId.hashCode);
    _$hash = $jc(_$hash, type.hashCode);
    _$hash = $jc(_$hash, itemId.hashCode);
    _$hash = $jc(_$hash, courseId.hashCode);
    _$hash = $jc(_$hash, date.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Reminder')
          ..add('id', id)
          ..add('userDomain', userDomain)
          ..add('userId', userId)
          ..add('type', type)
          ..add('itemId', itemId)
          ..add('courseId', courseId)
          ..add('date', date))
        .toString();
  }
}

class ReminderBuilder implements Builder<Reminder, ReminderBuilder> {
  _$Reminder? _$v;

  int? _id;
  int? get id => _$this._id;
  set id(int? id) => _$this._id = id;

  String? _userDomain;
  String? get userDomain => _$this._userDomain;
  set userDomain(String? userDomain) => _$this._userDomain = userDomain;

  String? _userId;
  String? get userId => _$this._userId;
  set userId(String? userId) => _$this._userId = userId;

  String? _type;
  String? get type => _$this._type;
  set type(String? type) => _$this._type = type;

  String? _itemId;
  String? get itemId => _$this._itemId;
  set itemId(String? itemId) => _$this._itemId = itemId;

  String? _courseId;
  String? get courseId => _$this._courseId;
  set courseId(String? courseId) => _$this._courseId = courseId;

  DateTime? _date;
  DateTime? get date => _$this._date;
  set date(DateTime? date) => _$this._date = date;

  ReminderBuilder() {
    Reminder._initializeBuilder(this);
  }

  ReminderBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _userDomain = $v.userDomain;
      _userId = $v.userId;
      _type = $v.type;
      _itemId = $v.itemId;
      _courseId = $v.courseId;
      _date = $v.date;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Reminder other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Reminder;
  }

  @override
  void update(void Function(ReminderBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Reminder build() => _build();

  _$Reminder _build() {
    final _$result = _$v ??
        new _$Reminder._(
            id: id,
            userDomain: BuiltValueNullFieldError.checkNotNull(
                userDomain, r'Reminder', 'userDomain'),
            userId: BuiltValueNullFieldError.checkNotNull(
                userId, r'Reminder', 'userId'),
            type: BuiltValueNullFieldError.checkNotNull(
                type, r'Reminder', 'type'),
            itemId: BuiltValueNullFieldError.checkNotNull(
                itemId, r'Reminder', 'itemId'),
            courseId: BuiltValueNullFieldError.checkNotNull(
                courseId, r'Reminder', 'courseId'),
            date: date);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
