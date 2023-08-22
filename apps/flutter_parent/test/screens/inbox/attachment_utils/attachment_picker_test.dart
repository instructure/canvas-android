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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_handler.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_picker.dart';
import 'package:flutter_parent/screens/inbox/attachment_utils/attachment_picker_interactor.dart';
import 'package:flutter_parent/screens/inbox/create_conversation/create_conversation_interactor.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../utils/accessibility_utils.dart';
import '../../../utils/test_app.dart';
import '../../../utils/test_helpers/mock_helpers.mocks.dart';

void main() {
  final l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('Tapping camera option shows preparing UI', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    when(interactor.getImageFromCamera()).thenAnswer((_) => Completer<File>().future);

    await tester.pumpWidget(TestApp(AttachmentPicker()));
    await tester.pump();

    // Tap camera option
    await tester.tap(find.text(l10n.useCamera));
    await tester.pump();

    expect(find.text(l10n.attachmentPreparing), findsOneWidget);
    expect(find.byType(LinearProgressIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Tapping gallery option shows preparing UI', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    when(interactor.getImageFromGallery()).thenAnswer((_) => Completer<File>().future);

    await tester.pumpWidget(TestApp(AttachmentPicker()));
    await tester.pump();

    // Tap gallery option
    await tester.tap(find.text(l10n.chooseFromGallery));
    await tester.pump();

    expect(find.text(l10n.attachmentPreparing), findsOneWidget);
    expect(find.byType(LinearProgressIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Tapping file option shows preparing UI', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    when(interactor.getFileFromDevice()).thenAnswer((_) => Completer<File>().future);

    await tester.pumpWidget(TestApp(AttachmentPicker()));
    await tester.pump();

    // Tap file option
    await tester.tap(find.text(l10n.uploadFile));
    await tester.pump();

    expect(find.text(l10n.attachmentPreparing), findsOneWidget);
    expect(find.byType(LinearProgressIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Tapping camera option invokes interactor methods', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    await tester.pumpWidget(TestApp(AttachmentPicker()));
    await tester.pump();

    // Tap camera option
    await tester.tap(find.text(l10n.useCamera));
    await tester.pump();

    verify(interactor.getImageFromCamera()).called(1);
  });

  testWidgetsWithAccessibilityChecks('Tapping gallery option invokes interactor methods', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    await tester.pumpWidget(TestApp(AttachmentPicker()));
    await tester.pump();

    // Tap gallery option
    await tester.tap(find.text(l10n.chooseFromGallery));
    await tester.pump();

    verify(interactor.getImageFromGallery()).called(1);
  });

  testWidgetsWithAccessibilityChecks('Tapping file option invokes interactor methods', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    await tester.pumpWidget(TestApp(AttachmentPicker()));
    await tester.pump();

    // Tap file option
    await tester.tap(find.text(l10n.uploadFile));
    await tester.pump();

    verify(interactor.getFileFromDevice()).called(1);
  });

  testWidgetsWithAccessibilityChecks('Canceling camera option returns to picker ui', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    final completer = Completer<File?>();
    when(interactor.getImageFromCamera()).thenAnswer((_) => completer.future);

    await tester.pumpWidget(TestApp(AttachmentPicker()));
    await tester.pump();

    // Tap camera option
    await tester.tap(find.text(l10n.useCamera));
    await tester.pump();

    // Should show 'preparing' UI
    expect(find.text(l10n.attachmentPreparing), findsOneWidget);
    expect(find.byType(LinearProgressIndicator), findsOneWidget);

    // Complete with nul (i.e. canceled) value
    completer.complete(null);
    await tester.pumpAndSettle();

    // Should no longer show 'preparing' UI
    expect(find.text(l10n.attachmentPreparing), findsNothing);
    expect(find.byType(LinearProgressIndicator), findsNothing);

    // Should show picker options
    expect(find.text(l10n.useCamera), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Canceling gallery option returns to picker ui', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    final completer = Completer<File?>();
    when(interactor.getImageFromGallery()).thenAnswer((_) => completer.future);

    await tester.pumpWidget(TestApp(AttachmentPicker()));
    await tester.pump();

    // Tap gallery option
    await tester.tap(find.text(l10n.chooseFromGallery));
    await tester.pump();

    // Should show 'preparing' UI
    expect(find.text(l10n.attachmentPreparing), findsOneWidget);
    expect(find.byType(LinearProgressIndicator), findsOneWidget);

    // Complete with nul (i.e. canceled) value
    completer.complete(null);
    await tester.pumpAndSettle();

    // Should no longer show 'preparing' UI
    expect(find.text(l10n.attachmentPreparing), findsNothing);
    expect(find.byType(LinearProgressIndicator), findsNothing);

    // Should show picker options
    expect(find.text(l10n.chooseFromGallery), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('Canceling file option returns to picker ui', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    final completer = Completer<File?>();
    when(interactor.getFileFromDevice()).thenAnswer((_) => completer.future);

    await tester.pumpWidget(TestApp(AttachmentPicker()));
    await tester.pump();

    // Tap file option
    await tester.tap(find.text(l10n.uploadFile));
    await tester.pump();

    // Should show 'preparing' UI
    expect(find.text(l10n.attachmentPreparing), findsOneWidget);
    expect(find.byType(LinearProgressIndicator), findsOneWidget);

    // Complete with nul (i.e. canceled) value
    completer.complete(null);
    await tester.pumpAndSettle();

    // Should no longer show 'preparing' UI
    expect(find.text(l10n.attachmentPreparing), findsNothing);
    expect(find.byType(LinearProgressIndicator), findsNothing);

    // Should show picker options
    expect(find.text(l10n.uploadFile), findsOneWidget);
  });

  testWidgets('Returns non-null result when successful', (tester) async {
    final interactor = MockAttachmentPickerInteractor();
    _setupLocator(interactor);

    final file = File('/fake/path');
    when(interactor.getFileFromDevice()).thenAnswer((_) => Future.value(file));

    AttachmentHandler? result = null;

    await tester.pumpWidget(TestApp(Builder(
      builder: (context) => Container(
        child: ElevatedButton(
          child: Container(),
          onPressed: () async {
            result = await CreateConversationInteractor().addAttachment(context);
          },
        ),
      ),
    )));
    await tester.pump();

    await tester.tap(find.byType(ElevatedButton));
    await tester.pumpAndSettle();

    // Tap file option
    await tester.tap(find.text(l10n.uploadFile));
    await tester.pumpAndSettle();

    expect(find.byType(ElevatedButton), findsOneWidget);

    expect(result, isNotNull);
  });
}

_setupLocator(MockAttachmentPickerInteractor interactor) =>
    setupTestLocator((locator) => locator.registerFactory<AttachmentPickerInteractor>(() => interactor));
