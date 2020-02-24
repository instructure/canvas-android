library seeded_user;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

import '../login.dart';
import '../user.dart';

part 'seeded_user.g.dart';

abstract class SeededUser implements Built<SeededUser,SeededUserBuilder>{
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<SeededUser> get serializer => _$seededUserSerializer;

  SeededUser._();
  factory SeededUser([void Function(SeededUserBuilder) updates]) = _$SeededUser;

//  SeededUser({
//    this.id,
//    this.name,
//    this.shortName,
//    this.sortableName,
//    this.loginId,
//    this.password,
//    this.avatarUrl,
//    this.token,
//    this.domain
//  });

  String get id;
  String get name;
  @BuiltValueField(wireName: "short_name")
  String get shortName;
  @BuiltValueField(wireName: "sortable_name")
  String get sortableName;
  @nullable
  @BuiltValueField(wireName: "terms_of_use")
  bool get termsOfUse;
  @nullable
  @BuiltValueField(wireName: "login_id")
  String get loginId;
  @nullable
  String get password;
  @nullable
  @BuiltValueField(wireName: "avatar_url")
  String get avatarUrl;
  @nullable
  String get token;
  @nullable
  String get domain;

  static void _initializeBuilder(SeededUserBuilder b) => b
    ..name = '';

  User toUser() {
    return User((b) => b
        ..id = id
        ..name = name
        ..shortName = shortName
        ..sortableName = sortableName
        ..build()

    );
  }

  Login toLogin() {
    return Login((b) => b
        ..domain = domain
        ..clientSecret = token
        ..accessToken = token
        ..user.name = name
        ..user.id = id
        ..user.shortName = shortName
        ..user.sortableName = sortableName
        ..build()
    );
  }
}