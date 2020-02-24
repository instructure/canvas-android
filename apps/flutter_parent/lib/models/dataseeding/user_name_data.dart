library user_name_data;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
part 'user_name_data.g.dart';

abstract class UserNameData implements Built<UserNameData, UserNameDataBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<UserNameData> get serializer => _$userNameDataSerializer;

  UserNameData._();
  factory UserNameData([void Function(UserNameDataBuilder) updates]) = _$UserNameData;

  String get name;
  @BuiltValueField(wireName: "short_name")
  String get shortName;
  @BuiltValueField(wireName: "sortable_name")
  String get sortableName;
  @nullable
  @BuiltValueField(wireName: "terms_of_use")
  bool get termsOfUse;

  static void _initializeBuilder(UserNameDataBuilder b) => b
    ..name = '';
}