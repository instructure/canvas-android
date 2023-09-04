// Copyright (C) 2019 - present Instructure, Inc.
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
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/models/conversation.dart';
import 'package:flutter_parent/models/course.dart';
import 'package:flutter_parent/models/recipient.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_screen.dart';
import 'package:flutter_parent/utils/design/canvas_icons.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get_it/get_it.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/network_image_response.dart';
import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';
import '../../../utils/test_utils.dart';

/**
 * NOTE: This test file is from the before times, please don't use as reference.
 */
void main() {
  //String studentName = 'Student Name';
  String studentId = 'student_123';

  mockNetworkImageResponse();

  final l10n = AppLocalizations();

  _setupLocator(
      {int recipientCount = 4,
        AttachmentHandler? attachmentHandler,
        int fetchFailCount = 0,
        int sendFailCount = 0,
        bool pronouns = false}) async {
    await setupTestLocator((locator) {
      locator.registerFactory<CreateConversationInteractor>(
              () => _MockInteractor(recipientCount, attachmentHandler, fetchFailCount, sendFailCount, pronouns));
    });
  }

  Widget _testableWidget({Course? course, String? subject, String? postscript, MockNavigatorObserver? observer}) {
    if (course == null) course = _mockCourse('0');
    return TestApp(
      CreateConversationScreen(course.id, studentId, subject ?? course.name, postscript),
      navigatorObservers: [if (observer != null) observer],
    );
  }

  testWidgetsWithAccessibilityChecks('shows loading when retrieving participants', (tester) async {
    await _setupLocator();
    await tester.pumpWidget(_testableWidget());
    await tester.pump();
    final matchedWidget = find.byType(CircularProgressIndicator);
    expect(matchedWidget, findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('does not show loading when participants are loaded', (tester) async {
    await _setupLocator();

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    final matchedWidget = find.byType(CircularProgressIndicator);
    expect(matchedWidget, findsNothing);
  });

  testWidgetsWithAccessibilityChecks('sending disabled when no message is present', (tester) async {
    await _setupLocator();

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    final matchedWidget = find.byKey(CreateConversationScreen.sendKey);
    expect(matchedWidget, findsOneWidget);
    expect(tester.widget<IconButton>(matchedWidget).onPressed == null, isTrue);
  });

  testWidgetsWithAccessibilityChecks('Shows error state on fetch fail, allows retry', (tester) async {
    await _setupLocator(fetchFailCount: 1);

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    // Should show error message and retry button
    expect(find.text(l10n.errorLoadingRecipients), findsOneWidget);
    expect(find.text(l10n.retry), findsOneWidget);

    // Show not show attachment button
    expect(find.byTooltip(l10n.addAttachment), findsNothing);

    // Tap retry button, wait for success state
    await tester.tap(find.text(l10n.retry));
    await tester.pumpAndSettle();

    // Should no longer show error message or retry button
    expect(find.text(l10n.errorLoadingRecipients), findsNothing);
    expect(find.text(l10n.retry), findsNothing);

    // Attachment button show now be visible
    expect(find.byTooltip(l10n.addAttachment), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('can enter message text', (tester) async {
    await _setupLocator();

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    final messageText = 'Some text here';
    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, messageText);

    matchedWidget = find.text(messageText);
    expect(matchedWidget, findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('sending is enabled once message is present', (tester) async {
    await _setupLocator();

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    matchedWidget = find.byKey(CreateConversationScreen.sendKey);
    expect(matchedWidget, findsOneWidget);
    expect(tester.widget<IconButton>(matchedWidget).onPressed != null, isTrue);
  });

  testWidgetsWithAccessibilityChecks('sending is disabled when subject is empty and message is present',
      (tester) async {
    await _setupLocator();

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    // Set message body
    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');

    // Clear out subject
    matchedWidget = find.byKey(CreateConversationScreen.subjectKey);
    await tester.enterText(matchedWidget, '');
    await tester.pump();

    matchedWidget = find.byKey(CreateConversationScreen.sendKey);
    expect(matchedWidget, findsOneWidget);
    expect(tester.widget<IconButton>(matchedWidget).onPressed == null, isTrue);
  });

  testWidgetsWithAccessibilityChecks(
      'sending is disabled when attachment is uploading or failed, enabled when finished', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = _MockAttachmentHandler()..stage = AttachmentUploadStage.UPLOADING;

    await _setupLocator(attachmentHandler: handler);

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    // Set message body
    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    // Make sure send button is enabled at this point
    matchedWidget = find.byKey(CreateConversationScreen.sendKey);
    expect(tester.widget<IconButton>(matchedWidget).onPressed != null, isTrue);

    // Add attachment
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
    await tester.pump();

    // Assert sending is disabled
    matchedWidget = find.byKey(CreateConversationScreen.sendKey);
    expect(tester.widget<IconButton>(matchedWidget).onPressed == null, isTrue);

    // Move to failed state
    handler.stage = AttachmentUploadStage.FAILED;

    // Assert sending is still disabled
    matchedWidget = find.byKey(CreateConversationScreen.sendKey);
    expect(tester.widget<IconButton>(matchedWidget).onPressed == null, isTrue);

    // Move to finished state
    handler.attachment = Attachment((b) => b
      ..displayName = 'File'
      ..thumbnailUrl = 'fake url');
    handler.stage = AttachmentUploadStage.FINISHED;
    await tester.pump();

    // Sending should now be enabled
    matchedWidget = find.byKey(CreateConversationScreen.sendKey);
    expect(tester.widget<IconButton>(matchedWidget).onPressed != null, isTrue);
  });

  testWidgetsWithAccessibilityChecks(
      'sending is disabled when no participants are selected, but subject and message are present', (tester) async {
    await _setupLocator(recipientCount: 0);

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    // Set message body
    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    matchedWidget = find.byKey(CreateConversationScreen.sendKey);
    expect(matchedWidget, findsOneWidget);
    expect(tester.widget<IconButton>(matchedWidget).onPressed == null, isTrue);
  });

  testWidgetsWithAccessibilityChecks('prepopulates course name as subject', (tester) async {
    await _setupLocator();

    final course = _mockCourse('0');
    await tester.pumpWidget(_testableWidget(course: course));
    await tester.pumpAndSettle();

    var matchedWidget = find.byKey(CreateConversationScreen.subjectKey);
    expect(tester.widget<TextField>(matchedWidget).controller!.text, course.name);
  });

  testWidgetsWithAccessibilityChecks('subject can be edited', (tester) async {
    await _setupLocator();

    final course = _mockCourse('0');
    await tester.pumpWidget(_testableWidget(course: course));
    await tester.pumpAndSettle();

    var matchedWidget = find.byKey(CreateConversationScreen.subjectKey);
    await tester.enterText(matchedWidget, course.courseCode!);
    await tester.pump();

    expect(tester.widget<TextField>(matchedWidget).controller!.text, course.courseCode);
  });

  testWidgetsWithAccessibilityChecks('prepopulates recipients', (tester) async {
    final recipientCount = 2;
    await _setupLocator(recipientCount: recipientCount);

    final course = _mockCourse('0');
    await tester.pumpWidget(_testableWidget(course: course));
    await tester.pumpAndSettle();

    var matchedWidget = find.byKey(CreateConversationScreen.recipientsKey);

    // Our MockInteractor sets odd enrollments as teachers, which are the only recipients pre-populated
    expect(tester.widget<Wrap>(matchedWidget).children.length, recipientCount / 2);
  });

  testWidgetsWithAccessibilityChecks('backing out without text in the body does not show a dialog', (tester) async {
    await _setupLocator();
    final course = _mockCourse('0');

    // Load up a temp page with a button to navigate to our screen, that way the back button exists in the app bar
    await _pumpTestableWidgetWithBackButton(
      tester,
      CreateConversationScreen(course.id, studentId, '', null),
    );

    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);

    await tester.pageBack();
    await tester.pump();

    matchedWidget = find.text(l10n.unsavedChangesDialogBody);
    expect(matchedWidget, findsNothing);
  });

  testWidgetsWithAccessibilityChecks('backing out with text in the body will show confirmation dialog', (tester) async {
    await _setupLocator();
    final course = _mockCourse('0');

    await _pumpTestableWidgetWithBackButton(
      tester,
      CreateConversationScreen(course.id, studentId, '', null),
    );

    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    await tester.pageBack();
    await tester.pump();

    matchedWidget = find.text(l10n.unsavedChangesDialogBody);
    expect(matchedWidget, findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('backing out and pressing yes on the dialog closes the screen', (tester) async {
    await _setupLocator();
    final course = _mockCourse('0');
    final observer = MockNavigatorObserver();

    await _pumpTestableWidgetWithBackButton(
      tester,
      CreateConversationScreen(course.id, studentId, '', null),
      observer: observer,
    );

    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    await tester.pageBack();
    await tester.pump();

    matchedWidget = find.text(l10n.yes);
    await tester.tap(matchedWidget);

    verify(observer.didPop(any, any)).called(2); // Twice, first for the dialog, then for the screen
  });

  testWidgetsWithAccessibilityChecks('backing out and pressing no on the dialog keeps screen open', (tester) async {
    await _setupLocator();
    final course = _mockCourse('0');
    final observer = MockNavigatorObserver();

    await _pumpTestableWidgetWithBackButton(
      tester,
      CreateConversationScreen(course.id, studentId, '', null),
      observer: observer,
    );

    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    await tester.pageBack();
    await tester.pump();

    matchedWidget = find.text(l10n.no);
    await tester.tap(matchedWidget);

    verify(observer.didPop(any, any)).called(1); // Only once, for the dialog
  });

  testWidgetsWithAccessibilityChecks('backing out deletes attachments', (tester) async {
    final course = _mockCourse('0');

    // Set up attachment handler in 'uploading' stage
    var handler = MockAttachmentHandler();
    when(handler.stage).thenReturn(AttachmentUploadStage.FINISHED);
    when(handler.attachment).thenReturn(Attachment((b) => b
      ..displayName = 'File'
      ..thumbnailUrl = 'fake url'));

    await _setupLocator(attachmentHandler: handler);

    // Create page and add attachment
    await _pumpTestableWidgetWithBackButton(tester, CreateConversationScreen(course.id, studentId, '', null));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
    await tester.pumpAndSettle();

    // Attempt to go back
    await tester.pageBack();
    await tester.pump();

    // Press 'yes' on confirmation dialog
    await tester.tap(find.text(l10n.yes));
    await tester.pumpAndSettle();

    verify(handler.deleteAttachment());
  });

  testWidgetsWithAccessibilityChecks('clicking the add participants button shows the modal', (tester) async {
    await _setupLocator();
    final course = _mockCourse('0');
    final observer = MockNavigatorObserver();

    await tester.pumpWidget(_testableWidget(course: course, observer: observer));
    await tester.pumpAndSettle();

    var matchedWidget = find.byKey(CreateConversationScreen.recipientsAddKey);
    await tester.tap(matchedWidget);
    await tester.pumpAndSettle();

    matchedWidget = find.text(l10n.recipients);
    expect(matchedWidget, findsOneWidget);
    verify(observer.didPush(any, any)).called(2); // Twice, first for the initial page, then for the modal

    // Test that clicking outside closes the modal
    await tester.tapAt(Offset(0, 0));
    await tester.pumpAndSettle();
    verify(observer.didPop(any, any)).called(1); // Only once, for the modal
  });

  testWidgetsWithAccessibilityChecks('displays attachment upload state', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = _MockAttachmentHandler()
      ..stage = AttachmentUploadStage.UPLOADING
      ..progress = 0.25;

    await _setupLocator(attachmentHandler: handler);

    // Create page and add attachment
    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(CreateConversationScreen.attachmentKey));
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

    await _setupLocator(attachmentHandler: handler);

    // Create page and add attachment
    await tester.pumpWidget(_testableWidget());
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

    await _setupLocator(attachmentHandler: handler);

    // Create page and add attachment
    await tester.pumpWidget(_testableWidget());
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

  testWidgetsWithAccessibilityChecks('deleting an attachment calls AttachmentHandler.deleteAttachment', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = MockAttachmentHandler();
    when(handler.stage).thenReturn(AttachmentUploadStage.FINISHED);
    when(handler.attachment).thenReturn(Attachment((b) => b
      ..displayName = 'File'
      ..thumbnailUrl = 'fake url'));

    await _setupLocator(attachmentHandler: handler);

    // Create page and add attachment
    await tester.pumpWidget(_testableWidget());
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

    verify(handler.deleteAttachment());
  });

  testWidgetsWithAccessibilityChecks('displays attachment tooltip in upload state', (tester) async {
    // Set up attachment handler in 'uploading' stage
    var handler = AttachmentHandler(File('path/to/file.txt'))
      ..stage = AttachmentUploadStage.UPLOADING
      ..progress = 0.25;

    await _setupLocator(attachmentHandler: handler);

    // Create page and add attachment
    await tester.pumpWidget(_testableWidget());
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

    await _setupLocator(attachmentHandler: handler);

    // Create page and add attachment
    await tester.pumpWidget(_testableWidget());
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

    await _setupLocator(attachmentHandler: handler);

    // Create page and add attachment
    await tester.pumpWidget(_testableWidget());
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
    // Set up attachment handler in 'uploading' stage
    var handler = _MockAttachmentHandler()
      ..stage = AttachmentUploadStage.FINISHED
      ..attachment = Attachment((b) => b
        ..displayName = 'File'
        ..thumbnailUrl = 'fake url');

    await _setupLocator(attachmentHandler: handler);

    // Create page and add attachment
    await tester.pumpWidget(_testableWidget());
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
    await tester.pump(Duration(milliseconds: 200));
  });

  testWidgetsWithAccessibilityChecks('Expands and collapses recipient box', (tester) async {
    await _setupLocator();

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    // Should show one chip and the text '+1'
    expect(find.byType(Chip), findsOneWidget);
    expect(find.text('+1'), findsOneWidget);

    // Tap recipient box to expand
    await tester.tap(find.byKey(CreateConversationScreen.recipientsKey));
    await tester.pump();

    // Should show two recipient chips
    expect(find.byType(Chip), findsNWidgets(2));
    expect(find.text('+1'), findsNothing);
  });

  testWidgetsWithAccessibilityChecks('Selects recipients from list', (tester) async {
    await _setupLocator(recipientCount: 2); // One teacher, one student
    final course = _mockCourse('0');

    await tester.pumpWidget(_testableWidget(course: course));
    await tester.pumpAndSettle();

    // Should show one teacher recipient (User 1)
    var chip = find.byType(Chip);
    expect(chip, findsOneWidget);
    expect(find.descendant(of: chip, matching: find.text('User 1')), findsOneWidget);

    var recipientsButton = find.byKey(CreateConversationScreen.recipientsAddKey);
    await tester.tap(recipientsButton);
    await tester.pumpAndSettle();

    // Deselect User 1, select User 0
    await tester.tap(find.text('User 1').last);
    await tester.tap(find.text('User 0'));
    await tester.tapAt(Offset(0, 0));
    await tester.pumpAndSettle();

    // Should show one student recipient (User 1)
    chip = find.byType(Chip);
    expect(chip, findsOneWidget);
    expect(find.descendant(of: chip, matching: find.text('User 0')), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows error on send fail', (tester) async {
    await _setupLocator(sendFailCount: 1);
    final course = _mockCourse('0');

    await tester.pumpWidget(_testableWidget(course: course));
    await tester.pumpAndSettle();

    // Set message body
    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    await tester.tap(find.byKey(CreateConversationScreen.sendKey));
    await tester.pumpAndSettle();

    expect(find.text(l10n.errorSendingMessage), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Shows sending indicator and closes after success', (tester) async {
    await _setupLocator();
    final course = _mockCourse('0');
    final observer = MockNavigatorObserver();

    await _pumpTestableWidgetWithBackButton(
      tester,
      CreateConversationScreen(course.id, studentId, course.name, null),
    );
    await tester.pumpWidget(_testableWidget(course: course, observer: observer));
    await tester.pumpAndSettle();

    // Set message body
    var matchedWidget = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(matchedWidget, 'Some text here');
    await tester.pump();

    await tester.tap(find.byKey(CreateConversationScreen.sendKey));
    await tester.pump();
    await tester.pump();

    // Should show progressbar in app bar
    var appBar = find.byType(AppBar);
    expect(appBar, findsOneWidget);
    var progressBar = find.descendant(of: appBar, matching: find.byType(CircularProgressIndicator));
    expect(progressBar, findsOneWidget);

    await tester.pumpAndSettle();
    verify(observer.didPop(any, any)).called(1);
  });

  testWidgetsWithAccessibilityChecks('Displays pronouns for recipients', (tester) async {
    await _setupLocator(recipientCount: 2, pronouns: true); // One teacher, one student
    final course = _mockCourse('0');

    await tester.pumpWidget(_testableWidget(course: course));
    await tester.pumpAndSettle();

    // Should show one teacher recipient (User 1)
    var chip = find.byType(Chip);
    expect(chip, findsOneWidget);
    expect(find.descendant(of: chip, matching: find.text('User 1 (They/them)')), findsOneWidget);

    var recipientsButton = find.byKey(CreateConversationScreen.recipientsAddKey);
    await tester.tap(recipientsButton);
    await tester.pumpAndSettle();
  });

  testWidgetsWithAccessibilityChecks('Displays enrollment type in recipient chip', (tester) async {
    await _setupLocator(recipientCount: 2);
    final course = _mockCourse('0');

    await tester.pumpWidget(_testableWidget(course: course));
    await tester.pumpAndSettle();

    var chip = find.byType(Chip);
    expect(chip, findsOneWidget);
    expect(find.descendant(of: chip, matching: find.text('Teacher')), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Displays enrollment types', (tester) async {
    var interactor = MockCreateConversationInteractor();
    await GetIt.instance.reset();
    GetIt.instance.registerFactory<CreateConversationInteractor>(() => interactor);

    Recipient _makeRecipient(String id, String type) {
      return Recipient((b) => b
        ..id = id
        ..name = 'User $id'
        ..commonCourses = MapBuilder<String, BuiltList<String>>({
          '0': BuiltList<String>([type])
        }));
    }

    var recipients = [
      _makeRecipient('0', 'StudentEnrollment'),
      _makeRecipient('1', 'TeacherEnrollment'),
      _makeRecipient('2', 'TaEnrollment'),
      _makeRecipient('3', 'ObserverEnrollment'),
    ];

    when(interactor.loadData(any, any)).thenAnswer((_) async => CreateConversationData(_mockCourse('0'), recipients));

    await tester.pumpWidget(_testableWidget());
    await tester.pumpAndSettle();

    var recipientsButton = find.byKey(CreateConversationScreen.recipientsAddKey);
    await tester.tap(recipientsButton);
    await tester.pumpAndSettle();

    _expectRole(String userName, String enrollmentType) {
      var tile = find.ancestor(of: find.text(userName), matching: find.byType(ListTile));
      expect(tile, findsOneWidget);
      var role = find.descendant(of: tile, matching: find.text(enrollmentType));
      expect(role, findsOneWidget);
    }

    _makeTileVisible(String withText) async {
      var finder = find.ancestor(of: find.text(withText), matching: find.byType(ListTile));
      await ensureVisibleByScrolling(finder, tester, scrollFrom: ScreenVerticalLocation.MID_BOTTOM);
      await tester.pumpAndSettle();
    }

    await _makeTileVisible(l10n.enrollmentTypeStudent);
    _expectRole('User 0', l10n.enrollmentTypeStudent);
    await _makeTileVisible(l10n.enrollmentTypeTeacher);
    _expectRole('User 1', l10n.enrollmentTypeTeacher);
    await _makeTileVisible(l10n.enrollmentTypeTA);
    _expectRole('User 2', l10n.enrollmentTypeTA);
    await _makeTileVisible(l10n.enrollmentTypeObserver);
    _expectRole('User 3', l10n.enrollmentTypeObserver);

    await tester.tapAt(Offset(0, 0));
    await tester.pumpAndSettle();
  });

  testWidgetsWithAccessibilityChecks('passing in subject shows in subject text widget', (tester) async {
    await _setupLocator();

    final course = _mockCourse('0');
    final subject = 'Instructure Rocks!';

    await tester.pumpWidget(_testableWidget(course: course, subject: subject));
    await tester.pumpAndSettle();

    expect(find.text(subject), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('appends postscript if present', (tester) async {
    final course = _mockCourse('0');
    Recipient _makeRecipient(String id, String type) {
      return Recipient((b) => b
        ..id = id
        ..name = 'User $id'
        ..commonCourses = MapBuilder<String, BuiltList<String>>({
          '0': BuiltList<String>([type])
        }));
    }

    var interactor = MockCreateConversationInteractor();
    final data = CreateConversationData(course, [_makeRecipient('123', 'TeacherEnrollment')]);
    when(interactor.loadData(any, any)).thenAnswer((_) async => data);
    await GetIt.instance.reset();
    GetIt.instance.registerFactory<CreateConversationInteractor>(() => interactor);

    final subject = 'Regarding Instructure Pandas';
    final bodyText = 'Instructure Pandas';
    final postscript = 'P. S. More Pandas';
    final sendText = ['$bodyText\n\n$postscript'];

    await tester.pumpWidget(_testableWidget(course: course, subject: subject, postscript: postscript));
    await tester.pumpAndSettle();

    // Check for the subject
    expect(find.text(subject), findsOneWidget);

    // Type some text into the message body to enable the send button
    var bodyMatcher = find.byKey(CreateConversationScreen.messageKey);
    await tester.enterText(bodyMatcher, 'Instructure Pandas');
    await tester.pump();

    // Verify send button exists and is enabled
    final sendMatcher = find.byKey(CreateConversationScreen.sendKey);
    expect(sendMatcher, findsOneWidget);
    expect(tester.widget<IconButton>(sendMatcher).onPressed == null, isFalse);

    // Click on the send button
    await tester.tap(sendMatcher);
    await tester.pump();

    // Check the outgoing message for the url
    expect(verify(interactor.createConversation(any, any, any, captureAny, any)).captured, sendText);
  });
}

/// Load up a temp page with a button to navigate to our screen, that way the back button exists in the app bar
Future<void> _pumpTestableWidgetWithBackButton(tester, Widget widget, {MockNavigatorObserver? observer}) async {
  final app = TestApp(
    Builder(
      builder: (context) => TextButton(
        child: Semantics(label: 'test', child: const SizedBox()),
        onPressed: () => Navigator.of(context).push(MaterialPageRoute(builder: (context) => widget)),
      ),
    ),
    navigatorObservers: [if (observer != null) observer],
  );

  await tester.pumpWidget(app);
  await tester.pumpAndSettle();
  await tester.tap(find.byType(TextButton));
  await tester.pumpAndSettle();
  if (observer != null) {
    verify(observer.didPush(any, any)).called(2); // Twice, first for the initial page, then for the navigator route
  }
}

class _MockInteractor extends CreateConversationInteractor {
  final _recipientCount;

  final AttachmentHandler? mockAttachmentHandler;

  int _fetchFailCount;

  int _sendFailCount;

  bool _pronouns;

  _MockInteractor(this._recipientCount, this.mockAttachmentHandler, this._fetchFailCount, this._sendFailCount,
      [this._pronouns = false]);

  @override
  Future<CreateConversationData> loadData(String courseId, String? studentId) async {
    if (_fetchFailCount > 0) {
      _fetchFailCount--;
      return Future.error('Error!');
    }
    final list = List<Recipient>.generate(
        _recipientCount,
        (index) => Recipient((r) => r
          ..id = index.toString()
          ..name = 'User $index'
          ..pronouns = _pronouns ? 'They/them' : null
          ..avatarUrl = ''
          ..commonCourses = _generateCourseMap(courseId, index.toString())));

    return CreateConversationData(_mockCourse(courseId), list);
  }

  MapBuilder<String, BuiltList<String>> _generateCourseMap(String courseId, String userId) {
    return MapBuilder<String, BuiltList<String>>({
      courseId.toString(): BuiltList<String>([int.parse(userId) % 2 == 0 ? 'StudentEnrollment' : 'TeacherEnrollment'])
    });
  }

  @override
  Future<AttachmentHandler> addAttachment(BuildContext context) {
    return Future.value(mockAttachmentHandler);
  }

  @override
  Future<Conversation> createConversation(
    String courseId,
    List<String> recipientIds,
    String subject,
    String body,
    List<String> attachmentIds,
  ) {
    if (_sendFailCount > 0) {
      _sendFailCount--;
      return Future.delayed(Duration(milliseconds: 200), () => Future.error('Error!'));
    }
    return Future.delayed(Duration(milliseconds: 200), () => Conversation((b) => b));
  }
}

class _MockAttachmentHandler extends AttachmentHandler {
  _MockAttachmentHandler() : super(null);

  @override
  Future<void> performUpload() => Future.value();
}

Course _mockCourse(String courseId) {
  return Course((c) => c
    ..id = courseId
    ..name = 'Course Name'
    ..courseCode = 'Course Code'
    ..build());
}
