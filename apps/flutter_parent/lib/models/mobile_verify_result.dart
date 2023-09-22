// Copyright (C) 2019 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'mobile_verify_result.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class MobileVerifyResult implements Built<MobileVerifyResult, MobileVerifyResultBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<MobileVerifyResult> get serializer => _$mobileVerifyResultSerializer;

  MobileVerifyResult._();

  factory MobileVerifyResult([void Function(MobileVerifyResultBuilder) updates]) = _$MobileVerifyResult;

  bool get authorized;

  VerifyResultEnum get result;

  @BuiltValueField(wireName: 'client_id')
  String get clientId;

  @BuiltValueField(wireName: 'client_secret')
  String get clientSecret;

  @BuiltValueField(wireName: 'api_key')
  String get apiKey;

  @BuiltValueField(wireName: 'base_url')
  String get baseUrl;

  static void _initializeBuilder(MobileVerifyResultBuilder b) => b
    ..authorized = true
    ..result = VerifyResultEnum.success
    ..clientId = ''
    ..clientSecret = ''
    ..apiKey = ''
    ..baseUrl = '';
}

enum VerifyResultEnum {
  success,
  generalError,
  domainNotAuthorized,
  unknownUserAgent,
  unknownError,
}

class ResultEnumSerializer extends PrimitiveSerializer<VerifyResultEnum> {
  @override
  final Iterable<Type> types = BuiltList<Type>([VerifyResultEnum]);

  @override
  final String wireName = 'int';

  @override
  Object serialize(Serializers serializers, VerifyResultEnum integer, {FullType specifiedType = FullType.unspecified}) {
    return integer.index;
  }

  @override
  VerifyResultEnum deserialize(Serializers serializers, Object serialized,
      {FullType specifiedType = FullType.unspecified}) {
    return VerifyResultEnum.values[serialized as int];
  }
}
