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
import 'package:flutter_parent/api/inbox_api.dart';
import 'package:flutter_parent/api/utils/api_prefs_wrapper.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/models/user.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/test_app.dart';

void main() {
  test('createConversation calls InboxApi.createConversation', () {
    var inboxApi = _MockInboxApi();
    setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
    });

    var interactor = CreateConversationInteractor();
    final course = Course();
    final recipients = [1, 2, 3];
    final subject = "Message Subject";
    final body = "Message Body";
    final attachments = [4, 5, 6];

    interactor.createConversation(course, recipients, subject, body, attachments);
    verify(inboxApi.createConversation(course, recipients, subject, body, attachments)).called(1);
  });

  test('getAllRecipients filters out current user', () async {
    var inboxApi = _MockInboxApi();
    var prefs = _MockApiPrefs();

    setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => inboxApi);
      locator.registerLazySingleton<ApiPrefsWrapper>(() => prefs);
    });

    final self = Recipient((b) => b
      ..id = 123
      ..name = 'self');
    final user1 = Recipient((b) => b
      ..id = 456
      ..name = 'user1');
    final user2 = Recipient((b) => b
      ..id = 789
      ..name = 'user2');

    final response = [self, user1, user2];
    final expected = [user1, user2];

    when(prefs.getUser()).thenReturn(User((b) => b..id = 123));
    when(inboxApi.getRecipients(any)).thenAnswer((_) => Future.value(response));

    final actual = await CreateConversationInteractor().getAllRecipients(Course());
    expect(listEquals(actual, expected), isTrue);
    verify(inboxApi.getRecipients(any)).called(1);
  });
}

class _MockInboxApi extends Mock implements InboxApi {}

class _MockApiPrefs extends Mock implements ApiPrefsWrapper {}
