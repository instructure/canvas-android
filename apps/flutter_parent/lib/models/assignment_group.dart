import 'package:built_collection/built_collection.dart';
import 'package:built_value/built_value.dart';
import 'package:built_value/serializer.dart';
import 'package:flutter_parent/models/assignment.dart';

part 'assignment_group.g.dart';

/// To have this built_value be generated, run this command from the project root:
/// flutter packages pub run build_runner build --delete-conflicting-outputs
abstract class AssignmentGroup implements Built<AssignmentGroup, AssignmentGroupBuilder> {
  @BuiltValueSerializer(serializeNulls: true)
  static Serializer<AssignmentGroup> get serializer => _$assignmentGroupSerializer;

  AssignmentGroup._();

  factory AssignmentGroup([void Function(AssignmentGroupBuilder) updates]) = _$AssignmentGroup;

  int get id;

  String get name;

  int get position; // the position of the Assignment Group

  @BuiltValueField(wireName: 'group_weight')
  double get groupWeight; // the weight of the Assignment Group

  BuiltList<Assignment> get assignments;
}
