// Copyright (C) 2020 - present Instructure, Inc.
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