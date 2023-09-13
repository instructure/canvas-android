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
import 'package:built_value/json_object.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/media_comment.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/message_widget.dart';
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

  final String currentUserId = '123';

  group('Displays correct Author info', () {
    testWidgetsWithAccessibilityChecks('for monologue message', (tester) async {
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byKey(Key('author-info')), findsOneWidget);

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'Me');
    });

    testWidgetsWithAccessibilityChecks('for message to one other', (tester) async {
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
          BasicUser((b) => b
            ..id = '456'
            ..name = 'User 1'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byKey(Key('author-info')), findsOneWidget);

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'Me to User 1');
    });

    testWidgetsWithAccessibilityChecks('for message to multiple others', (tester) async {
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456', '789', '111', '222']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
          BasicUser((b) => b
            ..id = '456'
            ..name = 'User 1'),
          BasicUser((b) => b
            ..id = '789'
            ..name = 'User 2'),
          BasicUser((b) => b
            ..id = '111'
            ..name = 'User 3'),
          BasicUser((b) => b
            ..id = '422256'
            ..name = 'User 4'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byKey(Key('author-info')), findsOneWidget);

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'Me to 4 others');
    });

    testWidgetsWithAccessibilityChecks('expands to show participant info', (tester) async {
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456', '789', '111', '222']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Author'),
          BasicUser((b) => b
            ..id = '456'
            ..name = 'User 1'),
          BasicUser((b) => b
            ..id = '789'
            ..name = 'User 2'),
          BasicUser((b) => b
            ..id = '111'
            ..name = 'User 3'),
          BasicUser((b) => b
            ..id = '222'
            ..name = 'User 4'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      // Should not display participant info by default
      expect(find.byKey(Key('participants')), findsNothing);

      // Tap header to expand participant info
      await tester.tap(find.byKey(Key('message-header')));
      await tester.pumpAndSettle();

      // Participant info show now be displayed
      var participants = find.byKey(Key('participants'));
      expect(participants, findsOneWidget);

      // Should show all non-author participants
      expect(find.descendant(of: participants, matching: find.text('Author')), findsNothing);
      expect(find.descendant(of: participants, matching: find.text('User 1')), findsOneWidget);
      expect(find.descendant(of: participants, matching: find.text('User 2')), findsOneWidget);
      expect(find.descendant(of: participants, matching: find.text('User 3')), findsOneWidget);
      expect(find.descendant(of: participants, matching: find.text('User 4')), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('does not expand participant info for monologue', (tester) async {
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Author'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      // Should not display participant info by default
      expect(find.byKey(Key('participants')), findsNothing);

      // Tap header to attempt expanding participant info
      await tester.tap(find.byKey(Key('message-header')));
      await tester.pumpAndSettle();

      // Participant info should still not be displayed
      expect(find.byKey(Key('participants')), findsNothing);
    });

    testWidgetsWithAccessibilityChecks('for message from another', (tester) async {
      final message = Message((m) => m
        ..authorId = '456'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
          BasicUser((b) => b
            ..id = '456'
            ..name = 'User 1'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byKey(Key('author-info')), findsOneWidget);

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'User 1 to me');
    });

    testWidgetsWithAccessibilityChecks('for message from another to multiple others', (tester) async {
      final message = Message((m) => m
        ..authorId = '456'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456', '789', '111', '222']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
          BasicUser((b) => b
            ..id = '456'
            ..name = 'User 1'),
          BasicUser((b) => b
            ..id = '789'
            ..name = 'User 2'),
          BasicUser((b) => b
            ..id = '111'
            ..name = 'User 3'),
          BasicUser((b) => b
            ..id = '422256'
            ..name = 'User 4'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byKey(Key('author-info')), findsOneWidget);

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'User 1 to me & 3 others');
    });

    testWidgetsWithAccessibilityChecks('with pronoun', (tester) async {
      final message = Message((m) => m
        ..authorId = '456'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
          BasicUser((b) => b
            ..id = '456'
            ..pronouns = 'pro/noun'
            ..name = 'User 1'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byKey(Key('author-info')), findsOneWidget);

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'User 1 (pro/noun) to me');
    });

    testWidgetsWithAccessibilityChecks('for message to one other with pronouns', (tester) async {
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
          BasicUser((b) => b
            ..id = '456'
            ..pronouns = 'pro/noun'
            ..name = 'User 1'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byKey(Key('author-info')), findsOneWidget);

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'Me to User 1 (pro/noun)');
    });

    testWidgetsWithAccessibilityChecks('for message to unknown user', (tester) async {
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byKey(Key('author-info')), findsOneWidget);

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'Me to Unknown User');
    });

    testWidgetsWithAccessibilityChecks('for message from unknown user', (tester) async {
      final message = Message((m) => m
        ..authorId = '456'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.byKey(Key('author-info')), findsOneWidget);

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'Unknown User to me');
    });
  });

  group('Displays message details', () {
    testWidgetsWithAccessibilityChecks('author info does not cause overflow', (tester) async {
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..participatingUserIds = ListBuilder(['123', '456']));

      // Make a really long name
      final longUserName = List.generate(100, (_) => 'Name').join(' ');

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
          BasicUser((b) => b
            ..id = '456'
            ..name = longUserName),
        ]));

      await tester.pumpWidget(
        TestApp(
          SingleChildScrollView(
              child: MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId)),
        ),
      );
      await tester.pumpAndSettle();

      var widget = find.byKey(Key('author-info')).evaluate().first.widget as Text;
      expect(widget.textSpan?.toPlainText(), 'Me to $longUserName');
      // At this point the test should have succeeded without throwing an overflow error
    });

    testWidgetsWithAccessibilityChecks('message date', (tester) async {
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
        ..body = ''
        ..participatingUserIds = ListBuilder(['123']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      var date = find.byKey(Key('message-date'));
      expect(date, findsOneWidget);

      var dateText = await tester.widget(date) as Text;
      expect(dateText.data, 'Dec 25 at 8:34 AM');
    });

    testWidgetsWithAccessibilityChecks('message body', (tester) async {
      final body = 'This is a message body';
      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
        ..body = body
        ..participatingUserIds = ListBuilder(['123']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.richText(body), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('attachments', (tester) async {
      final attachments = [
        Attachment((b) => b
          ..jsonId = JsonObject('1')
          ..displayName = 'Attachment 1'),
        Attachment((b) => b
          ..jsonId = JsonObject('2')
          ..thumbnailUrl = 'https://fake.url.com'
          ..displayName = 'Attachment 2'),
      ];

      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
        ..body = ''
        ..attachments = ListBuilder(attachments)
        ..participatingUserIds = ListBuilder(['123']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      var attachment1 = find.byKey(Key('attachment-1'));
      expect(attachment1, findsOneWidget);
      expect(find.descendant(of: attachment1, matching: find.text(attachments[0].displayName!)), findsOneWidget);
      expect(find.descendant(of: attachment1, matching: find.byType(FadeInImage)), findsNothing);

      var attachment2 = find.byKey(Key('attachment-2'));
      expect(attachment2, findsOneWidget);
      expect(find.descendant(of: attachment2, matching: find.text(attachments[1].displayName!)), findsOneWidget);
      expect(find.descendant(of: attachment2, matching: find.byType(FadeInImage)), findsOneWidget);
    });

    testWidgetsWithAccessibilityChecks('media comment as attachment', (tester) async {
      final mediaComment = MediaCommentBuilder()
        ..mediaId = 'fake-id'
        ..displayName = 'Display Name'
        ..url = 'fake url'
        ..mediaType = MediaType.video
        ..contentType = 'video/mp4';

      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
        ..body = ''
        ..mediaComment = mediaComment
        ..participatingUserIds = ListBuilder(['123']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
        ),
      );
      await tester.pumpAndSettle();

      var attachment1 = find.byKey(Key('attachment-media-comment-fake-id'));
      expect(attachment1, findsOneWidget);
      expect(find.descendant(of: attachment1, matching: find.text(mediaComment.displayName!)), findsOneWidget);
    });
  });

  group('interactions', () {
    testWidgetsWithAccessibilityChecks('Clicking attachment invokes callback', (tester) async {
      final attachment = Attachment((b) => b
        ..jsonId = JsonObject('1')
        ..displayName = 'Attachment 1');

      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..attachments = ListBuilder([attachment])
        ..participatingUserIds = ListBuilder(['123']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      Attachment? actual = null;

      await tester.pumpWidget(
        TestApp(
          MessageWidget(
            conversation: conversation,
            message: message,
            currentUserId: currentUserId,
            onAttachmentClicked: (attachment) {
              actual = attachment;
            },
          ),
        ),
      );
      await tester.pumpAndSettle();
      await tester.tap(find.byKey(Key('attachment-1')));
      expect(actual, attachment);
    });

    // Can't use a11y checks here because it doesn't account for scroll offset and fails when an attachment is partially scrolled off screen
    testWidgets('Scroll to view additional attachments', (tester) async {
      final attachments = List.generate(
          30,
          (index) => Attachment((b) => b
            ..jsonId = JsonObject(index)
            ..displayName = 'Attachment $index'));

      final message = Message((m) => m
        ..authorId = '123'
        ..createdAt = DateTime.now()
        ..body = ''
        ..attachments = ListBuilder(attachments)
        ..participatingUserIds = ListBuilder(['123']));

      final conversation = Conversation((c) => c
        ..messages = ListBuilder([message])
        ..participants = ListBuilder([
          BasicUser((b) => b
            ..id = '123'
            ..name = 'Myself'),
        ]));

      await tester.pumpWidget(
        TestApp(
          MessageWidget(
            conversation: conversation,
            message: message,
            currentUserId: currentUserId,
            onAttachmentClicked: (attachment) {},
          ),
        ),
      );
      await tester.pumpAndSettle();

      var firstAttachment = find.byKey(Key('attachment-0'));
      var lastAttachment = find.byKey(Key('attachment-29'));
      var attachmentList = find.byKey(Key('message_attachment_list'));

      expect(firstAttachment, findsOneWidget);
      expect(lastAttachment, findsNothing);

      await tester.drag(attachmentList, Offset(-5000, 0));
      await tester.pumpAndSettle();

      expect(firstAttachment, findsNothing);
      expect(lastAttachment, findsOneWidget);
    });

    // TODO Fix test
    testWidgetsWithAccessibilityChecks(
      'links are selectable',
      (tester) async {
        final nav = MockQuickNav();
        setupTestLocator((locator) => locator.registerLazySingleton<QuickNav>(() => nav));

        final url = 'https://www.google.com';
        final body = 'Tap this $url link here';
        final message = Message((m) => m
          ..authorId = '123'
          ..createdAt = DateTime(2020, 12, 25, 8, 34, 0, 0, 0)
          ..body = body
          ..participatingUserIds = ListBuilder(['123']));

        final conversation = Conversation((c) => c
          ..messages = ListBuilder([message])
          ..participants = ListBuilder([
            BasicUser((b) => b
              ..id = '123'
              ..name = 'Myself'),
          ]));

        await tester.pumpWidget(
          TestApp(
            MessageWidget(conversation: conversation, message: message, currentUserId: currentUserId),
          ),
        );
        await tester.pumpAndSettle();

        // Tap link
        await tester.tap(find.richText(body));
        await tester.pumpAndSettle();

        verify(nav.routeInternally(any, url));
      },
      a11yExclusions: {A11yExclusion.minTapSize}, skip: true // inline links are not required to meet the min tap target size
    );
  });
}