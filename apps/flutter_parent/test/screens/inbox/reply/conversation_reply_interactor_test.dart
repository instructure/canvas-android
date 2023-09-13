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

import 'package:built_collection/built_collection.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course_permissions.dart';
import 'package:flutter_parent/models/enrollment.dart';
import 'package:flutter_parent/models/login.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/network/api/course_api.dart';
import 'package:flutter_parent/network/api/enrollments_api.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/screens/inbox/reply/conversation_reply_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../../utils/platform_config.dart';
import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  setUp(() async {
    final login = Login((b) => b..user = User((u) => u.id = 'self').toBuilder());
    await setupPlatformChannels(config: PlatformConfig(initLoggedInUser: login));
  });

  test('getCurrentUserId calls ApiPrefs', () {
    var userId = ConversationReplyInteractor().getCurrentUserId();
    expect(userId, 'self');
  });

  group('createReply calls InboxApi with correct params', () {
    test('for conversation reply', () async {
      var api = MockInboxApi();
      await setupTestLocator((locator) {
        locator.registerLazySingleton<InboxApi>(() => api);
      });

      var interactor = ConversationReplyInteractor();

      Conversation conversation = _makeConversation();
      String body = 'This is a reply';
      List<String> attachmentIds = ['a', 'b', 'c'];
      bool replyAll = false;

      interactor.createReply(conversation, null, body, attachmentIds, replyAll);

      verify(api.addMessage(conversation.id, body, ['author1'], attachmentIds, ['message4'])).called(1);
    });

    test('for conversation reply all', () async {
      var api = MockInboxApi();
      var enrollmentsApi = MockEnrollmentsApi();
      var courseApi = MockCourseApi();
      await setupTestLocator((locator) {
        locator.registerLazySingleton<InboxApi>(() => api);
        locator.registerLazySingleton<EnrollmentsApi>(() => enrollmentsApi);
        locator.registerLazySingleton<CourseApi>(() => courseApi);
      });

      when(enrollmentsApi.getObserveeEnrollments())
          .thenAnswer((_) => Future.value([_mockEnrollment(_mockStudent('hodor', '123').toBuilder())]));
      when(api.getRecipients(any)).thenAnswer((_) => Future.value([]));
      when(courseApi.getCoursePermissions(any)).thenAnswer((_) => Future.value(CoursePermissions()));

      var interactor = ConversationReplyInteractor();

      Conversation conversation = _makeConversation();
      String body = 'This is a reply';
      List<String> attachmentIds = ['a', 'b', 'c'];
      bool replyAll = true;

      await interactor.createReply(conversation, null, body, attachmentIds, replyAll);

      verify(api.addMessage(conversation.id, body, [], attachmentIds, [])).called(1);
    });

    test('for message reply', () async {
      var api = MockInboxApi();
      await setupTestLocator((locator) {
        locator.registerLazySingleton<InboxApi>(() => api);
      });

      var interactor = ConversationReplyInteractor();

      Conversation conversation = _makeConversation();
      String body = 'This is a reply';
      List<String> attachmentIds = ['a', 'b', 'c'];
      bool replyAll = false;
      Message message = conversation.messages![1];

      interactor.createReply(conversation, message, body, attachmentIds, replyAll);

      verify(api.addMessage(conversation.id, body, ['author2'], attachmentIds, ['message3'])).called(1);
    });

    test('for message reply all', () async {
      var api = MockInboxApi();
      var enrollmentsApi = MockEnrollmentsApi();
      var courseApi = MockCourseApi();
      await setupTestLocator((locator) {
        locator.registerLazySingleton<InboxApi>(() => api);
        locator.registerLazySingleton<EnrollmentsApi>(() => enrollmentsApi);
        locator.registerLazySingleton<CourseApi>(() => courseApi);
      });

      when(enrollmentsApi.getObserveeEnrollments()).thenAnswer((_) => Future.value([]));

      var recipientList = [
        _makeRecipient('self', 'ObserverEnrollment'),
        _makeRecipient('author1', 'TeacherEnrollment'),
        _makeRecipient('author2', 'TaEnrollment'),
        _makeRecipient('author3', 'TeacherEnrollment'),
      ];
      when(api.getRecipients(any)).thenAnswer((_) => Future.value(recipientList));

      when(courseApi.getCoursePermissions(any))
          .thenAnswer((_) => Future.value(CoursePermissions((b) => b..sendMessages = true)));

      var interactor = ConversationReplyInteractor();

      Conversation conversation = _makeConversation();
      String body = 'This is a reply';
      List<String> attachmentIds = ['a', 'b', 'c'];
      bool replyAll = true;
      Message message = conversation.messages![3];

      await interactor.createReply(conversation, message, body, attachmentIds, replyAll);

      verify(
        api.addMessage(conversation.id, body, ['self', 'author1', 'author2', 'author3'], attachmentIds, ['message1']),
      ).called(1);
    });

    test('for message reply all filters out non-observed students', () async {
      var api = MockInboxApi();
      var enrollmentsApi = MockEnrollmentsApi();
      var courseApi = MockCourseApi();
      await setupTestLocator((locator) {
        locator.registerLazySingleton<InboxApi>(() => api);
        locator.registerLazySingleton<EnrollmentsApi>(() => enrollmentsApi);
        locator.registerLazySingleton<CourseApi>(() => courseApi);
      });

      var enrollmentsList = [_mockEnrollment(_mockStudent('student2', 'student2').toBuilder())];
      when(enrollmentsApi.getObserveeEnrollments()).thenAnswer((_) => Future.value(enrollmentsList));

      var recipientList = [
        _makeRecipient('self', 'ObserverEnrollment'),
        _makeRecipient('author1', 'TeacherEnrollment'),
        _makeRecipient('author2', 'TaEnrollment'),
        _makeRecipient('author3', 'TeacherEnrollment'),
        _makeRecipient('student1', 'StudentEnrollment'),
        _makeRecipient('student2', 'StudentEnrollment'),
      ];
      when(api.getRecipients(any)).thenAnswer((_) => Future.value(recipientList));

      when(courseApi.getCoursePermissions(any))
          .thenAnswer((_) => Future.value(CoursePermissions((b) => b..sendMessages = true)));

      var interactor = ConversationReplyInteractor();

      Conversation conversation = _makeConversation();
      String body = 'This is a reply';
      List<String> attachmentIds = ['a', 'b', 'c'];
      bool replyAll = true;
      Message message = conversation.messages![4];

      await interactor.createReply(conversation, message, body, attachmentIds, replyAll);

      verify(
        api.addMessage(
            conversation.id, body, ['self', 'author1', 'author2', 'author3', 'student2'], attachmentIds, ['message1']),
      ).called(1);
    });

    test('for self-authored message reply', () async {
      var api = MockInboxApi();
      await setupTestLocator((locator) {
        locator.registerLazySingleton<InboxApi>(() => api);
      });

      var interactor = ConversationReplyInteractor();

      Conversation conversation = _makeConversation();
      String body = 'This is a reply';
      List<String> attachmentIds = ['a', 'b', 'c'];
      bool replyAll = false;
      Message message = conversation.messages![2];

      interactor.createReply(conversation, message, body, attachmentIds, replyAll);

      verify(
        api.addMessage(conversation.id, body, ['self', 'author1', 'author2', 'author3'], attachmentIds, ['message2']),
      ).called(1);
    });

    test('for self-authored conversation reply', () async {
      var api = MockInboxApi();
      await setupTestLocator((locator) {
        locator.registerLazySingleton<InboxApi>(() => api);
      });

      var interactor = ConversationReplyInteractor();

      Conversation conversation = _makeConversation().rebuild((c) => c
        ..messages = ListBuilder([
          c.messages[2], // self-authored message moved to first position
          c.messages[0],
          c.messages[1],
          c.messages[3],
        ]));
      String body = 'This is a reply';
      List<String> attachmentIds = ['a', 'b', 'c'];
      bool replyAll = false;

      interactor.createReply(conversation, null, body, attachmentIds, replyAll);

      verify(
        api.addMessage(conversation.id, body, ['self', 'author1', 'author2', 'author3'], attachmentIds, ['message2']),
      ).called(1);
    });

    test('for monologue message reply', () async {
      var api = MockInboxApi();
      await setupTestLocator((locator) {
        locator.registerLazySingleton<InboxApi>(() => api);
      });

      var interactor = ConversationReplyInteractor();

      Conversation conversation = Conversation((c) => c
        ..id = 'conversation1'
        ..messages = ListBuilder([
          Message((m) => m
            ..id = 'message1'
            ..authorId = 'self'
            ..participatingUserIds = ListBuilder(['self'])),
        ]));
      String body = 'This is a reply';
      List<String> attachmentIds = ['a', 'b', 'c'];
      bool replyAll = false;
      Message message = conversation.messages![0];

      interactor.createReply(conversation, message, body, attachmentIds, replyAll);

      verify(
        api.addMessage(conversation.id, body, ['self'], attachmentIds, ['message1']),
      ).called(1);
    });

    test('for monologue conversation reply', () async {
      var api = MockInboxApi();
      await setupTestLocator((locator) {
        locator.registerLazySingleton<InboxApi>(() => api);
      });

      var interactor = ConversationReplyInteractor();

      Conversation conversation = Conversation((c) => c
        ..id = 'conversation1'
        ..messages = ListBuilder([
          Message((m) => m
            ..id = 'message1'
            ..authorId = 'self'
            ..participatingUserIds = ListBuilder(['self'])),
        ]));
      String body = 'This is a reply';
      List<String> attachmentIds = ['a', 'b', 'c'];
      bool replyAll = false;

      interactor.createReply(conversation, null, body, attachmentIds, replyAll);

      verify(
        api.addMessage(conversation.id, body, ['self'], attachmentIds, ['message1']),
      ).called(1);
    });
  });
}

