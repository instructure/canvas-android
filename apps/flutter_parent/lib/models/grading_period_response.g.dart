// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'grading_period_response.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<GradingPeriodResponse> _$gradingPeriodResponseSerializer =
    new _$GradingPeriodResponseSerializer();

class _$GradingPeriodResponseSerializer
    implements StructuredSerializer<GradingPeriodResponse> {
  @override
  final Iterable<Type> types = const [
    GradingPeriodResponse,
    _$GradingPeriodResponse
  ];
  @override
  final String wireName = 'GradingPeriodResponse';

  @override
  Iterable<Object> serialize(
      Serializers serializers, GradingPeriodResponse object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'grading_periods',
      serializers.serialize(object.gradingPeriods,
          specifiedType:
              const FullType(BuiltList, const [const FullType(GradingPeriod)])),
    ];

    return result;
  }

  @override
  GradingPeriodResponse deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradingPeriodResponseBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'grading_periods':
          result.gradingPeriods.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(GradingPeriod)]))
              as BuiltList<Object>);
          break;
      }
    }

    return result.build();
  }
}

class _$GradingPeriodResponse extends GradingPeriodResponse {
  @override
  final BuiltList<GradingPeriod> gradingPeriods;

  factory _$GradingPeriodResponse(
          [void Function(GradingPeriodResponseBuilder) updates]) =>
      (new GradingPeriodResponseBuilder()..update(updates)).build();

  _$GradingPeriodResponse._({this.gradingPeriods}) : super._() {
    if (gradingPeriods == null) {
      throw new BuiltValueNullFieldError(
          'GradingPeriodResponse', 'gradingPeriods');
    }
  }

  @override
  GradingPeriodResponse rebuild(
          void Function(GradingPeriodResponseBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  GradingPeriodResponseBuilder toBuilder() =>
      new GradingPeriodResponseBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is GradingPeriodResponse &&
        gradingPeriods == other.gradingPeriods;
  }

  @override
  int get hashCode {
    return $jf($jc(0, gradingPeriods.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('GradingPeriodResponse')
          ..add('gradingPeriods', gradingPeriods))
        .toString();
  }
}

class GradingPeriodResponseBuilder
    implements Builder<GradingPeriodResponse, GradingPeriodResponseBuilder> {
  _$GradingPeriodResponse _$v;

  ListBuilder<GradingPeriod> _gradingPeriods;
  ListBuilder<GradingPeriod> get gradingPeriods =>
      _$this._gradingPeriods ??= new ListBuilder<GradingPeriod>();
  set gradingPeriods(ListBuilder<GradingPeriod> gradingPeriods) =>
      _$this._gradingPeriods = gradingPeriods;

  GradingPeriodResponseBuilder();

  GradingPeriodResponseBuilder get _$this {
    if (_$v != null) {
      _gradingPeriods = _$v.gradingPeriods?.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradingPeriodResponse other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$GradingPeriodResponse;
  }

  @override
  void update(void Function(GradingPeriodResponseBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$GradingPeriodResponse build() {
    _$GradingPeriodResponse _$result;
    try {
      _$result = _$v ??
          new _$GradingPeriodResponse._(gradingPeriods: gradingPeriods.build());
    } catch (_) {
      String _$failedField;
      try {
        _$failedField = 'gradingPeriods';
        gradingPeriods.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            'GradingPeriodResponse', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
