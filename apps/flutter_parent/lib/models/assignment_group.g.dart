// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assignment_group.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<AssignmentGroup> _$assignmentGroupSerializer =
    new _$AssignmentGroupSerializer();

class _$AssignmentGroupSerializer
    implements StructuredSerializer<AssignmentGroup> {
  @override
  final Iterable<Type> types = const [AssignmentGroup, _$AssignmentGroup];
  @override
  final String wireName = 'AssignmentGroup';

  @override
  Iterable<Object> serialize(Serializers serializers, AssignmentGroup object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'name',
      serializers.serialize(object.name, specifiedType: const FullType(String)),
      'position',
      serializers.serialize(object.position,
          specifiedType: const FullType(int)),
      'group_weight',
      serializers.serialize(object.groupWeight,
          specifiedType: const FullType(double)),
      'assignments',
      serializers.serialize(object.assignments,
          specifiedType:
              const FullType(BuiltList, const [const FullType(Assignment)])),
    ];

    return result;
  }

  @override
  AssignmentGroup deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AssignmentGroupBuilder();

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
        case 'position':
          result.position = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
        case 'group_weight':
          result.groupWeight = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double;
          break;
        case 'assignments':
          result.assignments.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Assignment)]))
              as BuiltList<Object>);
          break;
      }
    }

    return result.build();
  }
}

class _$AssignmentGroup extends AssignmentGroup {
  @override
  final String id;
  @override
  final String name;
  @override
  final int position;
  @override
  final double groupWeight;
  @override
  final BuiltList<Assignment> assignments;

  factory _$AssignmentGroup([void Function(AssignmentGroupBuilder) updates]) =>
      (new AssignmentGroupBuilder()..update(updates)).build();

  _$AssignmentGroup._(
      {this.id, this.name, this.position, this.groupWeight, this.assignments})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('AssignmentGroup', 'id');
    }
    if (name == null) {
      throw new BuiltValueNullFieldError('AssignmentGroup', 'name');
    }
    if (position == null) {
      throw new BuiltValueNullFieldError('AssignmentGroup', 'position');
    }
    if (groupWeight == null) {
      throw new BuiltValueNullFieldError('AssignmentGroup', 'groupWeight');
    }
    if (assignments == null) {
      throw new BuiltValueNullFieldError('AssignmentGroup', 'assignments');
    }
  }

  @override
  AssignmentGroup rebuild(void Function(AssignmentGroupBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AssignmentGroupBuilder toBuilder() =>
      new AssignmentGroupBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is AssignmentGroup &&
        id == other.id &&
        name == other.name &&
        position == other.position &&
        groupWeight == other.groupWeight &&
        assignments == other.assignments;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc($jc($jc($jc(0, id.hashCode), name.hashCode), position.hashCode),
            groupWeight.hashCode),
        assignments.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('AssignmentGroup')
          ..add('id', id)
          ..add('name', name)
          ..add('position', position)
          ..add('groupWeight', groupWeight)
          ..add('assignments', assignments))
        .toString();
  }
}

class AssignmentGroupBuilder
    implements Builder<AssignmentGroup, AssignmentGroupBuilder> {
  _$AssignmentGroup _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _name;
  String get name => _$this._name;
  set name(String name) => _$this._name = name;

  int _position;
  int get position => _$this._position;
  set position(int position) => _$this._position = position;

  double _groupWeight;
  double get groupWeight => _$this._groupWeight;
  set groupWeight(double groupWeight) => _$this._groupWeight = groupWeight;

  ListBuilder<Assignment> _assignments;
  ListBuilder<Assignment> get assignments =>
      _$this._assignments ??= new ListBuilder<Assignment>();
  set assignments(ListBuilder<Assignment> assignments) =>
      _$this._assignments = assignments;

  AssignmentGroupBuilder();

  AssignmentGroupBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _name = _$v.name;
      _position = _$v.position;
      _groupWeight = _$v.groupWeight;
      _assignments = _$v.assignments?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AssignmentGroup other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$AssignmentGroup;
  }

  @override
  void update(void Function(AssignmentGroupBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$AssignmentGroup build() {
    _$AssignmentGroup _$result;
    try {
      _$result = _$v ??
          new _$AssignmentGroup._(
              id: id,
              name: name,
              position: position,
              groupWeight: groupWeight,
              assignments: assignments.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'assignments';
        assignments.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'AssignmentGroup', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
