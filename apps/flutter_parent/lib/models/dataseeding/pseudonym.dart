library pseudonym;

import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
//import 'package:flutter/cupertino.dart';

part 'pseudonym.g.dart';

abstract class Pseudonym implements Built<Pseudonym, PseudonymBuilder>{
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<Pseudonym> get serializer => _$pseudonymSerializer;

  Pseudonym._();
  factory Pseudonym([void Function(PseudonymBuilder) updates]) = _$Pseudonym;

//  Pseudonym({
//    this.uniqueId,
//    this.password
//  });

  @BuiltValueField(wireName: "unique_id")
  String get uniqueId;
  String get password;

  static void _initializeBuilder(PseudonymBuilder b) => b
    ..uniqueId = ''
    ..password = '';
}