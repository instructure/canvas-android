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
    result.add('points_possible');
    if (object.pointsPossible == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.pointsPossible,
          specifiedType: const FullType(double)));
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
        case 'points_possible':
          result.pointsPossible = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'due_at':
          result.dueAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'assignment_id':
          result.assignmentId = serializers.deserialize(value,
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
  final double pointsPossible;
  @override
  final DateTime dueAt;
  @override
  final String assignmentId;

  factory _$Plannable([void Function(PlannableBuilder) updates]) =>
      (new PlannableBuilder()..update(updates)).build();

  _$Plannable._(
      {this.id, this.title, this.pointsPossible, this.dueAt, this.assignmentId})
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
        pointsPossible == other.pointsPossible &&
        dueAt == other.dueAt &&
        assignmentId == other.assignmentId;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc($jc($jc(0, id.hashCode), title.hashCode),
                pointsPossible.hashCode),
            dueAt.hashCode),
        assignmentId.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('Plannable')
          ..add('id', id)
          ..add('title', title)
          ..add('pointsPossible', pointsPossible)
          ..add('dueAt', dueAt)
          ..add('assignmentId', assignmentId))
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

  double _pointsPossible;
  double get pointsPossible => _$this._pointsPossible;
  set pointsPossible(double pointsPossible) =>
      _$this._pointsPossible = pointsPossible;

  DateTime _dueAt;
  DateTime get dueAt => _$this._dueAt;
  set dueAt(DateTime dueAt) => _$this._dueAt = dueAt;

  String _assignmentId;
  String get assignmentId => _$this._assignmentId;
  set assignmentId(String assignmentId) => _$this._assignmentId = assignmentId;

  PlannableBuilder();

  PlannableBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _title = _$v.title;
      _pointsPossible = _$v.pointsPossible;
      _dueAt = _$v.dueAt;
      _assignmentId = _$v.assignmentId;
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
            pointsPossible: pointsPossible,
            dueAt: dueAt,
            assignmentId: assignmentId);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
