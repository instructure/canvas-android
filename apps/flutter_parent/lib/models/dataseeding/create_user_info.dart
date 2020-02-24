library create_user_info;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/dataseeding/communication_channel.dart';
import 'package:flutter_parent/models/dataseeding/pseudonym.dart';
import 'package:flutter_parent/models/dataseeding/user_name_data.dart';

part 'create_user_info.g.dart';

abstract class CreateUserInfo implements Built<CreateUserInfo, CreateUserInfoBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<CreateUserInfo> get serializer => _$createUserInfoSerializer;

  CreateUserInfo._();
  factory CreateUserInfo([void Function(CreateUserInfoBuilder) updates]) = _$CreateUserInfo;


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