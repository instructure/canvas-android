library oauth_token;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'oauth_token.g.dart';

abstract class OAuthToken implements Built<OAuthToken,OAuthTokenBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<OAuthToken> get serializer => _$oAuthTokenSerializer;

  OAuthToken._();
  factory OAuthToken([void Function(OAuthTokenBuilder) updates]) = _$OAuthToken;

//  OAuthToken({
//    this.accessToken
//  });

  @BuiltValueField(wireName: "access_token")
  String get accessToken;

  static void _initializeBuilder(OAuthTokenBuilder b) => b
    ..accessToken = '';
}