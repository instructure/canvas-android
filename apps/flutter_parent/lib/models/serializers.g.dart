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
      ..add(AssignmentGroup.serializer)
      ..add(Attachment.serializer)
      ..add(BasicUser.serializer)
      ..add(CanvasToken.serializer)
      ..add(Conversation.serializer)
      ..add(Course.serializer)
      ..add(Enrollment.serializer)
      ..add(FileUploadConfig.serializer)
      ..add(Grade.serializer)
      ..add(MediaComment.serializer)
      ..add(Message.serializer)
      ..add(MobileVerifyResult.serializer)
      ..add(Recipient.serializer)
      ..add(SchoolDomain.serializer)
      ..add(Submission.serializer)
      ..add(UnreadCount.serializer)
      ..add(User.serializer)
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Assignment)]),
          () => new ListBuilder<Assignment>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Attachment)]),
          () => new ListBuilder<Attachment>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Message)]),
          () => new ListBuilder<Message>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(int)]),
          () => new ListBuilder<int>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Enrollment)]),
          () => new ListBuilder<Enrollment>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Submission)]),
          () => new ListBuilder<Submission>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(int)]),
          () => new ListBuilder<int>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(BasicUser)]),
          () => new ListBuilder<BasicUser>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Message)]),
          () => new ListBuilder<Message>())
      ..addBuilderFactory(
          const FullType(BuiltMap, const [
            const FullType(String),
            const FullType(BuiltList, const [const FullType(String)])
          ]),
          () => new MapBuilder<String, BuiltList<String>>())
      ..addBuilderFactory(
          const FullType(
              BuiltMap, const [const FullType(String), const FullType(String)]),
          () => new MapBuilder<String, String>()))
    .build();

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
