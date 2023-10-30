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
  Iterable<Object?> serialize(
      Serializers serializers, GradingPeriodResponse object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'grading_periods',
      serializers.serialize(object.gradingPeriods,
          specifiedType:
              const FullType(BuiltList, const [const FullType(GradingPeriod)])),
    ];

    return result;
  }

  @override
  GradingPeriodResponse deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new GradingPeriodResponseBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'grading_periods':
          result.gradingPeriods.replace(serializers.deserialize(value,
                  specifiedType: const FullType(
                      BuiltList, const [const FullType(GradingPeriod)]))!
              as BuiltList<Object?>);
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
          [void Function(GradingPeriodResponseBuilder)? updates]) =>
      (new GradingPeriodResponseBuilder()..update(updates))._build();

  _$GradingPeriodResponse._({required this.gradingPeriods}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        gradingPeriods, r'GradingPeriodResponse', 'gradingPeriods');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, gradingPeriods.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'GradingPeriodResponse')
          ..add('gradingPeriods', gradingPeriods))
        .toString();
  }
}

class GradingPeriodResponseBuilder
    implements Builder<GradingPeriodResponse, GradingPeriodResponseBuilder> {
  _$GradingPeriodResponse? _$v;

  ListBuilder<GradingPeriod>? _gradingPeriods;
  ListBuilder<GradingPeriod> get gradingPeriods =>
      _$this._gradingPeriods ??= new ListBuilder<GradingPeriod>();
  set gradingPeriods(ListBuilder<GradingPeriod>? gradingPeriods) =>
      _$this._gradingPeriods = gradingPeriods;

  GradingPeriodResponseBuilder();

  GradingPeriodResponseBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _gradingPeriods = $v.gradingPeriods.toBuilder();
      _$v = null;
    }
    return this;
  }

  @override
  void replace(GradingPeriodResponse other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$GradingPeriodResponse;
  }

  @override
  void update(void Function(GradingPeriodResponseBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  GradingPeriodResponse build() => _build();

  _$GradingPeriodResponse _build() {
    _$GradingPeriodResponse _$result;
    try {
      _$result = _$v ??
          new _$GradingPeriodResponse._(gradingPeriods: gradingPeriods.build());
    } catch (_) {
      late String _$failedField;
      try {
        _$failedField = 'gradingPeriods';
        gradingPeriods.build();
      } catch (e) {
        throw new BuiltValueNestedFieldError(
            r'GradingPeriodResponse', _$failedField, e.toString());
      }
      rethrow;
    }
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
