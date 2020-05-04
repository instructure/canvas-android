// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'plannable.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<Plannable> _$plannableSerializer = new _$PlannableSerializer();

class _$PlannableSerializer implements StructuredSerializer<Plannable> {
  @override
  final Iterable<Type> types = const [Plannable, _$Plannable];
  @override
  final String wireName = 'Plannable';

  @override
  Iterable<Object> serialize(Serializers serializers, Plannable object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'title',
      serializers.serialize(object.title,
          specifiedType: const FullType(String)),
    ];
    result.add('course_id');
    if (object.courseId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.courseId,
          specifiedType: const FullType(String)));
    }
    result.add('group_id');
    if (object.groupId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.groupId,
          specifiedType: const FullType(String)));
    }
    result.add('user_id');
    if (object.userId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.userId,
          specifiedType: const FullType(String)));
    }
    result.add('points_possible');
    if (object.pointsPossible == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.pointsPossible,
          specifiedType: const FullType(double)));
    }
    result.add('todo_date');
    if (object.toDoDate == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.toDoDate,
          specifiedType: const FullType(DateTime)));
    }
    result.add('due_at');
    if (object.dueAt == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.dueAt,
          specifiedType: const FullType(DateTime)));
    }
    result.add('assignment_id');
    if (object.assignmentId == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.assignmentId,
          specifiedType: const FullType(String)));
    }
    result.add('details');
    if (object.details == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.details,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  Plannable deserialize(Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PlannableBuilder();

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
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'course_id':
          result.courseId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'group_id':
          result.groupId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'points_possible':
          result.pointsPossible = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'todo_date':
          result.toDoDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'due_at':
          result.dueAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'assignment_id':
          result.assignmentId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'details':
          result.details = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$Plannable extends Plannable {
  @override
  final String id;
  @override
  final String title;
  @override
  final String courseId;
  @override
  final String groupId;
  @override
  final String userId;
  @override
  final double pointsPossible;
  @override
  final DateTime toDoDate;
  @override
  final DateTime dueAt;
  @override
  final String assignmentId;
  @override
  final String details;

  factory _$Plannable([void Function(PlannableBuilder) updates]) =>
      (new PlannableBuilder()..update(updates)).build();

  _$Plannable._(
      {this.id,
      this.title,
      this.courseId,
      this.groupId,
      this.userId,
      this.pointsPossible,
      this.toDoDate,
      this.dueAt,
      this.assignmentId,
      this.details})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('Plannable', 'id');
    }
    if (title == null) {
      throw new BuiltValueNullFieldError('Plannable', 'title');
    }
  }

  @override
  Plannable rebuild(void Function(PlannableBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  PlannableBuilder toBuilder() => new PlannableBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is Plannable &&
        id == other.id &&
        title == other.title &&
        courseId == other.courseId &&
        groupId == other.groupId &&
        userId == other.userId &&
        pointsPossible == other.pointsPossible &&
        toDoDate == other.toDoDate &&
        dueAt == other.dueAt &&
        assignmentId == other.assignmentId &&
        details == other.details;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc(
                $jc(
                    $jc(
                        $jc(
                            $jc(
                                $jc($jc($jc(0, id.hashCode), title.hashCode),
                                    courseId.hashCode),
                                groupId.hashCode),
                            userId.hashCode),
                        pointsPossible.hashCode),
                    toDoDate.hashCode),
                dueAt.hashCode),
            assignmentId.hashCode),
        details.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Plannable')
          ..add('id', id)
          ..add('title', title)
          ..add('courseId', courseId)
          ..add('groupId', groupId)
          ..add('userId', userId)
          ..add('pointsPossible', pointsPossible)
          ..add('toDoDate', toDoDate)
          ..add('dueAt', dueAt)
          ..add('assignmentId', assignmentId)
          ..add('details', details))
        .toString();
  }
}

class PlannableBuilder implements Builder<Plannable, PlannableBuilder> {
  _$Plannable _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _title;
  String get title => _$this._title;
  set title(String title) => _$this._title = title;

  String _courseId;
  String get courseId => _$this._courseId;
  set courseId(String courseId) => _$this._courseId = courseId;

  String _groupId;
  String get groupId => _$this._groupId;
  set groupId(String groupId) => _$this._groupId = groupId;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  double _pointsPossible;
  double get pointsPossible => _$this._pointsPossible;
  set pointsPossible(double pointsPossible) =>
      _$this._pointsPossible = pointsPossible;

  DateTime _toDoDate;
  DateTime get toDoDate => _$this._toDoDate;
  set toDoDate(DateTime toDoDate) => _$this._toDoDate = toDoDate;

  DateTime _dueAt;
  DateTime get dueAt => _$this._dueAt;
  set dueAt(DateTime dueAt) => _$this._dueAt = dueAt;

  String _assignmentId;
  String get assignmentId => _$this._assignmentId;
  set assignmentId(String assignmentId) => _$this._assignmentId = assignmentId;

  String _details;
  String get details => _$this._details;
  set details(String details) => _$this._details = details;

  PlannableBuilder();

  PlannableBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _title = _$v.title;
      _courseId = _$v.courseId;
      _groupId = _$v.groupId;
      _userId = _$v.userId;
      _pointsPossible = _$v.pointsPossible;
      _toDoDate = _$v.toDoDate;
      _dueAt = _$v.dueAt;
      _assignmentId = _$v.assignmentId;
      _details = _$v.details;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Plannable other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$Plannable;
  }

  @override
  void update(void Function(PlannableBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$Plannable build() {
    final _$result = _$v ??
        new _$Plannable._(
            id: id,
            title: title,
            courseId: courseId,
            groupId: groupId,
            userId: userId,
            pointsPossible: pointsPossible,
            toDoDate: toDoDate,
            dueAt: dueAt,
            assignmentId: assignmentId,
            details: details);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
