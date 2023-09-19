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
import 'dart:io';

import 'package:built_collection/built_collection.dart';
import 'package:built_value/json_object.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/basic_user.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/message.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_picker.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_picker_interactor.dart';
import 'package:flutter_parent/screens/inbox/conversation_details/message_widget.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/screens/inbox/reply/conversation_reply_interactor.dart';
import 'package:flutter_parent/screens/inbox/reply/conversation_reply_screen.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_screen.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/finders.dart';
import '../../../utils/network_image_response.dart';
import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';
import '../create_conversation/create_conversation_screen_test.dart';

void main() {
  mockNetworkImageResponse();

  final l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('displays "Reply" as as title for reply', (tester) async {
    await _setupInteractor();

    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();

    expect(find.descendant(of: find.byType(AppBar), matching: find.text(l10n.reply)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('displays "Reply All" as as title for reply all', (tester) async {
    await _setupInteractor();

    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, true)));
    await tester.pumpAndSettle();

    expect(find.descendant(of: find.byType(AppBar), matching: find.text(l10n.replyAll)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('displays subject as subtitle', (tester) async {
    await _setupInteractor();

    final conversation = _makeConversation();

    await tester.pumpWidget(TestApp(ConversationReplyScreen(conversation, null, false)));
    await tester.pumpAndSettle();

    expect(find.descendant(of: find.byType(AppBar), matching: find.text(conversation.subject)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('displays details of message being replied to', (tester) async {
    await _setupInteractor();

    final conversation = _makeConversation();
    final message = conversation.messages![1];

    await tester.pumpWidget(TestApp(ConversationReplyScreen(conversation, message, false)));
    await tester.pumpAndSettle();

    expect(find.descendant(of: find.byType(MessageWidget), matching: find.richText(message.body!)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('tapping attachment on message being replied to shows viewer', (tester) async {
    await _setupInteractor();
    GetIt.instance.registerLazySingleton<QuickNav>(() => QuickNav());
    GetIt.instance.registerLazySingleton<ViewAttachmentInteractor>(() => ViewAttachmentInteractor());

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

    await tester.pumpWidget(TestApp(ConversationReplyScreen(conversation, message, false)));
    await tester.pumpAndSettle();

    await tester.tap(find.byKey(Key('attachment-1')));
    await tester.pumpAndSettle();

    expect(find.byType(ViewAttachmentScreen), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('displays details of top message if no message is specified', (tester) async {
    await _setupInteractor();

    final conversation = _makeConversation();

    await tester.pumpWidget(TestApp(ConversationReplyScreen(conversation, null, false)));
    await tester.pumpAndSettle();

    final expectedMessage = conversation.messages![0];
    expect(
        find.descendant(of: find.byType(MessageWidget), matching: find.richText(expectedMessage.body!)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('sending disabled when no message is present', (tester) async {
    await _setupInteractor();

    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();

    final sendButton = find.byKey(ConversationReplyScreen.sendKey);
    expect(sendButton, findsOneWidget);
    expect(tester.widget<IconButton>(sendButton).onPressed == null, isTrue);
  });

  testWidgetsWithAccessibilityChecks('can enter message text', (tester) async {
    await _setupInteractor();

    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();

    final messageText = 'Some text here';
    var matchedWidget = find.byKey(ConversationReplyScreen.messageKey);
    await tester.enterText(matchedWidget, messageText);

    matchedWidget = find.text(messageText);
    expect(matchedWidget, findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('sending is enabled once message is present', (tester) async {
    await _setupInteractor();

    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();

    var matchedWidget = find.byKey(ConversationReplyScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    matchedWidget = find.byKey(ConversationReplyScreen.sendKey);
    expect(matchedWidget, findsOneWidget);
    expect(tester.widget<IconButton>(matchedWidget).onPressed != null, isTrue);
  });

  testWidgetsWithAccessibilityChecks('sending calls interactor with correct parameters', (tester) async {
    final interactor = await _setupInteractor();
    final conversation = _makeConversation();
    final message = conversation.messages![0];
    final replyAll = true;
    final text = 'some text here';

    await tester.pumpWidget(TestApp(ConversationReplyScreen(conversation, message, replyAll)));
    await tester.pumpAndSettle();

    await tester.enterText(find.byKey(ConversationReplyScreen.messageKey), text);
    await tester.pump();

    await tester.tap(find.byKey(ConversationReplyScreen.sendKey));
    await tester.pump();

    verify(interactor.createReply(conversation, message, text, [], replyAll));
  });

  testWidgetsWithAccessibilityChecks('backing out with text in the body will show confirmation dialog', (tester) async {
    await _setupInteractor();

    await _pumpTestableWidgetWithBackButton(tester, ConversationReplyScreen(_makeConversation(), null, false));

    var matchedWidget = find.byKey(ConversationReplyScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    await tester.pageBack();
    await tester.pump();

    matchedWidget = find.text(l10n.unsavedChangesDialogBody);
    expect(matchedWidget, findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('backing out without text in the body does not show a dialog', (tester) async {
    await _setupInteractor();
    // Load up a temp page with a button to navigate to our screen so we are able to navigate backward
    await _pumpTestableWidgetWithBackButton(tester, ConversationReplyScreen(_makeConversation(), null, false));

    await tester.pageBack();
    await tester.pump();

    expect(find.text(l10n.unsavedChangesDialogBody), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('backing out and pressing yes on the dialog closes the screen', (tester) async {
    await _setupInteractor();
    final observer = MockNavigatorObserver();

    await _pumpTestableWidgetWithBackButton(
      tester,
      ConversationReplyScreen(_makeConversation(), null, false),
      observer: observer,
    );

    await tester.enterText(find.byKey(ConversationReplyScreen.messageKey), 'Some text here');
    await tester.pump();

    await tester.pageBack();
    await tester.pump();

    expect(find.text(l10n.unsavedChangesDialogBody), findsOneWidget);
    await tester.tap(find.text(l10n.yes));
    await tester.pumpAndSettle();

    verify(observer.didPop(any, any)).called(2); // Twice, first for the dialog, then for the screen
  });

  testWidgetsWithAccessibilityChecks('choosing no on the dialog does not close the screen', (tester) async {
    await _setupInteractor();
    final observer = MockNavigatorObserver();

    await _pumpTestableWidgetWithBackButton(
      tester,
      ConversationReplyScreen(_makeConversation(), null, false),
      observer: observer,
    );

    await tester.enterText(find.byKey(ConversationReplyScreen.messageKey), 'Some text here');
    await tester.pump();

    await tester.pageBack();
    await tester.pump();

    expect(find.text(l10n.unsavedChangesDialogBody), findsOneWidget);
    await tester.tap(find.text(l10n.no));
    await tester.pump();

    verify(observer.didPop(any, any)).called(1); // Only once, for the dialog
  });

  testWidgetsWithAccessibilityChecks('Shows error on send fail', (tester) async {
    final interactor = await _setupInteractor();
    when(interactor.createReply(any, any, any, any, any)).thenAnswer((_) => Future.error(''));

    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();

    // Set message body
    var matchedWidget = find.byKey(ConversationReplyScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    await tester.tap(find.byKey(ConversationReplyScreen.sendKey));
    await tester.pumpAndSettle();

    expect(find.text(l10n.errorSendingMessage), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows sending indicator and closes after success', (tester) async {
    final interactor = await _setupInteractor();
    final observer = MockNavigatorObserver();

    await _pumpTestableWidgetWithBackButton(
      tester,
      ConversationReplyScreen(_makeConversation(), null, false),
      observer: observer,
    );

    // Set message body
    var matchedWidget = find.byKey(ConversationReplyScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    Completer<Conversation> completer = Completer();

    when(interactor.createReply(any, any, any, any, any)).thenAnswer((_) => completer.future);

    await tester.tap(find.byKey(ConversationReplyScreen.sendKey));
    await tester.pump();

    // Should show progressbar in app bar
    var progressBar = find.descendant(of: find.byType(AppBar), matching: find.byType(CircularProgressIndicator));
    expect(progressBar, findsOneWidget);

    completer.complete(_makeConversation());
    await tester.pumpAndSettle();

    verify(observer.didPop(any, any)).called(1);
  });

  testWidgetsWithAccessibilityChecks('send disabled for unsuccessful attachment, enabled on success', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = _MockAttachmentHandler()..stage = AttachmentUploadStage.UPLOADING;
    final interactor = await _setupInteractor();

    when(interactor.addAttachment(any)).thenAnswer((_) => Future.value(handler));

    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();

    // Set message body
    var matchedWidget = find.byKey(ConversationReplyScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    // Make sure send button is enabled at this point
    matchedWidget = find.byKey(ConversationReplyScreen.sendKey);
    expect(tester.widget<IconButton>(matchedWidget).onPressed != null, isTrue);

    // Add attachment
    await tester.tap(find.byKey(ConversationReplyScreen.attachmentKey));
    await tester.pump();

    // Assert sending is disabled
    matchedWidget = find.byKey(ConversationReplyScreen.sendKey);
    expect(tester.widget<IconButton>(matchedWidget).onPressed == null, isTrue);

    // Move to failed state
    handler.stage = AttachmentUploadStage.FAILED;

    // Assert sending is still disabled
    matchedWidget = find.byKey(ConversationReplyScreen.sendKey);
    expect(tester.widget<IconButton>(matchedWidget).onPressed == null, isTrue);

    // Move to finished state
    handler.attachment = Attachment((b) => b
      ..displayName = 'File'
      ..thumbnailUrl = 'fake url');
    handler.stage = AttachmentUploadStage.FINISHED;
    await tester.pump();

    // Sending should now be enabled
    matchedWidget = find.byKey(ConversationReplyScreen.sendKey);
    expect(tester.widget<IconButton>(matchedWidget).onPressed != null, isTrue);
  });

  testWidgetsWithAccessibilityChecks('displays attachment upload state', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = _MockAttachmentHandler()
      ..stage = AttachmentUploadStage.UPLOADING
      ..progress = 0.25;

    final interactor = await _setupInteractor();
    when(interactor.addAttachment(any)).thenAnswer((_) => Future.value(handler));

    // Create page and add attachment
    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();

    await tester.tap(find.byKey(ConversationReplyScreen.attachmentKey));
    await tester.pump();

    // Assert attachment widget is displayed
    var attachmentWidget = find.byType(AttachmentWidget);
    expect(attachmentWidget, findsOneWidget);

    // Assert attachment widget displays progress
    var progressWidget = find.descendant(of: attachmentWidget, matching: find.byType(CircularProgressIndicator));
    expect(tester.widget<CircularProgressIndicator>(progressWidget).value, handler.progress);
    var percentageWidget = find.descendant(of: attachmentWidget, matching: find.byType(Text));
    expect(tester.widget<Text>(percentageWidget).data, '25%');

    // Update progress
    handler.progress = 0.75;
    handler.notifyListeners();
    await tester.pump();

    // Assert progress UI is updated
    progressWidget = find.descendant(of: attachmentWidget, matching: find.byType(CircularProgressIndicator));
    expect(tester.widget<CircularProgressIndicator>(progressWidget).value, handler.progress);
    percentageWidget = find.descendant(of: attachmentWidget, matching: find.byType(Text));
    expect(tester.widget<Text>(percentageWidget).data, '75%');
  });

  testWidgetsWithAccessibilityChecks('displays attachment failed state with retry and delete options', (tester) async {
    // Set up attachment handler in 'failed' stage
    var handler = _MockAttachmentHandler()..stage = AttachmentUploadStage.FAILED;

    final interactor = await _setupInteractor();
    when(interactor.addAttachment(any)).thenAnswer((_) => Future.value(handler));

    // Create page and add attachment
    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
    await tester.pump();

    // Assert attachment widget is displayed
    var attachmentWidget = find.byType(AttachmentWidget);
    expect(attachmentWidget, findsOneWidget);

    // Assert attachment widget displays failed state
    var warningIcon = find.descendant(of: attachmentWidget, matching: find.byType(Icon));
    expect(tester.widget<Icon>(warningIcon).icon, CanvasIcons.warning);
    expect(find.text(l10n.attachmentFailed), findsOneWidget);

    // Tap attachment
    await tester.tap(attachmentWidget);
    await tester.pumpAndSettle();

    // Tap retry button and move to uploading state
    await tester.tap(find.text(l10n.retry));
    handler.stage = AttachmentUploadStage.UPLOADING;
    handler.progress = 0.25;
    handler.notifyListeners();
    await tester.pump();

    // Assert attachment widget displays progress
    attachmentWidget = find.byType(AttachmentWidget);
    var progressWidget = find.descendant(of: attachmentWidget, matching: find.byType(CircularProgressIndicator));
    expect(tester.widget<CircularProgressIndicator>(progressWidget).value, handler.progress);
    var percentageWidget = find.descendant(of: attachmentWidget, matching: find.byType(Text));
    expect(tester.widget<Text>(percentageWidget).data, '25%');

    // Move to failed state
    handler.stage = AttachmentUploadStage.FAILED;
    handler.notifyListeners();
    await tester.pump();

    // Tap delete button
    attachmentWidget = find.byType(AttachmentWidget);
    await tester.tap(attachmentWidget);
    await tester.pumpAndSettle();
    await tester.tap(find.text(l10n.delete));
    await tester.pumpAndSettle();

    // Ensure attachment widget has been removed
    attachmentWidget = find.byType(AttachmentWidget);
    expect(attachmentWidget, findsNothing);
  });

  testWidgetsWithAccessibilityChecks('can delete successful attachment', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = _MockAttachmentHandler()
      ..stage = AttachmentUploadStage.FINISHED
      ..attachment = Attachment((b) => b
        ..displayName = 'File'
        ..thumbnailUrl = 'fake url');

    final interactor = await _setupInteractor();
    when(interactor.addAttachment(any)).thenAnswer((_) => Future.value(handler));

    // Create page and add attachment
    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
    await tester.pump();

    // Assert attachment widget is displayed
    var attachmentWidget = find.byType(AttachmentWidget);
    expect(attachmentWidget, findsOneWidget);

    // Tap delete button
    await tester.tap(attachmentWidget);
    await tester.pumpAndSettle();
    await tester.tap(find.text(l10n.delete));
    await tester.pumpAndSettle();

    // Ensure attachment widget has been removed
    attachmentWidget = find.byType(AttachmentWidget);
    expect(attachmentWidget, findsNothing);
  });

  testWidgetsWithAccessibilityChecks('displays attachment tooltip in upload state', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = AttachmentHandler(File('path/to/file.txt'))
      ..stage = AttachmentUploadStage.UPLOADING
      ..progress = 0.25;

    final interactor = await _setupInteractor();
    when(interactor.addAttachment(any)).thenAnswer((_) => Future.value(handler));

    // Create page and add attachment
    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
    await tester.pump();
    await tester.pumpAndSettle();

    // Assert attachment widget is displayed
    var attachmentWidget = find.byType(AttachmentWidget);
    expect(attachmentWidget, findsOneWidget);

    await tester.longPress(attachmentWidget);
    await tester.pump(Duration(milliseconds: 100));
    await tester.pumpAndSettle();

    expect(find.text('file.txt'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('displays attachment tooltip in failed state', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = AttachmentHandler(File('path/to/file.txt'))..stage = AttachmentUploadStage.FAILED;

    final interactor = await _setupInteractor();
    when(interactor.addAttachment(any)).thenAnswer((_) => Future.value(handler));

    // Create page and add attachment
    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
    await tester.pump();
    await tester.pumpAndSettle();

    // Assert attachment widget is displayed
    var attachmentWidget = find.byType(AttachmentWidget);
    expect(attachmentWidget, findsOneWidget);

    await tester.longPress(attachmentWidget);
    await tester.pump(Duration(milliseconds: 100));
    await tester.pumpAndSettle();

    expect(find.text('file.txt'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('displays attachment tooltip in finished state', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = AttachmentHandler(File('path/to/file.txt'))
      ..attachment = Attachment((b) => b..displayName = 'upload.txt')
      ..stage = AttachmentUploadStage.FINISHED;

    final interactor = await _setupInteractor();
    when(interactor.addAttachment(any)).thenAnswer((_) => Future.value(handler));

    // Create page and add attachment
    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
    await tester.pump();
    await tester.pumpAndSettle();

    // Assert attachment widget is displayed
    var attachmentWidget = find.byType(AttachmentWidget);
    expect(attachmentWidget, findsOneWidget);

    await tester.longPress(attachmentWidget);
    await tester.pump(Duration(milliseconds: 100));
    await tester.pumpAndSettle();

    expect(find.text('upload.txt'), findsNWidgets(2)); // 2 widgets: one is the tooltip and one is the regular label
  });

  testWidgetsWithAccessibilityChecks('disables attachment interactions while sending', (tester) async {
    var handler = AttachmentHandler(File('path/to/file.txt'))
      ..attachment = Attachment((b) => b..displayName = 'upload.txt')
      ..stage = AttachmentUploadStage.FINISHED;

    final interactor = await _setupInteractor();
    when(interactor.addAttachment(any)).thenAnswer((_) => Future.value(handler));

    Completer<Conversation> completer = Completer();
    when(interactor.createReply(any, any, any, any, any)).thenAnswer((_) => completer.future);

    // Create page and add attachment
    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
    await tester.pump();

    // Assert attachment widget is displayed
    var attachmentWidget = find.byType(AttachmentWidget);
    expect(attachmentWidget, findsOneWidget);

    // Tap attachment
    await tester.tap(attachmentWidget);
    await tester.pumpAndSettle();

    // Should display delete option
    expect(find.text(l10n.delete), findsOneWidget);

    // Tap outside to dismiss attachment options
    await tester.tapAt(Offset(0, 0));
    await tester.pumpAndSettle();

    // Add text to enable sending
    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    // Tap send button
    await tester.tap(find.byKey(CreateConversationScreen.sendKey));
    await tester.pump();

    // Tap attachment
    await tester.tap(attachmentWidget);
    await tester.pump(Duration(milliseconds: 150));

    // Should not display delete option
    expect(find.text(l10n.delete), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('tapping attachment button shows AttachmentPicker', (tester) async {
    final interactor = await _setupInteractor();
    GetIt.instance.registerLazySingleton<AttachmentPickerInteractor>(() => AttachmentPickerInteractor());
    when(interactor.addAttachment(any))
        .thenAnswer((answer) => ConversationReplyInteractor().addAttachment(answer.positionalArguments[0]));

    // Create page
    await tester.pumpWidget(TestApp(ConversationReplyScreen(_makeConversation(), null, false)));
    await tester.pumpAndSettle();

    // Tap attachment button
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
    await tester.pumpAndSettle();

    // AttachmentPicker should be displayed
    expect(find.byType(AttachmentPicker), findsOneWidget);
  });
}

/// Load up a temp page with a button to navigate to our screen, that way the back button exists in the app bar
Future<void> _pumpTestableWidgetWithBackButton(tester, Widget widget, {MockNavigatorObserver? observer}) async {
  if (observer == null) observer = MockNavigatorObserver();
  final app = TestApp(
    Builder(
      builder: (context) => TextButton(
        child: Semantics(label: 'test', child: const SizedBox()),
        onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (context) => widget)),
      ),
    ),
    navigatorObservers: [observer],
  );

  await tester.pumpWidget(app);
  await tester.pumpAndSettle();
  await tester.tap(find.byType(TextButton));
  await tester.pumpAndSettle();
  verify(observer.didPush(any, any)).called(2); // Twice, first for the initial page, then for the navigator route
}

Future<MockConversationReplyInteractor> _setupInteractor() async {
  final interactor = MockConversationReplyInteractor();
  await setupTestLocator((locator) {
    locator.registerFactory<ConversationReplyInteractor>(() => interactor);
  });
  when(interactor.getCurrentUserId()).thenReturn('self');
  return interactor;
}

Conversation _makeConversation() {
  return Conversation((c) => c
    ..id = 'conversation1'
    ..subject = 'Conversation subject'
    ..participants = ListBuilder([
      BasicUser((b) => b
        ..id = 'self'
        ..name = 'Myself'),
      BasicUser((b) => b
        ..id = 'author1'
        ..name = 'User 1'),
      BasicUser((b) => b
        ..id = 'author2'
        ..name = 'User 1'),
    ])
    ..messages = ListBuilder([
      Message((m) => m
        ..id = 'message2'
        ..body = 'This is message 2'
        ..createdAt = DateTime.now()
        ..authorId = 'author2'
        ..participatingUserIds = ListBuilder(['self', 'author1', 'author2'])),
      Message((m) => m
        ..id = 'message1'
        ..body = 'This is message 1'
        ..createdAt = DateTime.now()
        ..authorId = 'author1'
        ..participatingUserIds = ListBuilder(['self', 'author1', 'author2'])),
    ]));
}

class _MockAttachmentHandler extends AttachmentHandler {
  _MockAttachmentHandler() : super(null);

  @override
  Future<void> performUpload() => Future.value();
}
