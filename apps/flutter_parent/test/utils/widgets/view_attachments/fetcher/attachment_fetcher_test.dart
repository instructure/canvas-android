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

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_parent/l10n/app_localizations.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/common_widgets/error_panda_widget.dart';
import 'package:flutter_parent/utils/common_widgets/loading_indicator.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/fetcher/attachment_fetcher.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/fetcher/attachment_fetcher_interactor.dart';
import 'package:flutter_parent/utils/quick_nav.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../accessibility_utils.dart';
import '../../../test_app.dart';
import '../../../test_helpers/mock_helpers.mocks.dart';

void main() {
  testWidgetsWithAccessibilityChecks('displays loading state', (tester) async {
    var interactor = MockAttachmentFetcherInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AttachmentFetcherInteractor>(() => interactor);
    });

    Completer<File> completer = Completer();
    when(interactor.fetchAttachmentFile(any, any)).thenAnswer((_) => completer.future);
    when(interactor.generateCancelToken()).thenAnswer((_) => CancelToken());

    await tester.pumpWidget(
      TestApp(Material(child: AttachmentFetcher(attachment: Attachment(), builder: (_, __) => Container()))),
    );
    await tester.pump();

    expect(find.byType(LoadingIndicator), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('provides correct file to builder', (tester) async {
    var interactor = MockAttachmentFetcherInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AttachmentFetcherInteractor>(() => interactor);
    });

    var expectedFile = File('fakefile.exe');
    when(interactor.fetchAttachmentFile(any, any)).thenAnswer((_) => Future.value(expectedFile));
    when(interactor.generateCancelToken()).thenAnswer((_) => CancelToken());

    late File actualFile;
    await tester.pumpWidget(
      TestApp(Material(
          child: AttachmentFetcher(
        attachment: Attachment(),
        builder: (_, file) {
          actualFile = file;
          return Container();
        },
      ))),
    );
    await tester.pumpAndSettle();

    expect(actualFile, expectedFile);
  });

  testWidgetsWithAccessibilityChecks('builds child on success', (tester) async {
    var interactor = MockAttachmentFetcherInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AttachmentFetcherInteractor>(() => interactor);
    });

    var expectedFile = File('fakefile.exe');
    when(interactor.fetchAttachmentFile(any, any)).thenAnswer((_) => Future.value(expectedFile));
    when(interactor.generateCancelToken()).thenAnswer((_) => CancelToken());

    await tester.pumpWidget(
      TestApp(Material(
          child: AttachmentFetcher(
        attachment: Attachment(),
        builder: (_, file) => Text(file.path),
      ))),
    );
    await tester.pumpAndSettle();

    expect(find.text(expectedFile.path), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('displays error state', (tester) async {
    var interactor = MockAttachmentFetcherInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AttachmentFetcherInteractor>(() => interactor);
    });

    when(interactor.fetchAttachmentFile(any, any)).thenAnswer((_) => Future.error(''));
    when(interactor.generateCancelToken()).thenAnswer((_) => CancelToken());

    await tester.pumpWidget(
      TestApp(
        Material(child: AttachmentFetcher(attachment: Attachment(), builder: (_, file) => Container())),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.byType(ErrorPandaWidget), findsOneWidget);
    expect(find.text(AppLocalizations().errorLoadingFile), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('performs retry', (tester) async {
    var interactor = MockAttachmentFetcherInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AttachmentFetcherInteractor>(() => interactor);
    });

    when(interactor.fetchAttachmentFile(any, any)).thenAnswer((_) => Future.error(''));
    when(interactor.generateCancelToken()).thenAnswer((_) => CancelToken());

    await tester.pumpWidget(
      TestApp(
        Material(child: AttachmentFetcher(attachment: Attachment(), builder: (_, file) => Text('success'))),
      ),
    );
    await tester.pumpAndSettle();

    var retryButton = find.text(AppLocalizations().retry);
    expect(retryButton, findsOneWidget);
    expect(find.text('success'), findsNothing);

    when(interactor.fetchAttachmentFile(any, any)).thenAnswer((_) => Future.value(File('')));
    await tester.tap(retryButton);
    await tester.pumpAndSettle();

    expect(find.text('success'), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('cancels request on dispose', (tester) async {
    var interactor = MockAttachmentFetcherInteractor();
    setupTestLocator((locator) {
      locator.registerFactory<AttachmentFetcherInteractor>(() => interactor);
    });

    when(interactor.fetchAttachmentFile(any, any)).thenAnswer((_) => Future.error(''));

    var cancelToken = MockCancelToken();
    when(interactor.generateCancelToken()).thenReturn(cancelToken);

    await tester.pumpWidget(
      TestApp(
        Builder(
          builder: (context) => Center(
            child: ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.white,
              ),
              onPressed: () => QuickNav().push(
                context,
                Material(child: AttachmentFetcher(attachment: Attachment(), builder: (_, __) => Container())),
              ),
              child: Text(
                'click me',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(find.byType(ElevatedButton));
    await tester.pumpAndSettle();

    expect(find.byType(AttachmentFetcher), findsOneWidget);

    TestApp.navigatorKey.currentState?.pop();
    await tester.pumpAndSettle();

    verify(cancelToken.cancel()).called(1);
  });
}
