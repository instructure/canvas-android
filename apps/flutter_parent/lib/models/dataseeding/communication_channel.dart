
library communication_channel;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'communication_channel.g.dart';

abstract class CommunicationChannel implements Built<CommunicationChannel, CommunicationChannelBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CommunicationChannel> get serializer => _$communicationChannelSerializer;

  CommunicationChannel._();
  factory CommunicationChannel([void Function(CommunicationChannelBuilder) updates]) = _$CommunicationChannel;

  @BuiltValueField(wireName: "skip_confirmation")
  bool get skipConfirmation;

  static void _initializeBuilder(CommunicationChannelBuilder b) => b
      ..skipConfirmation = false;

}