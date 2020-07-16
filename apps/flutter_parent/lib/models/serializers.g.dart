// GENERATED CODE - DO NOT MODIFY BY HAND

part of serializers;

// **************************************************************************
// BuiltValueGenerator
// **************************************************************************

Serializers _$_serializers = (new Serializers().toBuilder()
      ..add(AccountNotification.serializer)
      ..add(AccountPermissions.serializer)
      ..add(Alert.serializer)
      ..add(AlertThreshold.serializer)
      ..add(AlertType.serializer)
      ..add(AlertWorkflowState.serializer)
      ..add(Announcement.serializer)
      ..add(Assignment.serializer)
      ..add(AssignmentGroup.serializer)
      ..add(AssignmentOverride.serializer)
      ..add(Attachment.serializer)
      ..add(AuthenticatedUrl.serializer)
      ..add(AvailableTo.serializer)
      ..add(BasicUser.serializer)
      ..add(CanvasPage.serializer)
      ..add(CanvasToken.serializer)
      ..add(CommunicationChannel.serializer)
      ..add(Conversation.serializer)
      ..add(ConversationWorkflowState.serializer)
      ..add(Course.serializer)
      ..add(CoursePermissions.serializer)
      ..add(CourseSettings.serializer)
      ..add(CourseTab.serializer)
      ..add(CreateAssignmentInfo.serializer)
      ..add(CreateAssignmentWrapper.serializer)
      ..add(CreateCourseInfo.serializer)
      ..add(CreateCourseWrapper.serializer)
      ..add(CreateEnrollmentInfo.serializer)
      ..add(CreateEnrollmentWrapper.serializer)
      ..add(CreateSubmissionInfo.serializer)
      ..add(CreateSubmissionWrapper.serializer)
      ..add(CreateUserInfo.serializer)
      ..add(Enrollment.serializer)
      ..add(FileUploadConfig.serializer)
      ..add(Grade.serializer)
      ..add(GradeSubmissionInfo.serializer)
      ..add(GradeSubmissionWrapper.serializer)
      ..add(GradingPeriod.serializer)
      ..add(GradingPeriodResponse.serializer)
      ..add(GradingType.serializer)
      ..add(HelpLink.serializer)
      ..add(HelpLinks.serializer)
      ..add(HomePage.serializer)
      ..add(LockInfo.serializer)
      ..add(LockedModule.serializer)
      ..add(Login.serializer)
      ..add(MediaComment.serializer)
      ..add(MediaType.serializer)
      ..add(Message.serializer)
      ..add(MobileVerifyResult.serializer)
      ..add(NotificationPayload.serializer)
      ..add(NotificationPayloadType.serializer)
      ..add(OAuthToken.serializer)
      ..add(PairingCode.serializer)
      ..add(Plannable.serializer)
      ..add(PlannerItem.serializer)
      ..add(PlannerSubmission.serializer)
      ..add(Pseudonym.serializer)
      ..add(Quiz.serializer)
      ..add(Recipient.serializer)
      ..add(Reminder.serializer)
      ..add(RemoteFile.serializer)
      ..add(ScheduleItem.serializer)
      ..add(SchoolDomain.serializer)
      ..add(Section.serializer)
      ..add(SeedContext.serializer)
      ..add(SeededUser.serializer)
      ..add(Submission.serializer)
      ..add(SubmissionTypes.serializer)
      ..add(SubmissionWrapper.serializer)
      ..add(Term.serializer)
      ..add(TermsOfService.serializer)
      ..add(UnreadCount.serializer)
      ..add(User.serializer)
      ..add(UserColors.serializer)
      ..add(UserNameData.serializer)
      ..add(UserPermission.serializer)
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Assignment)]),
          () => new ListBuilder<Assignment>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(AssignmentOverride)]),
          () => new ListBuilder<AssignmentOverride>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Attachment)]),
          () => new ListBuilder<Attachment>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Message)]),
          () => new ListBuilder<Message>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(String)]),
          () => new ListBuilder<String>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(AvailableTo)]),
          () => new ListBuilder<AvailableTo>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Enrollment)]),
          () => new ListBuilder<Enrollment>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Section)]),
          () => new ListBuilder<Section>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(GradingPeriod)]),
          () => new ListBuilder<GradingPeriod>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(HelpLink)]),
          () => new ListBuilder<HelpLink>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(HelpLink)]),
          () => new ListBuilder<HelpLink>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(RemoteFile)]),
          () => new ListBuilder<RemoteFile>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(String)]),
          () => new ListBuilder<String>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(String)]),
          () => new ListBuilder<String>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(BasicUser)]),
          () => new ListBuilder<BasicUser>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Message)]),
          () => new ListBuilder<Message>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Submission)]),
          () => new ListBuilder<Submission>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(Submission)]),
          () => new ListBuilder<Submission>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(SubmissionTypes)]),
          () => new ListBuilder<SubmissionTypes>())
      ..addBuilderFactory(
          const FullType(BuiltList, const [const FullType(SubmissionTypes)]),
          () => new ListBuilder<SubmissionTypes>())
      ..addBuilderFactory(
          const FullType(BuiltMap, const [
            const FullType(String),
            const FullType(BuiltList, const [const FullType(String)])
          ]),
          () => new MapBuilder<String, BuiltList<String>>())
      ..addBuilderFactory(
          const FullType(
              BuiltMap, const [const FullType(String), const FullType(String)]),
          () => new MapBuilder<String, String>())
      ..addBuilderFactory(
          const FullType(
              BuiltMap, const [const FullType(String), const FullType(String)]),
          () => new MapBuilder<String, String>())
      ..addBuilderFactory(
          const FullType(
              BuiltMap, const [const FullType(String), const FullType(String)]),
          () => new MapBuilder<String, String>()))
    .build();

// ignore_for_file: always_put_control_body_on_new_line,always_specify_types,annotate_overrides,avoid_annotating_with_dynamic,avoid_as,avoid_catches_without_on_clauses,avoid_returning_this,lines_longer_than_80_chars,omit_local_variable_types,prefer_expression_function_bodies,sort_constructors_first,test_types_in_equals,unnecessary_const,unnecessary_new
