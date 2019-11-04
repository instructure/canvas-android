import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';

part 'school_domain.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build
abstract class SchoolDomain implements Built<SchoolDomain, SchoolDomainBuilder> {
  @BuiltValueSerializer(serializeNulls: true) // Add this line to get nulls to serialize when we convert to JSON
  static Serializer<SchoolDomain> get serializer => _$schoolDomainSerializer;

  String get domain;
  String get name;

  @nullable
  @BuiltValueField(wireName: 'authentication_provider')
  String get authenticationProvider;

  SchoolDomain._();
  factory SchoolDomain([void Function(SchoolDomainBuilder) updates]) = _$SchoolDomain;
}
