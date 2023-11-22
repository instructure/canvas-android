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
  Iterable<Object?> serialize(Serializers serializers, GradingPeriod object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[];
    Object? value;
    value = object.id;

    result
      ..add('id')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.title;

    result
      ..add('title')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));
    value = object.startDate;

    result
      ..add('start_date')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.endDate;

    result
      ..add('end_date')
      ..add(serializers.serialize(value,
          specifiedType: const FullType(DateTime)));
    value = object.weight;

    result
      ..add('weight')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(double)));

    return result;
  }

  @override
  GradingPeriod deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradingPeriodBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'id':
          result.id = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'title':
          result.title = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'start_date':
          result.startDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'end_date':
          result.endDate = serializers.deserialize(value,
              specifiedType: const FullType(DateTime)) as DateTime?;
          break;
        case 'weight':
          result.weight = serializers.deserialize(value,
              specifiedType: const FullType(double)) as double?;
          break;
      }
    }

    return result.build();
  }
}

class _$GradingPeriod extends GradingPeriod {
  @override
  final String? id;
  @override
  final String? title;
  @override
  final DateTime? startDate;
  @override
  final DateTime? endDate;
  @override
  final double? weight;

  factory _$GradingPeriod([void Function(GradingPeriodBuilder)? updates]) =>
      (new GradingPeriodBuilder()..update(updates))._build();

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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, title.hashCode);
    _$hash = $jc(_$hash, startDate.hashCode);
    _$hash = $jc(_$hash, endDate.hashCode);
    _$hash = $jc(_$hash, weight.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'GradingPeriod')
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
  _$GradingPeriod? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  String? _title;
  String? get title => _$this._title;
  set title(String? title) => _$this._title = title;

  DateTime? _startDate;
  DateTime? get startDate => _$this._startDate;
  set startDate(DateTime? startDate) => _$this._startDate = startDate;

  DateTime? _endDate;
  DateTime? get endDate => _$this._endDate;
  set endDate(DateTime? endDate) => _$this._endDate = endDate;

  double? _weight;
  double? get weight => _$this._weight;
  set weight(double? weight) => _$this._weight = weight;

  GradingPeriodBuilder();

  GradingPeriodBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _title = $v.title;
      _startDate = $v.startDate;
      _endDate = $v.endDate;
      _weight = $v.weight;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradingPeriod other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$GradingPeriod;
  }

  @override
  void update(void Function(GradingPeriodBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  GradingPeriod build() => _build();

  _$GradingPeriod _build() {
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

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
