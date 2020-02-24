// GENERATED CODE - DO NOT MODIFY BY HAND

part of communication_channel;

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
  Iterable<Object> serialize(
      Serializers serializers, CommunicationChannel object,
      {FullType specifiedType = FullType.unspecified}) {
    final result = <Object>[
      'skip_confirmation',
      serializers.serialize(object.skipConfirmation,
          specifiedType: const FullType(bool)),
    ];

    return result;
  }

  @override
  CommunicationChannel deserialize(
      Serializers serializers, Iterable<Object> serialized,
      {FullType specifiedType = FullType.unspecified}) {
    final result = new CommunicationChannelBuilder();

    final iterator = serialized.iterator;
    while (iterator.moveNext()) {
      final key = iterator.current as String;
      iterator.moveNext();
      final dynamic value = iterator.current;
      if (value == null) continue;
      switch (key) {
        case 'skip_confirmation':
          result.skipConfirmation = serializers.deserialize(value,
              specifiedType: const FullType(bool)) as bool;
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
          [void Function(CommunicationChannelBuilder) updates]) =>
      (new CommunicationChannelBuilder()..update(updates)).build();

  _$CommunicationChannel._({this.skipConfirmation}) : super._() {
    if (skipConfirmation == null) {
      throw new BuiltValueNullFieldError(
          'CommunicationChannel', 'skipConfirmation');
    }
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
    return $jf($jc(0, skipConfirmation.hashCode));
  }

  @override
  String toString() {
    return (newBuiltValueToStringHelper('CommunicationChannel')
          ..add('skipConfirmation', skipConfirmation))
        .toString();
  }
}

class CommunicationChannelBuilder
    implements Builder<CommunicationChannel, CommunicationChannelBuilder> {
  _$CommunicationChannel _$v;

  bool _skipConfirmation;
  bool get skipConfirmation => _$this._skipConfirmation;
  set skipConfirmation(bool skipConfirmation) =>
      _$this._skipConfirmation = skipConfirmation;

  CommunicationChannelBuilder() {
    CommunicationChannel._initializeBuilder(this);
  }

  CommunicationChannelBuilder get _$this {
    if (_$v != null) {
      _skipConfirmation = _$v.skipConfirmation;
      _$v = null;
    }
    return this;
  }

  @override
  void replace(CommunicationChannel other) {
    if (other == null) {
      throw new ArgumentError.notNull('other');
    }
    _$v = other as _$CommunicationChannel;
  }

  @override
  void update(void Function(CommunicationChannelBuilder) updates) {
    if (updates != null) updates(this);
  }

  @override
  _$CommunicationChannel build() {
    final _$result =
        _$v ?? new _$CommunicationChannel._(skipConfirmation: skipConfirmation);
    replace(_$result);
    return _$result;
  }
}

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
