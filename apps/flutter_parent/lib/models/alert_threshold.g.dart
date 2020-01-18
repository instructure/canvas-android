// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_threshold.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<AlertThreshold> _$alertThresholdSerializer =
    new _$AlertThresholdSerializer();

class _$AlertThresholdSerializer
    implements StructuredSerializer<AlertThreshold> {
  @override
  final Iterable<Type> types = const [AlertThreshold, _$AlertThreshold];
  @override
  final String wireName = 'AlertThreshold';

  @override
  Iterable<Object> serialize(Serializers serializers, AlertThreshold object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'id',
      serializers.serialize(object.id, specifiedType: const FullType(String)),
      'alert_type',
      serializers.serialize(object.alertType,
          specifiedType: const FullType(AlertType)),
      'user_id',
      serializers.serialize(object.userId,
          specifiedType: const FullType(String)),
      'observer_id',
      serializers.serialize(object.observerId,
          specifiedType: const FullType(String)),
    ];
    result.add('threshold');
    if (object.threshold == null) {
      result.add(null);
    } else {
      result.add(serializers.serialize(object.threshold,
          specifiedType: const FullType(String)));
    }
    return result;
  }

  @override
  AlertThreshold deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AlertThresholdBuilder();

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
        case 'alert_type':
          result.alertType = serializers.deserialize(value,
              specifiedType: const FullType(AlertType)) as AlertType;
          break;
        case 'threshold':
          result.threshold = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
        case 'observer_id':
          result.observerId = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String;
          break;
      }
    }

    return result.build();
  }
}

class _$AlertThreshold extends AlertThreshold {
  @override
  final String id;
  @override
  final AlertType alertType;
  @override
  final String threshold;
  @override
  final String userId;
  @override
  final String observerId;

  factory _$AlertThreshold([void Function(AlertThresholdBuilder) updates]) =>
      (new AlertThresholdBuilder()..update(updates)).build();

  _$AlertThreshold._(
      {this.id, this.alertType, this.threshold, this.userId, this.observerId})
      : super._() {
    if (id == null) {
      throw new BuiltValueNullFieldError('AlertThreshold', 'id');
    }
    if (alertType == null) {
      throw new BuiltValueNullFieldError('AlertThreshold', 'alertType');
    }
    if (userId == null) {
      throw new BuiltValueNullFieldError('AlertThreshold', 'userId');
    }
    if (observerId == null) {
      throw new BuiltValueNullFieldError('AlertThreshold', 'observerId');
    }
  }

  @override
  AlertThreshold rebuild(void Function(AlertThresholdBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  AlertThresholdBuilder toBuilder() =>
      new AlertThresholdBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is AlertThreshold &&
        id == other.id &&
        alertType == other.alertType &&
        threshold == other.threshold &&
        userId == other.userId &&
        observerId == other.observerId;
  }

  @override
  int get hashCode {
    return $jf($jc(
        $jc(
            $jc($jc($jc(0, id.hashCode), alertType.hashCode),
                threshold.hashCode),
            userId.hashCode),
        observerId.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('AlertThreshold')
          ..add('id', id)
          ..add('alertType', alertType)
          ..add('threshold', threshold)
          ..add('userId', userId)
          ..add('observerId', observerId))
        .toString();
  }
}

class AlertThresholdBuilder
    implements Builder<AlertThreshold, AlertThresholdBuilder> {
  _$AlertThreshold _$v;

  String _id;
  String get id => _$this._id;
  set id(String id) => _$this._id = id;

  AlertType _alertType;
  AlertType get alertType => _$this._alertType;
  set alertType(AlertType alertType) => _$this._alertType = alertType;

  String _threshold;
  String get threshold => _$this._threshold;
  set threshold(String threshold) => _$this._threshold = threshold;

  String _userId;
  String get userId => _$this._userId;
  set userId(String userId) => _$this._userId = userId;

  String _observerId;
  String get observerId => _$this._observerId;
  set observerId(String observerId) => _$this._observerId = observerId;

  AlertThresholdBuilder() {
    AlertThreshold._initializeBuilder(this);
  }

  AlertThresholdBuilder get _$this {
    if (_$v != null) {
      _id = _$v.id;
      _alertType = _$v.alertType;
      _threshold = _$v.threshold;
      _userId = _$v.userId;
      _observerId = _$v.observerId;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AlertThreshold other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$AlertThreshold;
  }

  @override
  void update(void Function(AlertThresholdBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$AlertThreshold build() {
    final _$result = _$v ??
        new _$AlertThreshold._(
            id: id,
            alertType: alertType,
            threshold: threshold,
            userId: userId,
            observerId: observerId);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
