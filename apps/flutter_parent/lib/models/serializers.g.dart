// GENERATED CODE - DO NOT MODIFY BY HAND

part of serializers;

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializers _$_serializers = (new Serializers().toBuilder()
      ..add(Alert.serializer)
      ..add(AlertType.serializer)
      ..add(AlertWorkflowState.serializer)
      ..add(Assignment.serializer)
      ..add(CanvasToken.serializer)
      ..add(Course.serializer)
      ..add(Enrollment.serializer)
      ..add(Grade.serializer)
      ..add(MobileVerifyResult.serializer)
      ..add(SchoolDomain.serializer)
      ..add(Submission.serializer)
      ..add(User.serializer)
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Enrollment)]),
          () => new ListBuilder<Enrollment>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Submission)]),
          () => new ListBuilder<Submission>()))
    .build();

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
