import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/user.dart';

part 'canvas_token.g.dart';

abstract class CanvasToken implements Built<CanvasToken, CanvasTokenBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CanvasToken> get serializer => _$canvasTokenSerializer;
  
  CanvasToken._();

  factory CanvasToken([void Function(CanvasTokenBuilder) updates]) = _$CanvasToken;
  
  @BuiltValueField(wireName: 'access_token')
  String get accessToken;

  @nullable
  @BuiltValueField(wireName: 'refresh_token')
  String get refreshToken;
  
  @nullable
  User get user;
}
