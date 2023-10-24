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

import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/fetcher/attachment_fetcher_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/view_attachment_screen.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/audio_video_attachment_viewer.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/audio_video_attachment_viewer_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/image_attachment_viewer.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/text_attachment_viewer.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/unknown_attachment_type_viewer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../accessibility_utils.dart';
import '../../network_image_response.dart';
import '../../test_app.dart';
import '../../test_helpers/mock_helpers.mocks.dart';

void main() {
  mockNetworkImageResponse();

  AppLocalizations l10n = AppLocalizations();

  testWidgetsWithAccessibilityChecks('shows attachment display name', (tester) async {
    setupTestLocator((locator) => locator.registerFactory<ViewAttachmentInteractor>(() => MockViewAttachmentInteractor()));
    Attachment attachment = Attachment((a) => a..displayName = 'Display Name');

    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pumpAndSettle();

    expect(find.descendant(of: find.byType(AppBar), matching: find.text(attachment.displayName!)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows overflow menu', (tester) async {
    setupTestLocator((locator) => locator.registerFactory<ViewAttachmentInteractor>(() => MockViewAttachmentInteractor()));
    Attachment attachment = Attachment((a) => a..displayName = 'Display Name');

    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pumpAndSettle();

    var overflow = find.descendant(of: find.byType(AppBar), matching: find.byIcon(Icons.more_vert));
    expect(overflow, findsOneWidget);

    await tester.tap(overflow);
    await tester.pumpAndSettle();

    expect(find.text(l10n.openFileExternally), findsOneWidget);
    expect(find.text(l10n.download), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('download button calls interactor', (tester) async {
    var interactor = MockViewAttachmentInteractor();
    setupTestLocator((locator) => locator.registerFactory<ViewAttachmentInteractor>(() => interactor));
    Attachment attachment = Attachment((a) => a..displayName = 'Display Name');

    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pumpAndSettle();

    await tester.tap(find.descendant(of: find.byType(AppBar), matching: find.byIcon(Icons.more_vert)));
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.download));

    verify(interactor.downloadFile(attachment)).called(1);
  });

  testWidgetsWithAccessibilityChecks('open externally button calls interactor', (tester) async {
    var interactor = MockViewAttachmentInteractor();
    setupTestLocator((locator) => locator.registerFactory<ViewAttachmentInteractor>(() => interactor));
    Attachment attachment = Attachment((a) => a..displayName = 'Display Name');

    when(interactor.openExternally(any)).thenAnswer((_) => Future.value(null));

    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pumpAndSettle();

    await tester.tap(find.descendant(of: find.byType(AppBar), matching: find.byIcon(Icons.more_vert)));
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.openFileExternally));

    verify(interactor.openExternally(attachment)).called(1);
  });

  testWidgetsWithAccessibilityChecks('displays snackbar when open externally fails', (tester) async {
    var interactor = MockViewAttachmentInteractor();
    setupTestLocator((locator) => locator.registerFactory<ViewAttachmentInteractor>(() => interactor));
    Attachment attachment = Attachment((a) => a..displayName = 'Display Name');

    when(interactor.openExternally(any)).thenAnswer((_) => Future.error(''));

    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pumpAndSettle();

    await tester.tap(find.descendant(of: find.byType(AppBar), matching: find.byIcon(Icons.more_vert)));
    await tester.pumpAndSettle();

    await tester.tap(find.text(l10n.openFileExternally));
    await tester.pump(Duration(milliseconds: 200));

    expect(
      find.descendant(of: find.byType(SnackBar), matching: find.text(l10n.noApplicationsToHandleFile)),
      findsOneWidget,
    );
  });

  testWidgetsWithAccessibilityChecks('shows attachment file name if display name is null', (tester) async {
    setupTestLocator((locator) => locator.registerFactory<ViewAttachmentInteractor>(() => MockViewAttachmentInteractor()));
    Attachment attachment = Attachment((a) => a
      ..displayName = null
      ..filename = 'File Name');

    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pumpAndSettle();

    expect(find.descendant(of: find.byType(AppBar), matching: find.text(attachment.filename!)), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows correct widget for images', (tester) async {
    setupTestLocator((locator) => locator.registerFactory<ViewAttachmentInteractor>(() => MockViewAttachmentInteractor()));
    Attachment attachment = Attachment((a) => a
      ..displayName = 'Display Name'
      ..url = 'fake_url'
      ..contentType = 'image/fake');
    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pump();

    expect(find.byType(ImageAttachmentViewer), findsOneWidget);
  });

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('shows correct widget for videos', (tester) async {
    setupTestLocator((locator) {
      locator.registerFactory<ViewAttachmentInteractor>(() => MockViewAttachmentInteractor());
      locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => AudioVideoAttachmentViewerInteractor());
    });
    Attachment attachment = Attachment((a) => a
      ..displayName = 'Display Name'
      ..url = 'fake_url'
      ..contentType = 'video/fake');
    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pump();

    expect(find.byType(AudioVideoAttachmentViewer), findsOneWidget);
  }, skip: true);

  // TODO Fix test
  testWidgetsWithAccessibilityChecks('shows correct widget for audio', (tester) async {
    setupTestLocator((locator) {
      locator.registerFactory<ViewAttachmentInteractor>(() => MockViewAttachmentInteractor());
      locator.registerFactory<AudioVideoAttachmentViewerInteractor>(() => AudioVideoAttachmentViewerInteractor());
    });
    Attachment attachment = Attachment((a) => a
      ..displayName = 'Display Name'
      ..url = 'fake_url'
      ..contentType = 'audio/fake');
    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pump();

    expect(find.byType(AudioVideoAttachmentViewer), findsOneWidget);
  }, skip: true);

  testWidgetsWithAccessibilityChecks('shows correct widget for text', (tester) async {
    setupTestLocator((locator) {
      locator.registerFactory<ViewAttachmentInteractor>(() => MockViewAttachmentInteractor());
      locator.registerFactory<AttachmentFetcherInteractor>(() => AttachmentFetcherInteractor());
    });
    Attachment attachment = Attachment((a) => a
      ..displayName = 'Display Name'
      ..url = 'fake_url'
      ..contentType = 'text/plain');
    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pump();

    expect(find.byType(TextAttachmentViewer), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('shows correct widget for unknown type', (tester) async {
    setupTestLocator((locator) => locator.registerFactory<ViewAttachmentInteractor>(() => MockViewAttachmentInteractor()));
    Attachment attachment = Attachment((a) => a
      ..displayName = 'Display Name'
      ..url = 'fake_url'
      ..contentType = 'unknown/type');
    await tester.pumpWidget(TestApp(ViewAttachmentScreen(attachment)));
    await tester.pump();

    expect(find.byType(UnknownAttachmentTypeViewer), findsOneWidget);
  });
}