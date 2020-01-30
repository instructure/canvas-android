// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'grading_period.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<GradingPeriod> _$gradingPeriodSerializer =
    new _$GradingPeriodSerializer();

class _$GradingPeriodSerializer implements StructuredSerializer<GradingPeriod> {
  @override
  final Iterable<Type> types = const [GradingPeriod, _$GradingPeriod];
  @override
  final String wireName = 'GradingPeriod';

  @override
  Iterable<Object> serialize(Serializers serializers, GradingPeriod object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[];
    result.add('id');
    if (object.id == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.id,
          specifiedType: const FullType(String)));
    }
    result.add('title');
    if (object.title == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.title,
          specifiedType: const FullType(String)));
    }
    result.add('start_date');
    if (object.startDate == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.startDate,
          specifiedType: const FullType(DateTime)));
    }
    result.add('end_date');
    if (object.endDate == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.endDate,
          specifiedType: const FullType(DateTime)));
    }
    result.add('weight');
    if (object.weight == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.weight,
          specifiedType: const FullType(int)));
    }
    return result;
  }

  @override
  GradingPeriod deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradingPeriodBuilder();

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
        case 'start_date':
          result.startDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'end_date':
          result.endDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime;
          break;
        case 'weight':
          result.weight = serializers.deserialize(value,
              specifiedType: const FullType(int)) as int;
          break;
      }
    }

    return result.build();
  }
}

class _$GradingPeriod extends GradingPeriod {
  @override
  final String id;
  @override
  final String title;
  @override
  final DateTime startDate;
  @override
  final DateTime endDate;
  @override
  final int weight;

  factory _$GradingPeriod([void Function(GradingPeriodBuilder) updates]) =>
      (new GradingPeriodBuilder()..update(updates)).build();

  _$GradingPeriod._(
      {this.id, this.title, this.startDate, this.endDate, this.weight})
      : super._();

  @override
  GradingPeriod rebuild(void Function(GradingPeriodBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  GradingPeriodBuilder toBuilder() => new GradingPeriodBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is GradingPeriod &&
        id == other.id &&
        title == other.title &&
        startDate == other.startDate &&
        endDate == other.endDate &&
        weight == other.weight;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc($jc($jc($jc(0, id.hashCode), title.hashCode), startDate.hashCode),
            endDate.hashCode),
        weight.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('GradingPeriod')
          ..add('id', id)
          ..add('title', title)
          ..add('startDate', startDate)
          ..add('endDate', endDate)
          ..add('weight', weight))
        .toString();
  }
}

class GradingPeriodBuilder
    implements Builder<GradingPeriod, GradingPeriodBuilder> {
  _$GradingPeriod _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  String _title;
  String get title => _$this._title;
  set title(String title) => _$this._title = title;

  DateTime _startDate;
  DateTime get startDate => _$this._startDate;
  set startDate(DateTime startDate) => _$this._startDate = startDate;

  DateTime _endDate;
  DateTime get endDate => _$this._endDate;
  set endDate(DateTime endDate) => _$this._endDate = endDate;

  int _weight;
  int get weight => _$this._weight;
  set weight(int weight) => _$this._weight = weight;

  GradingPeriodBuilder();

  GradingPeriodBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _title = _$v.title;
      _startDate = _$v.startDate;
      _endDate = _$v.endDate;
      _weight = _$v.weight;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradingPeriod other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$GradingPeriod;
  }

  @override
  void update(void Function(GradingPeriodBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$GradingPeriod build() {
    final _$result = _$v ??
        new _$GradingPeriod._(
            id: id,
            title: title,
            startDate: startDate,
            endDate: endDate,
            weight: weight);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
