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

import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/network/api/inbox_api.dart';
import 'package:flutter_parent/screens/dashboard/inbox_notifier.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/conversation_details_interactor.dart';
import 'package:mockito/mockito.dart';
import 'package:test/test.dart';

import '../../../utils/test_app.dart';

void main() {
  test('getConversation calls InboxApi with correct parameters', () async {
    final conversationId = '123';

    var api = _MockInboxApi();
    setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => api);
      locator.registerLazySingleton<InboxCountNotifier>(() => _MockInboxNotifier());
    });

    await ConversationDetailsInteractor().getConversation(conversationId);
    verify(api.getConversation(conversationId, refresh: true)).called(1);
  });

  test('getConversation updates InboxCountNotifier when successful', () async {
    var api = _MockInboxApi();
    var notifier = _MockInboxNotifier();

    setupTestLocator((locator) {
      locator.registerLazySingleton<InboxApi>(() => api);
      locator.registerLazySingleton<InboxCountNotifier>(() => notifier);
    });

    when(api.getConversation(any, refresh: anyNamed('refresh'))).thenAnswer((_) => Future.value(Conversation()));

    await ConversationDetailsInteractor().getConversation('');

    verify(notifier.update()).called(1);
  });
}

class _MockInboxApi extends Mock implements InboxApi {}

class _MockInboxNotifier extends Mock implements InboxCountNotifier {}
