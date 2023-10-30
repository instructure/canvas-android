// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'communication_channel.dart';

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializer<CommunicationChannel> _$communicationChannelSerializer =
    new _$CommunicationChannelSerializer();

class _$CommunicationChannelSerializer
    implements StructuredSerializer<CommunicationChannel> {
  @override
  final Iterable<Type> types = const [
    CommunicationChannel,
    _$CommunicationChannel
  ];
  @override
  final String wireName = 'CommunicationChannel';

  @override
  Iterable<Object?> serialize(
      Serializers serializers, CommunicationChannel object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object?>[
      'skip_confirmation',
      serializers.serialize(object.skipConfirmation,
          specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  CommunicationChannel deserialize(
      Serializers serializers, Iterable<Object?> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CommunicationChannelBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current! as String;
      iterator.moveNext();
      final Object? value = iterator.current;
      switch (key) {
        case 'skip_confirmation':
          result.skipConfirmation = serializers.deserialize(value,
              specifiedType: const FullType(bool))! as bool;
          break;
      }
    }

    return result.build();
  }
}

class _$CommunicationChannel extends CommunicationChannel {
  @override
  final bool skipConfirmation;

  factory _$CommunicationChannel(
          [void Function(CommunicationChannelBuilder)? updates]) =>
      (new CommunicationChannelBuilder()..update(updates))._build();

  _$CommunicationChannel._({required this.skipConfirmation}) : super._() {
    BuiltValueNullFieldError.checkNotNull(
        skipConfirmation, r'CommunicationChannel', 'skipConfirmation');
  }

  @override
  CommunicationChannel rebuild(
          void Function(CommunicationChannelBuilder) updates) =>
      (toBuilder()..update(updates)).build();

  @override
  CommunicationChannelBuilder toBuilder() =>
      new CommunicationChannelBuilder()..replace(this);

  @override
  bool operator ==(Object other) {
    if (identical(other, this)) return true;
    return other is CommunicationChannel &&
        skipConfirmation == other.skipConfirmation;
  }

  @override
  int get hashCode {
    var _$hash = 0;
    _$hash = $jc(_$hash, skipConfirmation.hashCode);
    _$hash = $jf(_$hash);
    return _$hash;
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper(r'CommunicationChannel')
          ..add('skipConfirmation', skipConfirmation))
        .toString();
  }
}

class CommunicationChannelBuilder
    implements Builder<CommunicationChannel, CommunicationChannelBuilder> {
  _$CommunicationChannel? _$v;

  bool? _skipConfirmation;
  bool? get skipConfirmation => _$this._skipConfirmation;
  set skipConfirmation(bool? skipConfirmation) =>
      _$this._skipConfirmation = skipConfirmation;

  CommunicationChannelBuilder() {
    CommunicationChannel._initializeBuilder(this);
  }

  CommunicationChannelBuilder get _$this {
    final $v = _$v;
    if ($v != null) {
      _skipConfirmation = $v.skipConfirmation;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CommunicationChannel other) {
    ArgumentError.checkNotNull(other, 'other');
    _$v = other as _$CommunicationChannel;
  }

  @override
  void update(void Function(CommunicationChannelBuilder)? updates) {
    if (updates != null) updates(this);
  }

  @override
  CommunicationChannel build() => _build();

  _$CommunicationChannel _build() {
    final _$result = _$v ??
        new _$CommunicationChannel._(
            skipConfirmation: BuiltValueNullFieldError.checkNotNull(
                skipConfirmation, r'CommunicationChannel', 'skipConfirmation'));
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: deprecated_member_use_from_same_package,type=lint
