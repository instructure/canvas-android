/// Copyright (C) 2019 - present Instructure, Inc.
///
/// This program is free software: you can redistribute it and/or modify
/// it under the terms of the GNU General Public License as published by
/// the Free Software Foundation, version 3 of the License.
///
/// This program is distributed in the hope that it will be useful,
/// but WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU General Public License for more details.
///
/// You should have received a copy of the GNU General Public License
/// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'package:flutter/foundation.dart';
import 'package:flutter_parent/api/course_api.dart';
import 'package:flutter_parent/api/inbox_api.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/screens/inbox/conversation_list/conversation_list_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/test_app.dart';

void main() {
  test('getConversations calls api for normal scope and sent scope', () {
    var inboxApi = _MockInboxApi();

    when(inboxApi.getConversations(
      scope: anyNamed('scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.value([]));

    setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
    });

    var interactor = ConversationListInteractor();
    interactor.getConversations();
    verify(inboxApi.getConversations(forceRefresh: false)).called(1);
    verify(inboxApi.getConversations(scope: 'sent', forceRefresh: false)).called(1);
  });

  test('getConversations merges scopes and removes duplicates from sent scope', () async {
    var inboxApi = _MockInboxApi();

    setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
    });

    var normalScopeItems = [
      Conversation((c) => c
        ..id = 0
        ..lastMessageAt = DateTime.now()
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = 1
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 1))
        ..lastMessage = 'Message to user'),
    ];

    var sentScopeItems = [
      Conversation((c) => c
        ..id = 1
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 1, hours: 1))
        ..lastMessage = 'Message from User'),
      Conversation((c) => c
        ..id = 2
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 2))
        ..lastMessage = 'Message from User'),
    ];

    var expectedItems = [normalScopeItems[0], normalScopeItems[1], sentScopeItems[1]];

    when(inboxApi.getConversations(
      scope: anyNamed('scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.value(normalScopeItems));

    when(inboxApi.getConversations(
      scope: argThat(equals('sent'), named: 'scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.value(sentScopeItems));

    var interactor = ConversationListInteractor();
    var actualItems = await interactor.getConversations();
    expect(listEquals(expectedItems, actualItems), isTrue);
  });

  test('getConversations orders items by date (descending)', () async {
    var inboxApi = _MockInboxApi();

    setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
    });

    var apiItems = [
      Conversation((c) => c
        ..id = 0
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 3))
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = 1
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 5))
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = 2
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 1))
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = 3
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 2))
        ..lastMessage = 'Message to user'),
      Conversation((c) => c
        ..id = 4
        ..lastMessageAt = DateTime.now().subtract(Duration(days: 4))
        ..lastMessage = 'Message to user'),
    ];

    var expectedItems = [
      apiItems[2],
      apiItems[3],
      apiItems[0],
      apiItems[4],
      apiItems[1],
    ];

    when(inboxApi.getConversations(
      scope: anyNamed('scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.value(List.from(apiItems)));

    var interactor = ConversationListInteractor();
    var actualItems = await interactor.getConversations();
    expect(listEquals(expectedItems, actualItems), isTrue);
  });

  test('getConversations produces error when API fails', () async {
    var inboxApi = _MockInboxApi();

    setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
    });

    var expectedError = "fail";
    when(inboxApi.getConversations(
      scope: anyNamed('scope'),
      forceRefresh: anyNamed('forceRefresh'),
    )).thenAnswer((_) => Future.error(expectedError));

    try {
      await ConversationListInteractor().getConversations();
      fail('Interactor did not propogate failure');
    } catch (e) {
      expect(e, equals(expectedError));
    }
  });

  test('getCoursesForCompose calls CourseApi', () {
    var api = _MockCourseApi();
    setupTestLocator((locator) => locator.registerLazySingleton<CourseApi>(() => api));
    ConversationListInteractor().getCoursesForCompose();
    verify(api.getObserveeCourses()).called(1);
  });
}

class _MockInboxApi extends Mock implements InboxApi {}

class _MockCourseApi extends Mock implements CourseApi {}
