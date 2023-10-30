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
  Iterable<Object?> serialize(Serializers serializers, AssignmentGroup object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
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
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AssignmentGroupBuilder();

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
        case 'position':
          result.position = serializers.deserialize(value,
              specifiedType: const FullType(int))! as int;
          break;
        case 'group_weight':
          result.groupWeight = serializers.deserialize(value,
              specifiedType: const FullType(double))! as double;
          break;
        case 'assignments':
          result.assignments.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(Assignment)]))!
              as BuiltList<Object?>);
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

  factory _$AssignmentGroup([void Function(AssignmentGroupBuilder)? updates]) =>
      (new AssignmentGroupBuilder()..update(updates))._build();

  _$AssignmentGroup._(
      {required this.id,
      required this.name,
      required this.position,
      required this.groupWeight,
      required this.assignments})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'AssignmentGroup', 'id');
    BuiltValueNullFieldError.checkNotNull(name, r'AssignmentGroup', 'name');
    BuiltValueNullFieldError.checkNotNull(
        position, r'AssignmentGroup', 'position');
    BuiltValueNullFieldError.checkNotNull(
        groupWeight, r'AssignmentGroup', 'groupWeight');
    BuiltValueNullFieldError.checkNotNull(
        assignments, r'AssignmentGroup', 'assignments');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, name.hashCode);
    _$hash = $jc(_$hash, position.hashCode);
    _$hash = $jc(_$hash, groupWeight.hashCode);
    _$hash = $jc(_$hash, assignments.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'AssignmentGroup')
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
  _$AssignmentGroup? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _name;
  String? get name => _$this._name;
  set name(String? name) => _$this._name = name;

  int? _position;
  int? get position => _$this._position;
  set position(int? position) => _$this._position = position;

  double? _groupWeight;
  double? get groupWeight => _$this._groupWeight;
  set groupWeight(double? groupWeight) => _$this._groupWeight = groupWeight;

  ListBuilder<Assignment>? _assignments;
  ListBuilder<Assignment> get assignments =>
      _$this._assignments ??= new ListBuilder<Assignment>();
  set assignments(ListBuilder<Assignment>? assignments) =>
      _$this._assignments = assignments;

  AssignmentGroupBuilder() {
    AssignmentGroup._initializeBuilder(this);
  }

  AssignmentGroupBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _name = $v.name;
      _position = $v.position;
      _groupWeight = $v.groupWeight;
      _assignments = $v.assignments.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AssignmentGroup other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$AssignmentGroup;
  }

  @override
  void update(void Function(AssignmentGroupBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  AssignmentGroup build() => _build();

  _$AssignmentGroup _build() {
    _$AssignmentGroup _$result;
    try {
      _$result = _$v ??
          new _$AssignmentGroup._(
              id: BuiltValueNullFieldError.checkNotNull(
                  id, r'AssignmentGroup', 'id'),
              name: BuiltValueNullFieldError.checkNotNull(
                  name, r'AssignmentGroup', 'name'),
              position: BuiltValueNullFieldError.checkNotNull(
                  position, r'AssignmentGroup', 'position'),
              groupWeight: BuiltValueNullFieldError.checkNotNull(
                  groupWeight, r'AssignmentGroup', 'groupWeight'),
              assignments: assignments.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'assignments';
        assignments.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'AssignmentGroup', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
