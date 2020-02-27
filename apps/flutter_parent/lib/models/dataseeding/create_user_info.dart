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
import 'package:flutter_parent/models/dataseeding/communication_channel.dart';
import 'package:flutter_parent/models/dataseeding/pseudonym.dart';
import 'package:flutter_parent/models/dataseeding/user_name_data.dart';

part 'create_user_info.g.dart';

abstract class CreateUserInfo
    implements Built<CreateUserInfo, CreateUserInfoBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CreateUserInfo> get serializer =>
      _$createUserInfoSerializer;

  CreateUserInfo._();
  factory CreateUserInfo([void Function(CreateUserInfoBuilder) updates]) =
      _$CreateUserInfo;

//  CreateUserInfo({
//    this.user,
//    this.pseudonym,
//    this.communicationChannel
//  });

  UserNameData get user;
  Pseudonym get pseudonym;
  @BuiltValueField(wireName: "communication_channel")
  CommunicationChannel get communicationChannel;

  static void _initializeBuilder(CreateUserInfoBuilder b) => b;
}
