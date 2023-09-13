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

import 'dart:async';

import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/conversation_details_interactor.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/conversation_details_screen.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/message_widget.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/finders.dart';
import '../../../utils/network_image_response.dart';
import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  mockNetworkImageResponse();

  final AppLocalizations l10n = AppLocalizations();

  group('Displays messages', () {
    testWidgetsWithAccessibilityChecks('displays single message', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));

      var conversation = Conversation((c) => c
        ..messages = ListBuilder([
          Message((m) => m
            ..authorId = '123'
            ..createdAt = DateTime.now()
            ..body = 'This is a message'
            ..attachments = ListBuilder([])
            ..participatingUserIds = ListBuilder(['123']))
        ])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(conversation));
      when(interactor.getCurrentUserId()).thenReturn('123');

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();

      var messageWidget = find.byType(MessageWidget);
      expect(messageWidget, findsOneWidget);
      expect(
          find.descendant(of: messageWidget, matching: find.richText(conversation.messages![0].body!)), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('displays multiple messages in correct order', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));

      var conversation = Conversation((c) => c
        ..messages = ListBuilder([
          Message((m) => m
            ..authorId = '123'
            ..createdAt = DateTime.now().subtract(Duration(seconds: 5))
            ..body = 'This is message 1'
            ..attachments = ListBuilder([])
            ..participatingUserIds = ListBuilder(['123'])),
          Message((m) => m
            ..authorId = '124'
            ..createdAt = DateTime.now().subtract(Duration(minutes: 1))
            ..body = 'This is message 2'
            ..attachments = ListBuilder([])
            ..participatingUserIds = ListBuilder(['123'])),
          Message((m) => m
            ..authorId = '125'
            ..createdAt = DateTime.now().subtract(Duration(minutes: 5))
            ..body = 'This is message 3'
            ..attachments = ListBuilder([])
            ..participatingUserIds = ListBuilder(['123']))
        ])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
          BasicUser((b) => b
            ..id = '124'
            ..name = 'Some Person'),
          BasicUser((b) => b
            ..id = '125'
            ..name = 'Some Other Person'),
        ]));

      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(conversation));
      when(interactor.getCurrentUserId()).thenReturn('123');

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();

      // Should have three MessageWidgets
      expect(find.byType(MessageWidget).evaluate().length, 3);

      // Get widget positions in the same order as the messages in the conversation
      var messageWidgetOffsets = conversation.messages!
          .map((it) => find.ancestor(of: find.richText(it.body!), matching: find.byType(MessageWidget)))
          .map((it) => tester.getTopLeft(it))
          .toList();

      // The second message should be displayed below the first message
      expect(messageWidgetOffsets[1].dy, greaterThan(messageWidgetOffsets[0].dy));

      // The third message should be displayed below the second message
      expect(messageWidgetOffsets[2].dy, greaterThan(messageWidgetOffsets[1].dy));
    });
  });

  group('Displays base details', () {
    testWidgetsWithAccessibilityChecks('loading state', (tester) async {
      final subject = 'This is a subject';
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));

      Completer<Conversation> completer = Completer();
      when(interactor.getConversation(any)).thenAnswer((_) => completer.future);

      await tester.pumpWidget(
        TestApp(ConversationDetailsScreen(conversationId: '', conversationSubject: subject, courseName: '')),
      );
      await tester.pump();

      expect(find.byType(LoadingIndicator), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('conversation subject', (tester) async {
      final subject = 'This is a subject';
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));
      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(Conversation()));

      await tester.pumpWidget(
        TestApp(ConversationDetailsScreen(conversationId: '', conversationSubject: subject, courseName: '')),
      );
      await tester.pumpAndSettle();

      expect(find.descendant(of: find.byType(AppBar), matching: find.text(subject)), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('course name', (tester) async {
      final courseName = 'BIO 101';
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));
      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(Conversation()));

      await tester.pumpWidget(
        TestApp(ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: courseName)),
      );
      await tester.pumpAndSettle();

      expect(find.descendant(of: find.byType(AppBar), matching: find.text(courseName)), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('reply FAB', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));
      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(Conversation()));

      await tester.pumpWidget(
        TestApp(ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: '')),
      );
      await tester.pumpAndSettle();

      expect(find.byType(FloatingActionButton), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('error state', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));
      when(interactor.getConversation(any)).thenAnswer((_) => Future.error(''));

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();

      var errorWidget = find.byType(ErrorPandaWidget);
      var errorMessage = find.descendant(of: errorWidget, matching: find.text(l10n.errorLoadingConversation));

      expect(errorWidget, findsOneWidget);
      expect(errorMessage, findsOneWidget);
      expect(find.byType(FloatingActionButton), findsNothing);
    });
  });

  group('interactions', () {
    testWidgetsWithAccessibilityChecks('Displays conversation reply options', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));
      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(Conversation()));

      await tester.pumpWidget(
        TestApp(ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: '')),
      );
      await tester.pumpAndSettle();

      var fab = find.byType(FloatingActionButton);
      expect(fab, findsOneWidget);

      await tester.tap(fab);
      await tester.pumpAndSettle();

      expect(find.text(l10n.reply), findsOneWidget);
      expect(find.text(l10n.replyAll), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Clicking attachment calls interactor', (tester) async {
      final attachments = [
        Attachment((b) => b
          ..jsonId = JsonObject('1')
          ..displayName = 'Attachment 1'),
      ];
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));

      var conversation = Conversation((c) => c
        ..messages = ListBuilder([
          Message((m) => m
            ..authorId = '123'
            ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
            ..body = ''
            ..attachments = ListBuilder(attachments)
            ..participatingUserIds = ListBuilder(['123']))
        ])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(conversation));
      when(interactor.getCurrentUserId()).thenReturn('123');

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();

      var attachment1 = find.byKey(Key('attachment-1'));
      expect(attachment1, findsOneWidget);

      await tester.tap(attachment1);

      verify(interactor.viewAttachment(any, attachments[0])).called(1);
    });

    testWidgetsWithAccessibilityChecks('Replying to conversation calls interactor', (tester) async {
      final conversation = Conversation();
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));
      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(conversation));

      await tester.pumpWidget(
        TestApp(ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: '')),
      );
      await tester.pumpAndSettle();

      var fab = find.byType(FloatingActionButton);
      expect(fab, findsOneWidget);

      await tester.tap(fab);
      await tester.pumpAndSettle();

      await tester.tap(find.text(l10n.reply));
      await tester.pumpAndSettle();

      verify(interactor.addReply(any, conversation, null, false)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Replying all to conversation calls interactor', (tester) async {
      final conversation = Conversation();
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));
      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(conversation));

      await tester.pumpWidget(
        TestApp(ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: '')),
      );
      await tester.pumpAndSettle();

      var fab = find.byType(FloatingActionButton);
      expect(fab, findsOneWidget);

      await tester.tap(fab);
      await tester.pumpAndSettle();

      await tester.tap(find.text(l10n.replyAll));
      await tester.pumpAndSettle();

      verify(interactor.addReply(any, conversation, null, true)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Swiping message reveals reply options', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));

      var conversation = Conversation((c) => c
        ..messages = ListBuilder([
          Message((m) => m
            ..id = '1'
            ..authorId = '123'
            ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
            ..body = ''
            ..participatingUserIds = ListBuilder(['123']))
        ])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(conversation));
      when(interactor.getCurrentUserId()).thenReturn('123');

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();
      await tester.drag(find.byKey(Key('message-1')), Offset(-300, 0));
      await tester.pumpAndSettle();

      expect(find.text(l10n.reply), findsOneWidget);
      expect(find.text(l10n.replyAll), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('Replying to individual message calls interactor', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));

      var conversation = Conversation((c) => c
        ..messages = ListBuilder([
          Message((m) => m
            ..id = '1'
            ..authorId = '123'
            ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
            ..body = ''
            ..participatingUserIds = ListBuilder(['123']))
        ])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(conversation));
      when(interactor.getCurrentUserId()).thenReturn('123');

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();
      await tester.drag(find.byKey(Key('message-1')), Offset(-300, 0));
      await tester.pumpAndSettle();

      await tester.tap(find.text(l10n.reply));
      await tester.pumpAndSettle();

      verify(interactor.addReply(any, conversation, conversation.messages![0], false)).called(1);
    });

    testWidgetsWithAccessibilityChecks('Replying all to individual message calls interactor', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));

      var conversation = Conversation((c) => c
        ..messages = ListBuilder([
          Message((m) => m
            ..id = '1'
            ..authorId = '123'
            ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
            ..body = ''
            ..participatingUserIds = ListBuilder(['123']))
        ])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(conversation));
      when(interactor.getCurrentUserId()).thenReturn('123');

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();
      await tester.drag(find.byKey(Key('message-1')), Offset(-300, 0));
      await tester.pumpAndSettle();

      await tester.tap(find.text(l10n.replyAll));
      await tester.pumpAndSettle();

      verify(interactor.addReply(any, conversation, conversation.messages![0], true)).called(1);
    });

    testWidgetsWithAccessibilityChecks('error state refresh calls interactor', (tester) async {
      final conversationId = '100';
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));
      when(interactor.getConversation(any)).thenAnswer((_) => Future.error(''));

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: conversationId, conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();

      verify(interactor.getConversation(conversationId)).called(1);

      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(Conversation()));
      await tester.tap(find.text(l10n.retry));
      await tester.pumpAndSettle();

      verify(interactor.getConversation(conversationId)).called(1);
      expect(find.byType(ErrorPandaWidget), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('pull-to-refresh calls interactor', (tester) async {
      final conversationId = '100';
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));
      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(Conversation()));

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: conversationId, conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();

      verify(interactor.getConversation(conversationId)).called(1);

      await tester.drag(find.byType(RefreshIndicator), Offset(0, 300));
      await tester.pumpAndSettle();

      verify(interactor.getConversation(conversationId)).called(1);
    });

    testWidgetsWithAccessibilityChecks('updates after adding message', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) => locator.registerFactory<ConversationDetailsInteractor>(() => interactor));

      var message1Body = 'Original message';
      var message2Body = 'Message reply';

      var conversation = Conversation((c) => c
        ..messages = ListBuilder([
          Message((m) => m
            ..id = '1'
            ..authorId = '123'
            ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
            ..body = message1Body
            ..participatingUserIds = ListBuilder(['123']))
        ])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      var updatedConversation = conversation.rebuild((c) => c
        ..messages = ListBuilder([
          c.messages[0],
          c.messages[0].rebuild((m) => m
            ..body = message2Body
            ..createdAt = DateTime.now()),
        ]));

      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(conversation));
      when(interactor.addReply(any, any, any, any)).thenAnswer((_) => Future.value(updatedConversation));

      await tester.pumpWidget(
        TestApp(
          ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.richText(message1Body), findsOneWidget);
      expect(find.richText(message2Body), findsNothing);

      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();
      await tester.tap(find.text(l10n.reply));
      await tester.pumpAndSettle();

      expect(find.richText(message1Body), findsOneWidget);
      expect(find.richText(message2Body), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('route returns true if conversation was updated', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) {
        locator.registerFactory<ConversationDetailsInteractor>(() => interactor);
      });
      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(Conversation()));
      when(interactor.addReply(any, any, any, any)).thenAnswer((_) => Future.value(Conversation()));

      var returnValue = false;

      await tester.pumpWidget(
        TestApp(
          Builder(
            builder: (context) => Material(
              child: TextButton(
                child: Text('Click me'),
                onPressed: () async {
                  returnValue = await QuickNav().push(
                    context,
                    ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
                  );
                },
              ),
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      await tester.tap(find.byType(InkWell));
      await tester.pumpAndSettle();

      await tester.tap(find.byType(FloatingActionButton));
      await tester.pumpAndSettle();
      await tester.tap(find.text(l10n.reply));
      await tester.pumpAndSettle();
      await tester.tap(find.byType(BackButton));
      await tester.pumpAndSettle();

      expect(returnValue, isTrue);
    });

    testWidgetsWithAccessibilityChecks('route returns false if conversation was not updated', (tester) async {
      var interactor = MockConversationDetailsInteractor();
      setupTestLocator((locator) {
        locator.registerFactory<ConversationDetailsInteractor>(() => interactor);
      });
      when(interactor.getConversation(any)).thenAnswer((_) => Future.value(Conversation()));

      var returnValue = false;

      await tester.pumpWidget(
        TestApp(
          Builder(
            builder: (context) => Material(
              child: TextButton(
                child: Text('Click me'),
                onPressed: () async {
                  returnValue = await QuickNav().push(
                    context,
                    ConversationDetailsScreen(conversationId: '', conversationSubject: '', courseName: ''),
                  );
                },
              ),
            ),
          ),
        ),
      );
      await tester.pumpAndSettle();

      await tester.tap(find.byType(InkWell));
      await tester.pumpAndSettle();

      await tester.tap(find.byType(BackButton));
      await tester.pumpAndSettle();

      expect(returnValue, isFalse);
    });
  });
}
