// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assignment_override.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<AssignmentOverride> _$assignmentOverrideSerializer =
    new _$AssignmentOverrideSerializer();

class _$AssignmentOverrideSerializer
    implements StructuredSerializer<AssignmentOverride> {
  @override
  final Iterable<Type> types = const [AssignmentOverride, _$AssignmentOverride];
  @override
  final String wireName = 'AssignmentOverride';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, AssignmentOverride object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'assignment_id',
      serializers.serialize(object.assignmentId,
          specifiedType: const FullType(String)),
      'student_ids',
      serializers.serialize(object.studentIds,
          specifiedType:
              const FullType(BuiltList, const [const FullType(String)])),
    ];
    Object? value;
    value = object.title;

    result
      ..add('title')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.dueAt;

    result
      ..add('due_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.allDay;

    result
      ..add('all_day')
      ..add(serializers.serialize(value, specifiedType: const FullType(bool)));
    value = object.allDayDate;

    result
      ..add('all_day_date')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.unlockAt;

    result
      ..add('unlock_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.lockAt;

    result
      ..add('lock_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));

    return result;
  }

  @override
  AssignmentOverride deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AssignmentOverrideBuilder();

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
        case 'assignment_id':
          result.assignmentId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'due_at':
          result.dueAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'all_day':
          result.allDay = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool?;
          break;
        case 'all_day_date':
          result.allDayDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'unlock_at':
          result.unlockAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'lock_at':
          result.lockAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'student_ids':
          result.studentIds.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(String)]))!
              as BuiltList<Object?>);
          break;
      }
    }

    return result.build();
  }
}

class _$AssignmentOverride extends AssignmentOverride {
  @override
  final String id;
  @override
  final String assignmentId;
  @override
  final String? title;
  @override
  final DateTime? dueAt;
  @override
  final bool? allDay;
  @override
  final DateTime? allDayDate;
  @override
  final DateTime? unlockAt;
  @override
  final DateTime? lockAt;
  @override
  final BuiltList<String> studentIds;

  factory _$AssignmentOverride(
          [void Function(AssignmentOverrideBuilder)? updates]) =>
      (new AssignmentOverrideBuilder()..update(updates))._build();

  _$AssignmentOverride._(
      {required this.id,
      required this.assignmentId,
      this.title,
      this.dueAt,
      this.allDay,
      this.allDayDate,
      this.unlockAt,
      this.lockAt,
      required this.studentIds})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'AssignmentOverride', 'id');
    BuiltValueNullFieldError.checkNotNull(
        assignmentId, r'AssignmentOverride', 'assignmentId');
    BuiltValueNullFieldError.checkNotNull(
        studentIds, r'AssignmentOverride', 'studentIds');
  }

  @override
  AssignmentOverride rebuild(
          void Function(AssignmentOverrideBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AssignmentOverrideBuilder toBuilder() =>
      new AssignmentOverrideBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is AssignmentOverride &&
        id == other.id &&
        assignmentId == other.assignmentId &&
        title == other.title &&
        dueAt == other.dueAt &&
        allDay == other.allDay &&
        allDayDate == other.allDayDate &&
        unlockAt == other.unlockAt &&
        lockAt == other.lockAt &&
        studentIds == other.studentIds;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, assignmentId.hashCode);
    _$hash = $jc(_$hash, title.hashCode);
    _$hash = $jc(_$hash, dueAt.hashCode);
    _$hash = $jc(_$hash, allDay.hashCode);
    _$hash = $jc(_$hash, allDayDate.hashCode);
    _$hash = $jc(_$hash, unlockAt.hashCode);
    _$hash = $jc(_$hash, lockAt.hashCode);
    _$hash = $jc(_$hash, studentIds.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'AssignmentOverride')
          ..add('id', id)
          ..add('assignmentId', assignmentId)
          ..add('title', title)
          ..add('dueAt', dueAt)
          ..add('allDay', allDay)
          ..add('allDayDate', allDayDate)
          ..add('unlockAt', unlockAt)
          ..add('lockAt', lockAt)
          ..add('studentIds', studentIds))
        .toString();
  }
}

class AssignmentOverrideBuilder
    implements Builder<AssignmentOverride, AssignmentOverrideBuilder> {
  _$AssignmentOverride? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _assignmentId;
  String? get assignmentId => _$this._assignmentId;
  set assignmentId(String? assignmentId) => _$this._assignmentId = assignmentId;

  String? _title;
  String? get title => _$this._title;
  set title(String? title) => _$this._title = title;

  DateTime? _dueAt;
  DateTime? get dueAt => _$this._dueAt;
  set dueAt(DateTime? dueAt) => _$this._dueAt = dueAt;

  bool? _allDay;
  bool? get allDay => _$this._allDay;
  set allDay(bool? allDay) => _$this._allDay = allDay;

  DateTime? _allDayDate;
  DateTime? get allDayDate => _$this._allDayDate;
  set allDayDate(DateTime? allDayDate) => _$this._allDayDate = allDayDate;

  DateTime? _unlockAt;
  DateTime? get unlockAt => _$this._unlockAt;
  set unlockAt(DateTime? unlockAt) => _$this._unlockAt = unlockAt;

  DateTime? _lockAt;
  DateTime? get lockAt => _$this._lockAt;
  set lockAt(DateTime? lockAt) => _$this._lockAt = lockAt;

  ListBuilder<String>? _studentIds;
  ListBuilder<String> get studentIds =>
      _$this._studentIds ??= new ListBuilder<String>();
  set studentIds(ListBuilder<String>? studentIds) =>
      _$this._studentIds = studentIds;

  AssignmentOverrideBuilder() {
    AssignmentOverride._initializeBuilder(this);
  }

  AssignmentOverrideBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _assignmentId = $v.assignmentId;
      _title = $v.title;
      _dueAt = $v.dueAt;
      _allDay = $v.allDay;
      _allDayDate = $v.allDayDate;
      _unlockAt = $v.unlockAt;
      _lockAt = $v.lockAt;
      _studentIds = $v.studentIds.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AssignmentOverride other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$AssignmentOverride;
  }

  @override
  void update(void Function(AssignmentOverrideBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  AssignmentOverride build() => _build();

  _$AssignmentOverride _build() {
    _$AssignmentOverride _$result;
    try {
      _$result = _$v ??
          new _$AssignmentOverride._(
              id: BuiltValueNullFieldError.checkNotNull(
                  id, r'AssignmentOverride', 'id'),
              assignmentId: BuiltValueNullFieldError.checkNotNull(
                  assignmentId, r'AssignmentOverride', 'assignmentId'),
              title: title,
              dueAt: dueAt,
              allDay: allDay,
              allDayDate: allDayDate,
              unlockAt: unlockAt,
              lockAt: lockAt,
              studentIds: studentIds.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'studentIds';
        studentIds.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'AssignmentOverride', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
