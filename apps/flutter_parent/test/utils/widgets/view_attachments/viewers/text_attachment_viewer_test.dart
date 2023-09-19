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

import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_parent/models/attachment.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/fetcher/attachment_fetcher_interactor.dart';
import 'package:flutter_parent/utils/common_widgets/view_attachment/viewers/text_attachment_viewer.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/mockito.dart';

import '../../../accessibility_utils.dart';
import '../../../test_app.dart';
import '../../../test_helpers/mock_helpers.mocks.dart';

void main() {
  setUpAll(() async {
    // Move to a temp dir so we don't write test files to the project dir
    Directory.current = Directory.systemTemp;
    await Directory('cache').create();
  });

  tearDownAll(() async {
    // Delete cache dir and contents
    await Directory('cache').delete(recursive: true);
  });

  testWidgets('displays text', (tester) async {
    var text = 'This is a test';
    _setupLocator(text);

    await tester.pumpWidget(TestApp(TextAttachmentViewer(Attachment())));
    await tester.pumpAndSettle();

    expect(find.text(text), findsOneWidget);
  });

  testWidgetsWithAccessibilityChecks('zooms text', (tester) async {
    var text = 'This is a test';
    _setupLocator(text);

    await tester.pumpWidget(TestApp(TextAttachmentViewer(Attachment())));
    await tester.pumpAndSettle();

    var widget = await tester.widget<Text>(find.text(text));
    expect(widget.style?.fontSize, 14);

    // Start two gestures separated by 100px
    var finger1 = await tester.startGesture(Offset(100, 300));
    var finger2 = await tester.startGesture(Offset(200, 300));

    // Move second gesture by 100px, representing a 200% zoom
    await finger2.moveBy(Offset(100, 0));

    // Complete the gestures
    await finger1.up();
    await finger2.up();
    await tester.pump();

    // Font size should have increased 200% (from 14 to 28)
    widget = await tester.widget<Text>(find.text(text));
    expect(widget.style?.fontSize, 28);
  });

  testWidgetsWithAccessibilityChecks('zooms to min font size of 10', (tester) async {
    var text = 'This is a test';
    _setupLocator(text);

    await tester.pumpWidget(TestApp(TextAttachmentViewer(Attachment())));
    await tester.pumpAndSettle();

    var widget = await tester.widget<Text>(find.text(text));
    expect(widget.style?.fontSize, 14);

    // Start two gestures separated by 400px
    var finger1 = await tester.startGesture(Offset(100, 300));
    var finger2 = await tester.startGesture(Offset(500, 300));

    // Move second gesture by -400px, representing a 20% zoom
    await finger2.moveBy(Offset(-400, 0));

    // Complete the gestures
    await finger1.up();
    await finger2.up();
    await tester.pump();

    // Font size should have decreased to only 10
    widget = await tester.widget<Text>(find.text(text));
    expect(widget.style?.fontSize, 10);
  });

  testWidgetsWithAccessibilityChecks('zooms to max font size of 48', (tester) async {
    var text = 'This is a test';
    _setupLocator(text);

    await tester.pumpWidget(TestApp(TextAttachmentViewer(Attachment())));
    await tester.pumpAndSettle();

    var widget = await tester.widget<Text>(find.text(text));
    expect(widget.style?.fontSize, 14);

    // Start two gestures separated by 100px
    var finger1 = await tester.startGesture(Offset(100, 300));
    var finger2 = await tester.startGesture(Offset(200, 300));

    // Move second gesture by 400px, representing a 500% zoom
    await finger2.moveBy(Offset(400, 0));

    // Complete the gestures
    await finger1.up();
    await finger2.up();
    await tester.pump();

    // Font size should have increased only 48
    widget = await tester.widget<Text>(find.text(text));
    expect(widget.style?.fontSize, 48);
  });
}

_setupLocator(String text) {
  // Create file
  var file = File('cache/test-file.txt');
  file.writeAsStringSync(text);

  // Set up interactor
  var interactor = MockAttachmentFetcherInteractor();
  setupTestLocator((locator) {
    locator.registerLazySingleton<AttachmentFetcherInteractor>(() => interactor);
  });
  when(interactor.generateCancelToken()).thenReturn(CancelToken());
  when(interactor.fetchAttachmentFile(any, any)).thenAnswer((_) => Future.value(file));
}