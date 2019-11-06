/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'mobile_verify_result.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class MobileVerifyResult implements Built<MobileVerifyResult, MobileVerifyResultBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<MobileVerifyResult> get serializer => _$mobileVerifyResultSerializer;

  bool get authorized;

  int get result;

  @BuiltValueField(wireName: 'client_id')
  String get clientId;

  @BuiltValueField(wireName: 'api_key')
  String get apiKey;

  @BuiltValueField(wireName: 'client_secret')
  String get clientSecret;

  @BuiltValueField(wireName: 'base_url')
  String get baseUrl;

  MobileVerifyResult._();

  factory MobileVerifyResult([void Function(MobileVerifyResultBuilder) updates]) = _$MobileVerifyResult;
}