Conversation _makeConversation() {
  return Conversation((c) => c
    ..id = 'conversation1'
    ..contextCode = "course_123"
    ..messages = ListBuilder([
      Message((m) => m
        ..id = 'message4'
        ..authorId = 'author1'
        ..participatingUserIds = ListBuilder(['self', 'author1', 'author2', 'author3'])),
      Message((m) => m
        ..id = 'message3'
        ..authorId = 'author2'
        ..participatingUserIds = ListBuilder(['self', 'author2', 'author4'])),
      Message((m) => m
        ..id = 'message2'
        ..authorId = 'self'
        ..participatingUserIds = ListBuilder(['self', 'author1', 'author2', 'author3'])),
      Message((m) => m
        ..id = 'message1'
        ..authorId = 'author3'
        ..participatingUserIds = ListBuilder(['self', 'author1', 'author2', 'author3'])),
      Message((m) => m
        ..id = 'message1'
        ..authorId = 'author3'
        ..participatingUserIds = ListBuilder(['self', 'author1', 'author2', 'author3', 'student1', 'student2'])),
    ]));
}

User _mockStudent(String name, String id) => User((b) => b
  ..id = id
  ..sortableName = name
  ..build());

Enrollment _mockEnrollment(UserBuilder observedUser) => Enrollment((b) => b
  ..enrollmentState = ''
  ..observedUser = observedUser
  ..build());

Recipient _makeRecipient(String id, String type) {
  return Recipient((b) => b
    ..id = id
    ..name = 'User $id'
    ..commonCourses = MapBuilder<String, BuiltList<String>>({
      '123': BuiltList<String>([type])
    }));
}
