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
  Iterable<Object?> serialize(Serializers serializers, Plannable object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'title',
      serializers.serialize(object.title,
          specifiedType: const FullType(String)),
    ];
    Object? value;
    value = object.pointsPossible;

    result
      ..add('points_possible')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(double)));
    value = object.dueAt;

    result
      ..add('due_at')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.assignmentId;

    result
      ..add('assignment_id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  Plannable deserialize(Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new PlannableBuilder();

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
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'points_possible':
          result.pointsPossible = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double?;
          break;
        case 'due_at':
          result.dueAt = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'assignment_id':
          result.assignmentId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
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
  final double? pointsPossible;
  @override
  final DateTime? dueAt;
  @override
  final String? assignmentId;

  factory _$Plannable([void Function(PlannableBuilder)? updates]) =>
      (new PlannableBuilder()..update(updates))._build();

  _$Plannable._(
      {required this.id,
      required this.title,
      this.pointsPossible,
      this.dueAt,
      this.assignmentId})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'Plannable', 'id');
    BuiltValueNullFieldError.checkNotNull(title, r'Plannable', 'title');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, title.hashCode);
    _$hash = $jc(_$hash, pointsPossible.hashCode);
    _$hash = $jc(_$hash, dueAt.hashCode);
    _$hash = $jc(_$hash, assignmentId.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'Plannable')
          ..add('id', id)
          ..add('title', title)
          ..add('pointsPossible', pointsPossible)
          ..add('dueAt', dueAt)
          ..add('assignmentId', assignmentId))
        .toString();
  }
}

class PlannableBuilder implements Builder<Plannable, PlannableBuilder> {
  _$Plannable? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _title;
  String? get title => _$this._title;
  set title(String? title) => _$this._title = title;

  double? _pointsPossible;
  double? get pointsPossible => _$this._pointsPossible;
  set pointsPossible(double? pointsPossible) =>
      _$this._pointsPossible = pointsPossible;

  DateTime? _dueAt;
  DateTime? get dueAt => _$this._dueAt;
  set dueAt(DateTime? dueAt) => _$this._dueAt = dueAt;

  String? _assignmentId;
  String? get assignmentId => _$this._assignmentId;
  set assignmentId(String? assignmentId) => _$this._assignmentId = assignmentId;

  PlannableBuilder();

  PlannableBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _title = $v.title;
      _pointsPossible = $v.pointsPossible;
      _dueAt = $v.dueAt;
      _assignmentId = $v.assignmentId;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(Plannable other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$Plannable;
  }

  @override
  void update(void Function(PlannableBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  Plannable build() => _build();

  _$Plannable _build() {
    final _$result = _$v ??
        new _$Plannable._(
            id: BuiltValueNullFieldError.checkNotNull(id, r'Plannable', 'id'),
            title: BuiltValueNullFieldError.checkNotNull(
                title, r'Plannable', 'title'),
            pointsPossible: pointsPossible,
            dueAt: dueAt,
            assignmentId: assignmentId);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
