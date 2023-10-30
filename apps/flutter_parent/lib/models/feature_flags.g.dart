// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'feature_flags.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<FeatureFlags> _$featureFlagsSerializer =
    new _$FeatureFlagsSerializer();

class _$FeatureFlagsSerializer implements StructuredSerializer<FeatureFlags> {
  @override
  final Iterable<Type> types = const [FeatureFlags, _$FeatureFlags];
  @override
  final String wireName = 'FeatureFlags';

  @override
  Iterable<Object?> serialize(Serializers serializers, FeatureFlags object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'send_usage_metrics',
      serializers.serialize(object.sendUsageMetrics,
          specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  FeatureFlags deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new FeatureFlagsBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'send_usage_metrics':
          result.sendUsageMetrics = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$FeatureFlags extends FeatureFlags {
  @override
  final bool sendUsageMetrics;

  factory _$FeatureFlags([void Function(FeatureFlagsBuilder)? updates]) =>
      (new FeatureFlagsBuilder()..update(updates))._build();

  _$FeatureFlags._({required this.sendUsageMetrics}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        sendUsageMetrics, r'FeatureFlags', 'sendUsageMetrics');
  }

  @override
  FeatureFlags rebuild(void Function(FeatureFlagsBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  FeatureFlagsBuilder toBuilder() => new FeatureFlagsBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is FeatureFlags && sendUsageMetrics == other.sendUsageMetrics;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, sendUsageMetrics.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'FeatureFlags')
          ..add('sendUsageMetrics', sendUsageMetrics))
        .toString();
  }
}

class FeatureFlagsBuilder
    implements Builder<FeatureFlags, FeatureFlagsBuilder> {
  _$FeatureFlags? _$v;

  bool? _sendUsageMetrics;
  bool? get sendUsageMetrics => _$this._sendUsageMetrics;
  set sendUsageMetrics(bool? sendUsageMetrics) =>
      _$this._sendUsageMetrics = sendUsageMetrics;

  FeatureFlagsBuilder() {
    FeatureFlags._initializeBuilder(this);
  }

  FeatureFlagsBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _sendUsageMetrics = $v.sendUsageMetrics;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(FeatureFlags other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$FeatureFlags;
  }

  @override
  void update(void Function(FeatureFlagsBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  FeatureFlags build() => _build();

  _$FeatureFlags _build() {
    final _$result = _$v ??
        new _$FeatureFlags._(
            sendUsageMetrics: BuiltValueNullFieldError.checkNotNull(
                sendUsageMetrics, r'FeatureFlags', 'sendUsageMetrics'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
