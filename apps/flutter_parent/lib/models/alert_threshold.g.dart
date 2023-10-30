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
  Iterable<Object?> serialize(Serializers serializers, AlertThreshold object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
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
    Object? value;
    value = object.threshold;

    result
      ..add('threshold')
      ..add(
          serializers.serialize(value, specifiedType: const FullType(String)));

    return result;
  }

  @override
  AlertThreshold deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new AlertThresholdBuilder();

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
        case 'alert_type':
          result.alertType = serializers.deserialize(value,
              specifiedType: const FullType(AlertType))! as AlertType;
          break;
        case 'threshold':
          result.threshold = serializers.deserialize(value,
              specifiedType: const FullType(String)) as String?;
          break;
        case 'user_id':
          result.userId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
          break;
        case 'observer_id':
          result.observerId = serializers.deserialize(value,
              specifiedType: const FullType(String))! as String;
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
  final String? threshold;
  @override
  final String userId;
  @override
  final String observerId;

  factory _$AlertThreshold([void Function(AlertThresholdBuilder)? updates]) =>
      (new AlertThresholdBuilder()..update(updates))._build();

  _$AlertThreshold._(
      {required this.id,
      required this.alertType,
      this.threshold,
      required this.userId,
      required this.observerId})
      : super._() {
    BuiltValueNullFieldError.checkNotNull(id, r'AlertThreshold', 'id');
    BuiltValueNullFieldError.checkNotNull(
        alertType, r'AlertThreshold', 'alertType');
    BuiltValueNullFieldError.checkNotNull(userId, r'AlertThreshold', 'userId');
    BuiltValueNullFieldError.checkNotNull(
        observerId, r'AlertThreshold', 'observerId');
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
    var _$hash = 0;
    _$hash = $jc(_$hash, id.hashCode);
    _$hash = $jc(_$hash, alertType.hashCode);
    _$hash = $jc(_$hash, threshold.hashCode);
    _$hash = $jc(_$hash, userId.hashCode);
    _$hash = $jc(_$hash, observerId.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'AlertThreshold')
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
  _$AlertThreshold? _$v;

  String? _id;
  String? get id => _$this._id;
  set id(String? id) => _$this._id = id;

  AlertType? _alertType;
  AlertType? get alertType => _$this._alertType;
  set alertType(AlertType? alertType) => _$this._alertType = alertType;

  String? _threshold;
  String? get threshold => _$this._threshold;
  set threshold(String? threshold) => _$this._threshold = threshold;

  String? _userId;
  String? get userId => _$this._userId;
  set userId(String? userId) => _$this._userId = userId;

  String? _observerId;
  String? get observerId => _$this._observerId;
  set observerId(String? observerId) => _$this._observerId = observerId;

  AlertThresholdBuilder() {
    AlertThreshold._initializeBuilder(this);
  }

  AlertThresholdBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _id = $v.id;
      _alertType = $v.alertType;
      _threshold = $v.threshold;
      _userId = $v.userId;
      _observerId = $v.observerId;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(AlertThreshold other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$AlertThreshold;
  }

  @override
  void update(void Function(AlertThresholdBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  AlertThreshold build() => _build();

  _$AlertThreshold _build() {
    final _$result = _$v ??
        new _$AlertThreshold._(
            id: BuiltValueNullFieldError.checkNotNull(
                id, r'AlertThreshold', 'id'),
            alertType: BuiltValueNullFieldError.checkNotNull(
                alertType, r'AlertThreshold', 'alertType'),
            threshold: threshold,
            userId: BuiltValueNullFieldError.checkNotNull(
                userId, r'AlertThreshold', 'userId'),
            observerId: BuiltValueNullFieldError.checkNotNull(
                observerId, r'AlertThreshold', 'observerId'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
